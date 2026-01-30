// File: com/example/studylinx/CoursesActivity.kt
package com.example.studylinx

import android.content.Intent
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.viewmodel.CoursesViewModel

private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val PrimaryBlue = Color(0xFF2F79E6)
private val SoftBlue = Color(0xFFEAF2FF)
private val TextMuted = Color(0xFF7D8BA0)

class CoursesActivity : ComponentActivity() {

    private val vm by viewModels<CoursesViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by vm.state.collectAsState()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Courses", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->

                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                        .padding(16.dp)
                ) {

                    Column(Modifier.fillMaxSize()) {

                        // ✅ Countries option (like HomeScreen)
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    startActivity(Intent(this@CoursesActivity, CountryActivity::class.java))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("Countries", fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // ✅ Courses list section
                        when {
                            state.loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = PrimaryBlue)
                                }
                            }

                            state.error != null -> {
                                Text(state.error ?: "Error", color = Color.Red)
                            }

                            // ✅ EMPTY STATE
                            state.courses.isEmpty() -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "No courses available",
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(state.courses, key = { it.id }) { course ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // ✅ Open universities filtered by course
                                                    startActivity(
                                                        Intent(this@CoursesActivity, UniversitiesByCourseActivity::class.java)
                                                            .putExtra("courseName", course.name)
                                                    )
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(course.name, fontWeight = FontWeight.SemiBold)
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
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
}
