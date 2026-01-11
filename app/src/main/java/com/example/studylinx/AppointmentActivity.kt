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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studylinx.model.Appointment
import com.example.studylinx.repo.AppointmentRepoImpl
import com.example.studylinx.ui.viewmodel.AppointmentFilter
import com.example.studylinx.ui.viewmodel.AppointmentsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {

                val vm: AppointmentsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val repo = AppointmentRepoImpl()
                            @Suppress("UNCHECKED_CAST")
                            return AppointmentsViewModel(repo) as T
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser ?: auth.signInAnonymously().await().user
                    vm.setUser(user?.uid ?: "")
                }

                UserAppointmentsUI(
                    vm = vm,
                    onBack = { finish() }
                )
            }
        }
    }

    /* ===================== USER UI ===================== */

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserAppointmentsUI(
        vm: AppointmentsViewModel,
        onBack: () -> Unit
    ) {
        val state by vm.ui.collectAsState()

        val dayList = remember(state.monthAppointments, state.selectedDate, state.searchQuery, state.filter) {
            vm.appointmentsForSelectedDay()
        }
        val daysWith = remember(state.monthAppointments, state.month) {
            vm.daysWithAppointmentsInMonth()
        }

        var showAddDialog by remember { mutableStateOf(false) }
        var openDetails by remember { mutableStateOf<Appointment?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Appointments") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
            ) {
                CalendarCard(
                    month = state.month,
                    selectedDate = state.selectedDate,
                    daysWithAppointments = daysWith,
                    onPrevMonth = vm::prevMonth,
                    onNextMonth = vm::nextMonth,
                    onSelectDate = vm::setSelectedDate
                )

                Spacer(Modifier.height(10.dp))

                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = vm::setSearchQuery
                )

                Spacer(Modifier.height(10.dp))

                FilterRow(
                    filter = state.filter,
                    onChange = vm::setFilter
                )

                Spacer(Modifier.height(10.dp))

                val selectedLabel = remember(state.selectedDate) {
                    state.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
                    )
                }

                Text(
                    text = "Selected: $selectedLabel",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontWeight = FontWeight.SemiBold
                )

                if (state.loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 14.dp)
                    ) {
                        if (dayList.isNotEmpty()) {
                            items(dayList, key = { it.id }) { ap ->
                                UserAppointmentCard(
                                    appointment = ap,
                                    onOpen = { openDetails = ap }
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        } else {
                            item {
                                EmptyDayCard()
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        item {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add appointment")
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAppointmentDialog(
                selectedDate = state.selectedDate,
                onDismiss = { showAddDialog = false },
                onSave = { title, note, startMillis, endMillis ->
                    vm.addAppointment(title, note, startMillis, endMillis)
                    showAddDialog = false
                }
            )
        }

        if (openDetails != null) {
            AppointmentDetailsDialog(
                appointment = openDetails!!,
                onDismiss = { openDetails = null }
            )
        }
    }

    @Composable
    private fun CalendarCard(
        month: YearMonth,
        selectedDate: LocalDate,
        daysWithAppointments: Set<LocalDate>,
        onPrevMonth: () -> Unit,
        onNextMonth: () -> Unit,
        onSelectDate: (LocalDate) -> Unit
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevMonth) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev month")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }

                Spacer(Modifier.height(6.dp))

                val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(Modifier.fillMaxWidth()) {
                    weekDays.forEach {
                        Text(
                            text = it,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                MonthGrid(
                    month = month,
                    selectedDate = selectedDate,
                    daysWithAppointments = daysWithAppointments,
                    onSelectDate = onSelectDate
                )
            }
        }
    }

    @Composable
    private fun MonthGrid(
        month: YearMonth,
        selectedDate: LocalDate,
        daysWithAppointments: Set<LocalDate>,
        onSelectDate: (LocalDate) -> Unit
    ) {
        val firstDay = month.atDay(1)
        val daysInMonth = month.lengthOfMonth()

        fun sundayIndex(d: DayOfWeek): Int = when (d) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }

        val offset = sundayIndex(firstDay.dayOfWeek)
        val totalCells = offset + daysInMonth
        val rows = ((totalCells + 6) / 7)

        Column {
            var dayNum = 1
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0..6) {
                        val idx = r * 7 + c
                        if (idx < offset || dayNum > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).height(44.dp))
                        } else {
                            val date = month.atDay(dayNum)
                            val isSelected = date == selectedDate
                            val hasDot = daysWithAppointments.contains(date)

                            Box(
                                modifier = Modifier.weight(1f).height(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onSelectDate(date) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (hasDot) {
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                } else MaterialTheme.colorScheme.surface
                                            )
                                    )
                                }
                            }
                            dayNum++
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
    }

    @Composable
    private fun FilterRow(filter: AppointmentFilter, onChange: (AppointmentFilter) -> Unit) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = filter == AppointmentFilter.ALL,
                onClick = { onChange(AppointmentFilter.ALL) },
                label = { Text("All") },
                leadingIcon = if (filter == AppointmentFilter.ALL) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
            Spacer(Modifier.width(10.dp))
            FilterChip(
                selected = filter == AppointmentFilter.UPCOMING,
                onClick = { onChange(AppointmentFilter.UPCOMING) },
                label = { Text("Upcoming") }
            )
            Spacer(Modifier.width(10.dp))
            FilterChip(
                selected = filter == AppointmentFilter.PAST,
                onClick = { onChange(AppointmentFilter.PAST) },
                label = { Text("Past") }
            )
        }
    }

    @Composable
    private fun UserAppointmentCard(appointment: Appointment, onOpen: () -> Unit) {
        val zone = ZoneId.systemDefault()
        val timeText = remember(appointment.startMillis, appointment.endMillis) {
            val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val s = Instant.ofEpochMilli(appointment.startMillis).atZone(zone).toLocalTime()
            val e = Instant.ofEpochMilli(appointment.endMillis).atZone(zone).toLocalTime()
            "${s.format(fmt)} — ${e.format(fmt)}"
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onOpen() },
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = appointment.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (appointment.note.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = appointment.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun EmptyDayCard() {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "No appointments for this day.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun AppointmentDetailsDialog(appointment: Appointment, onDismiss: () -> Unit) {
        val zone = ZoneId.systemDefault()
        val date = remember(appointment.startMillis) {
            Instant.ofEpochMilli(appointment.startMillis).atZone(zone).toLocalDate()
        }
        val start = remember(appointment.startMillis) {
            Instant.ofEpochMilli(appointment.startMillis).atZone(zone).toLocalTime()
        }
        val end = remember(appointment.endMillis) {
            Instant.ofEpochMilli(appointment.endMillis).atZone(zone).toLocalTime()
        }

        val dateFmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()) }
        val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(appointment.title) },
            text = {
                Column {
                    Text(
                        text = "${date.format(dateFmt)} • ${start.format(timeFmt)} — ${end.format(timeFmt)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (appointment.note.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(appointment.note)
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
        )
    }

    @Composable
    private fun AddAppointmentDialog(
        selectedDate: LocalDate,
        onDismiss: () -> Unit,
        onSave: (title: String, note: String, startMillis: Long, endMillis: Long) -> Unit
    ) {
        val zone = ZoneId.systemDefault()
        var title by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        var startTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
        var endTime by remember { mutableStateOf(LocalTime.of(11, 0)) }

        val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Book appointment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeAdjust("Start", startTime.format(timeFmt),
                            onMinus = { startTime = startTime.minusMinutes(15) },
                            onPlus = { startTime = startTime.plusMinutes(15) }
                        )
                        TimeAdjust("End", endTime.format(timeFmt),
                            onMinus = { endTime = endTime.minusMinutes(15) },
                            onPlus = { endTime = endTime.plusMinutes(15) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val safeTitle = title.trim()
                        if (safeTitle.isBlank()) return@TextButton

                        var s = startTime
                        var e = endTime
                        if (!e.isAfter(s)) e = s.plusMinutes(60)

                        val startMillis = selectedDate.atTime(s).atZone(zone).toInstant().toEpochMilli()
                        val endMillis = selectedDate.atTime(e).atZone(zone).toInstant().toEpochMilli()

                        onSave(safeTitle, note, startMillis, endMillis)
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
    }

    @Composable
    private fun TimeAdjust(label: String, time: String, onMinus: () -> Unit, onPlus: () -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus) { Icon(Icons.Default.Remove, contentDescription = null) }
                Text(time, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onPlus) { Icon(Icons.Default.Add, contentDescription = null) }
            }
        }
    }
}