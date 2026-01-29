// File: com/example/studylinx/admin/AdminNotificationsActivity.kt
package com.example.studylinx.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AdminNotificationsActivity : ComponentActivity() {

    private val vm: AdminNotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AdminNotificationsScreen(vm = vm, onBack = { finish() })
            }
        }
    }
}

// ---------- App palette ----------
private val BgTop = Color(0xFF7EC7F5)
private val BgBottom = Color(0xFFEAF4FF)
private val PrimaryBlue = Color(0xFF2D7EF7)
private val CardBg = Color(0xFFF7FAFF)
private val TextDark = Color(0xFF0E2A47)
private val Muted = Color(0xFF6A7786)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminNotificationsScreen(
    vm: AdminNotificationViewModel,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()

    val bg = Brush.verticalGradient(listOf(BgTop, BgBottom))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Notifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // Header card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Send Notification", fontWeight = FontWeight.Bold, color = TextDark)
                            Text(
                                "Send to ALL users or a specific userId (UID).",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Form card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedTextField(
                            value = state.targetUserId,
                            onValueChange = vm::setTargetUserId,
                            label = { Text("Target UserId (ALL or UID)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.adminName,
                            onValueChange = vm::setAdminName,
                            label = { Text("Admin Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.title,
                            onValueChange = vm::setTitle,
                            label = { Text("Title / Short message") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.details,
                            onValueChange = vm::setDetails,
                            label = { Text("Details (shown in popup)") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        OutlinedTextField(
                            value = state.likeCount,
                            onValueChange = vm::setLikeCount,
                            label = { Text("Like count (optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Message
                state.message?.let { m ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = m,
                            modifier = Modifier.padding(12.dp),
                            color = if (m.startsWith("✅")) Color(0xFF1B7F3A) else Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Send button
                Button(
                    onClick = { vm.send() },
                    enabled = !state.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(if (state.loading) "Sending..." else "Send Notification", color = Color.White)
                }
            }
        }
    }
}