package com.example.petgrooming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val pointsNeeded: Int,
    val voucherType: String // "Espresso", "Cappuccino", "Latte"
)
