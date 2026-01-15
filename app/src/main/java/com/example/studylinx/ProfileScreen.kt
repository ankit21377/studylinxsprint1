package com.example.studylinx

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.studylinx.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.loadMe() }

    val bg = Brush.verticalGradient(
        colors = listOf(Color(0xFFBFD7FF), Color(0xFFEAF2FF), Color(0xFFF6FAFF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp)
    ) {

        Spacer(Modifier.height(26.dp))

        // Avatar + name + email (like screenshot)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val img = state.user.profileImageUrl

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (img.isNotBlank()) {
                    AsyncImage(
                        model = img,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // fallback initials
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F2937)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state.user.fullName().ifBlank { "U" }).take(1).uppercase(),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = state.user.fullName().ifBlank { "User" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2430),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = state.user.email.ifBlank { "No email" },
                fontSize = 14.sp,
                color = Color(0xFF5C6B80),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (state.loading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color.Red)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Menu list like screenshot
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            ProfileMenuCard(
                title = "Documents",
                icon = Icons.Default.Description
            ) {
                context.startActivity(Intent(context, DocumentActivity::class.java))
            }

            ProfileMenuCard(
                title = "Book Appointment",
                icon = Icons.Default.CalendarMonth
            ) {
                // You can point to your real appointment screen/activity
                context.startActivity(Intent(context, AppointmentActivity::class.java))
            }

            ProfileMenuCard(
                title = "Edit Profile",
                icon = Icons.Default.Edit
            ) {
//                context.startActivity(Intent(context, EditProfileActivity::class.java))
            }

            ProfileMenuCard(
                title = "Settings",
                icon = Icons.Default.Settings
            ) {
//                context.startActivity(Intent(context, SettingsActivity::class.java))
            }

            ProfileMenuCard(
                title = "Log Out",
                icon = Icons.Default.Logout
            ) {
                vm.logout { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) {
                        val i = Intent(context, LoginActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(i)
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun ProfileMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEAF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color(0xFF2D7EF7))
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B2430),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF8EA2BF)
            )
        }
    }
}
