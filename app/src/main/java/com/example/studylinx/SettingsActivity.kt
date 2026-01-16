
package com.example.studylinx

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.ui.theme.StudyLinXTheme
import com.example.studylinx.viewmodel.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val ui by vm.ui.collectAsState()

            StudyLinXTheme(darkTheme = ui.darkMode) {
                SettingsScreen(
                    uiDark = ui.darkMode,
                    saving = ui.saving,
                    message = ui.message,
                    error = ui.error,
                    onBack = { finish() },
                    onToggleDark = vm::setDarkMode,
                    onChangePassword = { oldP, newP ->
                        vm.changePassword(oldP, newP)
                    },
                    onClearBanner = vm::clearBanner,
                    toast = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiDark: Boolean,
    saving: Boolean,
    message: String?,
    error: String?,
    onBack: () -> Unit,
    onToggleDark: (Boolean) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onClearBanner: () -> Unit,
    toast: (String) -> Unit
) {
    val bg = Brush.verticalGradient(listOf(Color(0xFFEAF2FF), Color(0xFFF6FAFF)))

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showChange by remember { mutableStateOf(false) }

    LaunchedEffect(message, error) {
        when {
            !message.isNullOrBlank() -> {
                toast(message)
                onClearBanner()
            }
            !error.isNullOrBlank() -> {
                toast(error)
                onClearBanner()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Dark mode card
            Card(shape = RoundedCornerShape(18.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.DarkMode, contentDescription = null)
                        Column {
                            Text("Dark Mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (uiDark) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Switch(
                        checked = uiDark,
                        onCheckedChange = { onToggleDark(it) }
                    )
                }
            }

            // Change password card
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Column {
                            Text("Change Password", fontWeight = FontWeight.SemiBold)
                            Text("Secure update with re-authentication", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedButton(
                        onClick = { showChange = !showChange },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showChange) "Hide" else "Open")
                    }

                    if (showChange) {
                        OutlinedTextField(
                            value = currentPass,
                            onValueChange = { currentPass = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { onChangePassword(currentPass.trim(), newPass.trim()) },
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (saving) "Updating..." else "Update Password")
                        }
                    }
                }
            }
        }
    }
}