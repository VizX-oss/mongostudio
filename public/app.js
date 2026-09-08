// MongoStudio Beast JavaScript Logic
let currentConnectionId = null;
let currentClusterName = 'Cluster';
let currentServerVersion = '6.x';
let cachedOverview = null;
let currentDb = null;
let currentCol = null;
let currentQueryDocs = [];
let currentDocViewMode = 'json'; // 'json' or 'table'
let currentPage = 1;
let totalPages = 1;
let editingDocId = null;

document.addEventListener('DOMContentLoaded', () => {
  lucide.createIcons();
  setupEventListeners();
  loadSavedConnections();
});

function setupEventListeners() {
  document.getElementById('tabNewConnect').addEventListener('click', () => switchConnectTab('new'));
  document.getElementById('tabSavedConnect').addEventListener('click', () => switchConnectTab('saved'));
  document.getElementById('btnConnect').addEventListener('click', connectToMongo);

  document.getElementById('queryFilterInput').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') runQuery(1);
  });
}

// Drawer helpers for Mobile Off-Canvas
function toggleMobileSidebar() {
  const drawer = document.getElementById('sidebarDrawer');
  if (drawer.classList.contains('-translate-x-full')) {
    openMobileSidebar();
  } else {
    closeMobileSidebar();
  }
}

function openMobileSidebar() {
  const drawer = document.getElementById('sidebarDrawer');
  const backdrop = document.getElementById('sidebarBackdrop');
  drawer.classList.remove('-translate-x-full');
  backdrop.classList.remove('hidden');
}

function closeMobileSidebar() {
  const drawer = document.getElementById('sidebarDrawer');
  const backdrop = document.getElementById('sidebarBackdrop');
  drawer.classList.add('-translate-x-full');
  backdrop.classList.add('hidden');
}

function switchConnectTab(tab) {
  const tabNew = document.getElementById('tabNewConnect');
  const tabSaved = document.getElementById('tabSavedConnect');
  const formNew = document.getElementById('newConnectForm');
  const formSaved = document.getElementById('savedConnectForm');

  if (tab === 'new') {
    tabNew.className = 'py-2.5 rounded-xl bg-[#00ED64] text-black shadow-md flex items-center justify-center gap-2 transition';
    tabSaved.className = 'py-2.5 rounded-xl text-slate-400 hover:text-white flex items-center justify-center gap-2 transition';
    formNew.classList.remove('hidden');
    formSaved.classList.add('hidden');
  } else {
    tabSaved.className = 'py-2.5 rounded-xl bg-[#00ED64] text-black shadow-md flex items-center justify-center gap-2 transition';
    tabNew.className = 'py-2.5 rounded-xl text-slate-400 hover:text-white flex items-center justify-center gap-2 transition';
    formSaved.classList.remove('hidden');
    formNew.classList.add('hidden');
    loadSavedConnections();
  }
  lucide.createIcons();
}

function fillUri(uri, name) {
  document.getElementById('mongoUriInput').value = uri;
  if (name) document.getElementById('clusterNickName').value = name;
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  
  let bg = 'bg-[#081017] border-[#152433] text-white';
  let icon = 'info';

  if (type === 'success') {
    bg = 'bg-[#031d10] border-[#00ED64]/40 text-[#00ED64]';
    icon = 'check-circle-2';
  } else if (type === 'error') {
    bg = 'bg-[#26090e] border-rose-500/40 text-rose-300';
    icon = 'alert-circle';
  }

  toast.className = `px-4 py-3 rounded-2xl border shadow-2xl backdrop-blur-md text-xs flex items-center gap-2.5 transition-all transform duration-200 opacity-0 -translate-y-2 pointer-events-auto ${bg}`;
  toast.innerHTML = `<i data-lucide="${icon}" class="w-4 h-4 flex-shrink-0"></i><span class="truncate">${escapeHtml(message)}</span>`;
  container.appendChild(toast);
  lucide.createIcons();

  requestAnimationFrame(() => {
    toast.classList.remove('opacity-0', '-translate-y-2');
  });

  setTimeout(() => {
    toast.classList.add('opacity-0', '-translate-y-2');
    setTimeout(() => toast.remove(), 250);
  }, 3200);
}

// ==============================================
// 1. CONNECTION & AUTH
// ==============================================

async function loadSavedConnections() {
  try {
    const res = await fetch('/api/saved-connections');
    const list = await res.json();
    
    document.getElementById('savedCountBadge').textContent = list.length;
    const container = document.getElementById('savedConnectionsList');
    const emptyMsg = document.getElementById('noSavedConnections');

    if (list.length === 0) {
      container.innerHTML = '';
      emptyMsg.classList.remove('hidden');
      return;
    }

    emptyMsg.classList.add('hidden');
    container.innerHTML = list.map(item => `
      <div class="glass-card rounded-2xl p-3 flex items-center justify-between border border-[#152433]">
        <div class="min-w-0 pr-2">
          <div class="flex items-center gap-1.5">
            <span class="font-bold text-xs text-white truncate">${escapeHtml(item.name || 'MongoDB Cluster')}</span>
            <span class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-[#00ED64]/20 text-[#00ED64]">AES-256</span>
          </div>
          <p class="text-[10px] font-mono text-slate-400 truncate mt-0.5">${escapeHtml(item.maskedUri)}</p>
        </div>
        <div class="flex items-center gap-1.5 flex-shrink-0">
          <button onclick="connectSavedConnection('${item.id}', '${escapeHtml(item.name)}')" 
            class="tap-scale px-3 py-1.5 rounded-xl bg-[#00ED64] text-black font-extrabold text-xs shadow-md shadow-[#00ED64]/10">
            Connect
          </button>
          <button onclick="deleteSavedConnection('${item.id}')" class="p-2 text-slate-500 hover:text-rose-400" title="Delete">
            <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
          </button>
        </div>
      </div>
    `).join('');
    lucide.createIcons();
  } catch (err) {
    console.error(err);
  }
}

async function connectToMongo() {
  const uri = document.getElementById('mongoUriInput').value.trim();
  const name = document.getElementById('clusterNickName').value.trim() || 'MongoDB Cluster';
  const save = document.getElementById('saveConnectionCheck').checked;

  if (!uri) {
    showToast('Please enter a MongoDB connection URI', 'error');
    return;
  }

  const btn = document.getElementById('btnConnect');
  const btnText = document.getElementById('btnConnectText');
  const originalText = btnText.innerText;

  try {
    btn.disabled = true;
    btnText.innerText = 'Connecting to Cluster...';

    const res = await fetch('/api/connect', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ uri, name, saveConnection: save })
    });

    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.error || 'Connection failed');
    }

    currentConnectionId = data.connectionId;
    currentClusterName = name;
    currentServerVersion = data.serverVersion || 'Unknown';

    showToast('Connected to Cluster!', 'success');
    enterAppWorkspace();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btnText.innerText = originalText;
  }
}

async function connectSavedConnection(id, name) {
  try {
    showToast('Decrypting & connecting...', 'info');
    const res = await fetch(`/api/saved-connections/${id}/connect`, { method: 'POST' });
    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.error || 'Connection failed');
    }

    currentConnectionId = data.connectionId;
    currentClusterName = data.name || name || 'Saved Cluster';
    currentServerVersion = data.serverVersion || 'Unknown';

    showToast('Connected successfully!', 'success');
    enterAppWorkspace();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function deleteSavedConnection(id) {
  if (!confirm('Delete this saved connection from vault?')) return;
  try {
    await fetch(`/api/saved-connections/${id}`, { method: 'DELETE' });
    showToast('Connection removed from vault', 'info');
    loadSavedConnections();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function disconnectSession() {
  if (!confirm('Disconnect from current MongoDB cluster?')) return;
  try {
    if (currentConnectionId) {
      await fetch('/api/disconnect', {
        method: 'POST',
        headers: { 'x-connection-id': currentConnectionId }
      });
    }
  } catch (e) {}

  currentConnectionId = null;
  currentDb = null;
  currentCol = null;
  document.getElementById('appView').classList.add('hidden');
  document.getElementById('connectView').classList.remove('hidden');
  closeMobileSidebar();
  loadSavedConnections();
  showToast('Session disconnected', 'info');
}

function enterAppWorkspace() {
  document.getElementById('connectView').classList.add('hidden');
  document.getElementById('appView').classList.remove('hidden');

  document.getElementById('headerClusterName').textContent = currentClusterName;
  document.getElementById('headerServerVersion').textContent = `v${currentServerVersion}`;
  document.getElementById('statServerVersion').textContent = `v${currentServerVersion}`;

  switchView('dashboard');
  refreshOverview();
}

// ==============================================
// 2. DASHBOARD & OVERVIEW
// ==============================================

async function refreshOverview() {
  try {
    const res = await fetch('/api/overview', {
      headers: { 'x-connection-id': currentConnectionId }
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    cachedOverview = data;
    renderDashboard(data);
    renderSidebarTree(data.databases);
    populateBackupSelectors(data.databases);
    populateConsoleDatabases(data.databases);
  } catch (err) {
    showToast(`Sync error: ${err.message}`, 'error');
  }
}

function formatBytes(bytes, decimals = 1) {
  if (!+bytes) return '0 B';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}

function renderDashboard(data) {
  document.getElementById('statTotalSize').textContent = formatBytes(data.totalSize || 0);
  document.getElementById('statTotalDbs').textContent = data.databases.length;

  let totalCols = 0;
  data.databases.forEach(d => totalCols += (d.collectionsCount || 0));
  document.getElementById('statTotalCollections').textContent = totalCols;

  const grid = document.getElementById('dashboardDbsGrid');
  grid.innerHTML = data.databases.map(db => {
    const isSystem = ['admin', 'local', 'config'].includes(db.name);
    return `
      <div class="glass-card rounded-2xl p-4 flex flex-col justify-between hover:border-[#00ED64]/40 transition">
        <div>
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2 min-w-0">
              <div class="p-2 rounded-xl bg-[#00ED64]/10 text-[#00ED64] flex-shrink-0">
                <i data-lucide="database" class="w-4 h-4"></i>
              </div>
              <h4 class="font-bold text-xs sm:text-sm text-white truncate">${escapeHtml(db.name)}</h4>
            </div>
            ${isSystem 
              ? `<span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#04090e] text-slate-500 border border-[#152433]">System</span>` 
              : `<button onclick="confirmDropDatabase('${escapeHtml(db.name)}')" class="p-1 text-slate-500 hover:text-rose-400" title="Drop Database"><i data-lucide="trash-2" class="w-3.5 h-3.5"></i></button>`
            }
          </div>

          <div class="grid grid-cols-2 gap-2 text-xs py-2.5 border-y border-[#152433] my-2">
            <div>
              <span class="text-[10px] text-slate-500 block">Collections</span>
              <span class="font-bold text-slate-200 text-xs sm:text-sm">${db.collectionsCount || 0}</span>
            </div>
            <div>
              <span class="text-[10px] text-slate-500 block">Disk Usage</span>
              <span class="font-bold text-slate-200 text-xs sm:text-sm">${formatBytes(db.sizeOnDisk)}</span>
            </div>
          </div>
        </div>

        <div class="pt-1 flex items-center justify-between gap-2">
          <button onclick="downloadDbZipExport('${escapeHtml(db.name)}')" class="text-xs text-slate-400 hover:text-white flex items-center gap-1 p-1">
            <i data-lucide="download" class="w-3.5 h-3.5"></i> <span>Export</span>
          </button>
          <button onclick="openDatabaseCollections('${escapeHtml(db.name)}')" class="tap-scale px-3 py-1.5 rounded-xl bg-[#00ED64]/15 text-[#00ED64] border border-[#00ED64]/30 text-xs font-bold flex items-center gap-1">
            Explore <i data-lucide="chevron-right" class="w-3.5 h-3.5"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');
  lucide.createIcons();
}

function renderSidebarTree(databases) {
  const container = document.getElementById('sidebarDbTree');
  document.getElementById('sidebarTotalDbs').textContent = `${databases.length} Databases`;

  if (!databases || databases.length === 0) {
    container.innerHTML = `<div class="text-center py-6 text-xs text-slate-500">No databases found</div>`;
    return;
  }

  container.innerHTML = databases.map(db => `
    <div class="db-sidebar-item rounded-xl p-1 text-xs transition" data-dbname="${escapeHtml(db.name)}">
      <div class="flex items-center justify-between cursor-pointer py-1.5 px-2 rounded-xl hover:bg-[#081017] text-slate-200" onclick="toggleSidebarDb('${escapeHtml(db.name)}')">
        <div class="flex items-center gap-2 min-w-0">
          <i data-lucide="folder" class="w-3.5 h-3.5 text-[#00ED64] flex-shrink-0"></i>
          <span class="font-bold text-xs truncate">${escapeHtml(db.name)}</span>
        </div>
        <span class="text-[10px] text-slate-500 font-mono">${db.collectionsCount || 0}</span>
      </div>
      <div id="colList-${escapeHtml(db.name)}" class="hidden pl-3 pr-1 py-1 space-y-1 mt-0.5 border-l border-[#152433]"></div>
    </div>
  `).join('');
  lucide.createIcons();
}

async function toggleSidebarDb(dbName) {
  const container = document.getElementById(`colList-${dbName}`);
  if (!container) return;

  if (!container.classList.contains('hidden')) {
    container.classList.add('hidden');
    return;
  }

  container.classList.remove('hidden');
  container.innerHTML = `<div class="text-[11px] text-slate-500 py-1">Loading collections...</div>`;

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(dbName)}/collections`, {
      headers: { 'x-connection-id': currentConnectionId }
    });
    const collections = await res.json();
    if (!res.ok) throw new Error(collections.error);

    if (collections.length === 0) {
      container.innerHTML = `<div class="text-[11px] text-slate-500 py-1">No collections</div>`;
      return;
    }

    container.innerHTML = collections.map(col => `
      <div onclick="selectCollection('${escapeHtml(dbName)}', '${escapeHtml(col.name)}'); closeMobileSidebar();" 
        class="flex items-center justify-between py-1.5 px-2 rounded-xl text-slate-400 hover:text-white hover:bg-[#081017] cursor-pointer text-xs transition">
        <span class="truncate flex items-center gap-1.5">
          <i data-lucide="layers" class="w-3 h-3 text-cyan-400 flex-shrink-0"></i>
          <span class="truncate">${escapeHtml(col.name)}</span>
        </span>
        <span class="text-[10px] text-slate-500 font-mono">${col.docCount}</span>
      </div>
    `).join('');
    lucide.createIcons();
  } catch (err) {
    container.innerHTML = `<div class="text-[11px] text-rose-400 py-1">${err.message}</div>`;
  }
}

function filterSidebarDatabases(query) {
  const items = document.querySelectorAll('.db-sidebar-item');
  const q = query.toLowerCase();
  items.forEach(el => {
    const name = el.getAttribute('data-dbname').toLowerCase();
    el.style.display = name.includes(q) ? '' : 'none';
  });
}

function openDatabaseCollections(dbName) {
  toggleSidebarDb(dbName);
  fetch(`/api/databases/${encodeURIComponent(dbName)}/collections`, {
    headers: { 'x-connection-id': currentConnectionId }
  }).then(r => r.json()).then(cols => {
    if (cols && cols.length > 0) {
      selectCollection(dbName, cols[0].name);
    } else {
      selectCollection(dbName, '');
    }
  });
}

function switchView(view) {
  const views = ['dashboard', 'collections', 'console', 'backup'];
  views.forEach(v => {
    document.getElementById(`view${v.charAt(0).toUpperCase() + v.slice(1)}`).classList.add('hidden');
    
    // Desktop Nav
    const navBtn = document.getElementById(`navBtn-${v}`);
    if (navBtn) {
      navBtn.className = 'px-3.5 py-1.5 rounded-xl text-slate-400 hover:text-white flex items-center gap-1.5 transition';
    }

    // Mobile Nav
    const mobBtn = document.getElementById(`mobileNav-${v}`);
    if (mobBtn) {
      mobBtn.className = 'flex flex-col items-center gap-1 text-slate-400 py-1 px-3 tap-scale';
    }
  });

  document.getElementById(`view${view.charAt(0).toUpperCase() + view.slice(1)}`).classList.remove('hidden');
  
  const activeNav = document.getElementById(`navBtn-${view}`);
  if (activeNav) {
    activeNav.className = 'px-3.5 py-1.5 rounded-xl text-black bg-[#00ED64] font-bold flex items-center gap-1.5 transition';
  }

  const activeMob = document.getElementById(`mobileNav-${view}`);
  if (activeMob) {
    activeMob.className = 'flex flex-col items-center gap-1 text-[#00ED64] py-1 px-3 tap-scale';
  }

  closeMobileSidebar();
  lucide.createIcons();
}

// ==============================================
// 3. COLLECTIONS & DOCUMENTS MANAGER
// ==============================================

function selectCollection(dbName, colName) {
  currentDb = dbName;
  currentCol = colName;

  document.getElementById('currentDbLabel').textContent = dbName || 'DB';
  document.getElementById('currentColLabel').textContent = colName || 'Collection';

  switchView('collections');

  if (colName) {
    currentPage = 1;
    runQuery(1);
  } else {
    document.getElementById('documentsListContainer').innerHTML = `
      <div class="text-center py-16 text-slate-500 text-xs">
        Select a collection to view documents.
      </div>
    `;
  }
}

async function runQuery(page = 1) {
  if (!currentDb || !currentCol) {
    showToast('Select a database and collection first', 'error');
    return;
  }

  currentPage = page;
  const filterRaw = document.getElementById('queryFilterInput').value.trim();
  const sortRaw = document.getElementById('querySortInput').value.trim();
  const projRaw = document.getElementById('queryProjectInput').value.trim();

  let filter = {}, sort = {}, projection = {};
  try {
    if (filterRaw) filter = JSON.parse(filterRaw);
  } catch (e) {
    showToast('Invalid Filter JSON: ' + e.message, 'error');
    return;
  }
  try {
    if (sortRaw) sort = JSON.parse(sortRaw);
  } catch (e) {
    showToast('Invalid Sort JSON: ' + e.message, 'error');
    return;
  }
  try {
    if (projRaw) projection = JSON.parse(projRaw);
  } catch (e) {
    showToast('Invalid Projection JSON: ' + e.message, 'error');
    return;
  }

  const container = document.getElementById('documentsListContainer');
  container.innerHTML = `<div class="text-center py-8 text-xs text-slate-500">Executing find query...</div>`;

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(currentDb)}/collections/${encodeURIComponent(currentCol)}/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-connection-id': currentConnectionId
      },
      body: JSON.stringify({ filter, sort, projection, page: currentPage, limit: 15 })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    currentQueryDocs = data.documents || [];
    totalPages = data.totalPages || 1;

    document.getElementById('queryTotalCount').textContent = data.total;
    document.getElementById('queryCurrentPage').textContent = data.page;
    document.getElementById('queryTotalPages').textContent = data.totalPages;
    document.getElementById('paginationRange').textContent = `${data.documents.length} of ${data.total} items`;

    document.getElementById('btnPrevPage').disabled = (data.page <= 1);
    document.getElementById('btnNextPage').disabled = (data.page >= data.totalPages);

    renderDocuments();
  } catch (err) {
    container.innerHTML = `<div class="p-3 bg-[#26090e] border border-rose-500/40 text-rose-300 rounded-2xl text-xs">${err.message}</div>`;
  }
}

function resetQueryFilters() {
  document.getElementById('queryFilterInput').value = '';
  document.getElementById('querySortInput').value = '';
  document.getElementById('queryProjectInput').value = '';
  runQuery(1);
}

function changePage(delta) {
  const newPage = currentPage + delta;
  if (newPage >= 1 && newPage <= totalPages) {
    runQuery(newPage);
  }
}

function setDocViewMode(mode) {
  currentDocViewMode = mode;
  const btnTable = document.getElementById('viewModeTableBtn');
  const btnJson = document.getElementById('viewModeJsonBtn');

  if (mode === 'table') {
    btnTable.className = 'px-2.5 py-1 rounded-lg bg-[#00ED64]/20 text-[#00ED64] font-bold flex items-center gap-1';
    btnJson.className = 'px-2.5 py-1 rounded-lg text-slate-400 hover:text-white flex items-center gap-1';
  } else {
    btnJson.className = 'px-2.5 py-1 rounded-lg bg-[#00ED64]/20 text-[#00ED64] font-bold flex items-center gap-1';
    btnTable.className = 'px-2.5 py-1 rounded-lg text-slate-400 hover:text-white flex items-center gap-1';
  }
  renderDocuments();
}

function renderDocuments() {
  const container = document.getElementById('documentsListContainer');

  if (currentQueryDocs.length === 0) {
    container.innerHTML = `
      <div class="glass-dark rounded-2xl p-8 text-center text-slate-500 space-y-2">
        <i data-lucide="inbox" class="w-8 h-8 mx-auto text-slate-600"></i>
        <p class="text-xs">No documents match the query criteria.</p>
        <button onclick="openAddDocumentModal()" class="mt-2 px-3.5 py-1.5 rounded-xl bg-[#00ED64]/20 text-[#00ED64] text-xs font-bold">
          Insert Document
        </button>
      </div>
    `;
    lucide.createIcons();
    return;
  }

  if (currentDocViewMode === 'json') {
    container.innerHTML = currentQueryDocs.map((doc, idx) => {
      const docId = doc._id;
      const jsonStr = JSON.stringify(doc, null, 2);
      return `
        <div class="glass-card rounded-2xl p-3.5 space-y-2.5">
          <div class="flex items-center justify-between pb-2 border-b border-[#152433] gap-2">
            <span class="text-[10px] sm:text-[11px] font-mono font-bold px-2 py-0.5 rounded-lg bg-[#04090e] text-[#00ED64] border border-[#152433] truncate">_id: ${escapeHtml(String(docId))}</span>
            <div class="flex items-center gap-1 sm:gap-1.5 flex-shrink-0">
              <button onclick="copyDocJson(${idx})" class="p-1.5 rounded-lg text-slate-400 hover:text-white" title="Copy JSON">
                <i data-lucide="copy" class="w-3.5 h-3.5"></i>
              </button>
              <button onclick="openEditDocumentModal(${idx})" class="tap-scale px-2.5 py-1 rounded-lg bg-[#00ED64]/15 text-[#00ED64] text-xs font-bold flex items-center gap-1 border border-[#00ED64]/30">
                <i data-lucide="edit-3" class="w-3 h-3"></i> <span>Edit & Push</span>
              </button>
              <button onclick="deleteDocument('${escapeHtml(String(docId))}')" class="p-1.5 text-slate-500 hover:text-rose-400" title="Delete">
                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
              </button>
            </div>
          </div>
          <pre class="bg-[#03070b] p-3 rounded-xl overflow-x-auto text-[11px] font-mono leading-relaxed touch-scroll"><code class="language-json">${escapeHtml(jsonStr)}</code></pre>
        </div>
      `;
    }).join('');
  } else {
    // Table mode with clean touch scroll
    const allKeys = Array.from(new Set(currentQueryDocs.flatMap(d => Object.keys(d))));
    let tableHead = allKeys.map(k => `<th class="p-2.5 text-left font-bold text-slate-300">${escapeHtml(k)}</th>`).join('');
    let tableRows = currentQueryDocs.map((doc, idx) => {
      const docId = doc._id;
      const cells = allKeys.map(k => {
        const val = doc[k];
        let valStr = '';
        if (val === undefined) valStr = '<span class="text-slate-600">-</span>';
        else if (typeof val === 'object' && val !== null) valStr = `<span class="font-mono text-[10px] text-cyan-400">${escapeHtml(JSON.stringify(val))}</span>`;
        else valStr = `<span class="font-mono text-[11px] text-slate-200">${escapeHtml(String(val))}</span>`;
        return `<td class="p-2.5 border-t border-[#152433] max-w-xs truncate">${valStr}</td>`;
      }).join('');

      return `
        <tr class="hover:bg-[#0b1622]">
          <td class="p-2.5 border-t border-[#152433] flex items-center gap-1.5">
            <button onclick="openEditDocumentModal(${idx})" class="p-1 text-[#00ED64]"><i data-lucide="edit-3" class="w-3.5 h-3.5"></i></button>
            <button onclick="deleteDocument('${escapeHtml(String(docId))}')" class="p-1 text-rose-400"><i data-lucide="trash-2" class="w-3.5 h-3.5"></i></button>
          </td>
          ${cells}
        </tr>
      `;
    }).join('');

    container.innerHTML = `
      <div class="glass-dark rounded-2xl overflow-x-auto touch-scroll">
        <table class="w-full text-xs">
          <thead class="bg-[#04090e] border-b border-[#152433]">
            <tr>
              <th class="p-2.5 text-left text-slate-400">Actions</th>
              ${tableHead}
            </tr>
          </thead>
          <tbody>
            ${tableRows}
          </tbody>
        </table>
      </div>
    `;
  }

  lucide.createIcons();
  document.querySelectorAll('pre code').forEach((el) => {
    hljs.highlightElement(el);
  });
}

function copyDocJson(idx) {
  const doc = currentQueryDocs[idx];
  navigator.clipboard.writeText(JSON.stringify(doc, null, 2));
  showToast('JSON copied to clipboard', 'info');
}

// ==============================================
// 4. DIRECT PUSH / EDIT OPERATIONS
// ==============================================

function openAddDocumentModal() {
  if (!currentDb || !currentCol) {
    showToast('Select a database and collection first', 'error');
    return;
  }
  editingDocId = null;
  document.getElementById('modalDocumentTitle').textContent = `Insert into ${currentCol}`;
  document.getElementById('modalDocTextarea').value = JSON.stringify({
    name: "Sample Document",
    category: "Gadgets",
    price: 99.99,
    isActive: true,
    createdAt: new Date().toISOString()
  }, null, 2);
  document.getElementById('modalDocStatus').textContent = 'New Document';
  openModal('modalDocument');
}

function openEditDocumentModal(idx) {
  const doc = currentQueryDocs[idx];
  editingDocId = doc._id;
  document.getElementById('modalDocumentTitle').textContent = `Edit & Push: ${doc._id}`;
  document.getElementById('modalDocTextarea').value = JSON.stringify(doc, null, 2);
  document.getElementById('modalDocStatus').textContent = 'Direct Push';
  openModal('modalDocument');
}

function formatDocEditorJson() {
  try {
    const raw = document.getElementById('modalDocTextarea').value;
    const parsed = JSON.parse(raw);
    document.getElementById('modalDocTextarea').value = JSON.stringify(parsed, null, 2);
  } catch (e) {
    showToast('Invalid JSON: ' + e.message, 'error');
  }
}

async function saveDocumentDirectPush() {
  const raw = document.getElementById('modalDocTextarea').value;
  let parsed = null;
  try {
    parsed = JSON.parse(raw);
  } catch (e) {
    showToast('JSON parse error: ' + e.message, 'error');
    return;
  }

  const btn = document.getElementById('btnSaveDocDirect');
  btn.disabled = true;

  try {
    if (editingDocId) {
      const res = await fetch(`/api/databases/${encodeURIComponent(currentDb)}/collections/${encodeURIComponent(currentCol)}/documents/${encodeURIComponent(editingDocId)}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'x-connection-id': currentConnectionId
        },
        body: JSON.stringify({ document: parsed })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);

      showToast('Changes pushed directly to DB!', 'success');
    } else {
      const res = await fetch(`/api/databases/${encodeURIComponent(currentDb)}/collections/${encodeURIComponent(currentCol)}/documents`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-connection-id': currentConnectionId
        },
        body: JSON.stringify({ document: parsed })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);

      showToast('Document inserted successfully!', 'success');
    }

    closeModal('modalDocument');
    runQuery(currentPage);
  } catch (err) {
    showToast(`Failed to push: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
  }
}

async function deleteDocument(docId) {
  if (!confirm(`Delete document ${docId}? This is irreversible.`)) return;

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(currentDb)}/collections/${encodeURIComponent(currentCol)}/documents/${encodeURIComponent(docId)}`, {
      method: 'DELETE',
      headers: { 'x-connection-id': currentConnectionId }
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    showToast('Document deleted from database', 'info');
    runQuery(currentPage);
  } catch (err) {
    showToast(`Error: ${err.message}`, 'error');
  }
}

function openNewDatabaseModal() {
  document.getElementById('createDbNameInput').value = currentDb || '';
  document.getElementById('createColNameInput').value = '';
  openModal('modalCreateDbCol');
}

async function submitCreateDbCol() {
  const dbName = document.getElementById('createDbNameInput').value.trim();
  const colName = document.getElementById('createColNameInput').value.trim();

  if (!dbName || !colName) {
    showToast('Both DB and collection name are required', 'error');
    return;
  }

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(dbName)}/collections`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-connection-id': currentConnectionId
      },
      body: JSON.stringify({ collectionName: colName })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    showToast(`Created collection '${colName}' in '${dbName}'!`, 'success');
    closeModal('modalCreateDbCol');
    refreshOverview();
    selectCollection(dbName, colName);
  } catch (err) {
    showToast(`Creation failed: ${err.message}`, 'error');
  }
}

async function confirmDropDatabase(dbName) {
  if (!confirm(`CAUTION: Drop database '${dbName}'? Irreversible action.`)) return;

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(dbName)}`, {
      method: 'DELETE',
      headers: { 'x-connection-id': currentConnectionId }
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    showToast(`Database '${dbName}' dropped`, 'info');
    refreshOverview();
    if (currentDb === dbName) {
      currentDb = null;
      currentCol = null;
      switchView('dashboard');
    }
  } catch (err) {
    showToast(`Drop failed: ${err.message}`, 'error');
  }
}

// ==============================================
// 5. RAW MONGO COMMAND CONSOLE
// ==============================================

function populateConsoleDatabases(databases) {
  const sel = document.getElementById('rawConsoleDbSelect');
  sel.innerHTML = `<option value="admin">admin (system)</option>` + 
    databases.map(d => `<option value="${escapeHtml(d.name)}">${escapeHtml(d.name)}</option>`).join('');
}

function setRawCommand(cmdStr) {
  document.getElementById('rawCommandInput').value = cmdStr;
}

async function executeRawCommand() {
  const raw = document.getElementById('rawCommandInput').value.trim();
  const dbName = document.getElementById('rawConsoleDbSelect').value || 'admin';

  if (!raw) {
    showToast('Please enter a command JSON', 'error');
    return;
  }

  const output = document.getElementById('rawCommandOutput');
  output.textContent = 'Executing command...';

  try {
    const res = await fetch('/api/raw-command', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-connection-id': currentConnectionId
      },
      body: JSON.stringify({ dbName, command: raw })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    output.textContent = JSON.stringify(data.result, null, 2);
    hljs.highlightElement(output);
    showToast('Command executed successfully', 'success');
  } catch (err) {
    output.textContent = `Error: ${err.message}`;
    showToast(`Execution failed: ${err.message}`, 'error');
  }
}

// ==============================================
// 6. BACKUP & RESTORE ACTIONS
// ==============================================

function populateBackupSelectors(databases) {
  const backupDbSelect = document.getElementById('backupDbSelect');
  const exportJsonDbSelect = document.getElementById('exportJsonDbSelect');

  const options = databases.map(d => `<option value="${escapeHtml(d.name)}">${escapeHtml(d.name)}</option>`).join('');
  
  backupDbSelect.innerHTML = `<option value="">Full Cluster (All Databases)</option>` + options;
  exportJsonDbSelect.innerHTML = `<option value="">Select database...</option>` + options;
}

function onExportDbChanged(dbName) {
  const colSelect = document.getElementById('exportJsonColSelect');
  if (!dbName) {
    colSelect.innerHTML = `<option value="">All Collections (.ZIP)</option>`;
    return;
  }

  fetch(`/api/databases/${encodeURIComponent(dbName)}/collections`, {
    headers: { 'x-connection-id': currentConnectionId }
  }).then(r => r.json()).then(cols => {
    colSelect.innerHTML = `<option value="">All Collections (.ZIP)</option>` +
      cols.map(c => `<option value="${escapeHtml(c.name)}">${escapeHtml(c.name)}</option>`).join('');
  });
}

function triggerMongodump() {
  const db = document.getElementById('backupDbSelect').value;
  let url = `/api/backup/mongodump`;
  if (db) url += `?dbName=${encodeURIComponent(db)}`;

  showToast('Starting mongodump archive generation...', 'info');
  fetch(url, {
    headers: { 'x-connection-id': currentConnectionId }
  })
  .then(res => {
    if (!res.ok) return res.json().then(d => { throw new Error(d.error || 'Backup failed'); });
    return res.blob();
  })
  .then(blob => {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `mongodump_${db || 'cluster'}_${Date.now()}.archive.gz`;
    a.click();
    showToast('Dump archive downloaded successfully!', 'success');
  })
  .catch(err => {
    showToast(err.message, 'error');
  });
}

async function triggerMongorestore() {
  const fileInput = document.getElementById('restoreArchiveFileInput');
  const drop = document.getElementById('dropBeforeRestoreCheck').checked;

  if (!fileInput.files || fileInput.files.length === 0) {
    showToast('Select a .archive.gz file to restore', 'error');
    return;
  }

  if (drop && !confirm('Drop collections before restore? Existing data will be overwritten.')) {
    return;
  }

  const formData = new FormData();
  formData.append('archive', fileInput.files[0]);
  formData.append('drop', drop ? 'true' : 'false');

  const btn = document.getElementById('btnStartRestore');
  btn.disabled = true;
  showToast('Executing mongorestore in background...', 'info');

  try {
    const res = await fetch('/api/restore/mongorestore', {
      method: 'POST',
      headers: { 'x-connection-id': currentConnectionId },
      body: formData
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    showToast('Restore completed successfully!', 'success');
    refreshOverview();
  } catch (err) {
    showToast(`Restore failed: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
  }
}

function downloadJsonExport() {
  const db = document.getElementById('exportJsonDbSelect').value;
  const col = document.getElementById('exportJsonColSelect').value;

  if (!db) {
    showToast('Select a database to export', 'error');
    return;
  }

  let url = `/api/databases/${encodeURIComponent(db)}/export`;
  if (col) url += `?collection=${encodeURIComponent(col)}`;

  showToast('Generating JSON export...', 'info');
  fetch(url, {
    headers: { 'x-connection-id': currentConnectionId }
  })
  .then(res => {
    if (!res.ok) return res.json().then(d => { throw new Error(d.error || 'Export failed'); });
    return res.blob();
  })
  .then(blob => {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = col ? `${db}_${col}_export.json` : `${db}_backup.zip`;
    a.click();
    showToast('JSON downloaded!', 'success');
  })
  .catch(err => {
    showToast(err.message, 'error');
  });
}

function exportCurrentCollection() {
  if (!currentDb || !currentCol) return;
  const url = `/api/databases/${encodeURIComponent(currentDb)}/export?collection=${encodeURIComponent(currentCol)}`;
  fetch(url, { headers: { 'x-connection-id': currentConnectionId } })
    .then(r => r.blob())
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = `${currentDb}_${currentCol}.json`;
      a.click();
    });
}

function downloadDbZipExport(dbName) {
  const url = `/api/databases/${encodeURIComponent(dbName)}/export`;
  showToast(`Preparing ZIP for ${dbName}...`, 'info');
  fetch(url, { headers: { 'x-connection-id': currentConnectionId } })
    .then(r => r.blob())
    .then(blob => {
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = `${dbName}_backup.zip`;
      a.click();
      showToast('Export downloaded!', 'success');
    });
}

function openImportModal() {
  if (!currentDb || !currentCol) {
    showToast('Select a collection first', 'error');
    return;
  }
  openModal('modalImport');
}

async function submitImportJson() {
  const fileInput = document.getElementById('importJsonFileInput');
  if (!fileInput.files || fileInput.files.length === 0) {
    showToast('Select a JSON file to import', 'error');
    return;
  }

  const formData = new FormData();
  formData.append('file', fileInput.files[0]);

  const btn = document.getElementById('btnSubmitImport');
  btn.disabled = true;

  try {
    const res = await fetch(`/api/databases/${encodeURIComponent(currentDb)}/collections/${encodeURIComponent(currentCol)}/import`, {
      method: 'POST',
      headers: { 'x-connection-id': currentConnectionId },
      body: formData
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);

    showToast(data.message, 'success');
    closeModal('modalImport');
    runQuery(1);
    refreshOverview();
  } catch (err) {
    showToast(`Import failed: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
  }
}

// Modal and Utility Helpers
function openModal(id) {
  document.getElementById(id).classList.remove('hidden');
  lucide.createIcons();
}

function closeModal(id) {
  document.getElementById(id).classList.add('hidden');
}

function escapeHtml(str) {
  if (typeof str !== 'string') return str;
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
