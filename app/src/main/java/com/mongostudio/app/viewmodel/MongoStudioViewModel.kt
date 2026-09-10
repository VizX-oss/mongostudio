package com.mongostudio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mongostudio.app.data.model.*
import com.mongostudio.app.data.service.DirectMongoService
import com.mongostudio.app.data.service.StandaloneConnectionInfo
import com.mongostudio.app.data.vault.EncryptedVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val isConnectedToCluster: Boolean = false,
    val activeClusterName: String? = null,
    val activeClusterVersion: String? = null,
    val activeClusterPingMs: Long? = null,
    val isConnecting: Boolean = false,
    val isTestingPing: Boolean = false,
    val pingTestResult: StandaloneConnectionInfo? = null,
    val pingTestError: String? = null,
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
    val rawCommandResult: Map<String, Any?>? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class MongoStudioViewModel(application: Application) : AndroidViewModel(application) {
    val vault = EncryptedVault(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSavedConnections()
    }

    fun loadSavedConnections() {
        val list = vault.getSavedConnections()
        _uiState.value = _uiState.value.copy(savedConnections = list)
    }

    fun pingTest(uri: String) {
        val cleanUri = uri.trim()
        if (cleanUri.isBlank()) {
            _uiState.value = _uiState.value.copy(pingTestError = "MongoDB URI cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingPing = true, pingTestResult = null, pingTestError = null)
            DirectMongoService.pingOnly(cleanUri)
                .onSuccess { info ->
                    _uiState.value = _uiState.value.copy(
                        isTestingPing = false,
                        pingTestResult = info,
                        statusMessage = "Ping successful: ${info.pingMs}ms (v${info.serverVersion})"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isTestingPing = false,
                        pingTestError = error.localizedMessage ?: "Failed to ping MongoDB cluster"
                    )
                }
        }
    }

    fun clearPingTest() {
        _uiState.value = _uiState.value.copy(pingTestResult = null, pingTestError = null)
    }

    fun connect(uri: String, name: String?, save: Boolean, colorTag: String = "emerald") {
        val cleanUri = uri.trim()
        if (cleanUri.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "MongoDB URI cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
            val result = DirectMongoService.connect(cleanUri)
            result.onSuccess { info ->
                val clusterName = name?.ifBlank { null } ?: "MongoDB Cluster"
                if (save) {
                    vault.saveConnection(clusterName, cleanUri, colorTag)
                    loadSavedConnections()
                }

                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isConnectedToCluster = true,
                    activeClusterName = clusterName,
                    activeClusterVersion = info.serverVersion,
                    activeClusterPingMs = info.pingMs,
                    statusMessage = "Connected to $clusterName (${info.pingMs}ms)"
                )
                loadOverview()
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
            val decryptedUri = vault.getDecryptedUri(saved.id)
            if (decryptedUri == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Could not decrypt connection credentials from vault")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
            val result = DirectMongoService.connect(decryptedUri)
            result.onSuccess { info ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isConnectedToCluster = true,
                    activeClusterName = saved.name,
                    activeClusterVersion = info.serverVersion,
                    activeClusterPingMs = info.pingMs,
                    statusMessage = "Connected to ${saved.name} (${info.pingMs}ms)"
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

    fun updateSavedConnection(id: String, newName: String, newUri: String?, colorTag: String = "emerald") {
        val success = vault.updateConnection(id, newName, newUri, colorTag)
        if (success) {
            loadSavedConnections()
            _uiState.value = _uiState.value.copy(statusMessage = "Connection updated in encrypted vault")
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Failed to update connection")
        }
    }

    fun deleteSaved(id: String) {
        vault.deleteConnection(id)
        loadSavedConnections()
        _uiState.value = _uiState.value.copy(statusMessage = "Connection removed from encrypted vault")
    }

    fun disconnect() {
        viewModelScope.launch {
            DirectMongoService.disconnect()
            _uiState.value = _uiState.value.copy(
                isConnectedToCluster = false,
                activeClusterName = null,
                activeClusterVersion = null,
                activeClusterPingMs = null,
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
            DirectMongoService.getOverview().onSuccess { ov ->
                _uiState.value = _uiState.value.copy(
                    overview = ov,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Overview error: ${err.message}"
                )
            }
        }
    }

    fun selectDatabase(dbName: String) {
        _uiState.value = _uiState.value.copy(selectedDatabase = dbName, collections = emptyList())
        loadCollections(dbName)
    }

    fun loadCollections(dbName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.getCollections(dbName).onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    collections = list,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Collections error: ${err.message}"
                )
            }
        }
    }

    fun createCollection(dbName: String, colName: String) {
        viewModelScope.launch {
            DirectMongoService.createCollection(dbName, colName).onSuccess {
                _uiState.value = _uiState.value.copy(statusMessage = "Created collection '$colName'")
                loadCollections(dbName)
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot create collection: ${err.message}")
            }
        }
    }

    fun dropCollection(dbName: String, colName: String) {
        viewModelScope.launch {
            DirectMongoService.dropCollection(dbName, colName).onSuccess {
                _uiState.value = _uiState.value.copy(statusMessage = "Dropped collection '$colName'")
                loadCollections(dbName)
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot drop collection: ${err.message}")
            }
        }
    }

    fun dropDatabase(dbName: String) {
        viewModelScope.launch {
            DirectMongoService.dropDatabase(dbName).onSuccess {
                _uiState.value = _uiState.value.copy(statusMessage = "Dropped database '$dbName'")
                loadOverview()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot drop database: ${err.message}")
            }
        }
    }

    fun selectCollection(dbName: String, colName: String) {
        _uiState.value = _uiState.value.copy(
            selectedDatabase = dbName,
            selectedCollection = colName,
            currentPage = 1,
            filterJson = "",
            sortJson = "",
            projectionJson = ""
        )
        runQuery(page = 1)
    }

    fun updateQueryParams(filter: String, sort: String, projection: String) {
        _uiState.value = _uiState.value.copy(
            filterJson = filter,
            sortJson = sort,
            projectionJson = projection,
            currentPage = 1
        )
        runQuery(page = 1)
    }

    fun runQuery(page: Int = _uiState.value.currentPage) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPage = page)
            DirectMongoService.queryDocuments(
                dbName = db,
                colName = col,
                filterJson = _uiState.value.filterJson.ifBlank { null },
                sortJson = _uiState.value.sortJson.ifBlank { null },
                projectionJson = _uiState.value.projectionJson.ifBlank { null },
                page = page,
                limit = _uiState.value.pageSize
            ).onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    queryResult = res,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Query error: ${err.message}"
                )
            }
        }
    }

    fun insertDocument(docJson: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.createDocument(db, col, docJson).onSuccess { id ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Inserted document: $id"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Insert error: ${err.message}"
                )
            }
        }
    }

    fun updateDocument(docId: String, docJson: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.updateDocument(db, col, docId, docJson).onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Updated document: $count modified"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Update error: ${err.message}"
                )
            }
        }
    }

    fun deleteDocument(docId: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.deleteDocument(db, col, docId).onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Deleted document ($count)"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Delete error: ${err.message}"
                )
            }
        }
    }

    fun loadIndexes(dbName: String, colName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.getIndexes(dbName, colName).onSuccess { idxs ->
                _uiState.value = _uiState.value.copy(
                    indexes = idxs,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Indexes error: ${err.message}"
                )
            }
        }
    }

    fun createIndex(dbName: String, colName: String, keysJson: String, isUnique: Boolean) {
        viewModelScope.launch {
            DirectMongoService.createIndex(dbName, colName, keysJson, isUnique).onSuccess { name ->
                _uiState.value = _uiState.value.copy(statusMessage = "Created index '$name'")
                loadIndexes(dbName, colName)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot create index: ${err.message}")
            }
        }
    }

    fun dropIndex(dbName: String, colName: String, indexName: String) {
        viewModelScope.launch {
            DirectMongoService.dropIndex(dbName, colName, indexName).onSuccess {
                _uiState.value = _uiState.value.copy(statusMessage = "Dropped index '$indexName'")
                loadIndexes(dbName, colName)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot drop index: ${err.message}")
            }
        }
    }

    fun runAggregate(dbName: String, colName: String, pipelineJson: String) = runAggregation(dbName, colName, pipelineJson)

    fun runAggregation(dbName: String, colName: String, pipelineJson: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.aggregate(dbName, colName, pipelineJson).onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    aggregateResult = res,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Aggregation error: ${err.message}"
                )
            }
        }
    }

    fun executeRawCommand(dbName: String, commandJson: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.executeRawCommand(dbName, commandJson).onSuccess { map ->
                _uiState.value = _uiState.value.copy(
                    rawCommandResult = map,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Command error: ${err.message}"
                )
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
