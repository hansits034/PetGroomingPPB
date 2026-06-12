package com.example.petgrooming.data.local.dao

import androidx.room.*
import com.example.petgrooming.data.local.entity.MemberEntity
import com.example.petgrooming.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE isLoggedIn = 1 LIMIT 1")
    fun getMember(): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE email = :email AND password = :password LIMIT 1")
    suspend fun findMember(email: String, password: String): MemberEntity?

    @Query("UPDATE members SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE members SET isLoggedIn = 1 WHERE email = :email")
    suspend fun setLoggedIn(email: String)

    @Query("SELECT * FROM members WHERE memberId = :memberId")
    suspend fun getMemberById(memberId: String): MemberEntity?

    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)
}
