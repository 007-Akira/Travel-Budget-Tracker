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

    fun addExpense(
        tripId: Long,
        name: String,
        amount: Double,
        category: String,
        splitWith: String?,
        splitOwed: Double,
        receiptUri: String?
    ) {
        viewModelScope.launch {
            val expense = Expense(
                tripId = tripId,
                expenseName = name,
                amount = amount,
                date = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                category = category,
                splitWithName = splitWith,
                splitAmountOwed = splitOwed,
                receiptUri = receiptUri
            )
            repository.insertExpense(expense)
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