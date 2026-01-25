
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- DocumentActivity palette ----------
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
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1) Upcoming Appointment
            item {
                UpcomingAppointmentCard(
                    dateTimeMillis = state.upcomingAppointmentMillis,
                    counselorName = state.upcomingAppointmentCounselor,
                    statusText = state.upcomingAppointmentStatus,
                    onMoreDetails = {
                        // ✅ open your AppointmentActivity
                        ctx.startActivity(Intent(ctx, AppointmentActivity::class.java))
                    }
                )
            }

            // 2) Quick Actions
            item {
                QuickActionsSection(
                    onBookDate = { ctx.startActivity(Intent(ctx, AppointmentActivity::class.java)) },
                    onExplore = { ctx.startActivity(Intent(ctx, UniversityActivity::class.java)) },
                    onUpload = { ctx.startActivity(Intent(ctx, DocumentActivity::class.java)) }
                )
            }

            // 3) Countries LazyRow
            item {
                CountriesSection(
                    loading = state.loadingCountries,
                    countries = state.countries,
                    error = state.errorCountries,
                    onCountryClick = { country ->
                        // ✅ if you want: show filtered universities by country
                        // Option A: open UniversityActivity (and filter using intent extra)
                        val i = Intent(ctx, UniversityActivity::class.java)
                        i.putExtra(UniversityActivity.EXTRA_COUNTRY_ID, country.name) // or country.id if you use id
                        ctx.startActivity(i)
                    }
                )
            }

            // 4) Universities LazyColumn preview (top 4)
            item {
                UniversitiesSection(
                    loading = state.loadingUniversities,
                    universities = state.universities,
                    error = state.errorUniversities,
                    onViewAll = {
                        ctx.startActivity(Intent(ctx, UniversityActivity::class.java))
                    },
                    onUniversityClick = {
                        // For now open list screen; later you can make details screen.
                        ctx.startActivity(Intent(ctx, UniversityActivity::class.java))
                    }
                )
            }
        }
    }
}

/* ---------------- Upcoming Appointment Card (Attractive + More details) ---------------- */

