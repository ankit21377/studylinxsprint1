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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.UniversitiesByCourseViewModel

class UniversitiesByCourseActivity : ComponentActivity() {

    private val vm by viewModels<UniversitiesByCourseViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val courseName = intent.getStringExtra("courseName") ?: ""

        setContent {
            val state by vm.state.collectAsState()

            LaunchedEffect(courseName) { vm.start(courseName) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(courseName.ifBlank { "Universities" }, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->

                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color(0xFFF6FAFF))
                        .padding(16.dp)
                ) {
                    when {
                        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        state.error != null -> Text(state.error ?: "Error", color = Color.Red)
                        else -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(state.list, key = { it.id }) { uni ->
                                    UniversityCard(uni) {
                                        if (uni.locationUrl.isNotBlank()) {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uni.locationUrl)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UniversityCard(uni: University, onOpenLocation: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = uni.imageUrl,
                contentDescription = uni.name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    uni.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${uni.city}, ${uni.country}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (uni.courses.isNotEmpty()) {
                    Text(
                        "Courses: ${uni.courses.take(3).joinToString()}",
                        fontSize = 11.sp,
                        color = Color(0xFF6A7786),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F1FF))
                    .clickable { onOpenLocation() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFF2D7EF7))
            }
        }
    }
}
