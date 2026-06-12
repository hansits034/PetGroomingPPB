package com.example.petgrooming.data.model


data class Pet(
    val id: String,
    val name: String,
    val description: String?,
    val sex: String?,
    val breed: String?,
    val age: Int,
    val imageUrl: String?,
    val owner: String
)