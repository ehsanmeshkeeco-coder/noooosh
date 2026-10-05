package com.example.data.repository

import com.example.data.local.room.dao.RegisteredAccountDao
import com.example.data.local.room.dao.UserProfileDao
import com.example.data.local.room.entity.RegisteredAccountEntity
import com.example.data.local.room.entity.UserProfileEntity
import com.example.domain.model.UserProfile
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userProfileDao: UserProfileDao,
    private val registeredAccountDao: RegisteredAccountDao? = null
) : UserRepository {

    override fun getUserProfileFlow(userId: String): Flow<UserProfile?> {
        return userProfileDao.getUserProfileFlow(userId).map { it?.toDomain() }
    }

    override suspend fun getUserProfile(userId: String): UserProfile {
        var profile = userProfileDao.getUserProfile(userId)
        if (profile == null) {
            profile = UserProfileEntity(
                id = userId,
                name = "کاربر گرامی",
                email = "user@noosh.app",
                dailyWaterGoalMl = 2000,
                reminderIntervalMinutes = 60,
                reminderEnabled = true,
                wakeUpTime = "08:00",
                sleepTime = "23:00"
            )
            userProfileDao.insertOrUpdateProfile(profile)
        }
        return profile.toDomain()
    }

    override suspend fun initializeDefaultProfileIfNeeded(userId: String) {
        val existing = userProfileDao.getUserProfile(userId)
        if (existing == null) {
            val defaultEntity = UserProfileEntity(
                id = userId,
                name = "کاربر گرامی",
                email = "user@noosh.app",
                dailyWaterGoalMl = 2000,
                reminderIntervalMinutes = 60,
                reminderEnabled = true,
                wakeUpTime = "08:00",
                sleepTime = "23:00"
            )
            userProfileDao.insertOrUpdateProfile(defaultEntity)
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val entity = UserProfileEntity.fromDomain(profile.copy(updatedAt = System.currentTimeMillis()))
        userProfileDao.insertOrUpdateProfile(entity)
    }

    override suspend fun updateGoal(goalMl: Int, userId: String) {
        val current = getUserProfile(userId)
        updateProfile(current.copy(dailyWaterGoalMl = goalMl))
    }

    override suspend fun setReminderEnabled(enabled: Boolean, userId: String) {
        val current = getUserProfile(userId)
        updateProfile(current.copy(reminderEnabled = enabled))
    }

    override suspend fun updateReminderSettings(
        enabled: Boolean,
        intervalMinutes: Int,
        startTime: String,
        endTime: String,
        userId: String
    ) {
        val current = getUserProfile(userId)
        updateProfile(
            current.copy(
                reminderEnabled = enabled,
                reminderIntervalMinutes = intervalMinutes,
                wakeUpTime = startTime,
                sleepTime = endTime
            )
        )
    }

    override suspend fun updateThemeMode(themeMode: String, userId: String) {
        val current = getUserProfile(userId)
        updateProfile(current.copy(themeMode = themeMode))
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        // 1. Check local registered accounts
        val localAccount = registeredAccountDao?.findByEmail(cleanEmail)
        if (localAccount != null) return true

        // 2. Check active user profile
        val currentProfile = userProfileDao.getUserProfile()
        if (currentProfile != null && currentProfile.email.trim().lowercase() == cleanEmail && currentProfile.email != "user@noosh.app" && currentProfile.email != "user@example.com") {
            return true
        }

        return false
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (cleanUsername.isBlank()) return false

        // 1. Check local registered accounts
        val localAccount = registeredAccountDao?.findByUsername(cleanUsername)
        if (localAccount != null) return true

        // 2. Check active user profile
        val currentProfile = userProfileDao.getUserProfile()
        if (currentProfile != null && currentProfile.username.trim().lowercase().removePrefix("@") == cleanUsername) {
            return true
        }

        return false
    }

    override suspend fun saveRegisteredAccount(name: String, username: String, email: String, passwordHash: String) {
        val cleanUsername = username.trim().removePrefix("@")
        val cleanEmail = email.trim()
        val account = RegisteredAccountEntity(
            id = "acc_${System.currentTimeMillis()}_${cleanUsername.hashCode().toUInt()}",
            email = cleanEmail,
            username = cleanUsername,
            name = name.trim(),
            passwordHash = passwordHash,
            createdAt = System.currentTimeMillis()
        )
        registeredAccountDao?.insertAccount(account)
    }
}

