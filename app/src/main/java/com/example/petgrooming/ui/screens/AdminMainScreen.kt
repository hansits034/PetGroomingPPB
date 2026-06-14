package com.example.petgrooming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petgrooming.data.local.entity.*
import com.example.petgrooming.viewmodel.PetUiState
import com.example.petgrooming.viewmodel.PetViewModel

private enum class AdminTab(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Bookings("Bookings", Icons.Default.CalendarMonth),
    Services("Services", Icons.Default.ContentCut),
    Rewards("Rewards", Icons.Default.CardGiftcard)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(viewModel: PetViewModel, state: PetUiState.Success) {
    val tabs = AdminTab.values()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selectedTab = tabs[selectedIndex]

    var showServiceSheet by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var showRewardSheet by remember { mutableStateOf(false) }
    var editingReward by remember { mutableStateOf<RewardEntity?>(null) }

    if (showServiceSheet) {
        ServiceSheet(
            service = editingService,
            onDismiss = { showServiceSheet = false; editingService = null },
            onConfirm = { name, desc, price, dur ->
                val current = editingService
                if (current == null) {
                    viewModel.addService(name, desc, price, dur)
                } else {
                    viewModel.updateService(current.copy(name = name, description = desc, price = price, durationMinutes = dur))
                }
                showServiceSheet = false
                editingService = null
            }
        )
    }

    if (showRewardSheet) {
        RewardSheet(
            reward = editingReward,
            onDismiss = { showRewardSheet = false; editingReward = null },
            onConfirm = { name, desc, points, type ->
                val current = editingReward
                if (current == null) {
                    viewModel.addReward(name, desc, points, type)
                } else {
                    viewModel.updateReward(current.copy(name = name, description = desc, pointsNeeded = points, voucherType = type))
                }
                showRewardSheet = false
                editingReward = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                AdminTab.Services -> FloatingActionButton(onClick = { editingService = null; showServiceSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Service")
                }
                AdminTab.Rewards -> FloatingActionButton(onClick = { editingReward = null; showRewardSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reward")
                }
                else -> {}
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                AdminTab.Dashboard -> AdminDashboardTab(
                    state = state,
                    onUpdateStatus = { id, status -> viewModel.updateAppointmentStatus(id, status) }
                )
                AdminTab.Bookings -> AdminBookingsTab(
                    state = state,
                    onUpdateStatus = { id, status -> viewModel.updateAppointmentStatus(id, status) }
                )
                AdminTab.Services -> AdminServicesTab(
                    state = state,
                    onAdd = { editingService = null; showServiceSheet = true },
                    onEdit = { editingService = it; showServiceSheet = true },
                    onDelete = { viewModel.deleteService(it) }
                )
                AdminTab.Rewards -> AdminRewardsTab(
                    state = state,
                    onAdd = { editingReward = null; showRewardSheet = true },
                    onEdit = { editingReward = it; showRewardSheet = true },
                    onDelete = { viewModel.deleteReward(it) }
                )
            }
        }
    }
}

private val AdminContentPadding = PaddingValues(16.dp)

@Composable
private fun AdminDashboardTab(
    state: PetUiState.Success,
    onUpdateStatus: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = AdminContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Business Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            AdminSummary(state.allAppointments, state.transactions)
        }
        item { SectionHeader("Recent Requests", actionText = "") }
        val recent = state.allAppointments.sortedByDescending { it.id }.take(5)
        if (recent.isEmpty()) {
            item { EmptyStateCard("No appointments found", "Check back later for new customer requests.") }
        } else {
            items(recent) { appointment ->
                AdminAppointmentItem(appointment, onUpdateStatus)
            }
        }
    }
}

@Composable
private fun AdminBookingsTab(
    state: PetUiState.Success,
    onUpdateStatus: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = AdminContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Customer Appointments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (state.allAppointments.isEmpty()) {
            item { EmptyStateCard("No appointments found", "Check back later for new customer requests.") }
        } else {
            items(state.allAppointments.sortedByDescending { it.id }) { appointment ->
                AdminAppointmentItem(appointment, onUpdateStatus)
            }
        }
    }
}

@Composable
private fun AdminServicesTab(
    state: PetUiState.Success,
    onAdd: () -> Unit,
    onEdit: (ServiceEntity) -> Unit,
    onDelete: (ServiceEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = AdminContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Manage Services", actionText = "Add", onAction = onAdd) }
        if (state.services.isEmpty()) {
            item { EmptyStateCard("No services yet", "Add a grooming service for customers to book.") }
        } else {
            items(state.services) { service ->
                AdminServiceItem(service, onEdit = { onEdit(service) }, onDelete = { onDelete(service) })
            }
        }
    }
}

@Composable
private fun AdminRewardsTab(
    state: PetUiState.Success,
    onAdd: () -> Unit,
    onEdit: (RewardEntity) -> Unit,
    onDelete: (RewardEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = AdminContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Manage Rewards", actionText = "Add", onAction = onAdd) }
        if (state.rewards.isEmpty()) {
            item { EmptyStateCard("No rewards yet", "Add a reward customers can redeem with points.") }
        } else {
            items(state.rewards) { reward ->
                AdminRewardItem(reward, onEdit = { onEdit(reward) }, onDelete = { onDelete(reward) })
            }
        }
    }
}
