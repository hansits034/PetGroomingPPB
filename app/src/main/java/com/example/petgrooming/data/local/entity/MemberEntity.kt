package com.example.petgrooming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val memberId: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String = "123456",
    val isLoggedIn: Boolean = false,
    val role: String = "USER", // USER or ADMIN
    val status: String = "Silver",
    val totalPoints: Int = 0,
    val espressoVouchers: Int = 0,
    val cappuccinoVouchers: Int = 0,
    val latteVouchers: Int = 0
)
