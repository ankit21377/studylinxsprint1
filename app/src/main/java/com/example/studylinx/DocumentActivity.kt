package com.example.studylinx

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.viewmodel.DocumentViewModel

class DocumentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                DocumentScreen(
                    onBack = { finish() },
                    getFileName = { uri -> getFileName(uri) },
                    toast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
                )
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = "selected_file"
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) result = it.getString(nameIndex)
        }
        return result
    }
}

private enum class DocType(val key: String, val label: String) {
    PASSPORT("passport", "Passport"),
    ACADEMIC("academic", "Academic Certificates"),
    ENGLISH("english", "English Test Results"),
    FINANCIAL("financial", "Financial Statement")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentScreen(
    onBack: () -> Unit,
    getFileName: (Uri) -> String,
    toast: (String) -> Unit,
    vm: DocumentViewModel = viewModel()
) {
    // start observing user docs (loads filenames from Firestore)
    LaunchedEffect(Unit) {
        vm.startObservingUserDocs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* keep empty to match your UI */ },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        DocumentUploadContent(
            vm = vm,
            modifier = Modifier.padding(padding),
            getFileName = getFileName,
            toast = toast
        )
    }
}

@Composable
private fun DocumentUploadContent(
    vm: DocumentViewModel,
    modifier: Modifier = Modifier,
    getFileName: (Uri) -> String,
    toast: (String) -> Unit
) {
    val uploadedDocs by vm.uploadedDocs.collectAsState()
    val progress by vm.progress.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) toast(error!!)
    }

    // Colors like screenshot
    val bgTop = Color(0xFFF6FAFF)
    val bgBottom = Color(0xFFEAF2FF)
    val blue = Color(0xFF2F79E6)
    val softBlue = Color(0xFFEAF2FF)
    val textDark = Color(0xFF1C2B3A)
    val textMuted = Color(0xFF7D8BA0)

    var picking by remember { mutableStateOf<DocType?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val type = picking
        if (uri != null && type != null) {
            val name = getFileName(uri)
            vm.upload(type.key, uri, name)
        }
        picking = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            Text(
                text = "Document Upload",
                color = textDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Please upload your documents:",
                color = textMuted,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            DocRow(
                title = DocType.PASSPORT.label,
                icon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = blue) },
                fileName = uploadedDocs[DocType.PASSPORT.key]?.fileName ?: "No file chosen",
                percent = progress[DocType.PASSPORT.key],
                onUpload = {
                    picking = DocType.PASSPORT
                    picker.launch(arrayOf("application/pdf", "image/*"))
                },
                blue = blue,
                softBlue = softBlue,
                textDark = textDark,
                textMuted = textMuted
            )
            Spacer(Modifier.height(12.dp))

            DocRow(
                title = DocType.ACADEMIC.label,
                icon = { Icon(Icons.Filled.Description, contentDescription = null, tint = blue) },
                fileName = uploadedDocs[DocType.ACADEMIC.key]?.fileName ?: "No file chosen",
                percent = progress[DocType.ACADEMIC.key],
                onUpload = {
                    picking = DocType.ACADEMIC
                    picker.launch(arrayOf("application/pdf", "image/*"))
                },
                blue = blue,
                softBlue = softBlue,
                textDark = textDark,
                textMuted = textMuted
            )
            Spacer(Modifier.height(12.dp))

            DocRow(
                title = DocType.ENGLISH.label,
                icon = { Icon(Icons.Filled.Language, contentDescription = null, tint = blue) },
                fileName = uploadedDocs[DocType.ENGLISH.key]?.fileName ?: "No file chosen",
                percent = progress[DocType.ENGLISH.key],
                onUpload = {
                    picking = DocType.ENGLISH
                    picker.launch(arrayOf("application/pdf", "image/*"))
                },
                blue = blue,
                softBlue = softBlue,
                textDark = textDark,
                textMuted = textMuted
            )
            Spacer(Modifier.height(12.dp))

            DocRow(
                title = DocType.FINANCIAL.label,
                icon = { Icon(Icons.Filled.Work, contentDescription = null, tint = blue) },
                fileName = uploadedDocs[DocType.FINANCIAL.key]?.fileName ?: "No file chosen",
                percent = progress[DocType.FINANCIAL.key],
                onUpload = {
                    picking = DocType.FINANCIAL
                    picker.launch(arrayOf("application/pdf", "image/*"))
                },
                blue = blue,
                softBlue = softBlue,
                textDark = textDark,
                textMuted = textMuted
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    val missing = DocType.entries
                        .filter { uploadedDocs[it.key] == null }
                        .map { it.label }

                    if (missing.isEmpty()) toast("All documents uploaded ✅")
                    else toast("Missing: ${missing.joinToString()}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text(
                    text = if (loading) "Uploading..." else "Submit Documents",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
@Composable
private fun DocRow(
    title: String,
    icon: @Composable () -> Unit,
    fileName: String,
    percent: Int?,
    onUpload: () -> Unit,
    blue: Color,
    softBlue: Color,
    textDark: Color,
    textMuted: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5EEFF), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(softBlue),
            contentAlignment = Alignment.Center
        ) { icon() }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textDark,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = fileName,
                color = textMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (percent != null && percent in 0..99) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = percent / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = onUpload,
            modifier = Modifier
                .height(38.dp)
                .width(92.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = blue)
        ) {
            Text(
                text = "Upload",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}


