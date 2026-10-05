package com.example.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.NooshApplication
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class WakeUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "WakeUpReceiver triggered with action: $action")

        when (action) {
            ACTION_WAKE_UP_PROMPT -> {
                val pendingResult = goAsync()
                val app = context.applicationContext as? NooshApplication
                if (app != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // 1. If user already declared wake-up today, do not notify
                            if (WakeUpManager.isWakeUpConfirmedForToday(context)) {
                                Log.d(TAG, "Wake-up already confirmed today. Suppressing prompt.")
                                WakeUpManager.cancelHourlyWakeUpPrompt(context)
                                return@launch
                            }

                            // 2. Only show between 6:00 AM and 22:00 PM
                            val now = Calendar.getInstance()
                            val hour = now.get(Calendar.HOUR_OF_DAY)
                            if (hour < 6) {
                                // Too early, re-schedule for 6:00 AM
                                WakeUpManager.scheduleHourlyWakeUpPrompt(context)
                                return@launch
                            }

                            val profile = app.userRepository.getUserProfile()
                            if (!profile.reminderEnabled) {
                                Log.d(TAG, "Reminders disabled in profile, suppressing wake-up prompt.")
                                return@launch
                            }

                            val userName = profile.name.takeIf {
                                it.isNotBlank() && it != "کاربر مهمان" && it != "کاربر نوش" && it != "کاربر گرامی"
                            } ?: "دوست من"

                            // 3. Dispatch the hourly wake-up prompt notification
                            NotificationHelper.showMorningWakeUpNotification(
                                context = context,
                                personName = userName
                            )

                            // 4. Re-schedule for the next hour (60 minutes later) until confirmed
                            WakeUpManager.scheduleHourlyWakeUpPrompt(context, delayMinutes = 60)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error handling wake-up prompt: ${e.message}")
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    pendingResult.finish()
                }
            }

            ACTION_CONFIRM_WAKE_UP -> {
                val now = Calendar.getInstance()
                val hour = now.get(Calendar.HOUR_OF_DAY)
                val minute = now.get(Calendar.MINUTE)
                val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)

                WakeUpManager.confirmWakeUp(
                    context = context,
                    wakeUpTime = timeStr,
                    drinkFirstGlass = true
                )
            }
        }
    }

    companion object {
        const val ACTION_WAKE_UP_PROMPT = "com.example.action.WAKE_UP_PROMPT"
        const val ACTION_CONFIRM_WAKE_UP = "com.example.action.CONFIRM_WAKE_UP"
        private const val TAG = "WakeUpReceiver"
    }
}
