package com.example.studylinx

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Simple data models to keep the file self-contained
data class AppointmentInfo(val dateTime: String, val counselorName: String)
data class EventInfo(val title: String, val date: String, val description: String)
data class ApplicationInfo(val currentStep: Int)

@Composable
fun HomeScreen() {
    // Dummy data for the UI
    val appointment = AppointmentInfo("May 15, 2024 - 10:00 AM", "Dr. Sarah")
    val events = listOf(
        EventInfo("Study Abroad Seminar", "June 1", "Explore top universities in Europe."),
        EventInfo("IELTS Workshop", "June 5", "Tips and tricks for the speaking section.")
    )
    val appStatus = ApplicationInfo(currentStep = 2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F7FF)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { UpcomingAppointmentCard(appointment) }
        item { QuickActionsSection() }
        item { IELTSBookingCard() }
        item { EventsSection(events) }
        item { ApplicationStatusSection(appStatus) }
        item { ContactCounselorSection() }
    }
}

@Composable
fun UpcomingAppointmentCard(appointment: AppointmentInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4BB3C3)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Upcoming Appointment", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(appointment.dateTime, color = Color.White, fontWeight = FontWeight.Bold)
            Text("with ${appointment.counselorName}", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("View Details")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Reschedule")
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem("Book Date", Icons.Default.DateRange)
        QuickActionItem("IELTS", Icons.Default.Info) // Replaced School
        QuickActionItem("Explore", Icons.Default.LocationOn) // Replaced Public
        QuickActionItem("Upload", Icons.Default.Add) // Replaced Upload
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Icon(icon, contentDescription = title, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 12.sp)
    }
}

@Composable
fun IELTSBookingCard() {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("IELTS Booking", fontWeight = FontWeight.Bold)
            Text("Exam Type: Academic")
            Text("Next Test Date: May 10, 2024")
            Text("Status: Slots Available", color = Color(0xFF2E7D32))
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Book Now")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Contact Us")
                }
            }
        }
    }
}

@Composable
fun EventsSection(events: List<EventInfo>) {
    Column {
        Text("Events & Seminars", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        events.forEach { event ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(event.title, fontWeight = FontWeight.Bold)
                    Text(event.date)
                    Text(event.description, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ApplicationStatusSection(status: ApplicationInfo) {
    Column {
        Text("Application Status", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (status.currentStep + 1) / 4f },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ContactCounselorSection() {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {}) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Call")
            }
            Button(onClick = {}) {
                Icon(Icons.Default.Email, contentDescription = null) // Replaced Chat
                Spacer(Modifier.width(8.dp))
                Text("Email")
            }
        }
    }
}
