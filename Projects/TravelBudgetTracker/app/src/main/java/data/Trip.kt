package com.example.travelbudgettracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips_table")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val tripId: Long = 0L,
    val tripName: String,
    val createdAt: Long = System.currentTimeMillis()
)