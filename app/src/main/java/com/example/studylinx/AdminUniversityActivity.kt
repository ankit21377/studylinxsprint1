package com.example.studylinx

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.AdminUniversityViewModel

class AdminUniversityActivity : ComponentActivity() {

    private val vm by viewModels<AdminUniversityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AdminUniversitiesScreen(
                    vm = vm,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUniversitiesScreen(
    vm: AdminUniversityViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val state by vm.state.collectAsState()

    // Add dialog
    var showAdd by remember { mutableStateOf(false) }

    // Edit dialog
    var editTarget by remember { mutableStateOf<University?>(null) }

    // Delete confirm
    var deleteTarget by remember { mutableStateOf<University?>(null) }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
            vm.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin • Universities", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdd = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
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

                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::setQuery,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search universities...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(12.dp))

                when {
                    state.loading && state.universities.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.error != null -> Text(state.error ?: "Error", color = Color.Red)

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(state.filtered, key = { it.id }) { uni ->
                                AdminUniCard(
                                    uni = uni,
                                    onEdit = { editTarget = uni },
                                    onDelete = { deleteTarget = uni }
                                )
                            }
                        }
                    }
                }
            }

            if (showAdd) {
                AdminUniversityDialog(
                    title = "Add University",
                    initial = University(),
                    onDismiss = { showAdd = false },
                    onSave = { uni, imageUri ->
                        vm.addUniversity(uni, imageUri)
                        showAdd = false
                    }
                )
            }

            if (editTarget != null) {
                val u = editTarget!!
                AdminUniversityDialog(
                    title = "Edit University",
                    initial = u,
                    onDismiss = { editTarget = null },
                    onSave = { updated, imageUri ->
                        vm.updateUniversity(u.id, updated, imageUri)
                        editTarget = null
                    }
                )
            }

            if (deleteTarget != null) {
                val u = deleteTarget!!
                AlertDialog(
                    onDismissRequest = { deleteTarget = null },
                    title = { Text("Delete University?") },
                    text = { Text("This will remove university and course indexes.\n\n${u.name}") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteUniversity(u)
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

@Composable
private fun AdminUniCard(
    uni: University,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = uni.imageUrl,
                contentDescription = uni.name,
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF2FF))
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(uni.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${uni.city}, ${uni.country}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6A7786))
                if (uni.courses.isNotEmpty()) {
                    Text(
                        "Courses: ${uni.courses.take(3).joinToString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6A7786),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUniversityDialog(
    title: String,
    initial: University,
    onDismiss: () -> Unit,
    onSave: (University, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var city by remember { mutableStateOf(initial.city) }
    var country by remember { mutableStateOf(initial.country) }
    var description by remember { mutableStateOf(initial.description) }
    var locationUrl by remember { mutableStateOf(initial.locationUrl) }

    // Courses chip editing
    var courses by remember { mutableStateOf(initial.courses.toMutableList()) }
    var courseInput by remember { mutableStateOf("") }

    // Image picker
    var pickedImage by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pickedImage = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth()) {

                // Image preview
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = pickedImage ?: initial.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEAF2FF))
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(onClick = { picker.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose Image")
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = locationUrl, onValueChange = { locationUrl = it }, label = { Text("Location URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(12.dp))

                Text("Courses", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                // Chips
                FlowRowCourses(
                    courses = courses,
                    onRemove = { item -> courses = courses.toMutableList().apply { remove(item) } }
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = courseInput,
                        onValueChange = { courseInput = it },
                        label = { Text("Add course") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val c = courseInput.trim()
                        if (c.isNotBlank() && !courses.contains(c)) {
                            courses = courses.toMutableList().apply { add(c) }
                        }
                        courseInput = ""
                    }) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val u = initial.copy(
                    name = name.trim(),
                    city = city.trim(),
                    country = country.trim(),
                    description = description.trim(),
                    locationUrl = locationUrl.trim(),
                    courses = courses.map { it.trim() }.filter { it.isNotBlank() }.distinct()
                )
                onSave(u, pickedImage)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FlowRowCourses(
    courses: List<String>,
    onRemove: (String) -> Unit
) {
    // No external dependency; simple wrapping layout
    Column {
        var row by remember { mutableStateOf(listOf<String>()) }
        row = courses

        if (row.isEmpty()) {
            Text("No courses added", color = Color(0xFF6A7786))
            return
        }

        // Simple wrap-like effect using Rows in Column
        val chunked = row.chunked(3)
        chunked.forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                chunk.forEach { c ->
                    AssistChip(
                        onClick = { onRemove(c) },
                        label = { Text(c, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE1ECFF), RoundedCornerShape(999.dp))
                    )
                }
                // fill remaining
                repeat(3 - chunk.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
