package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.viewmodel.NotificationViewModel

class AdminNotificationsActivity : ComponentActivity() {

    private val vm: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AdminNotificationUploadScreen(vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationUploadScreen(vm: NotificationViewModel) {

    val bg = Brush.verticalGradient(listOf(Color(0xFF7EC7F5), Color(0xFFEAF4FF)))

    var targetUserId by remember { mutableStateOf("ALL") }
    var adminName by remember { mutableStateOf("Admin") }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var showSent by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Notifications", fontWeight = FontWeight.Bold) }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    OutlinedTextField(
                        value = targetUserId,
                        onValueChange = { targetUserId = it },
                        label = { Text("Target User ID (ALL or UID)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Admin Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            vm.createNotification(
                                targetUserId = targetUserId.trim(),
                                userName = adminName.trim().ifBlank { "Admin" },
                                title = title.trim(),
                                message = message.trim()
                            )
                            showSent = true
                            title = ""
                            message = ""
                        },
                        enabled = title.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Notification")
                    }

                    if (error != null) {
                        Text(error ?: "", color = Color.Red)
                    }
                }
            }

            if (showSent) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        TextButton(onClick = { showSent = false }) { Text("OK") }
                    }
                ) {
                    Text("Notification sent successfully ✅")
                }
            }
        }
    }
}