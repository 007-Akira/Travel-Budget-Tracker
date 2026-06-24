// ... existing code ...
package com.example.travelbudgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelbudgettracker.data.AppDatabase
import com.example.travelbudgettracker.data.BudgetRepository
import com.example.travelbudgettracker.ui.BudgetViewModel
import com.example.travelbudgettracker.ui.BudgetViewModelFactory
import com.example.travelbudgettracker.ui.HomeScreen
import com.example.travelbudgettracker.ui.TripDetailScreen
import com.example.travelbudgettracker.ui.theme.TravelBudgetTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BudgetRepository(database.tripDao(), database.expenseDao())

        setContent {
            TravelBudgetTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Setup ViewModel & Navigation
                    val viewModel: BudgetViewModel = viewModel(factory = BudgetViewModelFactory(repository))
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(viewModel = viewModel, navController = navController)
                        }
                        composable("trip_detail/{tripId}") { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: return@composable
                            TripDetailScreen(tripId = tripId, viewModel = viewModel) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
// ... existing code ...