package com.mongostudio.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mongostudio.app.data.model.*
import com.mongostudio.app.data.service.DirectMongoService
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
    private val vault = EncryptedVault(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSavedConnections()
    }

    fun loadSavedConnections() {
        val list = vault.getSavedConnections()
        _uiState.value = _uiState.value.copy(savedConnections = list)
    }

    fun connect(uri: String, name: String?, save: Boolean) {
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
                    vault.saveConnection(clusterName, cleanUri)
                    loadSavedConnections()
                }

                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isConnectedToCluster = true,
                    activeClusterName = clusterName,
                    activeClusterVersion = info.serverVersion,
                    activeClusterPingMs = info.pingMs,
                    statusMessage = "Direct connection established! Ping: ${info.pingMs}ms"
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
            DirectMongoService.getCollections(dbName).onSuccess { cols ->
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
            DirectMongoService.createCollection(dbName, colName).onSuccess {
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
            DirectMongoService.dropCollection(dbName, colName).onSuccess {
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
            DirectMongoService.dropDatabase(dbName).onSuccess {
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
            val res = DirectMongoService.queryDocuments(
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
            DirectMongoService.createDocument(db, col, docJson).onSuccess { insertedId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document inserted: $insertedId"
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
            DirectMongoService.updateDocument(db, col, docId, docJson).onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document updated ($count modified) directly in MongoDB!"
                )
                runQuery()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Direct Push failed"
                )
            }
        }
    }

    fun deleteDocument(docId: String) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.deleteDocument(db, col, docId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Document deleted from collection"
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
            DirectMongoService.aggregate(db, col, pipelineJson).onSuccess { res ->
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
            DirectMongoService.getIndexes(dbName, colName).onSuccess { idxs ->
                _uiState.value = _uiState.value.copy(indexes = idxs)
            }
        }
    }

    fun createIndex(keysJson: String, isUnique: Boolean) {
        val db = _uiState.value.selectedDatabase ?: return
        val col = _uiState.value.selectedCollection ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            DirectMongoService.createIndex(db, col, keysJson, isUnique).onSuccess { name ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Index '$name' created successfully"
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
            DirectMongoService.dropIndex(db, col, indexName).onSuccess {
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
            DirectMongoService.executeRawCommand(dbName, commandJson).onSuccess { res ->
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
