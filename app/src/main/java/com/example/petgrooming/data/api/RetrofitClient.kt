package com.example.petgrooming.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://newsapi.org/v2/"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Service for fetching random pet images. The endpoints use absolute URLs,
    // so this baseUrl is only a (required) placeholder.
    val animalApiService: AnimalApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://dog.ceo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnimalApiService::class.java)
    }
}