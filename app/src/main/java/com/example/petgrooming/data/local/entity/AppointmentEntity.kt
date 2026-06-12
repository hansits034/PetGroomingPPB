package com.example.petgrooming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: String,
    val petName: String,
    val serviceType: String,
    val date: String,
    val time: String,
    val status: String = "Scheduled", // Scheduled, ACC, Completed, Cancelled
    val useVoucher: Boolean = false
)
