package com.example.studylinx.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studylinx.model.UserModel
import com.example.studylinx.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    viewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogoutDone: () -> Unit,
    showToast: (String) -> Unit
) {
    val bg = Color(0xFFF7F1E7)

    var loading by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf(UserModel()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        loading = true
        viewModel.loadUser(uid) { ok, msg, model ->
            loading = false
            if (ok && model != null) {
                user = model
                error = null
            } else {
                error = msg
            }
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header (Avatar + Name + Email)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayName = (user.fullName.ifBlank { "User" })
                val displayEmail = (user.email.ifBlank { viewModel.getUid() ?: "" }) // fallback

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2937)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(displayEmail, fontSize = 13.sp, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            error?.let {
                Text(it, color = Color.Red)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuCard("Edit Profile", Icons.Filled.Edit, onEditProfile)
                MenuCard("Settings", Icons.Filled.Settings, onSettings)

                MenuCard("Logout", Icons.Filled.Logout) {
                    viewModel.logout { ok, msg ->
                        showToast(msg)
                        if (ok) onLogoutDone()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF))
        }
    }
}