package com.example.studylinx

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.studylinx.repo.UserRepoImpl
import com.example.studylinx.viewmodel.PersonalDetailsViewModel

// ---- DocumentActivity palette ----
private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val PrimaryBlue = Color(0xFF2F79E6)
private val PrimaryBlue2 = Color(0xFF6EA4EA)
private val SoftBlue = Color(0xFFEAF2FF)
private val TextDark = Color(0xFF1C2B3A)
private val TextMuted = Color(0xFF7D8BA0)

class PersonalDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PersonalDetailsScreen(onBack = { finish() }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    onBack: () -> Unit,
    vm: PersonalDetailsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PersonalDetailsViewModel(UserRepoImpl()) as T
            }
        }
    )
) {
    val ctx = LocalContext.current
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.loadMe() }

    // image picker
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        vm.setLocalImage(uri)
    }

    LaunchedEffect(state.error) {
        state.error?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.success) {
        state.success?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Details", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    // ---- Profile Image (click to change) ----
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {

                            val imgModel = state.localImageUri ?: state.profileImageUrl

                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(SoftBlue)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    state.localImageUri != null -> {
                                        AsyncImage(
                                            model = state.localImageUri,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    state.profileImageUrl.isNotBlank() -> {
                                        AsyncImage(
                                            model = imgModel,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    else -> {
                                        // fallback icon-like feel
                                        Text(
                                            text = "👤",
                                            fontSize = 30.sp
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ---- Fields (like the image) ----
                    PersonalTextField(
                        value = state.fullName,
                        onChange = vm::setFullName,
                        placeholder = "Full Name",
                        enabled = !state.saving
                    )
                    Spacer(Modifier.height(10.dp))

                    PersonalTextField(
                        value = state.email,
                        onChange = vm::setEmail,
                        placeholder = "Email Address",
                        enabled = !state.saving
                    )
                    Spacer(Modifier.height(10.dp))

                    PersonalTextField(
                        value = state.phone,
                        onChange = vm::setPhone,
                        placeholder = "Phone Number",
                        enabled = !state.saving
                    )
                    Spacer(Modifier.height(10.dp))

                    PersonalTextField(
                        value = state.dob,
                        onChange = vm::setDob,
                        placeholder = "Date of Birth",
                        enabled = !state.saving
                    )
                    Spacer(Modifier.height(10.dp))

                    PersonalTextField(
                        value = state.address,
                        onChange = vm::setAddress,
                        placeholder = "Address",
                        enabled = !state.saving
                    )
                    Spacer(Modifier.height(10.dp))

                    PersonalTextField(
                        value = state.interested,
                        onChange = vm::setInterested,
                        placeholder = "Interested Course / Country",
                        enabled = !state.saving
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // small extra validation in UI
                            val emailOk = state.email.trim().isNotBlank() &&
                                    Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()
                            if (!emailOk) {
                                Toast.makeText(ctx, "Enter a valid email", Toast.LENGTH_SHORT).show()
                            } else {
                                vm.saveDetails()
                            }
                        },
                        enabled = !state.saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = if (state.saving) "Saving..." else "Save Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (state.loading) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryBlue)
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tip: Tap on the profile image to change it.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalTextField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder, color = TextMuted) },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE3EBFF),
            focusedBorderColor = Color(0xFFBFD3FF)
        )
    )
}