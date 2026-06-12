package com.example.petgrooming.data.repository

import com.example.petgrooming.data.local.dao.MemberDao
import com.example.petgrooming.data.local.entity.MemberEntity
import com.example.petgrooming.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class PetRepository(private val memberDao: MemberDao) {
    fun getMember(): Flow<MemberEntity?> = memberDao.getMember()
    
    fun getTransactions(): Flow<List<TransactionEntity>> = memberDao.getAllTransactions()

    suspend fun registerMember(member: MemberEntity) = memberDao.insertMember(member)

    suspend fun updateMember(member: MemberEntity) = memberDao.updateMember(member)

    suspend fun addTransaction(transaction: TransactionEntity) = memberDao.insertTransaction(transaction)
}
