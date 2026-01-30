// File: com/example/studylinx/NotificationScreen.kt
package com.example.studylinx

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.model.NotificationItem
import com.example.studylinx.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NotificationTab { ALL, UNREAD, READ }

@Composable
fun NotificationScreen(
    vm: NotificationViewModel = viewModel(),
    userId: String? = null   // pass logged-in UID, if null show global
) {
    val allNotifications by vm.notifications.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    // ✅ start observing
    LaunchedEffect(userId) {
        if (userId.isNullOrBlank()) vm.startForAllUsers()
        else vm.startForUser(userId)
    }

    var tab by remember { mutableStateOf(NotificationTab.ALL) }
    val unreadCount = remember(allNotifications) { allNotifications.count { !it.isRead } }

    val shown = remember(allNotifications, tab) {
        when (tab) {
            NotificationTab.ALL -> allNotifications
            NotificationTab.UNREAD -> allNotifications.filter { !it.isRead }
            NotificationTab.READ -> allNotifications.filter { it.isRead }
        }
    }

    // ✅ popup selected item
    var selected by remember { mutableStateOf<NotificationItem?>(null) }

    val screenBg = Color(0xFFEEF5FF)
    val primaryBlue = Color(0xFF4E86D9)
    val primaryBlue2 = Color(0xFF6EA4EA)

    Column(
        modifier = Modifier
            .testTag("notification_screen")
            .fillMaxSize()
            .background(screenBg)
    ) {

        // header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(Brush.horizontalGradient(listOf(primaryBlue, primaryBlue2)))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Notifications", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        NotificationTabs(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            selected = tab,
            unreadCount = unreadCount,
            onSelect = { tab = it }
        )

        Spacer(Modifier.height(16.dp))

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Error", color = Color.Red)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(shown, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            primaryBlue = primaryBlue,
                            onClick = {
                                selected = item
                                // ✅ mark read when opened
                                if (!item.isRead) vm.markAsRead(item.id)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }

    // ✅ popup dialog (message box)
    if (selected != null) {
        NotificationDetailDialog(
            item = selected!!,
            onDismiss = { selected = null }
        )
    }
}

@Composable
private fun NotificationDetailDialog(item: NotificationItem, onDismiss: () -> Unit) {
    val fmt = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Text(item.title.ifBlank { "Notification" }, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(item.message.ifBlank { "No details available." })
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "From: ${item.userName.ifBlank { "System" }}",
                    fontSize = 12.sp,
                    color = Color(0xFF6A7786)
                )
                Text(
                    text = fmt.format(Date(item.createdAt)),
                    fontSize = 12.sp,
                    color = Color(0xFF6A7786)
                )
            }
        }
    )
}

@Composable
private fun NotificationTabs(
    modifier: Modifier = Modifier,
    selected: NotificationTab,
    unreadCount: Int,
    onSelect: (NotificationTab) -> Unit
) {
    val container = Color(0xFFE6F0FF)
    val pill = Color.White
    val selectedBlue = Color(0xFF4E86D9)
    val unselectedText = Color(0xFF6C7B95)

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabPill(
            text = "All",
            selected = selected == NotificationTab.ALL,
            onClick = { onSelect(NotificationTab.ALL) },
            pillColor = pill,
            selectedBlue = selectedBlue,
            unselectedText = unselectedText,
            modifier = Modifier.weight(1f)
        )

        TabUnread(
            selected = selected == NotificationTab.UNREAD,
            unreadCount = unreadCount,
            onClick = { onSelect(NotificationTab.UNREAD) },
            pillColor = pill,
            selectedBlue = selectedBlue,
            unselectedText = unselectedText,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(22.dp)
                .background(Color(0xFFCAD9F4))
        )

        TabPill(
            text = "Read",
            selected = selected == NotificationTab.READ,
            onClick = { onSelect(NotificationTab.READ) },
            pillColor = pill,
            selectedBlue = selectedBlue,
            unselectedText = unselectedText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    pillColor: Color,
    selectedBlue: Color,
    unselectedText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) pillColor else Color.Transparent)
            .padding(horizontal = 10.dp)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (selected) selectedBlue else unselectedText
            )
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TabUnread(
    selected: Boolean,
    unreadCount: Int,
    onClick: () -> Unit,
    pillColor: Color,
    selectedBlue: Color,
    unselectedText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) pillColor else Color.Transparent)
            .padding(horizontal = 10.dp)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (selected) selectedBlue else unselectedText
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Unread",
                    fontSize = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(selectedBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        unreadCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    primaryBlue: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.ifBlank { "Notification" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B2430),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.message.ifBlank { "" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF445466),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!item.isRead) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(primaryBlue)
                        .border(1.dp, Color(0xFF3E6FBF), CircleShape)
                )
            }
        }
    }
}

