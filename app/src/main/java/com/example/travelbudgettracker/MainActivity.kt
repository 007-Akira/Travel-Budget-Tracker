package com.example.travelbudgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.travelbudgettracker.ui.IntroScreen
import com.example.travelbudgettracker.ui.TripDetailScreen
import com.example.travelbudgettracker.ui.theme.TravelBudgetTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)

        val repository = BudgetRepository(
            tripDao = database.tripDao(),
            expenseDao = database.expenseDao()
        )

        setContent {
            TravelBudgetTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showIntro by remember {
                        mutableStateOf(true)
                    }

                    val budgetViewModel: BudgetViewModel = viewModel(
                        factory = BudgetViewModelFactory(repository)
                    )

                    val navController = rememberNavController()

                    if (showIntro) {
                        IntroScreen(
                            onFinished = {
                                showIntro = false
                            }
                        )
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable(route = "home") {
                                HomeScreen(
                                    viewModel = budgetViewModel,
                                    navController = navController
                                )
                            }

                            composable(route = "trip_detail/{tripId}") { backStackEntry ->
                                val tripId = backStackEntry.arguments
                                    ?.getString("tripId")
                                    ?.toLongOrNull()

                                if (tripId != null) {
                                    TripDetailScreen(
                                        tripId = tripId,
                                        viewModel = budgetViewModel,
                                        onBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
