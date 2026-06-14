package com.example.petgrooming.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petgrooming.data.local.entity.*
import com.example.petgrooming.viewmodel.PetUiState
import com.example.petgrooming.viewmodel.PetViewModel

private enum class UserTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Pets("Pets", Icons.Default.Pets),
    Bookings("Bookings", Icons.Default.CalendarMonth),
    Rewards("Rewards", Icons.Default.CardGiftcard),
    Profile("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(viewModel: PetViewModel, state: PetUiState.Success) {
    val tabs = UserTab.values()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selectedTab = tabs[selectedIndex]

    // Sheet state hoisted here so any tab / quick action can open them.
    var showBookingSheet by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<AppointmentEntity?>(null) }
    var showPetSheet by remember { mutableStateOf(false) }
    var editingPet by remember { mutableStateOf<UserPetEntity?>(null) }
    var showEditMember by remember { mutableStateOf(false) }

    val member = state.member

    if (showBookingSheet) {
        AppointmentSheet(
            appointment = editingAppointment,
            pets = state.pets,
            services = state.services,
            member = member,
            onDismiss = { showBookingSheet = false; editingAppointment = null },
            onConfirm = { pet, service, date, time, useVoucher ->
                val current = editingAppointment
                if (current == null) {
                    viewModel.scheduleAppointment(pet, service, date, time, useVoucher)
                } else {
                    viewModel.updateAppointment(
                        current.copy(petName = pet, serviceType = service, date = date, time = time, useVoucher = useVoucher)
                    )
                }
                showBookingSheet = false
                editingAppointment = null
            }
        )
    }

    if (showPetSheet) {
        PetSheet(
            pet = editingPet,
            onDismiss = { showPetSheet = false; editingPet = null },
            onConfirm = { name, breed, age, type, gender, description ->
                val current = editingPet
                if (current == null) {
                    viewModel.addPet(name, breed, age, type, gender ?: "Male", description ?: "")
                } else {
                    // If the type changed, drop the old image so a matching one is fetched.
                    val typeChanged = !current.type.equals(type, ignoreCase = true)
                    viewModel.updatePet(
                        current.copy(
                            name = name, breed = breed, age = age, type = type,
                            gender = gender, description = description,
                            imageUrl = if (typeChanged) null else current.imageUrl
                        )
                    )
                }
                showPetSheet = false
                editingPet = null
            }
        )
    }

    if (showEditMember) {
        EditMemberSheet(
            member = member,
            onDismiss = { showEditMember = false },
            onConfirm = { updated -> viewModel.updateMember(updated); showEditMember = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Pet Bliss", fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                UserTab.Home -> HomeTab(
                    state = state,
                    onEditMember = { showEditMember = true },
                    onAddPet = { selectedIndex = UserTab.Pets.ordinal },
                    onBook = { editingAppointment = null; showBookingSheet = true },
                    onSeeRewards = { selectedIndex = UserTab.Rewards.ordinal },
                    onEditAppointment = { editingAppointment = it; showBookingSheet = true },
                    onCancelAppointment = { viewModel.cancelAppointment(it) }
                )
                UserTab.Pets -> PetsTab(
                    pets = state.pets,
                    onAddPet = { editingPet = null; showPetSheet = true },
                    onEditPet = { editingPet = it; showPetSheet = true },
                    onDeletePet = { viewModel.deletePet(it) }
                )
                UserTab.Bookings -> BookingsTab(
                    state = state,
                    onBook = { editingAppointment = null; showBookingSheet = true },
                    onEditAppointment = { editingAppointment = it; showBookingSheet = true },
                    onCancelAppointment = { viewModel.cancelAppointment(it) },
                    onDeleteAppointment = { viewModel.deleteAppointment(it) }
                )
                UserTab.Rewards -> RewardsTab(
                    state = state,
                    onRedeem = { viewModel.redeemPoints(it) }
                )
                UserTab.Profile -> ProfileTab(
                    state = state,
                    onEditMember = { showEditMember = true },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}

private val ContentPadding = PaddingValues(16.dp)

@Composable
private fun HomeTab(
    state: PetUiState.Success,
    onEditMember: () -> Unit,
    onAddPet: () -> Unit,
    onBook: () -> Unit,
    onSeeRewards: () -> Unit,
    onEditAppointment: (AppointmentEntity) -> Unit,
    onCancelAppointment: (AppointmentEntity) -> Unit
) {
    val member = state.member
    val upcoming = state.appointments.filter { it.status == "Scheduled" || it.status == "ACC" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ContentPadding,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { MembershipCard(member, onClick = onEditMember) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionItem(Icons.Default.Pets, "Add Pet", onAddPet)
                QuickActionItem(Icons.Default.AddCircle, "Book", onBook)
                QuickActionItem(Icons.Default.CardGiftcard, "Rewards", onSeeRewards)
            }
        }
        item { SectionHeader("Upcoming Appointments", actionText = "Book", onAction = onBook) }
        if (upcoming.isEmpty()) {
            item { EmptyStateCard("No upcoming appointments", "Book a grooming session for your pet today!") }
        } else {
            items(upcoming) { appointment ->
                AppointmentItem(
                    appointment = appointment,
                    onEdit = { onEditAppointment(appointment) },
                    onCancel = { onCancelAppointment(appointment) },
                    onDelete = {}
                )
            }
        }
        item { SectionHeader("Recent Activity", actionText = "") }
        if (state.transactions.isEmpty()) {
            item { EmptyStateCard("No activity yet", "Your transactions will appear here.") }
        } else {
            items(state.transactions.take(5)) { tx -> TransactionItem(tx) }
        }
    }
}

@Composable
private fun PetsTab(
    pets: List<UserPetEntity>,
    onAddPet: () -> Unit,
    onEditPet: (UserPetEntity) -> Unit,
    onDeletePet: (UserPetEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("My Pets", actionText = "Add", onAction = onAddPet) }
        if (pets.isEmpty()) {
            item { EmptyStateCard("No pets added yet", "Add your first pet to start booking services.") }
        } else {
            items(pets) { pet ->
                PetListItem(pet, onEdit = { onEditPet(pet) }, onDelete = { onDeletePet(pet) })
            }
        }
    }
}

@Composable
private fun PetListItem(pet: UserPetEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onEdit() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (pet.imageUrl != null) {
                    AsyncImage(
                        model = pet.imageUrl,
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        pet.name.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.Bold)
                Text("${pet.breed} • ${pet.age} yrs • ${pet.type}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun BookingsTab(
    state: PetUiState.Success,
    onBook: () -> Unit,
    onEditAppointment: (AppointmentEntity) -> Unit,
    onCancelAppointment: (AppointmentEntity) -> Unit,
    onDeleteAppointment: (AppointmentEntity) -> Unit
) {
    val upcoming = state.appointments.filter { it.status == "Scheduled" || it.status == "ACC" }
    val history = state.appointments.filter { it.status == "Completed" || it.status == "Cancelled" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Book a Service", actionText = "New", onAction = onBook) }
        items(state.services) { service ->
            ServiceItem(service) { onBook() }
        }

        item { SectionHeader("Upcoming Appointments", actionText = "") }
        if (upcoming.isEmpty()) {
            item { EmptyStateCard("No upcoming appointments", "Book a grooming session for your pet today!") }
        } else {
            items(upcoming) { appointment ->
                AppointmentItem(
                    appointment = appointment,
                    onEdit = { onEditAppointment(appointment) },
                    onCancel = { onCancelAppointment(appointment) },
                    onDelete = { onDeleteAppointment(appointment) }
                )
            }
        }

        item { SectionHeader("History", actionText = "") }
        if (history.isEmpty()) {
            item { EmptyStateCard("No history yet", "Your past appointments will appear here.") }
        } else {
            items(history) { appointment ->
                AppointmentItem(
                    appointment = appointment,
                    onEdit = {},
                    onCancel = {},
                    onDelete = { onDeleteAppointment(appointment) }
                )
            }
        }
    }
}

@Composable
private fun RewardsTab(
    state: PetUiState.Success,
    onRedeem: (RewardEntity) -> Unit
) {
    val member = state.member
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PointsHeaderCard(member) }
        item { SectionHeader("Available Rewards", actionText = "") }
        items(state.rewards) { reward ->
            val owned = when (reward.voucherType) {
                "Espresso" -> member.espressoVouchers
                "Cappuccino" -> member.cappuccinoVouchers
                "Latte" -> member.latteVouchers
                else -> 0
            }
            RewardItem(
                reward = reward,
                canRedeem = member.totalPoints >= reward.pointsNeeded,
                ownedCount = owned,
                onRedeem = { onRedeem(reward) }
            )
        }
    }
}

@Composable
private fun PointsHeaderCard(member: MemberEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your Points", style = MaterialTheme.typography.labelMedium)
                Text("${member.totalPoints} PTS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Vouchers", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${member.espressoVouchers + member.cappuccinoVouchers + member.latteVouchers}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileTab(
    state: PetUiState.Success,
    onEditMember: () -> Unit,
    onLogout: () -> Unit
) {
    val member = state.member
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MembershipCard(member, onClick = onEditMember) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileRow(Icons.Default.Person, "Name", member.name)
                    ProfileRow(Icons.Default.Email, "Email", member.email)
                    ProfileRow(Icons.Default.Phone, "Phone", member.phone)
                    ProfileRow(Icons.Default.Star, "Status", member.status)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onEditMember, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }
        item { SectionHeader("Transaction History", actionText = "") }
        if (state.transactions.isEmpty()) {
            item { EmptyStateCard("No transactions yet", "Your payment history will appear here.") }
        } else {
            items(state.transactions) { tx -> TransactionItem(tx) }
        }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
