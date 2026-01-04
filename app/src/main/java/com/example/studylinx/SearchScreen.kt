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
    return listOf(
        DestinationItem(
            id = "usa",
            title = "USA",
            universities = listOf(
                uni("University of Texas at Arlington", listOf("Engineering & Technology", "Business & Management", "Science", "Computer & IT", "Arts & Humanities")),
                uni("University of North Texas", listOf("Business & Management", "Computer & IT", "Science", "Arts & Humanities", "Public Policy")),
                uni("California State University, Northridge", listOf("Business & Management", "Engineering & Technology", "Arts & Humanities", "Science", "Computer & IT")),
                uni("Arizona State University", listOf("Engineering & Technology", "Business & Management", "Computer & IT", "Science", "Public Policy")),
                uni("University of South Florida", listOf("Engineering & Technology", "Health Sciences", "Business & Management", "Science", "Computer & IT")),
                uni("University of Illinois Chicago", listOf("Engineering & Technology", "Business & Management", "Health Sciences", "Law", "Science")),
                uni("Northeastern University", listOf("Engineering & Technology", "Business & Management", "Computer & IT", "Science", "Health Sciences")),
                uni("Purdue University", listOf("Engineering & Technology", "Science", "Computer & IT", "Business & Management")),
                uni("George Mason University", listOf("Computer & IT", "Business & Management", "Public Policy", "Law", "Science")),
                uni("University of Central Missouri", listOf("Business & Management", "Computer & IT", "Science", "Arts & Humanities"))
            )
        ),
        DestinationItem(
            id = "australia",
            title = "Australia",
            universities = listOf(
                uni("University of Sydney", listOf("Engineering & Technology", "Business & Management", "Health Sciences", "Law", "Arts & Humanities", "Science")),
                uni("Monash University", listOf("Engineering & Technology", "Business & Management", "Health Sciences", "Science", "Arts & Humanities", "Law")),
                uni("University of Melbourne", listOf("Business & Management", "Science", "Engineering & Technology", "Health Sciences", "Law", "Arts & Humanities")),
                uni("Deakin University", listOf("Business & Management", "Engineering & Technology", "Computer & IT", "Health Sciences", "Arts & Humanities")),
                uni("RMIT University", listOf("Engineering & Technology", "Computer & IT", "Business & Management", "Arts & Design", "Science")),
                uni("University of Queensland", listOf("Engineering & Technology", "Business & Management", "Science", "Health Sciences", "Arts & Humanities")),
                uni("University of Adelaide", listOf("Engineering & Technology", "Science", "Business & Management", "Health Sciences", "Arts & Humanities")),
                uni("Macquarie University", listOf("Business & Management", "Science", "Computer & IT", "Arts & Humanities", "Public Policy")),
                uni("University of Technology Sydney (UTS)", listOf("Engineering & Technology", "Computer & IT", "Business & Management", "Science")),
                uni("La Trobe University", listOf("Health Sciences", "Business & Management", "Computer & IT", "Science", "Arts & Humanities"))
            )
        ),
        DestinationItem(
            id = "uk",
            title = "UK",
            universities = listOf(
                uni("University of Bedfordshire", listOf("Business & Management", "Computer & IT", "Health Sciences", "Arts & Humanities")),
                uni("University of Greenwich", listOf("Engineering & Technology", "Business & Management", "Science", "Computer & IT")),
                uni("Middlesex University", listOf("Business & Management", "Computer & IT", "Law", "Arts & Humanities", "Science")),
                uni("Cardiff University", listOf("Engineering & Technology", "Science", "Business & Management", "Law", "Arts & Humanities")),
                uni("University of East London", listOf("Business & Management", "Computer & IT", "Health Sciences", "Arts & Humanities")),
                uni("Sheffield Hallam University", listOf("Engineering & Technology", "Business & Management", "Health Sciences", "Arts & Humanities")),
                uni("University of Salford", listOf("Engineering & Technology", "Business & Management", "Science", "Arts & Humanities")),
                uni("University of Hertfordshire", listOf("Engineering & Technology", "Computer & IT", "Business & Management", "Health Sciences")),
                uni("Northumbria University", listOf("Business & Management", "Engineering & Technology", "Law", "Arts & Humanities")),
                uni("Liverpool John Moores University", listOf("Engineering & Technology", "Science", "Business & Management", "Health Sciences"))
            )
        ),
        DestinationItem(
            id = "canada",
            title = "Canada",
            universities = listOf(
                uni("University of Toronto", listOf("Engineering & Technology", "Business & Management", "Science", "Computer & IT", "Health Sciences", "Arts & Humanities", "Law")),
                uni("University of British Columbia (UBC)", listOf("Engineering & Technology", "Science", "Business & Management", "Computer & IT", "Arts & Humanities")),
                uni("McGill University", listOf("Science", "Engineering & Technology", "Health Sciences", "Arts & Humanities", "Law")),
                uni("University of Waterloo", listOf("Engineering & Technology", "Computer & IT", "Science", "Business & Management")),
                uni("Western University", listOf("Business & Management", "Science", "Engineering & Technology", "Health Sciences", "Arts & Humanities")),
                uni("York University", listOf("Business & Management", "Law", "Arts & Humanities", "Science", "Computer & IT")),
                uni("Toronto Metropolitan University (TMU)", listOf("Business & Management", "Engineering & Technology", "Computer & IT", "Arts & Humanities")),
                uni("University of Ottawa", listOf("Law", "Engineering & Technology", "Health Sciences", "Science", "Arts & Humanities")),
                uni("University of Calgary", listOf("Engineering & Technology", "Business & Management", "Science", "Health Sciences")),
                uni("Simon Fraser University", listOf("Science", "Computer & IT", "Business & Management", "Arts & Humanities"))
            )
        ),
        DestinationItem(
            id = "new_zealand",
            title = "New Zealand",
            universities = listOf(
                uni("University of Auckland", listOf("Engineering & Technology", "Business & Management", "Science", "Health Sciences", "Arts & Humanities", "Law")),
                uni("University of Otago", listOf("Health Sciences", "Science", "Arts & Humanities", "Business & Management")),
                uni("Massey University", listOf("Business & Management", "Engineering & Technology", "Science", "Arts & Humanities", "Health Sciences")),
                uni("Victoria University of Wellington", listOf("Law", "Business & Management", "Science", "Engineering & Technology", "Arts & Humanities")),
                uni("University of Canterbury", listOf("Engineering & Technology", "Science", "Computer & IT", "Arts & Humanities")),
                uni("University of Waikato", listOf("Business & Management", "Computer & IT", "Science", "Arts & Humanities")),
                uni("Auckland University of Technology (AUT)", listOf("Engineering & Technology", "Computer & IT", "Business & Management", "Health Sciences")),
                uni("Lincoln University", listOf("Science", "Business & Management", "Environment & Planning", "Arts & Humanities")),
                uni("Unitec Institute of Technology", listOf("Engineering & Technology", "Computer & IT", "Business & Management", "Health Sciences")),
                uni("Ara Institute of Canterbury", listOf("Engineering & Technology", "Business & Management", "Computer & IT", "Health Sciences"))
            )
        ),
        DestinationItem(
            id = "europe",
            title = "Europe",
            universities = listOf(
                uni("Technical University of Munich (Germany)", listOf("Engineering & Technology", "Science", "Computer & IT", "Business & Management")),
                uni("RWTH Aachen University (Germany)", listOf("Engineering & Technology", "Science", "Computer & IT")),
                uni("KU Leuven (Belgium)", listOf("Engineering & Technology", "Science", "Computer & IT", "Arts & Humanities", "Business & Management")),
                uni("Trinity College Dublin (Ireland)", listOf("Engineering & Technology", "Science", "Computer & IT", "Business & Management", "Arts & Humanities", "Law")),
                uni("University of Amsterdam (Netherlands)", listOf("Science", "Business & Management", "Arts & Humanities", "Law", "Computer & IT")),
                uni("TU Delft (Netherlands)", listOf("Engineering & Technology", "Science", "Computer & IT")),
                uni("KTH Royal Institute of Technology (Sweden)", listOf("Engineering & Technology", "Science", "Computer & IT")),
                uni("University of Helsinki (Finland)", listOf("Science", "Computer & IT", "Arts & Humanities", "Law", "Health Sciences")),
                uni("Sapienza University of Rome (Italy)", listOf("Engineering & Technology", "Science", "Arts & Humanities", "Law", "Business & Management")),
                uni("Sorbonne University (France)", listOf("Science", "Arts & Humanities", "Engineering & Technology", "Health Sciences"))
            )
        )
    )
}