package com.example.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.NooshApplication
import com.example.core.util.DateTimeUtils
import com.example.notifications.NotificationHelper
import com.example.workers.WaterReminderWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Manages daily wake-up confirmation and hourly wake-up reminder prompts.
 * Until the user confirms they have woken up for the day (from 6:00 AM onwards),
 * regular water drinking reminders and alarms are suppressed, and an hourly
 * prompt notification is dispatched to remind the user to declare their wake-up.
 */
object WakeUpManager {
    private const val TAG = "WakeUpManager"
    private const val PREFS_NAME = "noosh_wake_up_prefs"
    private const val KEY_LAST_CONFIRMED_DATE = "last_confirmed_wake_up_date"
    private const val KEY_TODAY_WAKE_UP_TIME = "today_confirmed_wake_up_time"
    private const val WAKE_UP_ALARM_REQUEST_CODE = 4001

    /**
     * Checks if the user has confirmed waking up today.
     */
    fun isWakeUpConfirmedForToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_CONFIRMED_DATE, null)
        val today = DateTimeUtils.getTodayDateString()
        return lastDate == today
    }

    /**
     * Returns the confirmed wake-up time for today (e.g. "07:30"), or null if not confirmed.
     */
    fun getTodayConfirmedWakeUpTime(context: Context): String? {
        if (!isWakeUpConfirmedForToday(context)) return null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TODAY_WAKE_UP_TIME, null)
    }

    /**
     * Confirms wake-up for today with the specified time, cancels hourly wake-up notifications,
     * and initializes the full day's regular water intake schedule starting from this moment.
     */
    fun confirmWakeUp(
        context: Context,
        wakeUpTime: String,
        drinkFirstGlass: Boolean = false,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        val today = DateTimeUtils.getTodayDateString()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_CONFIRMED_DATE, today)
            .putString(KEY_TODAY_WAKE_UP_TIME, wakeUpTime)
            .apply()

        // 1. Cancel ongoing hourly wake-up reminders & clear notification
        cancelHourlyWakeUpPrompt(context)
        NotificationHelper.cancelWakeUpNotification(context)

        // 2. Activate regular water reminders starting from wake up
        val app = context.applicationContext as? NooshApplication ?: return
        coroutineScope.launch {
            try {
                val profile = app.userRepository.getUserProfile()
                val updated = profile.copy(wakeUpTime = wakeUpTime)
                app.userRepository.updateProfile(updated)

                // Schedule database reminders and next exact alarm
                app.reminderRepository.scheduleDailyReminders(updated)
                if (updated.reminderEnabled) {
                    app.reminderScheduler.scheduleNextPendingReminder()
                    WaterReminderWorkScheduler.schedulePeriodicReminders(
                        context = context,
                        intervalMinutes = updated.reminderIntervalMinutes.coerceAtLeast(15)
                    )
                }

                if (drinkFirstGlass) {
                    app.addWaterIntakeUseCase(
                        amountMl = 250,
                        source = "morning_wake_up",
                        reminderId = null
                    )
                }
                Log.d(TAG, "Wake up confirmed for today ($today at $wakeUpTime). Regular reminders activated.")
            } catch (e: Exception) {
                Log.e(TAG, "Error activating reminders after wake-up confirmation: ${e.message}")
            }
        }
    }

    /**
     * Schedules an hourly wake-up prompt (starting from 6:00 AM) if wake-up hasn't been confirmed yet.
     */
    fun scheduleHourlyWakeUpPrompt(context: Context, delayMinutes: Long? = null) {
        if (isWakeUpConfirmedForToday(context)) {
            cancelHourlyWakeUpPrompt(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WakeUpReceiver::class.java).apply {
            action = WakeUpReceiver.ACTION_WAKE_UP_PROMPT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WAKE_UP_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)

        val triggerAtMillis: Long = when {
            delayMinutes != null -> {
                System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
            }
            hour < 6 -> {
                // If before 6:00 AM, schedule for exactly 6:00 AM today
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calendar.timeInMillis
            }
            else -> {
                // Already 6:00 AM or later: repeat every 1 hour (60 minutes)
                System.currentTimeMillis() + (60 * 60 * 1000L)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Hourly wake-up prompt scheduled for $triggerAtMillis")
        } catch (e: Exception) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.w(TAG, "Fallback alarm scheduling failed: ${fallbackEx.message}")
            }
        }
    }

    /**
     * Cancels any pending hourly wake-up prompt alarm.
     */
    fun cancelHourlyWakeUpPrompt(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WakeUpReceiver::class.java).apply {
            action = WakeUpReceiver.ACTION_WAKE_UP_PROMPT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WAKE_UP_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
