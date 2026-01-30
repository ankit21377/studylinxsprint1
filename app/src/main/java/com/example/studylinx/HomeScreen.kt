// File: com/example/studylinx/HomeScreen.kt
package com.example.studylinx

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.studylinx.model.Appointment
import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- palette ----------
private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val PrimaryBlue = Color(0xFF2F79E6)
private val PrimaryBlue2 = Color(0xFF6EA4EA)
private val SoftBlue = Color(0xFFEAF2FF)
private val TextDark = Color(0xFF1C2B3A)
private val TextMuted = Color(0xFF7D8BA0)

@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val state by vm.ui.collectAsState()

    Box(
        modifier = Modifier
            .testTag("home_screen")
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ✅ Upcoming Appointment
            item {
                UpcomingAppointmentCard(
                    appointment = state.upcomingAppointment,
                    onMoreDetails = {
                        ctx.startActivity(Intent(ctx, AppointmentActivity::class.java))
                    }
                )
            }

            // ✅ Quick Actions (Explore removed to avoid errors)
            item {
                QuickActionsSection(
                    onBookDate = { ctx.startActivity(Intent(ctx, AppointmentActivity::class.java)) },
                    onUpload = { ctx.startActivity(Intent(ctx, DocumentActivity::class.java)) }
                )
            }

            // ✅ Countries
            item {
                CountriesSection(
                    loading = state.loading,
                    countries = state.countries,
                    error = state.error,
                    onCountryClick = { country ->
                        val i = Intent(ctx, UniversityActivity::class.java)
                        i.putExtra(UniversityActivity.EXTRA_COUNTRY_ID, country.id) // ✅ use id
                        ctx.startActivity(i)
                    }
                )
            }

            // ✅ Universities preview (click opens details properly)
            item {
                UniversitiesSection(
                    loading = state.loading,
                    universities = state.universities,
                    error = state.error,
                    onViewAll = {
                        ctx.startActivity(Intent(ctx, UniversityActivity::class.java))
                    },
                    onUniversityClick = { uni ->
                        val i = Intent(ctx, UniversityDetailsActivity::class.java)
                        i.putExtra(UniversityActivity.EXTRA_UNI_ID, uni.id)
                        ctx.startActivity(i)
                    }
                )
            }
        }
    }
}

/* ---------------- Upcoming Appointment ---------------- */

@Composable
private fun UpcomingAppointmentCard(
    appointment: Appointment?,
    onMoreDetails: () -> Unit
) {
    val fmt = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val has = appointment != null && appointment.startMillis > 0L

    val timeText = if (has) fmt.format(Date(appointment!!.startMillis)) else "No upcoming appointment"
    val titleText = if (has) appointment!!.title.ifBlank { "Appointment" } else "No Appointment"
    val statusText = if (has) appointment!!.status.ifBlank { "Pending" } else "Pending"

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryBlue2)))
                .padding(16.dp)
        ) {
            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Upcoming Appointment",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(statusText, color = PrimaryBlue, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = titleText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(timeText, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMoreDetails() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "More details",
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryBlue)
                    }
                }
            }
        }
    }
}

/* ---------------- Quick Actions ---------------- */

@Composable
private fun QuickActionsSection(
    onBookDate: () -> Unit,
    onUpload: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem("Book Date", Icons.Default.DateRange, onBookDate)
        QuickActionItem("Upload", Icons.Default.UploadFile, onUpload)
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SoftBlue)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = PrimaryBlue)
        }
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 12.sp, color = TextDark)
    }
}

/* ---------------- Countries ---------------- */

@Composable
private fun CountriesSection(
    loading: Boolean,
    countries: List<Country>,
    error: String?,
    onCountryClick: (Country) -> Unit
) {
    Column {
        Text("Countries", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
        Spacer(Modifier.height(10.dp))

        when {
            loading && countries.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            error != null -> Text(error, color = Color.Red)
            countries.isEmpty() -> Text("No countries found", color = TextMuted)
            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(countries, key = { it.id }) { c ->
                        CountryCard(c) { onCountryClick(c) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryCard(country: Country, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = country.flagUrl,
                    contentDescription = country.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SoftBlue)
                )
                Spacer(Modifier.width(10.dp))
                Text(country.name, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
            Spacer(Modifier.height(8.dp))
            Text("Tap to explore", fontSize = 12.sp, color = TextMuted)
        }
    }
}

/* ---------------- Universities ---------------- */

@Composable
private fun UniversitiesSection(
    loading: Boolean,
    universities: List<University>,
    error: String?,
    onViewAll: () -> Unit,
    onUniversityClick: (University) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Universities",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onViewAll) { Text("View All", color = PrimaryBlue) }
        }

        Spacer(Modifier.height(8.dp))

        when {
            loading && universities.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            error != null -> Text(error, color = Color.Red)
            universities.isEmpty() -> Text("No universities found", color = TextMuted)
            else -> {
                universities.take(4).forEach { uni ->
                    UniversityHomeCard(uni) { onUniversityClick(uni) }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun UniversityHomeCard(uni: University, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = uni.imageUrl,
                contentDescription = uni.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftBlue)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    uni.name,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${uni.city}, ${uni.country}", fontSize = 12.sp, color = TextMuted)

                if (uni.description.isNotBlank()) {
                    Text(
                        uni.description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
