package com.example.travelbudgettracker.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: BudgetViewModel, navController: NavController) {
    val trips by viewModel.allTrips.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var tripName by remember { mutableStateOf("") }
    var selectedTripForDelete by remember { mutableStateOf<Trip?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AK'S Budget Tracker", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF6200EA),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Trip")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(trips) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .combinedClickable(
                            onClick = { navController.navigate("trip_detail/${trip.tripId}") },
                            onLongClick = { selectedTripForDelete = trip },
                            onLongClickLabel = "Delete trip"
                        ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = trip.tripName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = formatDate(trip.createdAt), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFF3E5F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Open", tint = Color(0xFF6200EA))
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Where to next?", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = tripName,
                        onValueChange = { tripName = it },
                        label = { Text("Trip Name (e.g., Manali Ride)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
                    ) { Text("Let's Go!") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel", color = Color.Gray) } }
            )
        }

        selectedTripForDelete?.let { trip ->
            AlertDialog(
                onDismissRequest = { selectedTripForDelete = null },
                title = { Text("Delete Trip", fontWeight = FontWeight.Bold) },
                text = { Text("Delete ${trip.tripName} and all its expenses?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTrip(trip)
                            selectedTripForDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
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
        topBar = {
            TopAppBar(
                title = { Text("Trip Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = { exportLauncher.launch("Trip_Expenses.csv") }) { Icon(Icons.Default.Share, "Export CSV") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showExpenseDialog = true },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Expense") },
                containerColor = Color(0xFF6200EA),
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- Premium Summary Dashboard ---
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Total Spent Card
                Box(
                    modifier = Modifier.weight(1f).background(
                        brush = Brush.linearGradient(colors = listOf(Color(0xFFE53935), Color(0xFFEF5350))),
                        shape = RoundedCornerShape(20.dp)
                    ).padding(16.dp)
                ) {
                    Column {
                        Text(headerText, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text("₹${formatAmt(displayTotal)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                    }
                }

                // Owed to You Card (CLICKABLE)
                Box(
                    modifier = Modifier.weight(1f).background(
                        brush = Brush.linearGradient(colors = listOf(Color(0xFF00BFA5), Color(0xFF1DE9B6))),
                        shape = RoundedCornerShape(20.dp)
                    ).clickable { showDebtsDialog = true }.padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("You are Owed", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Rounded.OpenInNew, contentDescription = "View", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
                        Text("₹${formatAmt(displayOwed)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                    }
                }
            }

            // --- Category Filters ---
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, fontWeight = if (selectedFilter == cat) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF6200EA), selectedLabelColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Text("Transactions", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // --- Expense List ---
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(filteredExpenses) { exp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .combinedClickable(
                                onClick = { selectedExpenseForDetails = exp },
                                onLongClick = { selectedExpenseForDelete = exp },
                                onLongClickLabel = "Delete expense"
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                            val icon = when(exp.category) {
                                "Food" -> Icons.Rounded.Restaurant
                                "Travel" -> Icons.Rounded.DirectionsCar
                                "Stay" -> Icons.Rounded.Hotel
                                else -> Icons.Rounded.ReceiptLong
                            }
                            Box(modifier = Modifier.size(48.dp).background(Color(0xFFE0E0E0), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = Color(0xFF424242), modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.expenseName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${exp.category} • ${formatDate(exp.date)}", fontSize = 12.sp, color = Color.Gray)

                                // Show pending splits summary
                                val pendingOwedForThis = exp.parsedSplitDebts().sumOf { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    if (paidDebts.contains(debtId)) 0.0 else debt.amount
                                }

                                if (pendingOwedForThis > 0) {
                                    Text("Pending Split: ₹${formatAmt(pendingOwedForThis)}", color = Color(0xFF00BFA5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else if (exp.splitAmountOwed > 0) {
                                    Text("Splits Settled!", color = Color(0xFF6200EA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${formatAmt(exp.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFD32F2F))
                                val imageCount = exp.receiptUri?.split("|")?.filter { it.isNotBlank() }?.size ?: 0
                                if (imageCount > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                        Text(" $imageCount", fontSize = 12.sp, color = Color.Gray)
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
                viewModel = viewModel,
                onDismiss = { selectedExpenseForDetails = null }
            )
        }

        selectedExpenseForDelete?.let { exp ->
            AlertDialog(
                onDismissRequest = { selectedExpenseForDelete = null },
                title = { Text("Delete Expense", fontWeight = FontWeight.Bold) },
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
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
        title = { Text("Who Owes You", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
        text = {
            if (debtsByPerson.isEmpty()) {
                Text("No one owes you money!", color = Color.Gray)
            } else {
                LazyColumn {
                    debtsByPerson.forEach { (person, debts) ->
                        val totalExpected = debts.sumOf { it.amount }
                        val totalPaid = debts.sumOf { if (paidDebts.contains("${it.expenseId}_${it.personName}")) it.amount else 0.0 }
                        val pending = totalExpected - totalPaid

                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(person, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF6200EA))
                                    Text(if (pending > 0) "Owes ₹${formatAmt(pending)}" else "Settled!", fontWeight = FontWeight.Bold, color = if (pending > 0) Color(0xFFE65100) else Color(0xFF00BFA5))
                                }

                                debts.forEach { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    val isPaid = paidDebts.contains(debtId)

                                    Row(modifier = Modifier.fillMaxWidth().clickable { onToggleDebt(debtId, !isPaid) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isPaid,
                                            onCheckedChange = { onToggleDebt(debtId, it) },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFA5))
                                        )
                                        Column {
                                            Text(debt.expenseName, fontSize = 16.sp, textDecoration = if (isPaid) TextDecoration.LineThrough else null, color = if (isPaid) Color.Gray else Color.Black)
                                            Text("₹${formatAmt(debt.amount)}", fontSize = 14.sp, color = if (isPaid) Color.Gray else Color(0xFFD32F2F), textDecoration = if (isPaid) TextDecoration.LineThrough else null)
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))) { Text("Done") }
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
        title = { Text("Record Expense", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(value = expName, onValueChange = { expName = it }, label = { Text("What did you pay for?") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expAmount,
                        onValueChange = { expAmount = it },
                        label = { Text("Total Bill Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = expAmount.isNotBlank() && (amountValue == null || amountValue <= 0.0),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Category", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    LazyRow(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Split with Friends", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { splits = splits + SplitEntry("", "") }) { Text("+ Add Person") }
                    }

                    splits.forEachIndexed { index, split ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = split.name,
                                onValueChange = { newName -> splits = splits.toMutableList().apply { this[index] = split.copy(name = newName) } },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = split.amount,
                                onValueChange = { newAmt -> splits = splits.toMutableList().apply { this[index] = split.copy(amount = newAmt) } },
                                label = { Text("₹ Owed") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = (split.name.isNotBlank() || split.amount.isNotBlank()) &&
                                        ((split.amount.toDoubleOrNull() ?: 0.0) <= 0.0 || splitExceedsAmount),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { splits = splits.toMutableList().apply { removeAt(index) } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
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
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5), contentColor = Color(0xFF6200EA))
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
            ) { Text("Save Expense") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailDialog(expense: Expense, paidDebts: Set<String>, onToggleDebt: (String, Boolean) -> Unit, viewModel: BudgetViewModel, onDismiss: () -> Unit) {
    val uris = expense.receiptUri?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
    val splitDebts = expense.parsedSplitDebts()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                IconButton(onClick = {
                    viewModel.deleteExpense(expense)
                    onDismiss()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Expense", tint = Color(0xFFD32F2F))
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(expense.expenseName, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${expense.category} • ${formatDate(expense.date)} • ${formatTime(expense.time)}", color = Color.Gray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Paid", color = Color(0xFFE65100), fontSize = 14.sp)
                            Text("₹${formatAmt(expense.amount)}", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFFE65100))
                        }
                    }

                    if (splitDebts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Split Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                splitDebts.forEach { debt ->
                                    val debtId = "${debt.expenseId}_${debt.personName}"
                                    val isPaid = paidDebts.contains(debtId)

                                    Row(modifier = Modifier.fillMaxWidth().clickable { onToggleDebt(debtId, !isPaid) }, verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isPaid,
                                            onCheckedChange = { onToggleDebt(debtId, it) },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BFA5))
                                        )
                                        Text(debt.personName, fontSize = 16.sp, color = if (isPaid) Color.Gray else Color(0xFF00695C), textDecoration = if (isPaid) TextDecoration.LineThrough else null)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("₹${formatAmt(debt.amount)}", fontWeight = FontWeight.Bold, color = if (isPaid) Color.Gray else Color(0xFF004D40), textDecoration = if (isPaid) TextDecoration.LineThrough else null)
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
                                    modifier = Modifier.height(250.dp).width(180.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))) { Text("Close") }
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
