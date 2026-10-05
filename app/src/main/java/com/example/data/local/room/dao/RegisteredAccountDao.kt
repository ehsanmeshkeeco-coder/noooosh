package com.example.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.room.entity.RegisteredAccountEntity

@Dao
interface RegisteredAccountDao {
    @Query("SELECT * FROM registered_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findByEmail(email: String): RegisteredAccountEntity?

    @Query("SELECT * FROM registered_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun findByUsername(username: String): RegisteredAccountEntity?

    @Query("SELECT * FROM registered_accounts WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun findByIdentifier(identifier: String): RegisteredAccountEntity?

    @Query("SELECT * FROM registered_accounts ORDER BY createdAt DESC")
    suspend fun getAllAccounts(): List<RegisteredAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: RegisteredAccountEntity)

    @Query("DELETE FROM registered_accounts")
    suspend fun deleteAll()
}
