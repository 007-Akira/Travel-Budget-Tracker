package com.example.travelbudgettracker.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelbudgettracker.ui.theme.Danger
import com.example.travelbudgettracker.ui.theme.Gold
import com.example.travelbudgettracker.ui.theme.GoldLight
import com.example.travelbudgettracker.ui.theme.Success
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.travelbudgettracker.data.Expense
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private data class SplitInput(
    val name: String = "",
    val amountText: String = ""
)

private data class SplitPerson(
    val name: String,
    val amount: Double,
    val paid: Boolean = false
)

private data class CsvExpenseImport(
    val name: String,
    val amount: Double,
    val category: String,
    val splitPeople: List<SplitPerson>
)

private data class OwedSplitEntry(
    val expense: Expense,
    val splitIndex: Int,
    val person: SplitPerson
)

@Composable
private fun TinyCircleButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    AnimatedPressBox(
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.45f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = color,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TripDetailScreen(
    tripId: Long,
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val expenses by viewModel.getExpensesForTrip(tripId)
        .collectAsState(initial = emptyList())

    val totalSpent by viewModel.getTotalSpent(tripId)
        .collectAsState(initial = 0.0)

    val totalOwedToUser by viewModel.getTotalOwedToUser(tripId)
        .collectAsState(initial = 0.0)

    val normalExpenses = expenses.filter {
        it.entryType != "YOU_OWE"
    }

    val debtsYouOwe = expenses.filter {
        it.entryType == "YOU_OWE"
    }

    val filterCategories = listOf(
        "All",
        "Food",
        "Travel",
        "Stay",
        "Shopping",
        "Other"
    )

    var selectedCategoryFilter by remember {
        mutableStateOf("All")
    }

    fun matchesSelectedCategory(expense: Expense): Boolean {
        return selectedCategoryFilter == "All" ||
            expense.category.equals(selectedCategoryFilter, ignoreCase = true)
    }

    val filteredNormalExpenses = normalExpenses.filter {
        matchesSelectedCategory(it)
    }

    val filteredDebtsYouOwe = debtsYouOwe.filter {
        matchesSelectedCategory(it)
    }

    val owedSplitEntries = filteredNormalExpenses.flatMap { expense ->
        splitPeopleFor(expense).mapIndexed { index, splitPerson ->
            OwedSplitEntry(
                expense = expense,
                splitIndex = index,
                person = splitPerson
            )
        }
    }

    val paidBackAmount = owedSplitEntries
        .filter { it.person.paid }
        .sumOf { it.person.amount }

    val filteredGrossSpent = filteredNormalExpenses.sumOf {
        it.amount
    } + filteredDebtsYouOwe.sumOf {
        if (it.isDebtPaid) it.owedAmount else 0.0
    }

    val netTotalSpent = (filteredGrossSpent - paidBackAmount).coerceAtLeast(0.0)
    val filteredOwedToUser = owedSplitEntries
        .filterNot { it.person.paid }
        .sumOf { it.person.amount }
    val filteredYouOwe = filteredDebtsYouOwe.sumOf {
        if (it.isDebtPaid) 0.0 else it.owedAmount
    }

    val pagerState = rememberPagerState(
        pageCount = {
            3
        }
    )
    val coroutineScope = rememberCoroutineScope()
    val pageTitles = listOf("Expenses", "Debts", "Owed to me")

    var showAddExpenseDialog by remember {
        mutableStateOf(false)
    }

    var showAddDebtDialog by remember {
        mutableStateOf(false)
    }

    var selectedExpense by remember {
        mutableStateOf<Expense?>(null)
    }

    var expensePendingDelete by remember {
        mutableStateOf<Expense?>(null)
    }

    var visible by remember {
        mutableStateOf(false)
    }

    var pendingCsvExport by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    fun updateSplitPeople(expense: Expense, updatedSplitPeople: List<SplitPerson>) {
        val updatedJson = encodeSplitPeople(updatedSplitPeople)
        val updatedOwed = updatedSplitPeople
            .filterNot { it.paid }
            .sumOf { it.amount }

        viewModel.updateSplitState(
            expenseId = expense.expenseId,
            splitDetailsJson = updatedJson,
            splitAmountOwed = updatedOwed
        )

        if (selectedExpense?.expenseId == expense.expenseId) {
            selectedExpense = expense.copy(
                splitDetailsJson = updatedJson,
                splitAmountOwed = updatedOwed
            )
        }
    }

    val csvImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = runCatching {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val csvText = BufferedReader(InputStreamReader(inputStream)).readText()
                    parseCsvExpenses(csvText)
                } ?: emptyList()
            }

            result.onSuccess { importedExpenses ->
                importedExpenses.forEach { importedExpense ->
                    val splitTotal = importedExpense.splitPeople.sumOf { it.amount }

                    viewModel.addExpense(
                        tripId = tripId,
                        name = importedExpense.name,
                        amount = importedExpense.amount,
                        category = importedExpense.category,
                        splitWith = importedExpense.splitPeople.firstOrNull()?.name,
                        splitOwed = splitTotal,
                        receiptUri = null,
                        splitDetailsJson = encodeSplitPeople(importedExpense.splitPeople),
                        receiptUrisJson = null
                    )
                }

                Toast.makeText(
                    context,
                    "Imported ${importedExpenses.size} expense${if (importedExpenses.size == 1) "" else "s"}",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                Toast.makeText(
                    context,
                    "Could not import this CSV",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val csvExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val result = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(pendingCsvExport)
                    }
                }
            }

            Toast.makeText(
                context,
                if (result.isSuccess) "CSV exported" else "Could not export CSV",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    PremiumScreenBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PremiumTopBar(
                    title = "Trip Dashboard",
                    subtitle = "Expenses, splits and debts",
                    onBack = onBack,
                    actionText = "📤",
                    onActionClick = {
                        pendingCsvExport = buildCsvExport(
                            expenses = filteredNormalExpenses,
                            debts = filteredDebtsYouOwe
                        )

                        val filterName = selectedCategoryFilter
                            .lowercase()
                            .replace(" ", "_")

                        csvExporter.launch("travel_budget_$filterName.csv")
                    }
                )

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = PremiumDimens.ScreenPadding,
                                end = PremiumDimens.ScreenPadding,
                                top = 10.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PremiumAmountBlock(
                            label = if (selectedCategoryFilter == "All") {
                                "Total Spent"
                            } else {
                                "$selectedCategoryFilter Spent"
                            },
                            amount = formatCurrency(netTotalSpent),
                            emoji = "💳",
                            highlighted = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompactStatCard(
                                title = "Owed to you",
                                value = formatCurrency(filteredOwedToUser),
                                emoji = "↙",
                                valueColor = GoldLight,
                                modifier = Modifier.weight(1f)
                            )

                            CompactStatCard(
                                title = "You owe",
                                value = formatCurrency(filteredYouOwe),
                                emoji = "↗",
                                valueColor = Danger,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumButton(
                                text = "Add Expense",
                                onClick = {
                                    showAddExpenseDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            )

                            PremiumOutlineButton(
                                text = "Add Debt",
                                onClick = {
                                    showAddDebtDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        CategoryChipRow(
                            categories = filterCategories,
                            selectedCategory = selectedCategoryFilter,
                            onCategorySelected = {
                                selectedCategoryFilter = it
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pageTitles.forEachIndexed { index, title ->
                                PremiumChip(
                                    text = title,
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            when (page) {
                                0 -> ExpensesPage(
                                    expenses = filteredNormalExpenses,
                                    onExpenseClick = {
                                        selectedExpense = it
                                    },
                                    onExpenseLongClick = {
                                        expensePendingDelete = it
                                    }
                                )

                                1 -> DebtsPage(
                                    debts = filteredDebtsYouOwe,
                                    onDebtClick = {
                                        selectedExpense = it
                                    },
                                    onDebtLongClick = {
                                        expensePendingDelete = it
                                    },
                                    onTogglePaid = {
                                        viewModel.toggleDebtPaid(it)
                                    }
                                )

                                2 -> OwedToMePage(
                                    owedEntries = owedSplitEntries,
                                    onExpenseClick = {
                                        selectedExpense = it
                                    },
                                    onTogglePaid = { entry ->
                                        val updatedSplitPeople = splitPeopleFor(entry.expense).toMutableList()
                                        val currentPerson = updatedSplitPeople.getOrNull(entry.splitIndex)

                                        if (currentPerson != null) {
                                            updatedSplitPeople[entry.splitIndex] = currentPerson.copy(
                                                paid = !currentPerson.paid
                                            )
                                            updateSplitPeople(entry.expense, updatedSplitPeople)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = {
                showAddExpenseDialog = false
            },
            onSaveExpense = {
                name,
                amount,
                category,
                splitWith,
                splitAmount,
                receiptUri,
                splitDetailsJson,
                receiptUrisJson ->
                viewModel.addExpense(
                    tripId = tripId,
                    name = name,
                    amount = amount,
                    category = category,
                    splitWith = splitWith,
                    splitOwed = splitAmount,
                    receiptUri = receiptUri,
                    splitDetailsJson = splitDetailsJson,
                    receiptUrisJson = receiptUrisJson
                )

                showAddExpenseDialog = false
            }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = {
                showAddDebtDialog = false
            },
            onSaveDebt = { personName, amount, category ->
                viewModel.addDebt(
                    tripId = tripId,
                    personName = personName,
                    amount = amount,
                    category = category
                )

                showAddDebtDialog = false
            }
        )
    }

    selectedExpense?.let { expense ->
        ExpenseDetailsDialog(
            expense = expense,
            onSplitPaidChange = { updatedSplitPeople ->
                updateSplitPeople(expense, updatedSplitPeople)
            },
            onDismiss = {
                selectedExpense = null
            }
        )
    }

    expensePendingDelete?.let { expense ->
        DeleteConfirmDialog(
            title = if (expense.entryType == "YOU_OWE") "Delete debt?" else "Delete expense?",
            message = if (expense.entryType == "YOU_OWE") {
                "This will delete the debt for ${expense.owedPersonName ?: "this person"}."
            } else {
                "This will delete ${expense.expenseName}."
            },
            onDismiss = {
                expensePendingDelete = null
            },
            onConfirm = {
                viewModel.deleteExpense(expense)
                expensePendingDelete = null
            }
        )
    }
}

@Composable
private fun ExpensesPage(
    expenses: List<Expense>,
    onExpenseClick: (Expense) -> Unit,
    onExpenseLongClick: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionTitle(
                title = "Expenses",
                subtitle = if (expenses.isEmpty()) {
                    "No expenses added yet."
                } else {
                    "${expenses.size} transaction${if (expenses.size == 1) "" else "s"}"
                }
            )
        }

        if (expenses.isEmpty()) {
            item {
                EmptyState(
                    title = "No expenses yet",
                    message = "Add food, travel, stay, shopping or any trip expense with split details.",
                    emoji = "🧾"
                )
            }
        } else {
            itemsIndexed(
                items = expenses,
                key = { _, expense -> expense.expenseId }
            ) { index, expense ->
                AnimatedExpenseItem(
                    expense = expense,
                    index = index,
                    onClick = {
                        onExpenseClick(expense)
                    },
                    onLongClick = {
                        onExpenseLongClick(expense)
                    }
                )
            }
        }
    }
}

@Composable
private fun DebtsPage(
    debts: List<Expense>,
    onDebtClick: (Expense) -> Unit,
    onDebtLongClick: (Expense) -> Unit,
    onTogglePaid: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionTitle(
                title = "Debts",
                subtitle = "Money you owe others"
            )
        }

        if (debts.isEmpty()) {
            item {
                EmptyState(
                    title = "No debts",
                    message = "Debts you owe will appear here.",
                    emoji = "↗"
                )
            }
        } else {
            itemsIndexed(
                items = debts,
                key = { _, expense -> expense.expenseId }
            ) { index, debt ->
                AnimatedDebtItem(
                    expense = debt,
                    index = index,
                    onTogglePaid = {
                        onTogglePaid(debt)
                    },
                    onClick = {
                        onDebtClick(debt)
                    },
                    onLongClick = {
                        onDebtLongClick(debt)
                    }
                )
            }
        }
    }
}

@Composable
private fun OwedToMePage(
    owedEntries: List<OwedSplitEntry>,
    onExpenseClick: (Expense) -> Unit,
    onTogglePaid: (OwedSplitEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionTitle(
                title = "Owed to me",
                subtitle = if (owedEntries.isEmpty()) {
                    "No split payments yet."
                } else {
                    "${owedEntries.count { !it.person.paid }} unpaid"
                }
            )
        }

        if (owedEntries.isEmpty()) {
            item {
                EmptyState(
                    title = "No one owes you",
                    message = "People from split expenses will appear here.",
                    emoji = "↙"
                )
            }
        } else {
            itemsIndexed(
                items = owedEntries,
                key = { _, entry -> "${entry.expense.expenseId}-${entry.splitIndex}" }
            ) { _, entry ->
                OwedPersonRow(
                    entry = entry,
                    onClick = {
                        onExpenseClick(entry.expense)
                    },
                    onTogglePaid = {
                        onTogglePaid(entry)
                    }
                )
            }
        }
    }
}

@Composable
private fun OwedPersonRow(
    entry: OwedSplitEntry,
    onClick: () -> Unit,
    onTogglePaid: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PremiumCard(
            modifier = Modifier.weight(1f),
            cornerRadius = 28.dp,
            contentPadding = PaddingValues(16.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.person.name,
                        style = PremiumText.CardTitle,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "${entry.expense.expenseName} · ${if (entry.person.paid) "Paid" else "Unpaid"}",
                        style = PremiumText.Small,
                        maxLines = 1
                    )
                }

                Text(
                    text = formatCurrency(entry.person.amount),
                    color = if (entry.person.paid) Success else GoldLight,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }
        }

        TinyCircleButton(
            text = if (entry.person.paid) "✓" else "○",
            color = if (entry.person.paid) Success else Gold,
            onClick = onTogglePaid
        )
    }
}

@Composable
private fun AnimatedExpenseItem(
    expense: Expense,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(55L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(420))
    ) {
        ExpenseCard(
            title = expense.expenseName,
            amount = formatCurrency(expense.amount),
            category = expense.category,
            dateText = formatShortDate(expense.date),
            splitText = buildSplitText(expense),
            receiptAvailable = receiptUrisFor(expense).isNotEmpty(),
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

@Composable
private fun AnimatedDebtItem(
    expense: Expense,
    index: Int,
    onTogglePaid: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(55L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(420))
    ) {
        DebtCard(
            personName = expense.owedPersonName ?: "Unknown person",
            amount = formatCurrency(expense.owedAmount),
            category = expense.category,
            paid = expense.isDebtPaid,
            youOwe = true,
            onTogglePaid = onTogglePaid,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSaveExpense: (
        name: String,
        amount: Double,
        category: String,
        splitWith: String?,
        splitAmount: Double,
        receiptUri: String?,
        splitDetailsJson: String?,
        receiptUrisJson: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val categories = listOf(
        "Food",
        "Travel",
        "Stay",
        "Shopping",
        "Other"
    )

    var name by remember {
        mutableStateOf("")
    }

    var amountText by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("Food")
    }

    val splitPeople = remember {
        mutableStateListOf(SplitInput())
    }

    val receiptUris = remember {
        mutableStateListOf<String>()
    }

    var selectedReceiptUri by remember {
        mutableStateOf<String?>(null)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val uriText = uri.toString()
            if (receiptUris.size < 3 && uriText !in receiptUris) {
                receiptUris.add(uriText)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            PremiumDialogContainer(
                title = "Add Expense",
                subtitle = "Record what you paid for and optionally split it.",
                onClose = onDismiss
            ) {
                PremiumTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = "What did you pay for?",
                    placeholder = "Dinner, taxi, hotel room...",
                    singleLine = false,
                    leadingIcon = {
                        Text(text = "🧾")
                    },
                    isError = error != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                PremiumTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = "Amount",
                    placeholder = "1200",
                    leadingIcon = {
                        Text(text = "₹")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = error != null
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Category",
                    style = PremiumText.Label
                )

                Spacer(modifier = Modifier.height(10.dp))

                CategoryChipRow(
                    categories = categories,
                    selectedCategory = category,
                    onCategorySelected = {
                        category = it
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Split with",
                        style = PremiumText.Label
                    )

                    GoldMiniButton(
                        text = "+",
                        onClick = {
                            splitPeople.add(SplitInput())
                        }
                    )
                }

                splitPeople.forEachIndexed { index, splitInput ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumTextField(
                            value = splitInput.name,
                            onValueChange = {
                                splitPeople[index] = splitInput.copy(name = it)
                                error = null
                            },
                            label = "Person",
                            placeholder = "Name",
                            leadingIcon = {
                                Text(text = "👤")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PremiumTextField(
                            value = splitInput.amountText,
                            onValueChange = {
                                splitPeople[index] = splitInput.copy(amountText = it)
                                error = null
                            },
                            label = "Amount",
                            placeholder = "400",
                            leadingIcon = {
                                Text(text = "₹")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (splitPeople.size > 1) {
                            TinyCircleButton(
                                text = "×",
                                color = Danger,
                                onClick = {
                                    splitPeople.removeAt(index)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (receiptUris.size < 3) {
                    PremiumOutlineButton(
                        text = "Attach Photo (${receiptUris.size}/3)",
                        onClick = {
                            receiptPicker.launch(arrayOf("image/*"))
                        }
                    )
                }

                receiptUris.forEachIndexed { index, uri ->
                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = uri,
                        contentDescription = "Attached receipt ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable {
                                selectedReceiptUri = uri
                            },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PremiumOutlineButton(
                        text = "Remove Photo",
                        onClick = {
                            receiptUris.removeAt(index)
                        }
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumWarningText(
                        text = error ?: ""
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PremiumButton(
                    text = "Save Expense",
                    onClick = {
                        val cleanName = name.trim()
                        val amount = amountText.toDoubleOrNull()
                        val parsedSplitPeople = mutableListOf<SplitPerson>()
                        var splitValidationError: String? = null

                        splitPeople.forEach { splitInput ->
                            val personName = splitInput.name.trim()
                            val personAmountText = splitInput.amountText.trim()

                            if (personName.isBlank() && personAmountText.isBlank()) {
                                return@forEach
                            }

                            val personAmount = personAmountText.toDoubleOrNull()

                            when {
                                personName.isBlank() -> {
                                    splitValidationError = "Enter a name for every split person."
                                }

                                personAmount == null || personAmount <= 0.0 -> {
                                    splitValidationError = "Enter a valid split amount for $personName."
                                }

                                else -> {
                                    parsedSplitPeople.add(
                                        SplitPerson(
                                            name = personName,
                                            amount = personAmount
                                        )
                                    )
                                }
                            }
                        }

                        val splitAmount = parsedSplitPeople.sumOf { it.amount }
                        val cleanSplitWith = parsedSplitPeople.firstOrNull()?.name

                        when {
                            cleanName.isBlank() -> {
                                error = "Expense name cannot be empty."
                            }

                            amount == null || amount <= 0.0 -> {
                                error = "Enter a valid amount."
                            }

                            splitValidationError != null -> {
                                error = splitValidationError
                            }

                            splitAmount > amount -> {
                                error = "Total split amount cannot be greater than the expense amount."
                            }

                            else -> {
                                onSaveExpense(
                                    cleanName,
                                    amount,
                                    category,
                                    cleanSplitWith,
                                    splitAmount,
                                    receiptUris.firstOrNull(),
                                    encodeSplitPeople(parsedSplitPeople),
                                    encodeReceiptUris(receiptUris)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PremiumOutlineButton(
                    text = "Cancel",
                    onClick = onDismiss
                )
            }
        }
    }

    selectedReceiptUri?.let { receiptUri ->
        ReceiptImageDialog(
            receiptUri = receiptUri,
            onDismiss = {
                selectedReceiptUri = null
            }
        )
    }
}

@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSaveDebt: (
        personName: String,
        amount: Double,
        category: String
    ) -> Unit
) {
    val categories = listOf(
        "Food",
        "Travel",
        "Stay",
        "Shopping"
    )

    var personName by remember {
        mutableStateOf("")
    }

    var amountText by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("Food")
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            PremiumDialogContainer(
                title = "Add Debt",
                subtitle = "Track money you owe someone else.",
                onClose = onDismiss
            ) {
                PremiumTextField(
                    value = personName,
                    onValueChange = {
                        personName = it
                        error = null
                    },
                    label = "Person Name",
                    placeholder = "Rahul, Amal, Sneha...",
                    leadingIcon = {
                        Text(text = "👤")
                    },
                    isError = error != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                PremiumTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = "Amount you owe",
                    placeholder = "800",
                    leadingIcon = {
                        Text(text = "₹")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = error != null
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Category",
                    style = PremiumText.Label
                )

                Spacer(modifier = Modifier.height(10.dp))

                CategoryChipRow(
                    categories = categories,
                    selectedCategory = category,
                    onCategorySelected = {
                        category = it
                    }
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumWarningText(
                        text = error ?: ""
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PremiumButton(
                    text = "Save Debt",
                    onClick = {
                        val cleanName = personName.trim()
                        val amount = amountText.toDoubleOrNull()

                        when {
                            cleanName.isBlank() -> {
                                error = "Person name cannot be empty."
                            }

                            amount == null || amount <= 0.0 -> {
                                error = "Enter a valid amount."
                            }

                            else -> {
                                onSaveDebt(
                                    cleanName,
                                    amount,
                                    category
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PremiumOutlineButton(
                    text = "Cancel",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ExpenseDetailsDialog(
    expense: Expense,
    onSplitPaidChange: (List<SplitPerson>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReceiptUri by remember {
        mutableStateOf<String?>(null)
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            PremiumDialogContainer(
                title = if (expense.entryType == "YOU_OWE") "Debt Details" else "Expense Details",
                subtitle = expense.expenseName,
                onClose = onDismiss
            ) {
                if (expense.entryType == "YOU_OWE") {
                    PremiumInfoRow(
                        label = "Person",
                        value = expense.owedPersonName ?: "Unknown"
                    )

                    PremiumInfoRow(
                        label = "Amount",
                        value = formatCurrency(expense.owedAmount)
                    )

                    PremiumInfoRow(
                        label = "Status",
                        value = if (expense.isDebtPaid) "Paid" else "Unpaid"
                    )
                } else {
                    PremiumInfoRow(
                        label = "Amount",
                        value = formatCurrency(expense.amount)
                    )

                    val splitPeople = splitPeopleFor(expense)
                    if (splitPeople.isNotEmpty()) {
                        splitPeople.forEachIndexed { index, splitPerson ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PremiumInfoRow(
                                    label = splitPerson.name,
                                    value = "${formatCurrency(splitPerson.amount)} · ${if (splitPerson.paid) "Paid" else "Unpaid"}",
                                    modifier = Modifier.weight(1f)
                                )

                                TinyCircleButton(
                                    text = if (splitPerson.paid) "✓" else "○",
                                    color = if (splitPerson.paid) Success else Gold,
                                    onClick = {
                                        val updatedSplitPeople = splitPeople.toMutableList()
                                        updatedSplitPeople[index] = splitPerson.copy(
                                            paid = !splitPerson.paid
                                        )
                                        onSplitPaidChange(updatedSplitPeople)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                PremiumInfoRow(
                    label = "Category",
                    value = expense.category
                )

                PremiumInfoRow(
                    label = "Date",
                    value = formatFullDate(expense.date)
                )

                val receiptUris = receiptUrisFor(expense)
                if (receiptUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    ReceiptPreviewCard(
                        imageLabel = "${receiptUris.size} photo${if (receiptUris.size == 1) "" else "s"} attached",
                        onClick = {
                            selectedReceiptUri = receiptUris.first()
                        }
                    )

                    receiptUris.forEachIndexed { index, receiptUri ->
                        Spacer(modifier = Modifier.height(10.dp))

                        AsyncImage(
                            model = receiptUri,
                            contentDescription = "Attached receipt ${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable {
                                    selectedReceiptUri = receiptUri
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                } else if (expense.entryType != "YOU_OWE") {
                    PremiumInfoRow(
                        label = "Receipt",
                        value = "No attachment"
                    )
                }
            }
        }
    }

    selectedReceiptUri?.let { receiptUri ->
        ReceiptImageDialog(
            receiptUri = receiptUri,
            onDismiss = {
                selectedReceiptUri = null
            }
        )
    }
}

@Composable
private fun ReceiptImageDialog(
    receiptUri: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = receiptUri,
                contentDescription = "Full receipt photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun buildSplitText(
    expense: Expense
): String? {
    val splitPeople = splitPeopleFor(expense)

    if (splitPeople.isNotEmpty()) {
        val unpaidSplitPeople = splitPeople.filterNot { it.paid }
        val total = unpaidSplitPeople.sumOf { it.amount }

        return when {
            unpaidSplitPeople.isEmpty() -> "Split settled"
            unpaidSplitPeople.size == 1 -> {
                "${unpaidSplitPeople.first().name} owes ${formatCurrency(total)}"
            }
            else -> "${unpaidSplitPeople.size} people owe ${formatCurrency(total)}"
        }
    }

    val name = expense.splitWithName
    val amount = expense.splitAmountOwed

    return if (!name.isNullOrBlank() && amount > 0.0) {
        "$name owes ${formatCurrency(amount)}"
    } else {
        null
    }
}

private fun splitPeopleFor(
    expense: Expense
): List<SplitPerson> {
    val json = expense.splitDetailsJson

    if (!json.isNullOrBlank()) {
        return try {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val name = item.optString("name").trim()
                    val amount = item.optDouble("amount", 0.0)
                    val paid = item.optBoolean("paid", false)

                    if (name.isNotBlank() && amount > 0.0) {
                        add(SplitPerson(name = name, amount = amount, paid = paid))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val name = expense.splitWithName
    val amount = expense.splitAmountOwed

    return if (!name.isNullOrBlank() && amount > 0.0) {
        listOf(SplitPerson(name = name, amount = amount, paid = false))
    } else {
        emptyList()
    }
}

private fun receiptUrisFor(
    expense: Expense
): List<String> {
    val json = expense.receiptUrisJson

    if (!json.isNullOrBlank()) {
        return try {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val uri = array.optString(index).trim()

                    if (uri.isNotBlank()) {
                        add(uri)
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    return if (!expense.receiptUri.isNullOrBlank()) {
        listOf(expense.receiptUri)
    } else {
        emptyList()
    }
}

private fun encodeSplitPeople(
    splitPeople: List<SplitPerson>
): String? {
    if (splitPeople.isEmpty()) {
        return null
    }

    val array = JSONArray()
    splitPeople.forEach { splitPerson ->
        array.put(
            JSONObject()
                .put("name", splitPerson.name)
                .put("amount", splitPerson.amount)
                .put("paid", splitPerson.paid)
        )
    }

    return array.toString()
}

private fun encodeReceiptUris(
    receiptUris: List<String>
): String? {
    val cleanUris = receiptUris
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(3)

    if (cleanUris.isEmpty()) {
        return null
    }

    val array = JSONArray()
    cleanUris.forEach { uri ->
        array.put(uri)
    }

    return array.toString()
}

private fun buildCsvExport(
    expenses: List<Expense>,
    debts: List<Expense>
): String {
    val rows = mutableListOf<List<String>>()

    rows.add(
        listOf(
            "type",
            "name",
            "amount",
            "category",
            "date",
            "status",
            "splits",
            "photos"
        )
    )

    expenses.forEach { expense ->
        val splitText = splitPeopleFor(expense).joinToString(";") { splitPerson ->
            "${splitPerson.name}:${splitPerson.amount}:${if (splitPerson.paid) "paid" else "unpaid"}"
        }

        rows.add(
            listOf(
                "expense",
                expense.expenseName,
                expense.amount.toString(),
                expense.category,
                formatFullDate(expense.date),
                "",
                splitText,
                receiptUrisFor(expense).joinToString(";")
            )
        )
    }

    debts.forEach { debt ->
        rows.add(
            listOf(
                "debt",
                debt.owedPersonName ?: debt.expenseName,
                debt.owedAmount.toString(),
                debt.category,
                formatFullDate(debt.date),
                if (debt.isDebtPaid) "paid" else "unpaid",
                "",
                ""
            )
        )
    }

    return rows.joinToString(separator = "\n") { row ->
        row.joinToString(separator = ",") { value ->
            csvEscape(value)
        }
    }
}

private fun csvEscape(
    value: String
): String {
    val escaped = value.replace("\"", "\"\"")

    return if (
        escaped.contains(",") ||
        escaped.contains("\n") ||
        escaped.contains("\"")
    ) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun parseCsvExpenses(
    csvText: String
): List<CsvExpenseImport> {
    val rows = parseCsvRows(csvText)
        .filter { row ->
            row.any { it.isNotBlank() }
        }

    if (rows.isEmpty()) {
        return emptyList()
    }

    val firstRow = rows.first().map { it.trim().lowercase() }
    val hasHeader = firstRow.any { header ->
        header in setOf(
            "name",
            "expense",
            "expense_name",
            "description",
            "title",
            "amount",
            "category",
            "split_with",
            "split_amount",
            "splits"
        )
    }

    val headers = if (hasHeader) firstRow else emptyList()
    val dataRows = if (hasHeader) rows.drop(1) else rows

    fun valueFor(row: List<String>, vararg names: String): String {
        if (headers.isEmpty()) {
            return ""
        }

        names.forEach { name ->
            val index = headers.indexOf(name)
            if (index >= 0 && index < row.size) {
                return row[index].trim()
            }
        }

        return ""
    }

    return dataRows.mapNotNull { row ->
        val name = if (headers.isEmpty()) {
            row.getOrNull(0)?.trim().orEmpty()
        } else {
            valueFor(row, "name", "expense", "expense_name", "description", "title")
        }

        val amountText = if (headers.isEmpty()) {
            row.getOrNull(1)?.trim().orEmpty()
        } else {
            valueFor(row, "amount", "cost", "price")
        }

        val amount = amountText.toDoubleOrNull()

        if (name.isBlank() || amount == null || amount <= 0.0) {
            return@mapNotNull null
        }

        val category = if (headers.isEmpty()) {
            row.getOrNull(2)?.trim().orEmpty()
        } else {
            valueFor(row, "category", "type")
        }.ifBlank {
            "Other"
        }

        val splitPeople = if (headers.isEmpty()) {
            parseSplitPeople(
                splitWith = row.getOrNull(3)?.trim().orEmpty(),
                splitAmountText = row.getOrNull(4)?.trim().orEmpty(),
                splitsText = ""
            )
        } else {
            parseSplitPeople(
                splitWith = valueFor(row, "split_with", "splitwith", "person", "friend"),
                splitAmountText = valueFor(row, "split_amount", "splitamount", "owed", "owed_amount"),
                splitsText = valueFor(row, "splits", "split_details")
            )
        }.filter { splitPerson ->
            splitPerson.amount > 0.0
        }

        val validSplitPeople = if (splitPeople.sumOf { it.amount } <= amount) {
            splitPeople
        } else {
            emptyList()
        }

        CsvExpenseImport(
            name = name,
            amount = amount,
            category = category,
            splitPeople = validSplitPeople
        )
    }
}

private fun parseSplitPeople(
    splitWith: String,
    splitAmountText: String,
    splitsText: String
): List<SplitPerson> {
    if (splitsText.isNotBlank()) {
        return splitsText
            .split(";", "|")
            .mapNotNull { chunk ->
                val parts = chunk.split(":", limit = 2)
                val name = parts.getOrNull(0)?.trim().orEmpty()
                val amount = parts.getOrNull(1)?.trim()?.toDoubleOrNull()

                if (name.isNotBlank() && amount != null && amount > 0.0) {
                    SplitPerson(name = name, amount = amount)
                } else {
                    null
                }
            }
    }

    val amount = splitAmountText.toDoubleOrNull()

    return if (splitWith.isNotBlank() && amount != null && amount > 0.0) {
        listOf(SplitPerson(name = splitWith, amount = amount))
    } else {
        emptyList()
    }
}

private fun parseCsvRows(
    csvText: String
): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val currentRow = mutableListOf<String>()
    val currentCell = StringBuilder()
    var insideQuotes = false
    var index = 0

    while (index < csvText.length) {
        val char = csvText[index]

        when {
            char == '"' && insideQuotes && csvText.getOrNull(index + 1) == '"' -> {
                currentCell.append('"')
                index++
            }

            char == '"' -> {
                insideQuotes = !insideQuotes
            }

            char == ',' && !insideQuotes -> {
                currentRow.add(currentCell.toString())
                currentCell.clear()
            }

            (char == '\n' || char == '\r') && !insideQuotes -> {
                if (char == '\r' && csvText.getOrNull(index + 1) == '\n') {
                    index++
                }

                currentRow.add(currentCell.toString())
                currentCell.clear()
                rows.add(currentRow.toList())
                currentRow.clear()
            }

            else -> {
                currentCell.append(char)
            }
        }

        index++
    }

    if (currentCell.isNotEmpty() || currentRow.isNotEmpty()) {
        currentRow.add(currentCell.toString())
        rows.add(currentRow.toList())
    }

    return rows
}

private fun formatFullDate(
    timestamp: Long
): String {
    return try {
        val formatter = java.text.SimpleDateFormat(
            "dd MMM yyyy, h:mm a",
            java.util.Locale.getDefault()
        )

        formatter.format(java.util.Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}

private fun formatShortDate(
    timestamp: Long
): String {
    return try {
        val formatter = java.text.SimpleDateFormat(
            "dd MMM",
            java.util.Locale.getDefault()
        )

        formatter.format(java.util.Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}
