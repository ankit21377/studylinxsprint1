package com.example.studylinx.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(viewModel: HomeViewModel = HomeViewModel()) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F7FF)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { UpcomingAppointmentCard(viewModel) }

        item { QuickActionsSection() }

        item { IELTSBookingCard() }

        item { EventsSection(viewModel) }

        item { ApplicationStatusSection(viewModel) }

        item { ContactCounselorSection() }
    }
}

@Composable
fun UpcomingAppointmentCard(vm: HomeViewModel) {
    Card(
        colors = CardDefaults.cardColors(Color(0xFF4BB3C3)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Upcoming Appointment", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(vm.appointment.dateTime, color = Color.White, fontWeight = FontWeight.Bold)
            Text("with ${vm.appointment.counselorName}", color = Color.White)

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
