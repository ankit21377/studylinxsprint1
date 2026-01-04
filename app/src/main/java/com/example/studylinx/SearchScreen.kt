package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// ----------------------------
// Data Models
// ----------------------------
data class UniversityItem(
    val name: String,
    val faculties: List<String>
)

data class DestinationItem(
    val id: String,           // "usa", "australia", ...
    val title: String,        // "USA", ...
    val universities: List<UniversityItem>
)

// ----------------------------
// Fake "Top picks for Nepali students" dataset
// (Popular options widely used by Nepali applicants + Nepal-focused guidance lists.)
// ----------------------------
private fun destinationsData(): List<DestinationItem> {
    val commonFaculties = listOf(
        "Engineering & Technology",
        "Business & Management",
        "Science",
        "Computer & IT",
        "Health Sciences",
        "Arts & Humanities",
        "Law & Public Policy"
    )

    fun uni(name: String, faculties: List<String> = commonFaculties) =
        UniversityItem(name = name, faculties = faculties)
