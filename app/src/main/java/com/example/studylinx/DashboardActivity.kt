package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

data class NavItem(val icon: Int, val label: String)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val homeVm: HomeViewModel = viewModel()
            val searchVm: SearchViewModel = viewModel()
            val notifVm: NotificationViewModel = viewModel()
            val profileVm: ProfileViewModel = viewModel()

            val uid = FirebaseAuth.getInstance().currentUser?.uid

            DashboardBody(
                homeVm = homeVm,
                searchVm = searchVm,
                notifVm = notifVm,
                profileVm = profileVm,
                userId = uid
            )
        }
    }
}

@Composable
fun DashboardBody(
    homeVm: HomeViewModel,
    searchVm: SearchViewModel,
    notifVm: NotificationViewModel,
    profileVm: ProfileViewModel,
    userId: String?
) {
    val listItems = listOf(
        NavItem(icon = R.drawable.baseline_home_24, label = "Home"),
        NavItem(icon = R.drawable.baseline_search_24, label = "Search"),
        NavItem(icon = R.drawable.baseline_notifications_24, label = "Notification"),
        NavItem(icon = R.drawable.baseline_person_24, label = "Profile"),
    )

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.testTag("dashboard"),
        bottomBar = {
            NavigationBar {
                listItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(item.icon), contentDescription = item.label) },
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
                0 -> HomeScreen(vm = homeVm)
                1 -> SearchScreen(vm = searchVm)
                2 -> NotificationScreen(vm = notifVm, userId = userId) // ✅ per-user notifications
                3 -> ProfileScreen(vm = profileVm)
                else -> HomeScreen(vm = homeVm)
            }
        }
    }
}
