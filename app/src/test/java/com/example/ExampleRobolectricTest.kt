package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.util.DateTimeUtils
import com.example.domain.model.WaterIntake
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName == "نوش" || appName == "Noosh")
    }

    @Test
    fun `test persian digits conversion`() {
        val englishDigits = "123450"
        val persian = DateTimeUtils.toPersianDigits(englishDigits)
        assertEquals("۱۲۳۴۵۰", persian)
    }

    @Test
    fun `test wake up manager status and confirmation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Initially or on fresh day, wake-up is not confirmed
        val prefs = context.getSharedPreferences("noosh_wake_up_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val isConfirmedInitial = com.example.alarms.WakeUpManager.isWakeUpConfirmedForToday(context)
        assertEquals(false, isConfirmedInitial)

        // Confirm wake up
        com.example.alarms.WakeUpManager.confirmWakeUp(
            context = context,
            wakeUpTime = "07:15",
            drinkFirstGlass = false
        )

        val isConfirmedAfter = com.example.alarms.WakeUpManager.isWakeUpConfirmedForToday(context)
        assertEquals(true, isConfirmedAfter)

        val confirmedTime = com.example.alarms.WakeUpManager.getTodayConfirmedWakeUpTime(context)
        assertEquals("07:15", confirmedTime)
    }

    @Test
    fun `test wake up prompt alarm scheduling and cancellation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("noosh_wake_up_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // Should schedule without error
        com.example.alarms.WakeUpManager.scheduleHourlyWakeUpPrompt(context, delayMinutes = 60)
        // Should cancel without error
        com.example.alarms.WakeUpManager.cancelHourlyWakeUpPrompt(context)
    }
}
