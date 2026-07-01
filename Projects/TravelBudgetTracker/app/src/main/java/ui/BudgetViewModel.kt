package com.example.travelbudgettracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelbudgettracker.data.BudgetRepository
import com.example.travelbudgettracker.data.Expense
import com.example.travelbudgettracker.data.Trip
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val allTrips = repository.allTrips

    fun getExpensesForTrip(tripId: Long) = repository.getExpensesForTrip(tripId)
    fun getTotalSpent(tripId: Long) = repository.getTotalSpent(tripId)
    fun getTotalOwedToUser(tripId: Long) = repository.getTotalOwedToUser(tripId)

    fun addTrip(name: String) {
        viewModelScope.launch {
            repository.insertTrip(Trip(tripName = name))
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    fun addExpense(
        tripId: Long,
        name: String,
        amount: Double,
        category: String,
        splitWith: String?,
        splitOwed: Double,
        receiptUri: String?,
        splitDetailsJson: String? = null,
        receiptUrisJson: String? = null
    ) {
        viewModelScope.launch {
            val cappedSplitOwed = if (amount > 0.0) splitOwed.coerceIn(0.0, amount) else 0.0
            val expense = Expense(
                tripId = tripId,
                expenseName = name,
                amount = amount,
                date = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                category = category,
                splitWithName = splitWith,
                splitAmountOwed = cappedSplitOwed,
                receiptUri = receiptUri,
                splitDetailsJson = splitDetailsJson,
                receiptUrisJson = receiptUrisJson,
                entryType = "EXPENSE"
            )
            repository.insertExpense(expense)
        }
    }

    // --- New: record money YOU owe someone else ---
    // Stored with amount = 0.0 so it does NOT count toward Total Spent yet.
    fun addDebt(tripId: Long, personName: String, amount: Double, category: String) {
        viewModelScope.launch {
            val expense = Expense(
                tripId = tripId,
                expenseName = "Owed to $personName",
                amount = 0.0,
                date = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                category = category,
                entryType = "YOU_OWE",
                owedPersonName = personName,
                owedAmount = amount,
                isDebtPaid = false
            )
            repository.insertExpense(expense)
        }
    }

    // Flips paid state. Paying it sets amount = owedAmount (now counts toward
    // Total Spent); un-paying resets amount back to 0.
    fun toggleDebtPaid(expense: Expense) {
        viewModelScope.launch {
            val paid = !expense.isDebtPaid
            val newAmount = if (paid) expense.owedAmount else 0.0
            repository.setDebtPaidState(expense.expenseId, newAmount, paid)
        }
    }

    fun updateSplitState(expenseId: Long, splitDetailsJson: String?, splitAmountOwed: Double) {
        viewModelScope.launch {
            repository.updateSplitState(expenseId, splitDetailsJson, splitAmountOwed)
        }
    }
}

// Factory to construct the ViewModel with the Repository
class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
