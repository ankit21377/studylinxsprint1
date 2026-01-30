// File: com/example/studylinx/viewmodel/NotificationViewModel.kt
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.NotificationItem
import com.example.studylinx.repo.NotificationRepo
import com.example.studylinx.repo.NotificationRepoImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val repo: NotificationRepo = NotificationRepoImpl()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var observeJob: Job? = null
    private var currentUserId: String = ""

    fun startForAllUsers() {
        currentUserId = ""
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeGlobal()
                .onStart { _loading.value = true; _error.value = null }
                .catch { e -> _loading.value = false; _error.value = e.message }
                .collect { list -> _notifications.value = list; _loading.value = false }
        }
    }

    fun startForUser(userId: String) {
        currentUserId = userId
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeForUser(userId)
                .onStart { _loading.value = true; _error.value = null }
                .catch { e -> _loading.value = false; _error.value = e.message }
                .collect { list -> _notifications.value = list; _loading.value = false }
        }
    }

    fun markAsRead(notificationId: String) {
        val uid = currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch { runCatching { repo.markAsRead(uid, notificationId) } }
    }

    fun delete(notificationId: String) {
        val uid = currentUserId
        if (uid.isBlank()) return
        viewModelScope.launch { runCatching { repo.delete(uid, notificationId) } }
    }

    fun createNotification(
        targetUserId: String,
        userName: String,
        title: String,
        message: String
    ) {
        viewModelScope.launch {
            runCatching {
                repo.createNotification(targetUserId, userName, title, message)
            }.onFailure { _error.value = it.message ?: "Failed to send notification" }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
