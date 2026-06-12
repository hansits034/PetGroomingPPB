package com.example.petgrooming.data.model

data class PetResponse(
    val status: String,
    val totalResults: Int,
    val pet: List<Pet>
)