package com.example.studylinx

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.studylinx.viewmodel.UniversityDetailsViewModel

class UniversityDetailsActivity : ComponentActivity() {

    private val vm: UniversityDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uniId = intent.getStringExtra(UniversityActivity.EXTRA_UNI_ID) ?: ""

        setContent {
            MaterialTheme {
                UniversityDetailsScreen(
                    uniId = uniId,
                    vm = vm,
                    onBack = { finish() },
                    toast = { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversityDetailsScreen(
    uniId: String,
    vm: UniversityDetailsViewModel,
    onBack: () -> Unit,
    toast: (String) -> Unit
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(uniId) {
        vm.loadUniversity(uniId)
    }

    val bg = Brush.verticalGradient(listOf(Color(0xFFF6FAFF), Color(0xFFEAF2FF)))
    val primaryBlue = Color(0xFF2F79E6)
    val textDark = Color(0xFF1C2B3A)
    val textMuted = Color(0xFF7D8BA0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("University", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            when {
                ui.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }

                ui.error != null -> {
                    Text(ui.error ?: "Error", color = Color.Red)
                }

                ui.university == null -> {
                    Text("University not found", color = textMuted)
                }

                else -> {
                    val uni = ui.university!!

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(16.dp)) {

                                AsyncImage(
                                    model = uni.imageUrl,
                                    contentDescription = uni.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = uni.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${uni.city}, ${uni.country}",
                                    fontSize = 13.sp,
                                    color = textMuted
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = uni.description.ifBlank { "No description provided." },
                                    fontSize = 13.sp,
                                    color = textDark
                                )

                                if (uni.courses.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    Text("Courses", fontWeight = FontWeight.Bold, color = textDark)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        uni.courses.joinToString(),
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                vm.enroll(
                                    uniId = uni.id,
                                    onDone = { ok, msg -> toast(msg) }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            enabled = !ui.enrolling
                        ) {
                            Text(
                                text = if (ui.enrolling) "Enrolling..." else "Enroll Now",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}