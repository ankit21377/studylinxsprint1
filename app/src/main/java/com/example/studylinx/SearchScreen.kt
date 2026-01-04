
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

// ----------------------------
// Activity
// ----------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF3F8EFC),
                    surface = Color.White,
                    background = Color(0xFFF3F6FB)
                )
            ) {
                StudyDestinationApp()
            }
        }
    }
}

// ----------------------------
// Navigation Host
// ----------------------------
@Composable
fun StudyDestinationApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "destinations") {
        composable("destinations") {
            DestinationHomeScreen(
                destinations = destinationsData(),
                onDestinationClick = { destId ->
                    navController.navigate("destination/$destId")
                }
            )
        }

        composable(
            route = "destination/{destId}",
            arguments = listOf(navArgument("destId") { type = NavType.StringType })
        ) { backStackEntry ->
            val destId = backStackEntry.arguments?.getString("destId").orEmpty()
            val destination = destinationsData().firstOrNull { it.id == destId }

            DestinationDetailScreen(
                destination = destination,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ----------------------------
// HOME SCREEN UI (matches your screenshot layout)
// ----------------------------
@Composable
fun DestinationHomeScreen(
    destinations: List<DestinationItem>,
    onDestinationClick: (String) -> Unit
) {
    val bgBlue = Color(0xFF6EA7FF)
    val pageBg = Color(0xFFF3F6FB)

    var searchText by remember { mutableStateOf("") }

    val filteredDestinations = remember(searchText, destinations) {
        val q = searchText.trim().lowercase()
        if (q.isEmpty()) destinations
        else destinations.filter { it.title.lowercase().contains(q) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        // Top blue header area with search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(bgBlue)
        ) {
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 18.dp)
            )
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            // Categories header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Categories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = "Show All",
                        fontSize = 14.sp,
                        color = Color(0xFF3F8EFC),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Only ONE category: Countries
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    CategoryCard(title = "Countries")
                }
            }

            // Top Destinations
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Top Destinations",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }

            items(filteredDestinations) { dest ->
                DestinationRow(
                    title = dest.title,
                    onClick = { onDestinationClick(dest.id) }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        "Search universities, countries",
                        color = Color(0xFF8C8C8C),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFF3F8EFC)
                )
            )

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF111111)
            )
        }
    }
}

@Composable
fun CategoryCard(title: String) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6ECFF))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111111)
            )
        }
    }
}

@Composable
fun DestinationRow(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(78.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDFF0FF))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
        }
    }
}

// ----------------------------
// DETAIL SCREEN: Top 10 universities + Faculties only
// ----------------------------
@Composable
fun DestinationDetailScreen(
    destination: DestinationItem?,
    onBack: () -> Unit
) {
    val pageBg = Color(0xFFF3F6FB)

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = destination?.title ?: "Destination",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = pageBg
    ) { padding ->
        if (destination == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Destination not found.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(18.dp)
        ) {
            item {
                Text(
                    text = "Top 10 Universities",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Faculties are shown (no course names).",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            items(destination.universities) { uni ->
                UniversityCard(uni)
            }
        }
    }
}

@Composable
fun UniversityCard(uni: UniversityItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = uni.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Faculties",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3F8EFC)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Faculties chips style
            FlowRowChips(items = uni.faculties)
        }
    }
}

// Simple chip row without extra libs
@Composable
fun FlowRowChips(items: List<String>) {
    Column {
        val rows = mutableListOf<List<String>>()
        var current = mutableListOf<String>()

        // naive wrap: 3 chips per row (nice for mobile)
        items.forEach { item ->
            if (current.size == 3) {
                rows.add(current)
                current = mutableListOf()
            }
            current.add(item)
        }
        if (current.isNotEmpty()) rows.add(current)

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                row.forEach { text ->
                    AssistChip(
                        onClick = { /* no action */ },
                        label = { Text(text, fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFEAF4FF),
                            labelColor = Color(0xFF111111)
                        ),
                        border = null
                    )
                }
            }
        }
    }
}
