package com.example.studylinx

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.studylinx.model.Country
import com.example.studylinx.repo.CountryRepoImpl
import com.example.studylinx.viewmodel.CountryViewModel

// ✅ DocumentActivity palette
private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val PrimaryBlue = Color(0xFF2F79E6)
private val PrimaryBlue2 = Color(0xFF6EA4EA)
private val SoftBlue = Color(0xFFEAF2FF)
private val TextDark = Color(0xFF1C2B3A)
private val TextMuted = Color(0xFF7D8BA0)

class CountryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: CountryViewModel = viewModel(factory = CountryViewModel.factory(CountryRepoImpl()))

            CountryScreen(
                vm = vm,
                onBack = { finish() },
                onCountryClick = { c ->
                    // ✅ open your existing UniversityActivity but FILTER by countryId
                    val i = Intent(this, UniversityActivity::class.java).apply {
                        putExtra("countryId", c.id)       // e.g. "usa"
                        putExtra("countryName", c.name)   // optional
                    }
                    startActivity(i)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryScreen(
    vm: CountryViewModel,
    onBack: () -> Unit,
    onCountryClick: (Country) -> Unit
) {
    val state by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Countries", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.error != null -> {
                    Text(state.error ?: "Error", color = Color.Red)
                }

                state.countries.isEmpty() -> {
                    Text("No countries found", color = TextMuted)
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.countries, key = { it.id }) { c ->
                            CountryRow(country = c, onClick = { onCountryClick(c) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryRow(country: Country, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftBlue),
                contentAlignment = Alignment.Center
            ) {
                if (country.flagUrl.isNotBlank()) {
                    AsyncImage(
                        model = country.flagUrl,
                        contentDescription = country.name,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = country.name.take(1).uppercase(),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to view universities",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}