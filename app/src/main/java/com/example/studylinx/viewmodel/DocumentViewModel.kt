package com.example.studylinx.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.UserDocument
import com.example.studylinx.repo.DocumentRepo
import com.example.studylinx.repo.DocumentRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val repo: DocumentRepo = DocumentRepoImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // docType -> percent (0..100)
    private val _progress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val progress: StateFlow<Map<String, Int>> = _progress.asStateFlow()

    // docType -> saved metadata
    private val _uploadedDocs = MutableStateFlow<Map<String, UserDocument>>(emptyMap())
    val uploadedDocs: StateFlow<Map<String, UserDocument>> = _uploadedDocs.asStateFlow()

    private var observeJob: Job? = null

    fun startObservingUserDocs() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeUserDocuments(userId)
                .catch { e -> _error.value = e.message ?: "Failed to load documents" }
                .collect { map -> _uploadedDocs.value = map }
        }
    }

    fun upload(docType: String, uri: Uri, fileName: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _error.value = null
            _loading.value = true
            _progress.value = _progress.value + (docType to 0)

            runCatching {
                repo.uploadDocument(
                    userId = userId,
                    docType = docType,
                    fileUri = uri,
                    fileName = fileName,
                    onProgress = { p -> _progress.value = _progress.value + (docType to p) }
                )
            }.onFailure {
                _error.value = it.message ?: "Upload failed"
            }

            _loading.value = false
            _progress.value = _progress.value + (docType to 100)
        }
    }
}
