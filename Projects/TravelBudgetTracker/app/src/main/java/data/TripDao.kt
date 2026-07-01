package com.example.travelbudgettracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    fun insertTrip(trip: Trip): Long // Removed 'suspend'

    @Query("SELECT * FROM trips_table ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Delete
    fun deleteTrip(trip: Trip)
}
