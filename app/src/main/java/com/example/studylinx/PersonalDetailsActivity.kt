package com.example.studylinx

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studylinx.repo.UserRepoImpl
import com.example.studylinx.viewmodel.PersonalDetailsViewModel
import java.util.Calendar

class PersonalDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = UserRepoImpl()
        val vm = PersonalDetailsViewModel(repo)

        setContent {
            MaterialTheme {
                PersonalDetailsScreen(vm = vm, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailsScreen(
    vm: PersonalDetailsViewModel,
    onBack: () -> Unit
) {
    var uiState by remember { mutableStateOf(vm.state) }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadUser { uiState = it }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        vm.setPickedImage(uri) { uiState = it }
    }

    fun openDobPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                vm.setDob("$y-$mm-$dd") { uiState = it }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7FF))
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.loading) {
                CircularProgressIndicator()
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // Profile circle + "+"
                        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2F79E6)),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    uiState.pickedImageUri != null -> {
                                        AsyncImage(
                                            model = uiState.pickedImageUri,
                                            contentDescription = "Picked Profile",
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    uiState.user.profileImageUrl.isNotBlank() -> {
                                        AsyncImage(
                                            model = uiState.user.profileImageUrl,
                                            contentDescription = "Profile",
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.9f))
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                    .clickable(enabled = !uiState.saving) { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = Color(0xFF2F79E6))
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        PersonalField(uiState.user.firstname, "First Name") {
                            vm.setFirstName(it) { uiState = it }
                        }

                        PersonalField(uiState.user.lastname, "Last Name") {
                            vm.setLastName(it) { uiState = it }
                        }

                        PersonalField(
                            value = uiState.user.email,
                            placeholder = "Email Address",
                            keyboardType = KeyboardType.Email
                        ) {
                            vm.setEmail(it) { uiState = it }
                        }

                        PersonalField(
                            value = uiState.user.phoneNumber,
                            placeholder = "Phone Number",
                            keyboardType = KeyboardType.Phone
                        ) {
                            vm.setPhone(it) { uiState = it }
                        }

                        PersonalField(
                            value = uiState.user.dateOfBirth,
                            placeholder = "Date of Birth",
                            readOnly = true,
                            onClick = { openDobPicker() }
                        ) { }

                        PersonalField(uiState.user.address, "Address") {
                            vm.setAddress(it) { uiState = it }
                        }

                        PersonalField(uiState.user.interestedCourseOrCountry, "Interested Course / Country") {
                            vm.setInterested(it) { uiState = it }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = { vm.saveDetails { uiState = it } },
                            enabled = !uiState.saving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F79E6))
                        ) {
                            if (uiState.saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Saving...", color = Color.White)
                            } else {
                                Text("Save Details", color = Color.White)
                            }
                        }

                        if (!uiState.message.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = uiState.message ?: "",
                                color = Color(0xFF2B3A55),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF4F7FF))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .clickable { vm.clearMessage { uiState = it } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsyncImage(model: String, contentDescription: String, modifier: Modifier) {
    TODO("Not yet implemented")
}

@Composable
private fun PersonalField(
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .let { m -> if (onClick != null) m.clickable { onClick() } else m },
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        placeholder = { Text(placeholder, color = Color(0xFF9AA6B2)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFDFE7F3),
            unfocusedBorderColor = Color(0xFFDFE7F3),
            focusedContainerColor = Color(0xFFF8FAFF),
            unfocusedContainerColor = Color(0xFFF8FAFF),
            cursorColor = Color(0xFF2F79E6)
        )
    )
}