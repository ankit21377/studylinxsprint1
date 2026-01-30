
package com.example.studylinx.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylinx.repo.NotificationRepo
import com.example.studylinx.repo.NotificationRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminNotificationUiState(
    val targetUserId: String = "ALL",
    val adminName: String = "Admin",
    val title: String = "",
    val details: String = "",
    val likeCount: String = "0",
    val loading: Boolean = false,
    val message: String? = null
)

class AdminNotificationViewModel : ViewModel() {

    private val repo: NotificationRepo = NotificationRepoImpl()

    private val _ui = MutableStateFlow(AdminNotificationUiState())
    val ui: StateFlow<AdminNotificationUiState> = _ui.asStateFlow()

    fun setTargetUserId(v: String) = _ui.value.let { _ui.value = it.copy(targetUserId = v, message = null) }
    fun setAdminName(v: String) = _ui.value.let { _ui.value = it.copy(adminName = v, message = null) }
    fun setTitle(v: String) = _ui.value.let { _ui.value = it.copy(title = v, message = null) }
    fun setDetails(v: String) = _ui.value.let { _ui.value = it.copy(details = v, message = null) }
    fun setLikeCount(v: String) = _ui.value.let {
        _ui.value = it.copy(likeCount = v.filter { ch -> ch.isDigit() }.ifBlank { "0" }, message = null)
    }

    fun clearMessage() {
        _ui.value = _ui.value.copy(message = null)
    }

    fun send() {
        val s = _ui.value
        if (s.title.trim().isBlank()) {
            _ui.value = s.copy(message = "Please enter notification title/message.")
            return
        }
        if (s.details.trim().isBlank()) {
            _ui.value = s.copy(message = "Please enter notification details.")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, message = null)

            runCatching {
                repo.createNotification(
                    targetUserId = s.targetUserId.trim().ifBlank { "ALL" },
                    userName = s.adminName.trim().ifBlank { "Admin" },
                    action = s.title.trim(),
                    details = s.details.trim(),
                    likeCount = s.likeCount.toIntOrNull() ?: 0
                )
            }.onSuccess {
                _ui.value = _ui.value.copy(
                    loading = false,
                    message = "✅ Notification sent!",
                    title = "",
                    details = "",
                    likeCount = "0"
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    message = "❌ Failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}