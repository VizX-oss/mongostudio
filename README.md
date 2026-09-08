# MongoStudio Pro - WebUI MongoDB Manager

A modern, clean, colorful, and lightweight WebUI for managing MongoDB databases, clusters (Atlas & Local), collections, and documents.

## ✨ Key Features

1. **Modern Colorful Glassmorphism UI**:
   - Built with Tailwind CSS, Lucide icons, and Atom One Dark syntax highlighting.
   - Clean dark palette with emerald accents, glowing cards, and responsive layout.

2. **Secure Connection Management**:
   - Direct connection via MongoDB URI strings (`mongodb+srv://...` or `mongodb://...`).
   - **Encrypted Vault**: Option to save credentials securely encrypted with AES-256 for one-click access.

3. **Full Cluster Dashboard**:
   - Real-time disk size statistics, database list, and collection tallies.
   - Server health check & MongoDB version detection.

4. **Document Browser & Direct Push**:
   - Filter, sort, and project documents with JSON queries (with automatic BSON `ObjectId` & `ISODate` conversion).
   - View in **Syntax-Highlighted JSON Cards** or **Structured Data Table**.
   - **Direct Push**: Edit JSON documents inline and commit changes directly to the database.
   - Quick Document Insertion and Deletion.

5. **Backup & Disaster Recovery**:
   - **Native Binary Backup**: Automated `mongodump` archive generation (`.archive.gz`) for entire clusters or single databases.
   - **Native Restore**: Integrated `mongorestore` tool with optional `--drop` flag.
   - **JSON Export / Import**: Single-click collection or whole-database JSON export (.zip) and JSON document importer.

## 🚀 Running the WebUI

```bash
# Start the server
npm start
```

Access the WebUI in your browser:
**`http://localhost:4000`**
