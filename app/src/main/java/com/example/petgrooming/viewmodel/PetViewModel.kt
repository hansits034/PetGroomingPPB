package com.example.petgrooming.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petgrooming.data.api.RetrofitClient
import com.example.petgrooming.data.local.PetDatabase
import com.example.petgrooming.data.local.entity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class PetUiState {
    object Loading : PetUiState()
    data class Success(
        val member: MemberEntity,
        val transactions: List<TransactionEntity> = emptyList(),
        val appointments: List<AppointmentEntity> = emptyList(),
        val pets: List<UserPetEntity> = emptyList(),
        val services: List<ServiceEntity> = emptyList(),
        val rewards: List<RewardEntity> = emptyList(),
        val allAppointments: List<AppointmentEntity> = emptyList() // Added for Admin
    ) : PetUiState()
    object NotRegistered : PetUiState()
    object LoggedOut : PetUiState()
    data class Error(val message: String, val isPersistent: Boolean = true) : PetUiState()
}

data class Reward(
    val pointsNeeded: Int,
    val name: String,
    val description: String
)

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PetDatabase.getDatabase(application)
    private val memberDao = db.memberDao()
    private val appointmentDao = db.appointmentDao()
    private val userPetDao = db.userPetDao()
    private val serviceDao = db.serviceDao()
    private val rewardDao = db.rewardDao()

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Loading)
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    init {
        observeMemberData()
        seedServices()
        seedRewards()
        seedAdmin()
    }

    private fun seedRewards() {
        viewModelScope.launch {
            rewardDao.getAllRewards().first().let { currentRewards ->
                if (currentRewards.isEmpty()) {
                    val initialRewards = listOf(
                        RewardEntity(name = "Espresso Wash (Basic)", description = "Mandi standar untuk pet", pointsNeeded = 50, voucherType = "Espresso"),
                        RewardEntity(name = "Cappuccino Groom (Full)", description = "Grooming lengkap & potong kuku", pointsNeeded = 100, voucherType = "Cappuccino"),
                        RewardEntity(name = "Latte Spa (Premium)", description = "Perawatan spa & pijat relaksasi", pointsNeeded = 150, voucherType = "Latte")
                    )
                    initialRewards.forEach { rewardDao.insertReward(it) }
                }
            }
        }
    }

    private fun seedServices() {
        viewModelScope.launch {
            serviceDao.getAllServices().first().let { currentServices ->
                if (currentServices.isEmpty()) {
                    val initialServices = listOf(
                        ServiceEntity(name = "Espresso Wash", description = "Bath, brush, and blow dry", price = 100000, durationMinutes = 45),
                        ServiceEntity(name = "Cappuccino Groom", description = "Bath, haircut, nail trim, and ear cleaning", price = 250000, durationMinutes = 90),
                        ServiceEntity(name = "Latte Spa", description = "Perawatan spa & pijat relaksasi", price = 350000, durationMinutes = 120),
                        ServiceEntity(name = "Basic Wash", description = "Bath, brush, and blow dry", price = 80000, durationMinutes = 30),
                        ServiceEntity(name = "Medicated Bath", description = "Special shampoo for shampoo for skin conditions", price = 180000, durationMinutes = 50)
                    )
                    serviceDao.insertServices(initialServices)
                }
            }
        }
    }

    private fun seedAdmin() {
        viewModelScope.launch {
            val adminEmail = "admin@admin.com"
            // Check if admin exists by trying to find them
            val admin = memberDao.findMember(adminEmail, "adminadmin")
            if (admin == null) {
                // We also check if the email exists at all to avoid duplicate emails if password changed
                // But for simplicity in this exercise, I'll just check existence of "admin" role or email
                val newAdmin = MemberEntity(
                    memberId = "ADMIN-001",
                    name = "Administrator",
                    email = adminEmail,
                    phone = "0000000000",
                    password = "adminadmin",
                    role = "ADMIN",
                    isLoggedIn = false
                )
                memberDao.insertMember(newAdmin)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMemberData() {
        viewModelScope.launch {
            memberDao.getMember().flatMapLatest { member ->
                when {
                    member == null -> flowOf(PetUiState.NotRegistered)
                    !member.isLoggedIn -> flowOf(PetUiState.LoggedOut)
                    else -> {
                        if (member.role == "ADMIN") {
                            combine(
                                memberDao.getAllTransactions(),
                                appointmentDao.getAllAppointments(),
                                userPetDao.getPetsByMember(member.memberId),
                                serviceDao.getAllServices(),
                                rewardDao.getAllRewards()
                            ) { transactions, appointments, pets, services, rewards ->
                                PetUiState.Success(
                                    member = member,
                                    transactions = transactions,
                                    appointments = emptyList(), // Admin sees allAppointments instead
                                    pets = pets,
                                    services = services,
                                    rewards = rewards,
                                    allAppointments = appointments
                                )
                            }
                        } else {
                            combine(
                                memberDao.getAllTransactions(),
                                appointmentDao.getAppointmentsByMember(member.memberId),
                                userPetDao.getPetsByMember(member.memberId),
                                serviceDao.getAllServices(),
                                rewardDao.getAllRewards()
                            ) { transactions, appointments, pets, services, rewards ->
                                PetUiState.Success(
                                    member = member,
                                    transactions = transactions,
                                    appointments = appointments,
                                    pets = pets,
                                    services = services,
                                    rewards = rewards
                                )
                            }
                        }
                    }
                }
            }.catch { e ->
                _uiState.value = PetUiState.Error(e.message ?: "Unknown Error")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: Int, status: String) {
        viewModelScope.launch {
            appointmentDao.updateStatus(appointmentId, status)
            
            // If status becomes "Completed", automatically add a transaction and points
            if (status == "Completed") {
                val appointment = appointmentDao.getAppointmentById(appointmentId)
                appointment?.let { appt ->
                    val service = serviceDao.getAllServices().first().find { it.name == appt.serviceType }
                    val amount = if (appt.useVoucher) 0L else (service?.price ?: 100000L)
                    addTransactionForMember(appt.memberId, amount)
                }
            }
        }
    }

    private suspend fun addTransactionForMember(memberId: String, amount: Long) {
        val points = if (amount > 0) (amount / 10000).toInt() else 0
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        
        val newTransaction = TransactionEntity(
            memberId = memberId,
            date = currentDate,
            amount = amount,
            pointsEarned = points
        )
        
        memberDao.insertTransaction(newTransaction)
        
        // Find and update the specific member
        val member = memberDao.getMemberById(memberId)
        member?.let {
            val updatedMember = it.copy(
                totalPoints = it.totalPoints + points
            )
            memberDao.updateMember(updatedMember)
        }
    }

    fun registerMember(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            memberDao.logoutAll() // Ensure no other session is active
            val newMember = MemberEntity(
                memberId = "PG-" + System.currentTimeMillis().toString().takeLast(6),
                name = name,
                email = email,
                phone = phone,
                password = password,
                role = "USER", // Explicitly set role
                isLoggedIn = true // Log in automatically after registration
            )
            memberDao.insertMember(newMember)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val member = memberDao.findMember(email, password)
            if (member != null) {
                memberDao.logoutAll() // Clear any existing sessions
                memberDao.setLoggedIn(email)
            } else {
                _uiState.value = PetUiState.Error("Invalid email or password", isPersistent = false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            memberDao.logoutAll()
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            memberDao.updateMember(member)
        }
    }

    fun addPet(name: String, breed: String, age: Int, type: String, gender: String?, description: String?) {
        val currentState = _uiState.value
        if (currentState is PetUiState.Success) {
            viewModelScope.launch {
                // Fetch a random image from the API based on the pet type (dog/cat/...)
                val imageUrl = fetchPetImage(type)
                val pet = UserPetEntity(
                    memberId = currentState.member.memberId,
                    name = name,
                    breed = breed,
                    age = age,
                    type = type,
                    gender = gender,
                    description = description,
                    imageUrl = imageUrl
                )
                userPetDao.insertPet(pet)
            }
        }
    }

    fun updatePet(pet: UserPetEntity) {
        viewModelScope.launch {
            // If the pet has no image yet (new pet from before this feature, or its
            // type was just changed), fetch a fresh one that matches the type.
            val petToSave = if (pet.imageUrl == null) pet.copy(imageUrl = fetchPetImage(pet.type)) else pet
            userPetDao.updatePet(petToSave)
        }
    }

    /**
     * Calls a public REST API to get a random image URL for the given pet type.
     * Returns null on any network/parse error so adding a pet never fails because
     * of the image lookup.
     */
    private suspend fun fetchPetImage(type: String): String? {
        return try {
            when (type.trim().lowercase()) {
                "dog" -> RetrofitClient.animalApiService.getRandomDog().message
                "cat" -> RetrofitClient.animalApiService.getRandomCat().firstOrNull()?.url
                else -> {
                    val tag = type.trim().lowercase().ifBlank { "animal" }
                    "https://loremflickr.com/640/480/$tag"
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun deletePet(pet: UserPetEntity) {
        viewModelScope.launch {
            userPetDao.deletePet(pet)
        }
    }

    fun scheduleAppointment(petName: String, serviceType: String, date: String, time: String, useVoucher: Boolean = false) {
        val currentState = _uiState.value
        if (currentState is PetUiState.Success) {
            viewModelScope.launch {
                // If using voucher, decrement it from member
                if (useVoucher) {
                    val updatedMember = when {
                        serviceType.contains("Espresso", ignoreCase = true) && currentState.member.espressoVouchers > 0 -> {
                            currentState.member.copy(espressoVouchers = currentState.member.espressoVouchers - 1)
                        }
                        serviceType.contains("Cappuccino", ignoreCase = true) && currentState.member.cappuccinoVouchers > 0 -> {
                            currentState.member.copy(cappuccinoVouchers = currentState.member.cappuccinoVouchers - 1)
                        }
                        serviceType.contains("Latte", ignoreCase = true) && currentState.member.latteVouchers > 0 -> {
                            currentState.member.copy(latteVouchers = currentState.member.latteVouchers - 1)
                        }
                        else -> currentState.member // Should not happen if UI is correct
                    }
                    if (updatedMember != currentState.member) {
                        memberDao.updateMember(updatedMember)
                    }
                }
                
                val appointment = AppointmentEntity(
                    memberId = currentState.member.memberId,
                    petName = petName,
                    serviceType = serviceType,
                    date = date,
                    time = time,
                    useVoucher = useVoucher
                )
                appointmentDao.insertAppointment(appointment)
            }
        }
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            appointmentDao.updateAppointment(appointment)
        }
    }

    fun cancelAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            appointmentDao.updateAppointment(appointment.copy(status = "Cancelled"))
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            appointmentDao.deleteAppointment(appointment)
        }
    }

    fun addTransaction(amount: Long) {
        val currentState = _uiState.value
        if (currentState is PetUiState.Success) {
            viewModelScope.launch {
                val points = (amount / 10000).toInt()
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val currentDate = sdf.format(Date())
                
                val newTransaction = TransactionEntity(
                    memberId = currentState.member.memberId,
                    date = currentDate,
                    amount = amount,
                    pointsEarned = points
                )
                
                memberDao.insertTransaction(newTransaction)
                
                val updatedMember = currentState.member.copy(
                    totalPoints = currentState.member.totalPoints + points
                )
                memberDao.updateMember(updatedMember)
            }
        }
    }

    fun redeemPoints(reward: RewardEntity) {
        val currentState = _uiState.value
        if (currentState is PetUiState.Success && currentState.member.totalPoints >= reward.pointsNeeded) {
            viewModelScope.launch {
                val updatedMember = when (reward.voucherType) {
                    "Espresso" -> currentState.member.copy(
                        totalPoints = currentState.member.totalPoints - reward.pointsNeeded,
                        espressoVouchers = currentState.member.espressoVouchers + 1
                    )
                    "Cappuccino" -> currentState.member.copy(
                        totalPoints = currentState.member.totalPoints - reward.pointsNeeded,
                        cappuccinoVouchers = currentState.member.cappuccinoVouchers + 1
                    )
                    "Latte" -> currentState.member.copy(
                        totalPoints = currentState.member.totalPoints - reward.pointsNeeded,
                        latteVouchers = currentState.member.latteVouchers + 1
                    )
                    else -> currentState.member
                }
                if (updatedMember != currentState.member) {
                    memberDao.updateMember(updatedMember)
                }
            }
        }
    }

    // CRUD for Rewards
    fun addReward(name: String, description: String, points: Int, voucherType: String) {
        viewModelScope.launch {
            rewardDao.insertReward(RewardEntity(name = name, description = description, pointsNeeded = points, voucherType = voucherType))
        }
    }

    fun updateReward(reward: RewardEntity) {
        viewModelScope.launch {
            rewardDao.updateReward(reward)
        }
    }

    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch {
            rewardDao.deleteReward(reward)
        }
    }

    // CRUD for Services
    fun addService(name: String, description: String, price: Long, duration: Int) {
        viewModelScope.launch {
            serviceDao.insertService(ServiceEntity(name = name, description = description, price = price, durationMinutes = duration))
        }
    }

    fun updateService(service: ServiceEntity) {
        viewModelScope.launch {
            serviceDao.updateService(service)
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            serviceDao.deleteService(service)
        }
    }
}
