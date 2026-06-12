package com.example.petgrooming.data.api

import com.example.petgrooming.data.model.PetResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("pets")
    suspend fun getPets(): PetResponse
}