package com.example.travelbudgettracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.travelbudgettracker.data.Trip
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: BudgetViewModel,
    navController: NavController
) {
    val trips by viewModel.allTrips.collectAsState(initial = emptyList())

    var showAddTripDialog by remember {
        mutableStateOf(false)
    }

    var tripPendingDelete by remember {
        mutableStateOf<Trip?>(null)
    }

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    PremiumScreenBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = PremiumDimens.ScreenPadding,
                        end = PremiumDimens.ScreenPadding,
                        top = 22.dp,
                        bottom = 110.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        PremiumHeader(
                            title = "Travel Budget",
                            subtitle = "Trips, expenses and debts."
                        )
                    }

                    item {
                        SectionTitle(
                            title = "Your Trips",
                            subtitle = if (trips.isEmpty()) "Create your first trip." else null
                        )
                    }

                    if (trips.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No trips yet",
                                message = "Create a trip and start adding your expenses, receipts and split payments.",
                                emoji = "✈️",
                                actionText = "Create Trip",
                                onActionClick = {
                                    showAddTripDialog = true
                                }
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = trips,
                            key = { _, trip -> trip.tripId }
                        ) { index, trip ->
                            HomeTripCard(
                                trip = trip,
                                index = index,
                                onClick = {
                                    navController.navigate("trip_detail/${trip.tripId}")
                                },
                                onLongClick = {
                                    tripPendingDelete = trip
                                }
                            )
                        }
                    }
                }
            }

            GoldFAB(
                onClick = {
                    showAddTripDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = PremiumDimens.ScreenPadding,
                        bottom = 28.dp
                    )
            )
        }
    }

    if (showAddTripDialog) {
        AddTripDialog(
            onDismiss = {
                showAddTripDialog = false
            },
            onAddTrip = { tripName ->
                viewModel.addTrip(tripName)
                showAddTripDialog = false
            }
        )
    }

    tripPendingDelete?.let { trip ->
        DeleteConfirmDialog(
            title = "Delete trip?",
            message = "This will delete ${trip.tripName} and its expenses.",
            confirmText = "Delete",
            onDismiss = {
                tripPendingDelete = null
            },
            onConfirm = {
                viewModel.deleteTrip(trip)
                tripPendingDelete = null
            }
        )
    }
}

@Composable
private fun HomeTripCard(
    trip: Trip,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(80L * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(450))
    ) {
        TripCard(
            tripName = trip.tripName,
            subtitle = "Tap to open",
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

@Composable
private fun AddTripDialog(
    onDismiss: () -> Unit,
    onAddTrip: (String) -> Unit
) {
    var tripName by remember {
        mutableStateOf("")
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
        ) {
            PremiumDialogContainer(
                title = "Create Trip",
                subtitle = "Give your journey a name and start tracking beautifully.",
                onClose = onDismiss
            ) {
                PremiumTextField(
                    value = tripName,
                    onValueChange = {
                        tripName = it
                        error = null
                    },
                    label = "Trip Name",
                    placeholder = "Goa Trip, Manali Ride, Thailand...",
                    isError = error != null,
                    leadingIcon = {
                        Text(text = "✈️")
                    }
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    PremiumWarningText(
                        text = error ?: ""
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                PremiumButton(
                    text = "Create Trip",
                    onClick = {
                        val cleanName = tripName.trim()

                        if (cleanName.isBlank()) {
                            error = "Trip name cannot be empty."
                            return@PremiumButton
                        }

                        onAddTrip(cleanName)
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

fun formatCurrency(
    amount: Double
): String {
    return "₹%,.2f".format(amount)
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
