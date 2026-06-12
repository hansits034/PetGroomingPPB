package com.example.petgrooming.data.local.dao

import androidx.room.*
import com.example.petgrooming.data.local.entity.UserPetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPetDao {
    @Query("SELECT * FROM user_pets WHERE memberId = :memberId")
    fun getPetsByMember(memberId: String): Flow<List<UserPetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: UserPetEntity)

    @Update
    suspend fun updatePet(pet: UserPetEntity)

    @Delete
    suspend fun deletePet(pet: UserPetEntity)
}
