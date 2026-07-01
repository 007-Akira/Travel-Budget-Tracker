package com.example.travelbudgettracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses_table",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE // Auto-deletes expenses if a trip is deleted
        )
    ],
    indices = [Index("tripId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0L,
    val tripId: Long,
    val expenseName: String,
    val amount: Double,
    val date: Long,
    val time: Long,
    val category: String,
    val receiptUri: String? = null,
    val splitWithName: String? = null,
    val splitAmountOwed: Double = 0.0,
    val splitDetailsJson: String? = null,
    val receiptUrisJson: String? = null,

    // --- New: "I Owe Someone" tracking ---
    // "EXPENSE" = normal spend (optionally split so others owe you)
    // "YOU_OWE" = money you owe someone else, not yet paid
    val entryType: String = "EXPENSE",
    val owedPersonName: String? = null,
    val owedAmount: Double = 0.0,
    val isDebtPaid: Boolean = false
)
