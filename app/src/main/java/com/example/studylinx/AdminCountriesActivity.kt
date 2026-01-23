// ============================================================
// ✅ 6) AdminCountriesActivity (Realtime DB CRUD UI)
// - Has explicit SAVE button (no "direct saving...")
// File: com/example/studylinx/admin/AdminCountriesActivity.kt
// ============================================================
package com.example.studylinx.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.model.Country
import com.example.studylinx.repo.CountryRepoImpl
import com.example.studylinx.viewmodel.AdminCountriesViewModel


private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val PrimaryBlue = Color(0xFF2F79E6)
private val TextDark = Color(0xFF1C2B3A)
private val TextMuted = Color(0xFF7D8BA0)

class AdminCountriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AdminCountriesViewModel =
                viewModel(factory = AdminCountriesViewModel.factory(CountryRepoImpl()))

            AdminCountriesScreen(vm = vm, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCountriesScreen(
    vm: AdminCountriesViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by vm.ui.collectAsState()

    var addName by remember { mutableStateOf("") }
    var addFlag by remember { mutableStateOf("") }

    var editing by remember { mutableStateOf<Country?>(null) }
    var editName by remember { mutableStateOf("") }
    var editFlag by remember { mutableStateOf("") }

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrBlank()) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin • Countries", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // -------- Add form (explicit Save button) --------
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Country", fontWeight = FontWeight.SemiBold, color = TextDark)

                        OutlinedTextField(
                            value = addName,
                            onValueChange = { addName = it },
                            label = { Text("Country name (e.g. USA)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = addFlag,
                            onValueChange = { addFlag = it },
                            label = { Text("Flag URL (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                vm.addNew(addName, addFlag)
                                addName = ""
                                addFlag = ""
                            },
                            enabled = !state.saving,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(if (state.saving) "Saving..." else "Save", color = Color.White)
                        }
                    }
                }

                // -------- List --------
                Text(
                    text = "All Countries",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )

                if (state.loading) {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (state.countries.isEmpty()) {
                    Text("No countries found", color = TextMuted)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.countries, key = { it.id }) { c ->
                            CountryAdminRow(
                                c = c,
                                onEdit = {
                                    editing = c
                                    editName = c.name
                                    editFlag = c.flagUrl
                                },
                                onDelete = { vm.delete(c.id) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }

            // -------- Edit dialog (explicit Save button) --------
            if (editing != null) {
                AlertDialog(
                    onDismissRequest = { editing = null },
                    title = { Text("Edit Country") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editFlag,
                                onValueChange = { editFlag = it },
                                label = { Text("Flag URL (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "ID: ${editing!!.id}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = editing!!.id
                                vm.update(id, editName, editFlag)
                                editing = null
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { editing = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
private fun CountryAdminRow(
    c: Country,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = c.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "id: ${c.id}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}
