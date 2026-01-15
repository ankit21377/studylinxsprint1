
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
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.viewmodel.CoursesViewModel

class CoursesActivity : ComponentActivity() {

    private val vm by viewModels<CoursesViewModel>()

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
                        state.loading -> CircularProgressIndicator()
                        state.error != null -> Text(state.error ?: "Error", color = Color.Red)
                        else -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(state.courses, key = { it.id }) { course ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                startActivity(
                                                    Intent(this@CoursesActivity, UniversitiesByCourseActivity::class.java)
                                                        .putExtra("courseName", course.name)
                                                )
                                            },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(course.name, fontWeight = FontWeight.SemiBold)
                                            Icon(Icons.Default.ChevronRight, contentDescription = null)
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
