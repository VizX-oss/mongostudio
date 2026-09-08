package com.mongostudio.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mongostudio.app.data.api.MongoStudioApiClient
import com.mongostudio.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val serverUrl: String = "http://10.0.2.2:4000",
    val serverPingMs: Long? = null,
    val isServerReachable: Boolean = false,
    val isConnectedToCluster: Boolean = false,
    val connectionId: String? = null,
    val activeClusterName: String? = null,
    val activeClusterVersion: String? = null,
    val isConnecting: Boolean = false,
    val isLoading: Boolean = false,
    val overview: ClusterOverview? = null,
    val savedConnections: List<SavedConnection> = emptyList(),
    val selectedDatabase: String? = null,
    val collections: List<CollectionInfo> = emptyList(),
    val selectedCollection: String? = null,
    val queryResult: QueryResponse? = null,
    val filterJson: String = "",
    val sortJson: String = "",
    val projectionJson: String = "",
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val indexes: List<IndexInfo> = emptyList(),
    val aggregateResult: AggregateResponse? = null,
    val rawCommandResult: GenericApiResponse? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class MongoStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("mongostudio_prefs", Context.MODE_PRIVATE)
    private val apiClient = MongoStudioApiClient()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val savedServerUrl = prefs.getString("server_url", "http://10.0.2.2:4000") ?: "http://10.0.2.2:4000"
        _uiState.value = _uiState.value.copy(serverUrl = savedServerUrl)
        apiClient.setBaseUrl(savedServerUrl)
        testServerHealth()
        loadSavedConnections()
    }

    fun setServerUrl(newUrl: String) {
        val clean = newUrl.trimEnd('/')
        prefs.edit().putString("server_url", clean).apply()
        apiClient.setBaseUrl(clean)
        _uiState.value = _uiState.value.copy(serverUrl = clean)
        testServerHealth()
        loadSavedConnections()
    }

    fun testServerHealth() {
        viewModelScope.launch {
            val result = apiClient.healthCheck()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isServerReachable = true,
                    serverPingMs = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isServerReachable = false,
                    serverPingMs = null
                )
            }
        }
    }

    fun loadSavedConnections() {
        viewModelScope.launch {
            apiClient.getSavedConnections().onSuccess { list ->
                _uiState.value = _uiState.value.copy(savedConnections = list)
            }.onFailure {
                // If offline, ignore
            }
        }
    }

    fun connect(uri: String, name: String?, save: Boolean) {
        if (uri.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "MongoDB URI cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
            val result = apiClient.connect(uri, name, save)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isConnectedToCluster = true,
                    connectionId = response.connectionId,
                    activeClusterName = name ?: "MongoDB Cluster",
                    activeClusterVersion = response.serverVersion,
                    statusMessage = "Connected successfully to cluster!"
                )
                loadOverview()
                if (save) loadSavedConnections()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = error.localizedMessage ?: "Failed to connect to MongoDB cluster"
                )
            }
        }
    }

    fun connectSaved(saved: SavedConnection) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
            val result = apiClient.connectSaved(saved.id)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isConnectedToCluster = true,
                    connectionId = response.connectionId,
                    activeClusterName = saved.name,
                    activeClusterVersion = response.serverVersion,
                    statusMessage = "Connected to ${saved.name}!"
                )
                loadOverview()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = error.localizedMessage ?: "Failed to connect to saved cluster"
                )
            }
        }
    }

    fun deleteSaved(id: String) {
        viewModelScope.launch {
            apiClient.deleteSavedConnection(id).onSuccess {
                loadSavedConnections()
                _uiState.value = _uiState.value.copy(statusMessage = "Connection removed from vault")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            apiClient.disconnect()
            _uiState.value = _uiState.value.copy(
                isConnectedToCluster = false,
                connectionId = null,
                activeClusterName = null,
                activeClusterVersion = null,
                overview = null,
                selectedDatabase = null,
                collections = emptyList(),
                selectedCollection = null,
                queryResult = null
            )
        }
    }

    fun loadOverview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.getOverview().onSuccess { ov ->
                _uiState.value = _uiState.value.copy(
                    overview = ov,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to load cluster overview"
                )
            }
        }
    }

    fun selectDatabase(dbName: String) {
        _uiState.value = _uiState.value.copy(
            selectedDatabase = dbName,
            selectedCollection = null,
            collections = emptyList()
        )
        loadCollections(dbName)
    }

    fun loadCollections(dbName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.getCollections(dbName).onSuccess { cols ->
                _uiState.value = _uiState.value.copy(
                    collections = cols,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to load collections"
                )
            }
        }
    }

    fun createCollection(dbName: String, colName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.createCollection(dbName, colName).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Collection '$colName' created"
                )
                loadCollections(dbName)
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to create collection"
                )
            }
        }
    }

    fun dropCollection(dbName: String, colName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.dropCollection(dbName, colName).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Collection '$colName' dropped"
                )
                loadCollections(dbName)
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to drop collection"
                )
            }
        }
    }

    fun dropDatabase(dbName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.dropDatabase(dbName).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedDatabase = null,
                    statusMessage = "Database '$dbName' dropped"
                )
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to drop database"
                )
            }
        }
    }

    fun selectCollection(dbName: String, colName: String) {
        _uiState.value = _uiState.value.copy(
            selectedDatabase = dbName,
            selectedCollection = colName,
            currentPage = 1
        )
        runQuery(page = 1)
        loadIndexes(dbName, colName)
    }

    fun updateQueryParams(filter: String, sort: String, projection: String) {
        _uiState.value = _uiState.value.copy(
            filterJson = filter,
            sortJson = sort,
            projectionJson = projection
        )
    }

    fun runQuery(page: Int = _uiState.value.currentPage) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPage = page)
            val res = apiClient.queryDocuments(
                dbName = db,
                colName = col,
                filterJson = _uiState.value.filterJson.ifBlank { null },
                sortJson = _uiState.value.sortJson.ifBlank { null },
                projectionJson = _uiState.value.projectionJson.ifBlank { null },
                page = page,
                limit = _uiState.value.pageSize
            )
            res.onSuccess { qRes ->
                _uiState.value = _uiState.value.copy(
                    queryResult = qRes,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Query execution failed"
                )
            }
        }
    }

    fun createDocument(docJson: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.createDocument(db, col, docJson).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document inserted successfully!"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Insert failed"
                )
            }
        }
    }

    fun updateDocument(docId: String, docJson: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.updateDocument(db, col, docId, docJson).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document updated and pushed to MongoDB!"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Update failed"
                )
            }
        }
    }

    fun deleteDocument(docId: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.deleteDocument(db, col, docId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document deleted"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Delete failed"
                )
            }
        }
    }

    fun runAggregate(pipelineJson: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, aggregateResult = null)
            apiClient.aggregate(db, col, pipelineJson).onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    aggregateResult = res,
                    isLoading = false,
                    statusMessage = "Aggregation completed (${res.count} results)"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Aggregation failed"
                )
            }
        }
    }

    fun loadIndexes(dbName: String, colName: String) {
        viewModelScope.launch {
            apiClient.getIndexes(dbName, colName).onSuccess { idxs ->
                _uiState.value = _uiState.value.copy(indexes = idxs)
            }
        }
    }

    fun createIndex(keysJson: String, optionsJson: String?) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.createIndex(db, col, keysJson, optionsJson).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Index created successfully"
                )
                loadIndexes(db, col)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Index creation failed"
                )
            }
        }
    }

    fun dropIndex(indexName: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiClient.dropIndex(db, col, indexName).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Index '$indexName' dropped"
                )
                loadIndexes(db, col)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Drop index failed"
                )
            }
        }
    }

    fun executeRawCommand(dbName: String, commandJson: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, rawCommandResult = null)
            apiClient.executeRawCommand(dbName, commandJson).onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    rawCommandResult = res,
                    isLoading = false,
                    statusMessage = "Command executed successfully"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Command failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
