package com.example.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registered_accounts",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class RegisteredAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val username: String,
    val name: String,
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
