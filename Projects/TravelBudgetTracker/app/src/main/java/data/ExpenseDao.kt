package com.example.travelbudgettracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    fun insertExpense(expense: Expense): Long // Removed 'suspend'

    @Query("SELECT * FROM expenses_table WHERE tripId = :tripId ORDER BY date DESC, time DESC")
    fun getExpensesForTrip(tripId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses_table WHERE tripId = :tripId")
    fun getTotalSpent(tripId: Long): Flow<Double?>

    @Query("""
        SELECT SUM(
            CASE
                WHEN amount <= 0 THEN 0
                WHEN splitAmountOwed < 0 THEN 0
                WHEN splitAmountOwed > amount THEN amount
                ELSE splitAmountOwed
            END
        )
        FROM expenses_table
        WHERE tripId = :tripId
    """)
    fun getTotalOwedToUser(tripId: Long): Flow<Double?>

    // When a "YOU_OWE" debt is marked paid, we set amount = owedAmount so it
    // finally counts toward Total Spent. Marking it unpaid again resets amount to 0.
    @Query("UPDATE expenses_table SET amount = :amount, isDebtPaid = :paid WHERE expenseId = :id")
    fun setDebtPaidState(id: Long, amount: Double, paid: Boolean)

    @Query("UPDATE expenses_table SET splitDetailsJson = :splitDetailsJson, splitAmountOwed = :splitAmountOwed WHERE expenseId = :id")
    fun updateSplitState(id: Long, splitDetailsJson: String?, splitAmountOwed: Double)

    @androidx.room.Delete
    fun deleteExpense(expense: Expense)
}
