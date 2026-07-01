package com.example.travelbudgettracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BudgetRepository(private val tripDao: TripDao, private val expenseDao: ExpenseDao) {

    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun insertTrip(trip: Trip): Long {
        // This safely moves the synchronous DAO call to a background thread
        return withContext(Dispatchers.IO) {
            tripDao.insertTrip(trip)
        }
    }

    suspend fun insertExpense(expense: Expense): Long {
        return withContext(Dispatchers.IO) {
            expenseDao.insertExpense(expense)
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteExpense(expense)
        }
    }

    suspend fun deleteTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.deleteTrip(trip)
        }
    }

    suspend fun setDebtPaidState(id: Long, amount: Double, paid: Boolean) {
        withContext(Dispatchers.IO) {
            expenseDao.setDebtPaidState(id, amount, paid)
        }
    }

    suspend fun updateSplitState(id: Long, splitDetailsJson: String?, splitAmountOwed: Double) {
        withContext(Dispatchers.IO) {
            expenseDao.updateSplitState(id, splitDetailsJson, splitAmountOwed)
        }
    }

    fun getExpensesForTrip(tripId: Long): Flow<List<Expense>> = expenseDao.getExpensesForTrip(tripId)

    fun getTotalSpent(tripId: Long): Flow<Double?> = expenseDao.getTotalSpent(tripId)

    fun getTotalOwedToUser(tripId: Long): Flow<Double?> = expenseDao.getTotalOwedToUser(tripId)
}
