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

    fun startObservingGlobal() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeGlobalNotifications()
                .onStart {
                    _error.value = null
                    _loading.value = true
                }
                .catch { e ->
                    _loading.value = false
                    _error.value = e.message ?: "Failed to load notifications"
                }
                .collect { list ->
                    _notifications.value = list
                    _loading.value = false
                }
        }
    }

    fun startObservingForUser(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeNotificationsForUser(userId)
                .onStart {
                    _error.value = null
                    _loading.value = true
                }
                .catch { e ->
                    _loading.value = false
                    _error.value = e.message ?: "Failed to load notifications"
                }
                .collect { list ->
                    _notifications.value = list
                    _loading.value = false
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            runCatching { repo.markAsRead(notificationId) }
                .onFailure { _error.value = it.message ?: "Failed to mark as read" }
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            runCatching { repo.markAllAsReadForUser(userId) }
                .onFailure { _error.value = it.message ?: "Failed to mark all as read" }
        }
    }

    fun delete(notificationId: String) {
        viewModelScope.launch {
            runCatching { repo.deleteNotification(notificationId) }
                .onFailure { _error.value = it.message ?: "Failed to delete notification" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
