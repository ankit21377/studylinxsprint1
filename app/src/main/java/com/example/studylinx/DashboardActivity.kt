package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth

data class NavItem(val icon: Int, val label: String)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DashboardBody() }
    }
}

@Composable
fun DashboardBody() {

    val listItems = listOf(
        NavItem(icon = R.drawable.baseline_home_24, label = "Home"),
        NavItem(icon = R.drawable.baseline_search_24, label = "Search"),
        NavItem(icon = R.drawable.baseline_notifications_24, label = "Notification"),
        NavItem(icon = R.drawable.baseline_person_24, label = "Profile"),
    )

    var selectedIndex by remember { mutableStateOf(0) }

    // ✅ logged-in UID (for NotificationScreen user filter)
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }

    Scaffold(
        modifier = Modifier.testTag("dashboard"),
        bottomBar = {
            NavigationBar {
                listItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen()
                1 -> SearchScreen()
                2 -> NotificationScreen(userId = uid) // ✅ IMPORTANT FIX
                3 -> ProfileScreen()
                else -> HomeScreen()
            }
        }
    }
}
