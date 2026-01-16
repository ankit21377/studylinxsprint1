// =====================================
// ✅ AdminCoursesActivity.kt
// - Add / Edit / Delete Courses
// - Deleting course: removes course_index node and removes this course from ALL universities.courses
// Requires: CourseRepoImpl, CourseIndexRepoImpl, CourseKey, University model
// =====================================
package com.example.studylinx

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.model.Course
import com.example.studylinx.viewmodel.AdminCoursesViewModel

class AdminCoursesActivity : ComponentActivity() {

    private val vm by viewModels<AdminCoursesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AdminCoursesScreen(
                    vm = vm,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCoursesScreen(
    vm: AdminCoursesViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val state by vm.state.collectAsState()

    var newCourse by remember { mutableStateOf("") }

    var editTarget by remember { mutableStateOf<Course?>(null) }
    var editName by remember { mutableStateOf("") }

    var deleteTarget by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
            vm.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin • Courses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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

            Column(Modifier.fillMaxSize()) {

                // Add course
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Add new course", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newCourse,
                            onValueChange = { newCourse = it },
                            placeholder = { Text("e.g. Computer Science") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val name = newCourse.trim()
                                if (name.isBlank()) return@Button
                                vm.addCourse(name)
                                newCourse = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.loading
                        ) {
                            Text(if (state.loading) "Saving..." else "Add Course")
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // List
                when {
                    state.loading && state.courses.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.error != null -> {
                        Text(state.error ?: "Error", color = Color.Red)
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(state.courses, key = { it.id }) { c ->
                                Card(shape = RoundedCornerShape(16.dp)) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(c.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "ID: ${c.id}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6A7786)
                                            )
                                        }

                                        IconButton(onClick = {
                                            editTarget = c
                                            editName = c.name
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }

                                        IconButton(onClick = { deleteTarget = c }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Edit dialog
            if (editTarget != null) {
                AlertDialog(
                    onDismissRequest = { editTarget = null },
                    title = { Text("Edit Course") },
                    text = {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val target = editTarget ?: return@TextButton
                                val newName = editName.trim()
                                if (newName.isBlank()) return@TextButton
                                vm.updateCourse(target, newName)
                                editTarget = null
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { editTarget = null }) { Text("Cancel") }
                    }
                )
            }

            // Delete confirm
            if (deleteTarget != null) {
                val c = deleteTarget!!
                AlertDialog(
                    onDismissRequest = { deleteTarget = null },
                    title = { Text("Delete Course?") },
                    text = {
                        Text(
                            "This will:\n" +
                                    "• remove the course from the course list\n" +
                                    "• delete course_index for this course\n" +
                                    "• remove this course from all universities\n\n" +
                                    "Course: ${c.name}"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteCourse(c)
                                deleteTarget = null
                            }
                        ) { Text("Delete", color = Color(0xFFD32F2F)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
