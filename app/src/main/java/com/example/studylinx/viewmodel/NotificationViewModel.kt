
package com.example.studylinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.model.NotificationItem
import com.example.studylinx.repo.NotificationRepo
import com.example.studylinx.repo.NotificationRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationDetailUi(
    val title: String = "",
    val details: String = "",
    val timeAgo: String = ""
)

class NotificationViewModel : ViewModel() {

    private val repo: NotificationRepo = NotificationRepoImpl()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ✅ popup dialog state
    private val _selectedDetail = MutableStateFlow<NotificationDetailUi?>(null)
    val selectedDetail: StateFlow<NotificationDetailUi?> = _selectedDetail.asStateFlow()

    private var observeJob: Job? = null

    fun startObservingForCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        startObserving(uid)
    }

    fun startObserving(userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repo.observeForUserAndGlobal(userId)
                .onStart {
                    _loading.value = true
                    _error.value = null
                }
                .catch { e ->
                    _loading.value = false
                    _error.value = e.message ?: "Failed to load notifications"
                }
                .collect { list ->
                    // ✅ compute timeAgo for UI
                    val mapped = list.map { it.copy(timeAgo = timeAgo(it.createdAt)) }
                    _notifications.value = mapped
                    _loading.value = false
                }
        }
    }

    fun closePopup() {
        _selectedDetail.value = null
    }

    fun onNotificationClick(item: NotificationItem) {
        // ✅ mark read + show popup with full details
        viewModelScope.launch {
            runCatching { repo.markAsRead(item.id) }

            val details = loadDetails(item.id)
            _selectedDetail.value = NotificationDetailUi(
                title = item.action,
                details = details.ifBlank { "No additional details." },
                timeAgo = item.timeAgo
            )
        }
    }

    private suspend fun loadDetails(notificationId: String): String {
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("notifications").document(notificationId).get().await()
        return doc.getString("details") ?: ""
    }

    private fun timeAgo(createdAt: Long): String {
        if (createdAt <= 0) return ""
        val diff = System.currentTimeMillis() - createdAt
        val sec = diff / 1000
        val min = sec / 60
        val hr = min / 60
        val day = hr / 24

        return when {
            sec < 60 -> "Just now"
            min < 60 -> "${min}m ago"
            hr < 24 -> "${hr}h ago"
            else -> "${day}d ago"
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}