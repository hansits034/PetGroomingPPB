package com.example.petgrooming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_pets")
data class UserPetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val type: String, // Dog, Cat, etc.
    val gender: String? = null,
    val description: String? = null,
    val weight: Double? = null,
    val imageUrl: String? = null
)
