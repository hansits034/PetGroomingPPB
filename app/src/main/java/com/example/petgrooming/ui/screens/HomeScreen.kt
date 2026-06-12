package com.example.petgrooming.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.petgrooming.data.local.entity.*
import com.example.petgrooming.ui.components.*
import com.example.petgrooming.viewmodel.PetUiState
import com.example.petgrooming.viewmodel.PetViewModel
import com.example.petgrooming.viewmodel.Reward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PetViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (state is PetUiState.Success) {
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
                    ),
                    actions = {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
                        )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is PetUiState.Loading -> LoadingView()
                is PetUiState.NotRegistered -> {
                    var isLoginMode by remember { mutableStateOf(true) } // Default to Login
                    if (isLoginMode) {
                        LoginScreen(
                            onLogin = viewModel::login,
                            onToggleMode = { isLoginMode = false }
                        )
                    } else {
                        RegistrationScreen(
                            onRegister = viewModel::registerMember,
                            onToggleMode = { isLoginMode = true }
                        )
                    }
                }
                is PetUiState.LoggedOut -> {
                    LoginScreen(
                        onLogin = viewModel::login,
                        onToggleMode = null // Already registered, just login
                    )
                }
                is PetUiState.Success -> {
                    val successState = state as PetUiState.Success
                    if (successState.member.role == "ADMIN") {
                        AdminDashboard(
                            member = successState.member,
                            allAppointments = successState.allAppointments,
                            services = successState.services,
                            transactions = successState.transactions,
                            rewards = successState.rewards,
                            onUpdateStatus = viewModel::updateAppointmentStatus,
                            onLogout = viewModel::logout,
                            onAddService = viewModel::addService,
                            onUpdateService = viewModel::updateService,
                            onDeleteService = viewModel::deleteService,
                            onAddReward = viewModel::addReward,
                            onUpdateReward = viewModel::updateReward,
                            onDeleteReward = viewModel::deleteReward
                        )
                    } else {
                        Dashboard(
                            member = successState.member,
                            transactions = successState.transactions,
                            appointments = successState.appointments,
                            pets = successState.pets,
                            services = successState.services,
                            rewards = successState.rewards,
                            onAddTransaction = { viewModel.addTransaction(150000) },
                            onRedeem = viewModel::redeemPoints,
                            onScheduleAppointment = viewModel::scheduleAppointment,
                            onUpdateAppointment = viewModel::updateAppointment,
                            onCancelAppointment = viewModel::cancelAppointment,
                            onDeleteAppointment = viewModel::deleteAppointment,
                            onUpdateMember = viewModel::updateMember,
                            onAddPet = viewModel::addPet,
                            onUpdatePet = viewModel::updatePet,
                            onDeletePet = viewModel::deletePet,
                            onAddService = viewModel::addService,
                            onUpdateService = viewModel::updateService,
                            onDeleteService = viewModel::deleteService
                        )
                    }
                }
                is PetUiState.Error -> {
                    val errorState = state as PetUiState.Error
                    if (errorState.isPersistent) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        ) {
                            ErrorView(errorState.message)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.logout() }) {
                                Text("Back to Login")
                            }
                        }
                    } else {
                        // For transient errors like login failure
                        var isLoginMode by remember { mutableStateOf(true) }
                        
                        Column {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = errorState.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            if (isLoginMode) {
                                LoginScreen(
                                    onLogin = viewModel::login,
                                    onToggleMode = { isLoginMode = false }
                                )
                            } else {
                                RegistrationScreen(
                                    onRegister = viewModel::registerMember,
                                    onToggleMode = { isLoginMode = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onToggleMode: (() -> Unit)?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("Login to Pet Bliss", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password") }, 
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Login")
        }
        if (onToggleMode != null) {
            TextButton(onClick = onToggleMode) {
                Text("Don't have an account? Register")
            }
        }
    }
}

@Composable
fun RegistrationScreen(onRegister: (String, String, String, String) -> Unit, onToggleMode: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text("Become a Member", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password") }, 
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword, 
            onValueChange = { confirmPassword = it }, 
            label = { Text("Confirm Password") }, 
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotEmpty() && password != confirmPassword
        )
        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text("Passwords do not match", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onRegister(name, email, phone, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && password == confirmPassword
        ) {
            Text("Register Now")
        }
        TextButton(onClick = onToggleMode) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun Dashboard(
    member: MemberEntity,
    transactions: List<TransactionEntity>,
    appointments: List<AppointmentEntity>,
    pets: List<UserPetEntity>,
    services: List<ServiceEntity>,
    rewards: List<RewardEntity>,
    onAddTransaction: () -> Unit,
    onRedeem: (RewardEntity) -> Unit,
    onScheduleAppointment: (String, String, String, String, Boolean) -> Unit,
    onUpdateAppointment: (AppointmentEntity) -> Unit,
    onCancelAppointment: (AppointmentEntity) -> Unit,
    onDeleteAppointment: (AppointmentEntity) -> Unit,
    onUpdateMember: (MemberEntity) -> Unit,
    onAddPet: (String, String, Int, String, String, String) -> Unit,
    onUpdatePet: (UserPetEntity) -> Unit,
    onDeletePet: (UserPetEntity) -> Unit,
    onAddService: (String, String, Long, Int) -> Unit,
    onUpdateService: (ServiceEntity) -> Unit,
    onDeleteService: (ServiceEntity) -> Unit
) {
    var showBookingSheet by remember { mutableStateOf(false) }
    var showEditMemberSheet by remember { mutableStateOf(false) }
    var showPetSheet by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<AppointmentEntity?>(null) }
    var selectedPet by remember { mutableStateOf<UserPetEntity?>(null) }
    
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    if (showBookingSheet) {
        AppointmentSheet(
            appointment = selectedAppointment,
            pets = pets,
            services = services,
            member = member,
            onDismiss = { 
                showBookingSheet = false
                selectedAppointment = null
            },
            onConfirm = { pet, service, date, time, useVoucher ->
                if (selectedAppointment == null) {
                    onScheduleAppointment(pet, service, date, time, useVoucher)
                } else {
                    onUpdateAppointment(selectedAppointment!!.copy(
                        petName = pet,
                        serviceType = service,
                        date = date,
                        time = time,
                        useVoucher = useVoucher
                    ))
                }
                showBookingSheet = false
                selectedAppointment = null
            }
        )
    }

    if (showEditMemberSheet) {
        EditMemberSheet(
            member = member,
            onDismiss = { showEditMemberSheet = false },
            onConfirm = { updatedMember ->
                onUpdateMember(updatedMember)
                showEditMemberSheet = false
            }
        )
    }

    if (showPetSheet) {
        PetSheet(
            pet = selectedPet,
            onDismiss = { 
                showPetSheet = false
                selectedPet = null
            },
            onConfirm = { name: String, breed: String, age: Int, type: String, gender: String?, description: String? ->
                if (selectedPet == null) {
                    onAddPet(name, breed, age, type, gender ?: "Male", description ?: "")
                } else {
                    onUpdatePet(selectedPet!!.copy(
                        name = name, 
                        breed = breed, 
                        age = age, 
                        type = type,
                        gender = gender,
                        description = description
                    ))
                }
                showPetSheet = false
                selectedPet = null
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { 
            MembershipCard(member, onClick = { showEditMemberSheet = true }) 
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionItem(Icons.Default.Pets, "Add Pet") { showPetSheet = true }
                QuickActionItem(Icons.Default.AddCircle, "Book Service") { showBookingSheet = true }
                QuickActionItem(Icons.Default.History, "History") { 
                    coroutineScope.launch {
                        // Find index of History section
                        // 1: MemberCard, 2: QuickActions, 3: MyPetsHeader, 4-N: PetCards, N+1: UpcomingHeader, ...
                        val historyHeaderIndex = 3 + (if (pets.isEmpty()) 1 else pets.size) + 1 + (if (appointments.filter { it.status == "Scheduled" || it.status == "ACC" }.isEmpty()) 1 else appointments.filter { it.status == "Scheduled" || it.status == "ACC" }.size)
                        listState.animateScrollToItem(historyHeaderIndex)
                    }
                }
            }
        }
        
        // My Pets Section
        item {
            SectionHeader("My Pets", actionText = "Add", onAction = { showPetSheet = true })
        }
        
        if (pets.isEmpty()) {
            item { EmptyStateCard("No pets added yet", "Add your first pet to start booking services.") }
        } else {
            items(pets) { pet ->
                PetCard(pet, onClick = {
                    selectedPet = pet
                    showPetSheet = true
                })
            }
        }

        // Upcoming Appointments
        item {
            SectionHeader("Upcoming Appointments", onAction = { showBookingSheet = true })
        }

        val upcomingAppointments = appointments.filter { it.status == "Scheduled" || it.status == "ACC" }
        if (upcomingAppointments.isEmpty()) {
            item { EmptyStateCard("No upcoming appointments", "Book a grooming session for your pet today!") }
        } else {
            items(upcomingAppointments) { appointment ->
                AppointmentItem(
                    appointment = appointment,
                    onEdit = {
                        selectedAppointment = appointment
                        showBookingSheet = true
                    },
                    onCancel = { onCancelAppointment(appointment) },
                    onDelete = { onDeleteAppointment(appointment) }
                )
            }
        }

        // Appointment History
        item {
            SectionHeader("Appointment History", actionText = "")
        }
        val historyAppointments = appointments.filter { it.status == "Completed" || it.status == "Cancelled" }
        if (historyAppointments.isEmpty()) {
            item { EmptyStateCard("No history yet", "Your past appointments will appear here.") }
        } else {
            items(historyAppointments) { appointment ->
                AppointmentItem(
                    appointment = appointment,
                    onEdit = {},
                    onCancel = {},
                    onDelete = { onDeleteAppointment(appointment) }
                )
            }
        }

        // Popular Services
        item {
            SectionHeader("Popular Services", actionText = "See All")
        }
        
        items(services) { service ->
            ServiceItem(service) {
                // Pre-fill booking with this service
                showBookingSheet = true
            }
        }

        item {
            SectionHeader("Available Rewards")
        }

        items(rewards) { reward ->
            val ownedVouchers = when (reward.voucherType) {
                "Espresso" -> member.espressoVouchers
                "Cappuccino" -> member.cappuccinoVouchers
                "Latte" -> member.latteVouchers
                else -> 0
            }
            RewardItem(
                reward = reward,
                canRedeem = member.totalPoints >= reward.pointsNeeded,
                ownedCount = ownedVouchers,
                onRedeem = { onRedeem(reward) }
            )
        }

        item {
            SectionHeader("Recent Activity")
        }

        items(transactions) { tx ->
            TransactionItem(tx)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String = "Add", onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun MembershipCard(member: MemberEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
            ).padding(24.dp)
        ) {
            Column {
                Text("DIGITAL MEMBER", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(member.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(member.memberId, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("STATUS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(member.status, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(32.dp))
                    Column {
                        Text("TOTAL POINTS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text("${member.totalPoints} PTS", color = Color.Yellow, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
fun RewardItem(reward: RewardEntity, canRedeem: Boolean, ownedCount: Int, onRedeem: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.name, fontWeight = FontWeight.Bold)
                Text(reward.description, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${reward.pointsNeeded} Points", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Owned: $ownedCount",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Button(onClick = onRedeem, enabled = canRedeem) {
                Text("Redeem")
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Service Payment", fontWeight = FontWeight.SemiBold)
            Text(tx.date, style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Rp${tx.amount}", fontWeight = FontWeight.Bold)
            Text("+${tx.pointsEarned} pts", color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: AppointmentEntity,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(appointment.status) {
                "Cancelled" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                "ACC" -> Color(0xFFE8F5E9).copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(appointment.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(appointment.serviceType, style = MaterialTheme.typography.bodyMedium)
                }
                Surface(
                    color = when(appointment.status) {
                        "Scheduled" -> MaterialTheme.colorScheme.primaryContainer
                        "ACC" -> Color(0xFF4CAF50)
                        "Completed" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val statusText = if (appointment.status == "ACC") "CONFIRMED" else appointment.status.uppercase()
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (appointment.status == "ACC") Color.White else Color.Unspecified,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text(appointment.date, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text(appointment.time, style = MaterialTheme.typography.bodySmall)
            }
            
            if (appointment.status == "Scheduled" || appointment.status == "ACC") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (appointment.status == "Scheduled") {
                        TextButton(onClick = onEdit) { Text("Edit") }
                    } else {
                        // For ACC status, show a little helper text
                        Text(
                            "Booking is confirmed", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
                        )
                    }
                    TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { 
                        Text("Cancel") 
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun UserPetCard(pet: UserPetEntity, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    pet.name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(pet.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(pet.breed, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Text("${pet.age} yrs • ${pet.type}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ServiceItem(service: ServiceEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold)
                Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${service.durationMinutes} mins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text("Rp${service.price / 1000}k", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentSheet(
    appointment: AppointmentEntity?,
    pets: List<UserPetEntity>,
    services: List<ServiceEntity>,
    member: MemberEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Boolean) -> Unit
) {
    var petName by remember { mutableStateOf(appointment?.petName ?: if (pets.isNotEmpty()) pets[0].name else "") }
    var service by remember { mutableStateOf(appointment?.serviceType ?: if (services.isNotEmpty()) services[0].name else "") }
    var date by remember { mutableStateOf(appointment?.date ?: "") }
    var time by remember { mutableStateOf(appointment?.time ?: "08:00-09:00") }
    var useVoucher by remember { mutableStateOf(appointment?.useVoucher ?: false) }

    val availableVouchers = when {
        service.contains("Espresso", ignoreCase = true) -> member.espressoVouchers
        service.contains("Cappuccino", ignoreCase = true) -> member.cappuccinoVouchers
        service.contains("Latte", ignoreCase = true) -> member.latteVouchers
        else -> 0
    }
    
    // Reset useVoucher if no vouchers available for selected service
    LaunchedEffect(service) {
        if (availableVouchers == 0) {
            useVoucher = false
        }
    }

    val timeSlots = listOf(
        "08:00-09:00", "09:15-10:15", "10:30-11:30",
        "13:00-14:00", "14:15-15:15", "15:30-16:30"
    )

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        date = sdf.format(java.util.Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(
                if (appointment == null) "Schedule Appointment" else "Edit Appointment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            
            Text("Select Pet", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                pets.forEach { pet ->
                    FilterChip(
                        selected = petName == pet.name,
                        onClick = { petName = pet.name },
                        label = { Text(pet.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            if (pets.isEmpty()) {
                OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Pet Name") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(8.dp))
            Text("Select Service", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                services.forEach { s ->
                    FilterChip(
                        selected = service == s.name,
                        onClick = { service = s.name },
                        label = { Text(s.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { },
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                trailingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }
            )
            
            Spacer(Modifier.height(16.dp))
            Text("Select Time Slot", style = MaterialTheme.typography.labelMedium)
            var expandedTime by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTime,
                onExpandedChange = { expandedTime = !expandedTime },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTime) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                    timeSlots.forEach { slot ->
                        DropdownMenuItem(text = { Text(slot) }, onClick = { time = slot; expandedTime = false })
                    }
                }
            }

            if (availableVouchers > 0) {
                val voucherType = when {
                    service.contains("Espresso", ignoreCase = true) -> "Espresso Wash"
                    service.contains("Cappuccino", ignoreCase = true) -> "Cappuccino Groom"
                    service.contains("Latte", ignoreCase = true) -> "Latte Spa"
                    else -> "Free Wash"
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useVoucher, onCheckedChange = { useVoucher = it })
                    Text("Use $voucherType Voucher (Available: $availableVouchers)")
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(petName, service, date, time, useVoucher) },
                modifier = Modifier.fillMaxWidth(),
                enabled = petName.isNotBlank() && service.isNotBlank() && date.isNotBlank() && time.isNotBlank()
            ) {
                Text(if (appointment == null) "Confirm Booking" else "Save Changes")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSheet(
    pet: UserPetEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(pet?.name ?: "") }
    var breed by remember { mutableStateOf(pet?.breed ?: "") }
    var age by remember { mutableStateOf(pet?.age?.toString() ?: "") }
    var type by remember { mutableStateOf(pet?.type ?: "Dog") }
    var gender by remember { mutableStateOf(pet?.gender ?: "Male") }
    var description by remember { mutableStateOf(pet?.description ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (pet == null) "Add New Pet" else "Edit Pet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pet Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                Spacer(Modifier.width(8.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Dog", "Cat", "Rabbit", "Other").forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expanded = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            Text("Gender", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = gender == "Male",
                    onClick = { gender = "Male" },
                    label = { Text("Male") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = gender == "Female",
                    onClick = { gender = "Female" },
                    label = { Text("Female") }
                )
            }
            
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name, breed, age.toIntOrNull() ?: 0, type, gender, description.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && breed.isNotBlank()
            ) {
                Text(if (pet == null) "Add Pet" else "Save Changes")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberSheet(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onConfirm: (MemberEntity) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var email by remember { mutableStateOf(member.email) }
    var phone by remember { mutableStateOf(member.phone) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text("Edit Membership", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(member.copy(name = name, email = email, phone = phone)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Membership")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    member: MemberEntity,
    allAppointments: List<AppointmentEntity>,
    services: List<ServiceEntity>,
    transactions: List<TransactionEntity>,
    rewards: List<RewardEntity>,
    onUpdateStatus: (Int, String) -> Unit,
    onLogout: () -> Unit,
    onAddService: (String, String, Long, Int) -> Unit,
    onUpdateService: (ServiceEntity) -> Unit,
    onDeleteService: (ServiceEntity) -> Unit,
    onAddReward: (String, String, Int, String) -> Unit,
    onUpdateReward: (RewardEntity) -> Unit,
    onDeleteReward: (RewardEntity) -> Unit
) {
    var showServiceSheet by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<ServiceEntity?>(null) }
    var showRewardSheet by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableStateOf<RewardEntity?>(null) }

    if (showServiceSheet) {
        ServiceSheet(
            service = selectedService,
            onDismiss = { showServiceSheet = false; selectedService = null },
            onConfirm = { name, desc, price, dur ->
                if (selectedService == null) {
                    onAddService(name, desc, price, dur)
                } else {
                    onUpdateService(selectedService!!.copy(name = name, description = desc, price = price, durationMinutes = dur))
                }
                showServiceSheet = false
                selectedService = null
            }
        )
    }

    if (showRewardSheet) {
        RewardSheet(
            reward = selectedReward,
            onDismiss = { showRewardSheet = false; selectedReward = null },
            onConfirm = { name, desc, points, type ->
                if (selectedReward == null) {
                    onAddReward(name, desc, points, type)
                } else {
                    onUpdateReward(selectedReward!!.copy(name = name, description = desc, pointsNeeded = points, voucherType = type))
                }
                showRewardSheet = false
                selectedReward = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Add Service") },
                        onClick = { showMenu = false; showServiceSheet = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Reward") },
                        onClick = { showMenu = false; showRewardSheet = true }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Business Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AdminSummary(allAppointments, transactions)
            }

            item {
                SectionHeader("Manage Services", actionText = "Add") {
                    showServiceSheet = true
                }
            }

            items(services) { service ->
                AdminServiceItem(service, onEdit = {
                    selectedService = service
                    showServiceSheet = true
                }, onDelete = { onDeleteService(service) })
            }

            item {
                SectionHeader("Manage Rewards", actionText = "Add") {
                    showRewardSheet = true
                }
            }

            items(rewards) { reward ->
                AdminRewardItem(reward, onEdit = {
                    selectedReward = reward
                    showRewardSheet = true
                }, onDelete = { onDeleteReward(reward) })
            }

            item {
                Text("Customer Appointments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (allAppointments.isEmpty()) {
                item { EmptyStateCard("No appointments found", "Check back later for new customer requests.") }
            } else {
                items(allAppointments.sortedByDescending { it.id }) { appointment ->
                    AdminAppointmentItem(appointment, onUpdateStatus)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSheet(
    service: ServiceEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int) -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var desc by remember { mutableStateOf(service?.description ?: "") }
    var price by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (service == null) "Add New Service" else "Edit Service", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (IDR)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (Mins)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name, desc, price.toLongOrNull() ?: 0L, duration.toIntOrNull() ?: 0) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text(if (service == null) "Add Service" else "Save Changes")
            }
        }
    }
}

@Composable
fun AdminServiceItem(service: ServiceEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold)
                Text("Rp${service.price}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun AdminSummary(appointments: List<AppointmentEntity>, transactions: List<TransactionEntity>) {
    val totalRevenue = transactions.sumOf { it.amount }
    val pending = appointments.count { it.status == "Scheduled" }
    val active = appointments.count { it.status == "ACC" }
    val completed = appointments.count { it.status == "Completed" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryItem("Total Revenue", "Rp${totalRevenue/1000}k", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            SummaryItem("New Requests", pending.toString(), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryItem("In Progress", active.toString(), Color(0xFFFFF3E0), Modifier.weight(1f))
            SummaryItem("Completed", completed.toString(), Color(0xFFE8F5E9), Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AdminAppointmentItem(
    appointment: AppointmentEntity,
    onUpdateStatus: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pet: ${appointment.petName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Service: ${appointment.serviceType}", style = MaterialTheme.typography.bodyMedium)
                    Text("Customer ID: ${appointment.memberId}", style = MaterialTheme.typography.labelSmall)
                }
                StatusBadge(appointment.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(appointment.date, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(appointment.time, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (appointment.status == "Scheduled") {
                    Button(
                        onClick = { onUpdateStatus(appointment.id, "ACC") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("ACC")
                    }
                } else if (appointment.status == "ACC") {
                    Button(
                        onClick = { onUpdateStatus(appointment.id, "Completed") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Complete")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "Scheduled" -> MaterialTheme.colorScheme.primaryContainer
        "ACC" -> Color(0xFFE8F5E9)
        "Completed" -> Color(0xFFE3F2FD)
        else -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(color = color, shape = RoundedCornerShape(8.dp)) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardSheet(
    reward: RewardEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(reward?.name ?: "") }
    var desc by remember { mutableStateOf(reward?.description ?: "") }
    var points by remember { mutableStateOf(reward?.pointsNeeded?.toString() ?: "") }
    var type by remember { mutableStateOf(reward?.voucherType ?: "Espresso") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (reward == null) "Add New Reward" else "Edit Reward", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Reward Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text("Points Needed") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(8.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Voucher Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Espresso", "Cappuccino", "Latte").forEach { t ->
                        DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expanded = false })
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name, desc, points.toIntOrNull() ?: 0, type) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && points.isNotBlank()
            ) {
                Text(if (reward == null) "Add Reward" else "Save Changes")
            }
        }
    }
}

@Composable
fun AdminRewardItem(reward: RewardEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.name, fontWeight = FontWeight.Bold)
                Text("${reward.pointsNeeded} Pts • Type: ${reward.voucherType}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(msg: String) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(msg, color = MaterialTheme.colorScheme.error)
    }
}
