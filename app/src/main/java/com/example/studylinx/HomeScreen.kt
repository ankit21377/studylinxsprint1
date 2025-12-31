package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
import com.example.studylinx.ui.theme.StudyLinXTheme

/* -------------------- DATA MODELS -------------------- */

data class Consultancy(val name: String, val rating: Float, val country: String)
data class JourneyItem(val title: String, val status: String, val color: Color)
data class UpcomingEvent(val title: String, val subtitle: String)

data class NavItem(val label: String, val icon: ImageVector)

/* -------------------- ACTIVITY -------------------- */

class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyLinXTheme {
                HomeScreen()
            }
        }
    }
}

/* -------------------- MAIN SCREEN -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Messages", Icons.Filled.Email),
        NavItem("Favorites", Icons.Filled.FavoriteBorder),
        NavItem("Profile", Icons.Filled.Person)
    )

    Scaffold(
        topBar = { HomeTopBar() },
    ) { padding ->
        if (selectedTab == 0) {
            HomeContent(modifier = Modifier.padding(padding))
        } else {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(navItems[selectedTab].label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* -------------------- TOP BAR -------------------- */

@Composable
fun HomeTopBar() {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF67A1E4))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "StudyLinX",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search consultancies") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

/* -------------------- HOME CONTENT (LazyColumn) -------------------- */

@Composable
fun HomeContent(modifier: Modifier = Modifier) {

    val consultancies = listOf(
        Consultancy("Global Pathways", 4.5f, "Canada"),
        Consultancy("Himalayan Abroad", 4.8f, "Australia"),
        Consultancy("Maple Education", 4.2f, "USA")
    )

    val journey = listOf(
        JourneyItem("Profile Completed", "Done", Color(0xFF4CAF50)),
        JourneyItem("Offer Received", "Pending", Color(0xFFFF9800)),
        JourneyItem("Visa Interview", "Scheduled", Color(0xFF2196F3))
    )

    val events = listOf(
        UpcomingEvent("IELTS Exam", "Oct 26"),
        UpcomingEvent("Visa Interview", "Dec 1")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F8FF)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            Text("Namaste 👋", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ActionChip("Search", Icons.Filled.Search)
                ActionChip("Visa", Icons.Filled.Info)
//                ActionChip("Docs", Icons.Filled.Folder)
                ActionChip("Chat", Icons.Filled.Email)
            }
        }

        item {
            SectionTitle("Featured Consultancies")
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(consultancies) {
                    ConsultancyCard(it)
                }
            }
        }

        item {
            SectionTitle("My Journey")
        }

        item {
            LinearProgressIndicator(progress = { 0.7f }, modifier = Modifier.fillMaxWidth())
        }

        items(journey) {
            JourneyRow(it)
        }

        item {
            SectionTitle("Upcoming Events")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                events.forEach {
                    EventCard(it, Modifier.weight(1f))
                }
            }
        }
    }
}

/* -------------------- COMPONENTS -------------------- */

@Composable
fun ActionChip(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(shape = CircleShape) {
            Icon(icon, label, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun ConsultancyCard(c: Consultancy) {
    Card(modifier = Modifier.width(220.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(c.name, fontWeight = FontWeight.Bold)
            Text("⭐ ${c.rating}")
            Text(c.country, fontSize = 12.sp)
        }
    }
}

@Composable
fun JourneyRow(item: JourneyItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, null, tint = item.color)
        Spacer(Modifier.width(8.dp))
        Text(item.title, modifier = Modifier.weight(1f))
        Text(item.status, color = item.color, fontSize = 12.sp)
    }
}

@Composable
fun EventCard(event: UpcomingEvent, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(event.title, fontWeight = FontWeight.Bold)
            Text(event.subtitle, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = {}) {
            Text("View All")
        }
    }
}

/* -------------------- PREVIEW -------------------- */

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StudyLinXTheme {
        HomeScreen()
    }
}
