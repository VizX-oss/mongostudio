const express = require('express');
const cors = require('cors');
const { MongoClient, ObjectId } = require('mongodb');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const multer = require('multer');
const archiver = require('archiver');

const app = express();
const PORT = process.env.PORT || 4000;

app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));
app.use(express.static(path.join(__dirname, 'public')));

// Uploads directory for restore / import
const UPLOADS_DIR = path.join(__dirname, 'temp_uploads');
if (!fs.existsSync(UPLOADS_DIR)) {
  fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

// Saved connections file
const SAVED_CONNECTIONS_FILE = path.join(__dirname, 'saved_connections.json');

const upload = multer({ dest: UPLOADS_DIR });

// Active MongoDB clients cache: connectionId -> { client, uri, createdAt }
const activeClients = new Map();

// Helper: Encryption & Decryption (AES-256-CBC)
const MASTER_SECRET = process.env.ENCRYPTION_KEY || 'mongo-studio-secret-vault-2026-key-32b!';
const KEY = crypto.scryptSync(MASTER_SECRET, 'salt_salt_salt', 32);

function encrypt(text) {
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv('aes-256-cbc', KEY, iv);
  let encrypted = cipher.update(text, 'utf8', 'hex');
  encrypted += cipher.final('hex');
  return iv.toString('hex') + ':' + encrypted;
}

function decrypt(text) {
  try {
    const parts = text.split(':');
    if (parts.length !== 2) return text;
    const iv = Buffer.from(parts[0], 'hex');
    const decipher = crypto.createDecipheriv('aes-256-cbc', KEY, iv);
    let decrypted = decipher.update(parts[1], 'hex', 'utf8');
    decrypted += decipher.final('utf8');
    return decrypted;
  } catch (err) {
    return text;
  }
}

// Helper: Read / Write Saved Connections
function readSavedConnections() {
  try {
    if (!fs.existsSync(SAVED_CONNECTIONS_FILE)) {
      return [];
    }
    const raw = fs.readFileSync(SAVED_CONNECTIONS_FILE, 'utf8');
    return JSON.parse(raw);
  } catch (err) {
    console.error('Error reading saved connections:', err);
    return [];
  }
}

function writeSavedConnections(connections) {
  try {
    fs.writeFileSync(SAVED_CONNECTIONS_FILE, JSON.stringify(connections, null, 2), 'utf8');
  } catch (err) {
    console.error('Error writing saved connections:', err);
  }
}

// Helper: Parse Query Strings safely with BSON types
function parseBsonQuery(obj) {
  if (!obj || typeof obj !== 'object') return obj;

  for (const key of Object.keys(obj)) {
    const val = obj[key];
    if (typeof val === 'string') {
      if (val.startsWith('ObjectId("') && val.endsWith('")')) {
        const idStr = val.slice(10, -2);
        if (ObjectId.isValid(idStr)) obj[key] = new ObjectId(idStr);
      } else if (key === '_id' && ObjectId.isValid(val) && val.length === 24) {
        obj[key] = new ObjectId(val);
      } else if (val.startsWith('ISODate("') && val.endsWith('")')) {
        const dateStr = val.slice(9, -2);
        obj[key] = new Date(dateStr);
      }
    } else if (typeof val === 'object' && val !== null) {
      if (val.$oid && ObjectId.isValid(val.$oid)) {
        obj[key] = new ObjectId(val.$oid);
      } else if (val.$date) {
        obj[key] = new Date(val.$date);
      } else {
        parseBsonQuery(val);
      }
    }
  }
  return obj;
}

// ==========================================
// ROUTES
// ==========================================

// 1. Test & Connect to MongoDB
app.post('/api/connect', async (req, res) => {
  let { uri, name, saveConnection } = req.body;
  if (!uri) {
    return res.status(400).json({ error: 'Connection string is required' });
  }
  uri = uri.trim();

  try {
    const client = new MongoClient(uri, {
      connectTimeoutMS: 12000,
      serverSelectionTimeoutMS: 12000,
    });
    await client.connect();

    const adminDb = client.db().admin();
    const pingResult = await adminDb.ping();
    const serverInfo = await adminDb.serverInfo().catch(() => ({ version: 'Unknown' }));

    const connectionId = crypto.randomUUID();
    activeClients.set(connectionId, {
      client,
      uri,
      createdAt: Date.now()
    });

    let savedId = null;
    if (saveConnection) {
      savedId = crypto.randomUUID();
      const encryptedUri = encrypt(uri);
      const connections = readSavedConnections();
      
      const maskedUri = uri.replace(/\/\/([^:]+):([^@]+)@/, '//$1:••••••••@');
      
      connections.push({
        id: savedId,
        name: name || 'MongoDB Cluster',
        maskedUri,
        encryptedUri,
        savedAt: new Date().toISOString()
      });
      writeSavedConnections(connections);
    }

    res.json({
      success: true,
      connectionId,
      serverVersion: serverInfo.version,
      ping: pingResult,
      savedId
    });
  } catch (err) {
    console.error('Connection failed:', err.message);
    res.status(500).json({ error: err.message || 'Failed to connect to cluster' });
  }
});

// 2. Saved connections list
app.get('/api/saved-connections', (req, res) => {
  const connections = readSavedConnections().map(c => ({
    id: c.id,
    name: c.name,
    maskedUri: c.maskedUri,
    savedAt: c.savedAt
  }));
  res.json(connections);
});

// 3. Connect from Saved Connection
app.post('/api/saved-connections/:id/connect', async (req, res) => {
  try {
    const connections = readSavedConnections();
    const target = connections.find(c => c.id === req.params.id);
    if (!target) {
      return res.status(404).json({ error: 'Saved connection not found' });
    }

    const decryptedUri = decrypt(target.encryptedUri);
    const client = new MongoClient(decryptedUri, {
      connectTimeoutMS: 12000,
      serverSelectionTimeoutMS: 12000,
    });
    await client.connect();
    const adminDb = client.db().admin();
    const serverInfo = await adminDb.serverInfo().catch(() => ({ version: 'Unknown' }));

    const connectionId = crypto.randomUUID();
    activeClients.set(connectionId, {
      client,
      uri: decryptedUri,
      createdAt: Date.now()
    });

    res.json({
      success: true,
      connectionId,
      name: target.name,
      maskedUri: target.maskedUri,
      serverVersion: serverInfo.version
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 4. Delete saved connection
app.delete('/api/saved-connections/:id', (req, res) => {
  let connections = readSavedConnections();
  connections = connections.filter(c => c.id !== req.params.id);
  writeSavedConnections(connections);
  res.json({ success: true });
});

// Middleware to get active client
const withClient = (req, res, next) => {
  const connectionId = req.headers['x-connection-id'];
  if (!connectionId || !activeClients.has(connectionId)) {
    return res.status(401).json({ error: 'Session expired or invalid connection ID. Please reconnect.' });
  }
  req.mongo = activeClients.get(connectionId);
  next();
};

// 5. Disconnect
app.post('/api/disconnect', withClient, async (req, res) => {
  const connectionId = req.headers['x-connection-id'];
  try {
    const item = activeClients.get(connectionId);
    if (item && item.client) {
      await item.client.close();
    }
    activeClients.delete(connectionId);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 6. Cluster Overview / Databases List
app.get('/api/overview', withClient, async (req, res) => {
  try {
    const client = req.mongo.client;
    const adminDb = client.db().admin();

    const dbsResult = await adminDb.listDatabases();
    let serverStatus = {};
    try {
      serverStatus = await adminDb.serverStatus();
    } catch (e) {}

    const databases = [];
    for (const dbInfo of dbsResult.databases) {
      try {
        const db = client.db(dbInfo.name);
        const collections = await db.listCollections().toArray();
        let stats = {};
        try {
          stats = await db.command({ dbStats: 1 });
        } catch (e) {}

        databases.push({
          name: dbInfo.name,
          sizeOnDisk: dbInfo.sizeOnDisk || 0,
          empty: dbInfo.empty || false,
          collectionsCount: collections.length,
          objectsCount: stats.objects || 0,
          dataSize: stats.dataSize || 0,
          storageSize: stats.storageSize || 0,
          indexes: stats.indexes || 0,
          indexSize: stats.indexSize || 0
        });
      } catch (err) {
        databases.push({
          name: dbInfo.name,
          sizeOnDisk: dbInfo.sizeOnDisk || 0,
          collectionsCount: 0
        });
      }
    }

    res.json({
      totalSize: dbsResult.totalSize || 0,
      databases,
      serverStatus: {
        version: serverStatus.version,
        uptime: serverStatus.uptime,
        host: serverStatus.host,
        connections: serverStatus.connections,
        mem: serverStatus.mem,
        network: serverStatus.network
      }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 7. Get Collections in a Database
app.get('/api/databases/:dbName/collections', withClient, async (req, res) => {
  try {
    const { dbName } = req.params;
    const db = req.mongo.client.db(dbName);
    const collectionsList = await db.listCollections().toArray();

    const collections = [];
    for (const col of collectionsList) {
      try {
        const count = await db.collection(col.name).estimatedDocumentCount().catch(() => 0);
        const collStats = await db.command({ collStats: col.name }).catch(() => ({}));
        collections.push({
          name: col.name,
          type: col.type || 'collection',
          docCount: count,
          size: collStats.size || 0,
          storageSize: collStats.storageSize || 0,
          totalIndexSize: collStats.totalIndexSize || 0,
          nindexes: collStats.nindexes || 0
        });
      } catch (e) {
        collections.push({
          name: col.name,
          type: col.type || 'collection',
          docCount: 0
        });
      }
    }

    res.json(collections);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 8. Create Database / Collection
app.post('/api/databases/:dbName/collections', withClient, async (req, res) => {
  try {
    const { dbName } = req.params;
    const { collectionName } = req.body;
    if (!collectionName) return res.status(400).json({ error: 'Collection name is required' });

    const db = req.mongo.client.db(dbName);
    await db.createCollection(collectionName);
    res.json({ success: true, message: `Collection '${collectionName}' created successfully` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 9. Drop Collection
app.delete('/api/databases/:dbName/collections/:colName', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    const db = req.mongo.client.db(dbName);
    await db.collection(colName).drop();
    res.json({ success: true, message: `Collection '${colName}' dropped` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 10. Drop Database
app.delete('/api/databases/:dbName', withClient, async (req, res) => {
  try {
    const { dbName } = req.params;
    if (['admin', 'local', 'config'].includes(dbName)) {
      return res.status(403).json({ error: `Cannot drop system database '${dbName}'` });
    }
    const db = req.mongo.client.db(dbName);
    await db.dropDatabase();
    res.json({ success: true, message: `Database '${dbName}' dropped` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 11. Query & Browse Documents
app.post('/api/databases/:dbName/collections/:colName/query', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    let { filter = {}, sort = {}, projection = {}, page = 1, limit = 20 } = req.body;

    page = parseInt(page) || 1;
    limit = Math.min(parseInt(limit) || 20, 100);
    const skip = (page - 1) * limit;

    if (typeof filter === 'string') {
      filter = filter.trim() ? JSON.parse(filter) : {};
    }
    filter = parseBsonQuery(filter);

    if (typeof sort === 'string') {
      sort = sort.trim() ? JSON.parse(sort) : {};
    }

    if (typeof projection === 'string') {
      projection = projection.trim() ? JSON.parse(projection) : {};
    }

    const col = req.mongo.client.db(dbName).collection(colName);
    const total = await col.countDocuments(filter);
    const docs = await col.find(filter)
      .sort(sort)
      .project(projection)
      .skip(skip)
      .limit(limit)
      .toArray();

    res.json({
      total,
      page,
      limit,
      totalPages: Math.ceil(total / limit) || 1,
      documents: docs
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 12. Create Document(s)
app.post('/api/databases/:dbName/collections/:colName/documents', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    let { document, documents } = req.body;
    const col = req.mongo.client.db(dbName).collection(colName);

    if (documents && Array.isArray(documents)) {
      const parsedDocs = documents.map(d => parseBsonQuery(d));
      const result = await col.insertMany(parsedDocs);
      return res.json({ success: true, insertedCount: result.insertedCount, insertedIds: result.insertedIds });
    }

    if (!document) return res.status(400).json({ error: 'Document data is required' });
    const parsedDoc = parseBsonQuery(document);
    const result = await col.insertOne(parsedDoc);
    res.json({ success: true, insertedId: result.insertedId });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 13. Update Document (Push changes directly to DB)
app.put('/api/databases/:dbName/collections/:colName/documents/:docId', withClient, async (req, res) => {
  try {
    const { dbName, colName, docId } = req.params;
    const { document } = req.body;
    if (!document) return res.status(400).json({ error: 'Updated document is required' });

    const col = req.mongo.client.db(dbName).collection(colName);
    
    let filter = { _id: docId };
    if (ObjectId.isValid(docId)) {
      filter = { $or: [{ _id: new ObjectId(docId) }, { _id: docId }] };
    }

    const updatePayload = { ...document };
    delete updatePayload._id;

    const result = await col.replaceOne(filter, updatePayload);
    if (result.matchedCount === 0) {
      return res.status(404).json({ error: 'Document not found or ID mismatch' });
    }

    res.json({ success: true, modifiedCount: result.modifiedCount });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 14. Delete Document
app.delete('/api/databases/:dbName/collections/:colName/documents/:docId', withClient, async (req, res) => {
  try {
    const { dbName, colName, docId } = req.params;
    const col = req.mongo.client.db(dbName).collection(colName);

    let filter = { _id: docId };
    if (ObjectId.isValid(docId)) {
      filter = { $or: [{ _id: new ObjectId(docId) }, { _id: docId }] };
    }

    const result = await col.deleteOne(filter);
    res.json({ success: true, deletedCount: result.deletedCount });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 15. Aggregation Pipeline Runner
app.post('/api/databases/:dbName/collections/:colName/aggregate', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    let { pipeline } = req.body;

    if (typeof pipeline === 'string') {
      pipeline = JSON.parse(pipeline);
    }
    if (!Array.isArray(pipeline)) {
      return res.status(400).json({ error: 'Pipeline must be an array of stages' });
    }

    const parsedPipeline = pipeline.map(stage => parseBsonQuery(stage));
    const col = req.mongo.client.db(dbName).collection(colName);
    const results = await col.aggregate(parsedPipeline).toArray();

    res.json({ success: true, count: results.length, results });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 16. Indexes Management
app.get('/api/databases/:dbName/collections/:colName/indexes', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    const col = req.mongo.client.db(dbName).collection(colName);
    const indexes = await col.indexes();
    res.json(indexes);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/databases/:dbName/collections/:colName/indexes', withClient, async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    const { keys, options } = req.body;
    const col = req.mongo.client.db(dbName).collection(colName);
    const result = await col.createIndex(keys, options || {});
    res.json({ success: true, indexName: result });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/databases/:dbName/collections/:colName/indexes/:indexName', withClient, async (req, res) => {
  try {
    const { dbName, colName, indexName } = req.params;
    const col = req.mongo.client.db(dbName).collection(colName);
    await col.dropIndex(indexName);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 17. Raw Query / Command Console
app.post('/api/raw-command', withClient, async (req, res) => {
  try {
    const { dbName = 'admin', command } = req.body;
    if (!command) return res.status(400).json({ error: 'Command is required' });

    let parsed = typeof command === 'string' ? JSON.parse(command) : command;
    parsed = parseBsonQuery(parsed);

    const db = req.mongo.client.db(dbName);
    const result = await db.command(parsed);
    res.json({ success: true, result });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ==========================================
// BACKUP & RESTORE / EXPORT & IMPORT
// ==========================================

// 18. Export Collection or Database to JSON
app.get('/api/databases/:dbName/export', withClient, async (req, res) => {
  try {
    const { dbName } = req.params;
    const { collection } = req.query;
    const db = req.mongo.client.db(dbName);

    if (collection) {
      const docs = await db.collection(collection).find({}).toArray();
      res.setHeader('Content-Type', 'application/json');
      res.setHeader('Content-Disposition', `attachment; filename="${dbName}_${collection}_export.json"`);
      return res.send(JSON.stringify(docs, null, 2));
    }

    const collections = await db.listCollections().toArray();
    res.setHeader('Content-Type', 'application/zip');
    res.setHeader('Content-Disposition', `attachment; filename="${dbName}_backup.zip"`);

    const archive = archiver('zip', { zlib: { level: 9 } });
    archive.pipe(res);

    for (const col of collections) {
      const docs = await db.collection(col.name).find({}).toArray();
      archive.append(JSON.stringify(docs, null, 2), { name: `${col.name}.json` });
    }

    await archive.finalize();
  } catch (err) {
    if (!res.headersSent) res.status(500).json({ error: err.message });
  }
});

// 19. Native mongodump Backup (creates .gz archive)
app.get('/api/backup/mongodump', withClient, (req, res) => {
  const uri = req.mongo.uri;
  const dbName = req.query.dbName;
  const timestamp = Date.now();
  const backupFile = path.join(UPLOADS_DIR, `mongodump_${dbName || 'cluster'}_${timestamp}.archive.gz`);

  let cmd = `mongodump --uri="${uri}" --archive="${backupFile}" --gzip`;
  if (dbName) {
    cmd += ` --db="${dbName}"`;
  }

  exec(cmd, (err, stdout, stderr) => {
    if (err) {
      console.error('mongodump error:', stderr || err.message);
      return res.status(500).json({ error: `Mongodump failed: ${stderr || err.message}` });
    }

    res.download(backupFile, `mongodump_${dbName || 'all'}_${timestamp}.archive.gz`, (downloadErr) => {
      fs.unlink(backupFile, () => {});
    });
  });
});

// 20. Restore via JSON import
app.post('/api/databases/:dbName/collections/:colName/import', withClient, upload.single('file'), async (req, res) => {
  try {
    const { dbName, colName } = req.params;
    if (!req.file) return res.status(400).json({ error: 'JSON file is required' });

    const fileContent = fs.readFileSync(req.file.path, 'utf8');
    fs.unlinkSync(req.file.path);

    let parsed = JSON.parse(fileContent);
    if (!Array.isArray(parsed)) {
      parsed = [parsed];
    }

    if (parsed.length === 0) {
      return res.json({ success: true, count: 0, message: 'File was empty' });
    }

    const prepared = parsed.map(d => parseBsonQuery(d));
    const col = req.mongo.client.db(dbName).collection(colName);
    const result = await col.insertMany(prepared);

    res.json({
      success: true,
      count: result.insertedCount,
      message: `Successfully imported ${result.insertedCount} documents into '${colName}'`
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 21. Restore via native mongorestore (upload archive.gz)
app.post('/api/restore/mongorestore', withClient, upload.single('archive'), (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'Archive file is required' });
    const uri = req.mongo.uri;
    const dropBeforeRestore = req.body.drop === 'true';

    let cmd = `mongorestore --uri="${uri}" --archive="${req.file.path}" --gzip`;
    if (dropBeforeRestore) {
      cmd += ' --drop';
    }

    exec(cmd, (err, stdout, stderr) => {
      fs.unlink(req.file.path, () => {});
      if (err) {
        console.error('mongorestore error:', stderr || err.message);
        return res.status(500).json({ error: `Mongorestore failed: ${stderr || err.message}` });
      }

      res.json({
        success: true,
        message: 'Database restore completed successfully',
        details: stdout || stderr
      });
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', time: Date.now() });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`MongoDB Studio WebUI running at http://localhost:${PORT}`);
});
