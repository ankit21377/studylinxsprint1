package com.example.studylinx

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.model.Country
import com.example.studylinx.model.University
import com.example.studylinx.viewmodel.SearchViewModel

// ✅ Match DocumentActivity palette
private val BgTop = Color(0xFFF6FAFF)
private val BgBottom = Color(0xFFEAF2FF)
private val HeaderBlue1 = Color(0xFF2F79E6)
private val HeaderBlue2 = Color(0xFF6EA4EA)
private val SoftBlue = Color(0xFFEAF2FF)
private val TextDark = Color(0xFF1C2B3A)
private val TextMuted = Color(0xFF7D8BA0)

@Composable
fun SearchScreen(
    vm: SearchViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val state by vm.ui.collectAsState()

    Column(
        modifier = Modifier
            .testTag("search_screen")
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {

        // Top gradient header + search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Brush.horizontalGradient(listOf(HeaderBlue1, HeaderBlue2))),
            contentAlignment = Alignment.Center
        ) {
            SearchBar(
                value = state.query,
                onValueChange = vm::setQuery,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Categories header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Text("Show All", color = HeaderBlue1, fontSize = 14.sp)
        }

        Spacer(Modifier.height(12.dp))

        // Only Countries category (like your design)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            CategoryCard(title = "Countries")
        }

        Spacer(Modifier.height(16.dp))

        // List section
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    text = "Top Destinations",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            }

            // Loading / error states
            if (state.loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HeaderBlue1)
                    }
                }
            }

            state.error?.let { err ->
                item { Text(err, color = Color.Red) }
            }

            // Countries from RTDB
            items(state.filteredCountries, key = { it.id }) { country ->
                DestinationRow(country = country) {
                    // ✅ click country -> open UniversityActivity filtered by countryId
                    val i = Intent(ctx, UniversityActivity::class.java)
                    i.putExtra(UniversityActivity.EXTRA_COUNTRY_ID, country.id) // must exist in UniversityActivity
                    ctx.startActivity(i)
                }
            }

            // Optional: matching universities preview when query typed
            if (state.filteredUniversities.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Matching Universities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }

                items(state.filteredUniversities, key = { it.id }) { uni ->
                    UniversitySearchRow(uni = uni)
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun SearchBar(
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
                        "Search universities,country,courses",
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
                    cursorColor = HeaderBlue1
                )
            )

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextDark
            )
        }
    }
}

@Composable
private fun CategoryCard(title: String) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    .background(SoftBlue)
            )
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
        }
    }
}

@Composable
private fun DestinationRow(
    country: Country,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
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
            Spacer(Modifier.width(14.dp))
            Text(
                text = country.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}

@Composable
private fun UniversitySearchRow(uni: University) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = uni.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextDark
            )
            Text(
                text = "${uni.city}, ${uni.country}",
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (uni.courses.isNotEmpty()) {
                Text(
                    text = "Courses: ${uni.courses.take(3).joinToString(", ")}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
