package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.viewmodel.AdminUniversityViewModel


class AdminUniversityActivity : ComponentActivity() {

    private val vm: AdminUniversityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AdminUniversityScreen(
                    vm = vm,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUniversityScreen(
    vm: AdminUniversityViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add University", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.name,
                onValueChange = vm::onName,
                label = { Text("University Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.city,
                onValueChange = vm::onCity,
                label = { Text("City *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.country,
                onValueChange = vm::onCountry,
                label = { Text("Country *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = vm::onDesc,
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = vm::onImage,
                label = { Text("Image URL (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.locationUrl,
                onValueChange = vm::onLocation,
                label = { Text("Google Maps Link (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = vm::saveUniversity,
                enabled = !state.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Saving...")
                } else {
                    Text("Save University")
                }
            }

            if (state.message != null) {
                Spacer(Modifier.height(6.dp))
                Text(state.message!!, color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(state.message) {
                    // auto clear after showing once
                    kotlinx.coroutines.delay(2000)
                    vm.clearMessage()
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
