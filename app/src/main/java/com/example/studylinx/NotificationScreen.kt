package com.example.studylinx

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.model.NotificationItem
import com.example.studylinx.viewmodel.NotificationViewModel

private enum class NotificationTab { ALL, UNREAD, READ }

@Composable
fun NotificationScreen(
    vm: NotificationViewModel = viewmodel(),

    loadForCurrentUser: Boolean = false
) {

    val allNotifications by vm.notifications.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()


    LaunchedEffect(loadForCurrentUser) {

        vm.startObservingGlobal()
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

    val screenBg = Color(0xFFEEF5FF)
    val primaryBlue = Color(0xFF4E86D9)
    val primaryBlue2 = Color(0xFF6EA4EA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryBlue, primaryBlue2)
                    )
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Notifications",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
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

        // --- Content states ---
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
                    items(shown) { item ->
                        NotificationCard(
                            item = item,
                            primaryBlue = primaryBlue
                        )
                    }

                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }
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

        // small divider like image
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
    val underlineColor = selectedBlue

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
            colors = ButtonDefaults.textButtonColors(contentColor = if (selected) selectedBlue else unselectedText)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(42.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (selected) underlineColor else Color.Transparent)
                )
            }
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
            colors = ButtonDefaults.textButtonColors(contentColor = if (selected) selectedBlue else unselectedText)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Unread",
                    fontSize = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.width(10.dp))
                // Badge like image (blue circle with number)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(selectedBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.coerceAtLeast(0).toString(),
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
    primaryBlue: Color
) {
    // Card like image: big rounded, light shadow, bell icon left, text, optional blue dot at right for unread
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bell icon in soft blue circle-ish feel
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

            Text(
                text = buildString {
                    // If your DB stores only "action" as full sentence, this still works
                    val msg = if (item.userName.isNotBlank() && item.action.isNotBlank())
                        "${item.userName} ${item.action}"
                    else item.action.ifBlank { "Notification" }
                    append(msg)
                    // In your screenshot, only one line message. Keep it clean.
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1B2430),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )


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