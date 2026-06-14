package com.example.petgrooming.data.api

import retrofit2.http.GET

/**
 * Fetches a random animal image based on the pet type.
 *
 * Two free, public REST APIs are used (no API key required):
 *  - Dogs: https://dog.ceo/dog-api/  -> returns { "message": "<imageUrl>", "status": "success" }
 *  - Cats: https://thecatapi.com/     -> returns [ { "id": "...", "url": "<imageUrl>" } ]
 *
 * Each endpoint uses a full absolute URL, so it overrides the Retrofit baseUrl.
 */
interface AnimalApiService {

    @GET("https://dog.ceo/api/breeds/image/random")
    suspend fun getRandomDog(): DogResponse

    @GET("https://api.thecatapi.com/v1/images/search")
    suspend fun getRandomCat(): List<CatResponse>
}

data class DogResponse(
    val message: String,
    val status: String
)

data class CatResponse(
    val id: String?,
    val url: String
)
