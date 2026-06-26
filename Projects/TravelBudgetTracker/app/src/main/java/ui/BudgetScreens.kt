package com.example.travelbudgettracker.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.travelbudgettracker.data.Expense
import com.example.travelbudgettracker.data.Trip
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

private val ScreenBackground = Color(0xFFF7F8FC)
private val CardSurface = Color.White
private val Ink = Color(0xFF111827)
private val MutedInk = Color(0xFF687386)
private val Hairline = Color(0xFFE0E7F2)
private val BrandBlue = Color(0xFF3157D5)
private val BrandTeal = Color(0xFF00A896)
private val BrandOrange = Color(0xFFF97316)
private val BrandViolet = Color(0xFF6D5DF6)
private val DangerRed = Color(0xFFD32F2F)

private fun categoryAccent(category: String): Color = when (category) {
    "Food" -> BrandOrange
    "Travel" -> BrandBlue
    "Stay" -> BrandViolet
    else -> Color(0xFF64748B)
}

private fun categorySoft(category: String): Color = when (category) {
    "Food" -> Color(0xFFFFF3E6)
    "Travel" -> Color(0xFFEAF0FF)
    "Stay" -> Color(0xFFF0EDFF)
    else -> Color(0xFFEFF3F8)
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Food" -> Icons.Rounded.Restaurant
    "Travel" -> Icons.Rounded.DirectionsCar
    "Stay" -> Icons.Rounded.Hotel
    else -> Icons.AutoMirrored.Rounded.ReceiptLong
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = CircleShape,
            color = Color(0xFFEAF0FF),
            contentColor = BrandBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(message, color = MutedInk, fontSize = 13.sp)
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    brush: Brush,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Box(
        modifier = cardModifier
            .heightIn(min = 108.dp)
            .background(brush = brush, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    contentColor = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.White.copy(alpha = 0.84f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: BudgetViewModel, navController: NavController) {
    val trips by viewModel.allTrips.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var tripName by remember { mutableStateOf("") }
    var selectedTripForDelete by remember { mutableStateOf<Trip?>(null) }

    Scaffold(
        containerColor = ScreenBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Trip", fontWeight = FontWeight.Bold) },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 12.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 92.dp
            ),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 138.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF152E8E), BrandBlue, BrandTeal)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(formatDate(System.currentTimeMillis()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Trips, tabs, and totals", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("A cleaner place to track every ride, stay, meal, and split.", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                    }
                }
            }

            if (trips.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Add,
                        title = "No trips yet",
                        message = "Your next travel budget will land here."
                    )
                }
            }

            items(trips, key = { it.tripId }) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { navController.navigate("trip_detail/${trip.tripId}") },
                            onLongClick = { selectedTripForDelete = trip },
                            onLongClickLabel = "Delete trip"
                        ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEAF0FF),
                                contentColor = BrandBlue
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = trip.tripName, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(text = formatDate(trip.createdAt), fontSize = 12.sp, color = MutedInk, fontWeight = FontWeight.Medium)
                            }
                        }
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFFF2F5FA),
                            contentColor = BrandBlue
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.ChevronRight, contentDescription = "Open", modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(8.dp),
                containerColor = CardSurface,
                title = { Text("Where to next?", color = Ink, fontWeight = FontWeight.ExtraBold) },
                text = {
                    OutlinedTextField(
                        value = tripName,
                        onValueChange = { tripName = it },
                        label = { Text("Trip Name (e.g., Manali Ride)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tripName.isNotBlank()) {
                                viewModel.addTrip(tripName)
                                tripName = ""
                                showDialog = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) { Text("Let's Go!") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel", color = Color.Gray) } }
            )
        }

        selectedTripForDelete?.let { trip ->
            AlertDialog(
                onDismissRequest = { selectedTripForDelete = null },
                shape = RoundedCornerShape(8.dp),
                containerColor = CardSurface,
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed) },
                title = { Text("Delete Trip", color = Ink, fontWeight = FontWeight.ExtraBold) },
                text = { Text("Delete ${trip.tripName} and all its expenses?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTrip(trip)
                            selectedTripForDelete = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTripForDelete = null }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }
    }
}

// Data class to hold parsed debt information
data class DebtItem(val expenseId: Long, val expenseName: String, val personName: String, val amount: Double)

private const val SPLIT_ENTRY_SEPARATOR = " | "
private const val SPLIT_AMOUNT_SEPARATOR = ": ₹"

private fun Expense.parsedSplitDebts(): List<DebtItem> {
    var remainingAmount = amount.coerceAtLeast(0.0)

    return splitWithName
        ?.split(SPLIT_ENTRY_SEPARATOR)
        ?.mapNotNull { split ->
            if (remainingAmount <= 0.0) return@mapNotNull null

            val parts = split.split(SPLIT_AMOUNT_SEPARATOR, limit = 2)
            if (parts.size != 2) return@mapNotNull null

            val personName = parts[0].trim()
            val requestedAmount = parts[1].toDoubleOrNull()?.coerceAtLeast(0.0) ?: return@mapNotNull null
            if (personName.isBlank() || requestedAmount <= 0.0) return@mapNotNull null

            val cappedAmount = requestedAmount.coerceAtMost(remainingAmount)
            remainingAmount -= cappedAmount

            DebtItem(expenseId, expenseName, personName, cappedAmount)
        }
        ?: emptyList()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TripDetailScreen(tripId: Long, viewModel: BudgetViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val allExpenses by viewModel.getExpensesForTrip(tripId).collectAsState(initial = emptyList())

    // --- Paid Debts State (Uses SharedPreferences to avoid DB changes) ---
    val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
    var paidDebts by remember {
        val saved = prefs.getString("paid_debts_$tripId", "") ?: ""
        mutableStateOf(if (saved.isNotBlank()) saved.split(",").toSet() else emptySet<String>())
    }

    val toggleDebt: (String, Boolean) -> Unit = { debtId, isPaid ->
        val newSet = paidDebts.toMutableSet()
        if (isPaid) newSet.add(debtId) else newSet.remove(debtId)
        paidDebts = newSet
        prefs.edit().putString("paid_debts_$tripId", newSet.joinToString(",")).apply()
    }

    // --- Filtering Logic ---
    val categories = listOf("All", "Food", "Travel", "Stay", "Other")
    var selectedFilter by remember { mutableStateOf("All") }
    val filteredExpenses = if (selectedFilter == "All") allExpenses else allExpenses.filter { it.category == selectedFilter }

    // --- Dynamic Summaries ---
    val displayTotal = filteredExpenses.sumOf { it.amount }
    val headerText = if (selectedFilter == "All") "Total Spent" else "$selectedFilter Spent"

    // Parse all debts to calculate remaining active owed money
    val allParsedDebts = allExpenses.flatMap { it.parsedSplitDebts() }

    // Calculate dynamic owed based on what ISN'T in the paidDebts set
    val displayOwed = filteredExpenses.sumOf { exp ->
        exp.parsedSplitDebts().sumOf { debt ->
            val debtId = "${debt.expenseId}_${debt.personName}"
            if (paidDebts.contains(debtId)) 0.0 else debt.amount
        }
    }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showDebtsDialog by remember { mutableStateOf(false) }
    var selectedExpenseForDetails by remember { mutableStateOf<Expense?>(null) }
    var selectedExpenseForDelete by remember { mutableStateOf<Expense?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) exportToCsv(context, uri, allExpenses)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trip Dashboard", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                        Text("${filteredExpenses.size} transactions", color = MutedInk, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Ink) } },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("Trip_Expenses.csv") }) {
                        Icon(Icons.Default.Share, "Export CSV", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showExpenseDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Expense", fontWeight = FontWeight.Bold) },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = headerText,
                    value = "₹${formatAmt(displayTotal)}",
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    brush = Brush.linearGradient(listOf(Color(0xFFE53935), BrandOrange)),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "You are Owed",
                    value = "₹${formatAmt(displayOwed)}",
                    icon = Icons.AutoMirrored.Rounded.OpenInNew,
                    brush = Brush.linearGradient(listOf(BrandTeal, Color(0xFF18C6B5), BrandBlue)),
                    modifier = Modifier.weight(1f),
                    onClick = { showDebtsDialog = true }
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (selectedFilter == cat) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardSurface,
                            labelColor = MutedInk,
                            selectedContainerColor = BrandBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transactions", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Surface(shape = CircleShape, color = Color(0xFFEAF0FF), contentColor = BrandBlue) {
                    Text("${filteredExpenses.size}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredExpenses.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            title = "No transactions",
                            message = if (selectedFilter == "All") {
                                "Your spend will land here."
                            } else {
                                "Your ${selectedFilter.lowercase(Locale.getDefault())} spend will land here."
                            }
                        )
                    }
                }

                items(filteredExpenses, key = { it.expenseId }) { exp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { selectedExpenseForDetails = exp },
                                onLongClick = { selectedExpenseForDelete = exp },
                                onLongClickLabel = "Delete expense"
                            ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Hairline),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = categorySoft(exp.category),
                                contentColor = categoryAccent(exp.category)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(categoryIcon(exp.category), contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.expenseName, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${exp.category} • ${formatDate(exp.date)}", fontSize = 12.sp, color = MutedInk, fontWeight = FontWeight.Medium)

                                val pendingOwedForThis = exp.parsedSplitDebts().sumOf { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    if (paidDebts.contains(debtId)) 0.0 else debt.amount
                                }

                                if (pendingOwedForThis > 0) {
                                    Text("Pending Split: ₹${formatAmt(pendingOwedForThis)}", color = BrandTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else if (exp.splitAmountOwed > 0) {
                                    Text("Splits Settled!", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE), contentColor = DangerRed) {
                                    Text("₹${formatAmt(exp.amount)}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                }
                                val imageCount = exp.receiptUri?.split("|")?.filter { it.isNotBlank() }?.size ?: 0
                                if (imageCount > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp), tint = MutedInk)
                                        Text(" $imageCount", fontSize = 12.sp, color = MutedInk)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dialogs ---
        if (showExpenseDialog) {
            AddExpenseDialog(tripId = tripId, viewModel = viewModel, onDismiss = { showExpenseDialog = false })
        }

        selectedExpenseForDetails?.let { exp ->
            ExpenseDetailDialog(
                expense = exp,
                paidDebts = paidDebts,
                onToggleDebt = toggleDebt,
                onDeleteRequested = {
                    selectedExpenseForDetails = null
                    selectedExpenseForDelete = exp
                },
                onDismiss = { selectedExpenseForDetails = null }
            )
        }

        selectedExpenseForDelete?.let { exp ->
            AlertDialog(
                onDismissRequest = { selectedExpenseForDelete = null },
                shape = RoundedCornerShape(8.dp),
                containerColor = CardSurface,
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed) },
                title = { Text("Delete Expense", color = Ink, fontWeight = FontWeight.ExtraBold) },
                text = { Text("Delete ${exp.expenseName}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteExpense(exp)
                            if (selectedExpenseForDetails?.expenseId == exp.expenseId) {
                                selectedExpenseForDetails = null
                            }
                            selectedExpenseForDelete = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { selectedExpenseForDelete = null }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showDebtsDialog) {
            DebtsOverviewDialog(
                allDebts = allParsedDebts,
                paidDebts = paidDebts,
                onToggleDebt = toggleDebt,
                onDismiss = { showDebtsDialog = false }
            )
        }
    }
}

// --- Checkbox Dashboard for Debts ---
@Composable
fun DebtsOverviewDialog(
    allDebts: List<DebtItem>,
    paidDebts: Set<String>,
    onToggleDebt: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val debtsByPerson = allDebts.groupBy { it.personName }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = CardSurface,
        title = { Text("Who Owes You", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
        text = {
            if (debtsByPerson.isEmpty()) {
                Text("No one owes you money!", color = MutedInk)
            } else {
                LazyColumn {
                    debtsByPerson.forEach { (person, debts) ->
                        val totalExpected = debts.sumOf { it.amount }
                        val totalPaid = debts.sumOf { if (paidDebts.contains("${it.expenseId}_${it.personName}")) it.amount else 0.0 }
                        val pending = totalExpected - totalPaid

                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(person, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = BrandBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (pending > 0) Color(0xFFFFF3E6) else Color(0xFFE7F8F5),
                                        contentColor = if (pending > 0) BrandOrange else BrandTeal
                                    ) {
                                        Text(if (pending > 0) "Owes ₹${formatAmt(pending)}" else "Settled!", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                debts.forEach { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    val isPaid = paidDebts.contains(debtId)

                                    Row(modifier = Modifier.fillMaxWidth().clickable { onToggleDebt(debtId, !isPaid) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isPaid,
                                            onCheckedChange = { onToggleDebt(debtId, it) },
                                            colors = CheckboxDefaults.colors(checkedColor = BrandTeal)
                                        )
                                        Column {
                                            Text(debt.expenseName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textDecoration = if (isPaid) TextDecoration.LineThrough else null, color = if (isPaid) MutedInk else Ink)
                                            Text("₹${formatAmt(debt.amount)}", fontSize = 13.sp, color = if (isPaid) MutedInk else DangerRed, textDecoration = if (isPaid) TextDecoration.LineThrough else null)
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Hairline)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text("Done") }
        }
    )
}

// Data class for dynamic splits
data class SplitEntry(val name: String, val amount: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(tripId: Long, viewModel: BudgetViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var expName by remember { mutableStateOf("") }
    var expAmount by remember { mutableStateOf("") }

    val categories = listOf("Food", "Travel", "Stay", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var splits by remember { mutableStateOf(listOf<SplitEntry>()) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)) { uris ->
        selectedUris = uris
    }

    val amountValue = expAmount.toDoubleOrNull()
    val validSplits = splits.mapNotNull { split ->
        val name = split.name.trim()
        val owedAmount = split.amount.toDoubleOrNull()

        if (name.isNotBlank() && owedAmount != null && owedAmount > 0.0) {
            name to owedAmount
        } else {
            null
        }
    }
    val totalOwed = validSplits.sumOf { it.second }
    val hasIncompleteSplit = splits.any { split ->
        val hasSplitInput = split.name.isNotBlank() || split.amount.isNotBlank()
        val owedAmount = split.amount.toDoubleOrNull()

        hasSplitInput && (split.name.isBlank() || owedAmount == null || owedAmount <= 0.0)
    }
    val splitExceedsAmount = amountValue != null && amountValue > 0.0 && totalOwed > amountValue
    val saveEnabled = expName.isNotBlank() &&
            amountValue != null &&
            amountValue > 0.0 &&
            !hasIncompleteSplit &&
            !splitExceedsAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = CardSurface,
        title = { Text("Record Expense", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = expName,
                        onValueChange = { expName = it },
                        label = { Text("What did you pay for?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expAmount,
                        onValueChange = { expAmount = it },
                        label = { Text("Total Bill Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = expAmount.isNotBlank() && (amountValue == null || amountValue <= 0.0),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MutedInk)
                    LazyRow(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(categoryIcon(cat), contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF2F5FA),
                                    labelColor = MutedInk,
                                    iconColor = MutedInk,
                                    selectedContainerColor = categoryAccent(cat),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Split with Friends", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        TextButton(onClick = { splits = splits + SplitEntry("", "") }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Person")
                        }
                    }

                    splits.forEachIndexed { index, split ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = split.name,
                                onValueChange = { newName -> splits = splits.toMutableList().apply { this[index] = split.copy(name = newName) } },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = split.amount,
                                onValueChange = { newAmt -> splits = splits.toMutableList().apply { this[index] = split.copy(amount = newAmt) } },
                                label = { Text("₹ Owed") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = (split.name.isNotBlank() || split.amount.isNotBlank()) &&
                                        ((split.amount.toDoubleOrNull() ?: 0.0) <= 0.0 || splitExceedsAmount),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            IconButton(onClick = { splits = splits.toMutableList().apply { removeAt(index) } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = DangerRed)
                            }
                        }
                    }

                    val splitStatusText = when {
                        hasIncompleteSplit -> "Enter a name and valid amount for each split."
                        splitExceedsAmount -> "Split total ₹${formatAmt(totalOwed)} cannot be more than bill ₹${formatAmt(amountValue ?: 0.0)}."
                        validSplits.isNotEmpty() -> "Split total: ₹${formatAmt(totalOwed)}"
                        else -> null
                    }
                    if (splitStatusText != null) {
                        Text(
                            text = splitStatusText,
                            color = if (hasIncompleteSplit || splitExceedsAmount) MaterialTheme.colorScheme.error else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (hasIncompleteSplit || splitExceedsAmount) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF0FF), contentColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedUris.isEmpty()) "Attach Screenshots (Max 5)" else "${selectedUris.size} Images Attached")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountValue ?: 0.0
                    if (saveEnabled) {
                        val formattedSplitDetails = validSplits
                            .joinToString(SPLIT_ENTRY_SEPARATOR) { "${it.first}: ₹${it.second}" }
                        val internalImagePaths = saveImagesToInternalStorage(context, selectedUris)

                        viewModel.addExpense(
                            tripId = tripId,
                            name = expName,
                            amount = amount,
                            category = selectedCategory,
                            splitWith = formattedSplitDetails.takeIf { it.isNotEmpty() },
                            splitOwed = totalOwed.coerceAtMost(amount),
                            receiptUri = internalImagePaths
                        )
                        onDismiss()
                    }
                },
                enabled = saveEnabled,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save Expense") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailDialog(expense: Expense, paidDebts: Set<String>, onToggleDebt: (String, Boolean) -> Unit, onDeleteRequested: () -> Unit, onDismiss: () -> Unit) {
    val uris = expense.receiptUri?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
    val splitDebts = expense.parsedSplitDebts()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        shape = RoundedCornerShape(8.dp),
        containerColor = CardSurface,
        modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Details", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                IconButton(onClick = onDeleteRequested) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Expense", tint = DangerRed)
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(expense.expenseName, color = Ink, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${expense.category} • ${formatDate(expense.date)} • ${formatTime(expense.time)}", color = MutedInk, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E6)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD9B3))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Paid", color = BrandOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("₹${formatAmt(expense.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = BrandOrange)
                        }
                    }

                    if (splitDebts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Split Breakdown", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F8F5)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFEDE7))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                splitDebts.forEach { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    val isPaid = paidDebts.contains(debtId)

                                    Row(modifier = Modifier.fillMaxWidth().clickable { onToggleDebt(debtId, !isPaid) }, verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isPaid,
                                            onCheckedChange = { onToggleDebt(debtId, it) },
                                            colors = CheckboxDefaults.colors(checkedColor = BrandTeal)
                                        )
                                        Text(debt.personName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (isPaid) MutedInk else Color(0xFF00695C), textDecoration = if (isPaid) TextDecoration.LineThrough else null)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("₹${formatAmt(debt.amount)}", fontWeight = FontWeight.ExtraBold, color = if (isPaid) MutedInk else Color(0xFF004D40), textDecoration = if (isPaid) TextDecoration.LineThrough else null)
                                    }
                                }
                            }
                        }
                    }

                    if (uris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Attached Receipts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uris) { uriString ->
                                AsyncImage(
                                    model = uriString,
                                    contentDescription = "Receipt",
                                    modifier = Modifier.height(250.dp).width(180.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text("Close") }
        }
    )
}

// --- Utility Functions ---

fun formatAmt(amt: Double): String {
    return if (amt % 1.0 == 0.0) "%.0f".format(amt) else "%.2f".format(amt)
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun formatTime(millis: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun saveImagesToInternalStorage(context: Context, uris: List<Uri>): String? {
    if (uris.isEmpty()) return null
    val savedPaths = uris.mapNotNull { uri ->
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
            val fileName = "receipt_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    return savedPaths.joinToString("|").takeIf { it.isNotEmpty() }
}

fun exportToCsv(context: Context, uri: Uri, expenses: List<Expense>) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = outputStream.bufferedWriter()
            writer.write("Expense Name,Amount (Rs),Date,Category,Split Details,Total Owed\n")
            expenses.forEach { exp ->
                val splitDebts = exp.parsedSplitDebts()
                val cleanSplit = splitDebts
                    .joinToString(SPLIT_ENTRY_SEPARATOR) { "${it.personName}: ₹${formatAmt(it.amount)}" }
                    .replace(",", ";")
                val cappedOwed = splitDebts.sumOf { it.amount }

                writer.write("${exp.expenseName},${exp.amount},${formatDate(exp.date)},${exp.category},$cleanSplit,$cappedOwed\n")
            }
            writer.flush()
        }
        Toast.makeText(context, "Exported Successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show()
    }
}
