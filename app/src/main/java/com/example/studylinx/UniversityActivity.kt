package com.example.studylinx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.UniversityViewModel

class UniversityActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COUNTRY_ID = "countryId"
        const val EXTRA_UNI_ID = "uniId"
    }

    private val vm: UniversityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val countryId = intent.getStringExtra(EXTRA_COUNTRY_ID) ?: ""
        vm.setCountryFilter(countryId)

        setContent {
            MaterialTheme {
                UniversityScreen(
                    vm = vm,
                    onBack = { finish() },
                    onOpenLocation = { url ->
                        if (url.isNotBlank()) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    onUniversityClick = { uni ->
                        startActivity(
                            Intent(this, UniversityDetailsActivity::class.java).apply {
                                putExtra(EXTRA_UNI_ID, uni.id)
                            }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversityScreen(
    vm: UniversityViewModel,
    onBack: () -> Unit,
    onOpenLocation: (String) -> Unit,
    onUniversityClick: (University) -> Unit
) {
    val state by vm.state.collectAsState()

    val bg = Brush.verticalGradient(colors = listOf(Color(0xFF7EC7F5), Color(0xFFEAF4FF)))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Universities", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Column(Modifier.fillMaxSize()) {

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    placeholder = { Text("Search universities...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color(0xFFF3F7FF),
                        focusedContainerColor = Color(0xFFF3F7FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(Modifier.height(10.dp))

                when {
                    state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error ?: "Error", color = Color.Red)
                    }
                    state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No universities found", color = Color(0xFF6A7786))
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.filtered, key = { it.id }) { uni ->
                                UniversityCard(
                                    uni = uni,
                                    onOpenLocation = { onOpenLocation(uni.locationUrl) },
                                    onClick = { onUniversityClick(uni) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UniversityCard(
    uni: University,
    onOpenLocation: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uni.imageUrl,
                contentDescription = uni.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uni.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF0E2A47)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${uni.city}, ${uni.country}",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5B6B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = uni.description,
                    fontSize = 11.sp,
                    color = Color(0xFF6A7786),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F1FF))
                    .clickable { onOpenLocation() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF2D7EF7)
                )
            }
        }
    }
}
