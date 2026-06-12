package com.example.petgrooming.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.petgrooming.data.local.dao.AppointmentDao
import com.example.petgrooming.data.local.dao.MemberDao
import com.example.petgrooming.data.local.dao.RewardDao
import com.example.petgrooming.data.local.dao.ServiceDao
import com.example.petgrooming.data.local.dao.UserPetDao
import com.example.petgrooming.data.local.entity.AppointmentEntity
import com.example.petgrooming.data.local.entity.MemberEntity
import com.example.petgrooming.data.local.entity.RewardEntity
import com.example.petgrooming.data.local.entity.ServiceEntity
import com.example.petgrooming.data.local.entity.TransactionEntity
import com.example.petgrooming.data.local.entity.UserPetEntity

@Database(
    entities = [
        MemberEntity::class,
        TransactionEntity::class,
        AppointmentEntity::class,
        UserPetEntity::class,
        ServiceEntity::class,
        RewardEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class PetDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun userPetDao(): UserPetDao
    abstract fun serviceDao(): ServiceDao
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null

        fun getDatabase(context: Context): PetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetDatabase::class.java,
                    "pet_grooming_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
