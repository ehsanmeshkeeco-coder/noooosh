package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.util.DateTimeUtils
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderStatus
import com.example.presentation.theme.NooshPrimary
import com.example.presentation.theme.NooshSubtleBlue
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.viewmodel.MainViewModel

@Composable
fun RemindersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dashboardState by viewModel.dashboardState.collectAsState()
    val remindersList by viewModel.reminders.collectAsState()
    val todayWakeUpTime by viewModel.todayWakeUpTime.collectAsState()

    val profile = dashboardState?.profile

    var isEnabled by remember(profile) { mutableStateOf(profile?.reminderEnabled ?: true) }
    var selectedInterval by remember(profile) { mutableIntStateOf(profile?.reminderIntervalMinutes ?: 60) }
    val displayWakeUp = todayWakeUpTime ?: profile?.wakeUpTime ?: "08:00"
    val displaySleep = profile?.sleepTime ?: "23:00"

    var soundEnabled by remember(profile) { mutableStateOf(profile?.soundEnabled ?: true) }
    var vibrateEnabled by remember(profile) { mutableStateOf(profile?.vibrateEnabled ?: true) }

    var showSleepTimePicker by remember { mutableStateOf(false) }
    var selectedSleepTime by remember(displaySleep) { mutableStateOf(displaySleep) }
    var showWakeUpTimePicker by remember { mutableStateOf(false) }
    var selectedWakeUpTime by remember(displayWakeUp) { mutableStateOf(displayWakeUp) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("reminders_screen"),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "یادآورهای هوشمند نوشیدن آب",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Card 1: Dynamic Wake-Up & Sleep Hours Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "برنامه بیداری و خواب شما",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Wake up Box (Clickable to edit)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { showWakeUpTimePicker = true }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.WbSunny,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ساعت بیداری", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("ویرایش ✏️", fontSize = 11.sp, color = NooshPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "ساعت ${DateTimeUtils.toPersianDigits(displayWakeUp)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Sleep Box (Clickable to edit)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { showSleepTimePicker = true }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Bedtime,
                                                contentDescription = null,
                                                tint = Color(0xFF6366F1),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ساعت خواب", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("ویرایش ✏️", fontSize = 11.sp, color = NooshPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "ساعت ${DateTimeUtils.toPersianDigits(displaySleep)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Main Reminder Toggle Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnabled) NooshSubtleBlue else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (isEnabled) NooshPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "وضعیت یادآورهای روزانه",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEnabled) "یادآوری منظم فعال است ✓" else "یادآورها موقتاً خاموش هستند",
                                    fontSize = 12.sp,
                                    color = if (isEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                viewModel.updateReminderSettings(checked, selectedInterval, displayWakeUp, displaySleep)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NooshPrimary
                            )
                        )
                    }
                }
            }

            // Card 3: Reminder Interval Selection (Clean 3x2 Grid)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "فاصله بین یادآورها",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val intervalText = when (selectedInterval) {
                                30 -> "هر ۳۰ دقیقه"
                                45 -> "هر ۴۵ دقیقه"
                                60 -> "هر ۱ ساعت"
                                90 -> "هر ۱.۵ ساعت"
                                120 -> "هر ۲ ساعت"
                                180 -> "هر ۳ ساعت"
                                else -> "هر $selectedInterval دقیقه"
                            }
                            Text(
                                text = DateTimeUtils.toPersianDigits(intervalText),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NooshPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val intervalOptions = listOf(
                            listOf(30 to "۳۰ دقیقه", 45 to "۴۵ دقیقه", 60 to "۱ ساعت"),
                            listOf(90 to "۱.۵ ساعت", 120 to "۲ ساعت", 180 to "۳ ساعت")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            intervalOptions.forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowOptions.forEach { (mins, label) ->
                                        val isSelected = selectedInterval == mins
                                        Surface(
                                            onClick = {
                                                selectedInterval = mins
                                                viewModel.updateReminderSettings(isEnabled, mins, displayWakeUp, displaySleep)
                                                Toast.makeText(context, "فاصله یادآوری روی ${DateTimeUtils.toPersianDigits(label)} تنظیم شد ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) NooshPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) NooshPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 4: Sound & Vibration
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NooshPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "پخش صدای زنگ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { checked ->
                                    soundEnabled = checked
                                    profile?.let { viewModel.updateProfile(it.copy(soundEnabled = checked)) }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = NooshPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "لرزش هنگام یادآوری", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Switch(
                                checked = vibrateEnabled,
                                onCheckedChange = { checked ->
                                    vibrateEnabled = checked
                                    profile?.let { viewModel.updateProfile(it.copy(vibrateEnabled = checked)) }
                                }
                            )
                        }
                    }
                }
            }

            // Card 5: Schedule List for Today
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نوبت‌های یادآوری امروز",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (remindersList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "یادآوری برای ساعات باقیمانده امروز تنظیم نشده است.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(remindersList) { reminder ->
                    ReminderItemCard(reminder = reminder)
                }
            }
        }

        // Sleep Time Picker Dialog
        if (showSleepTimePicker) {
            val sleepOptions = listOf("21:00", "21:30", "22:00", "22:30", "23:00", "23:30", "00:00", "00:30", "01:00")
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSleepTimePicker = false },
                title = {
                    Text(
                        text = "انتخاب ساعت خواب شبانه 🌙",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "یادآورها پس از این ساعت خاموش می‌شوند:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 3
                        ) {
                            sleepOptions.forEach { timeStr ->
                                val isSelected = selectedSleepTime == timeStr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSleepTime = timeStr },
                                    label = { Text("ساعت ${DateTimeUtils.toPersianDigits(timeStr)}", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NooshPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateReminderSettings(
                                isEnabled,
                                selectedInterval,
                                displayWakeUp,
                                selectedSleepTime
                            )
                            showSleepTimePicker = false
                            Toast.makeText(context, "ساعت خواب روی ${DateTimeUtils.toPersianDigits(selectedSleepTime)} تنظیم شد ✓", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary)
                    ) {
                        Text("ذخیره ساعت خواب", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showSleepTimePicker = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        // Wake-Up Time Picker Dialog
        if (showWakeUpTimePicker) {
            val wakeUpOptions = listOf("05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30")
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showWakeUpTimePicker = false },
                title = {
                    Text(
                        text = "انتخاب ساعت بیداری صبحگاهی ☀️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "یادآورها از این ساعت آغاز به فعالیت می‌کنند:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 3
                        ) {
                            wakeUpOptions.forEach { timeStr ->
                                val isSelected = selectedWakeUpTime == timeStr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWakeUpTime = timeStr },
                                    label = { Text("ساعت ${DateTimeUtils.toPersianDigits(timeStr)}", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NooshPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateReminderSettings(
                                isEnabled,
                                selectedInterval,
                                selectedWakeUpTime,
                                displaySleep
                            )
                            showWakeUpTimePicker = false
                            Toast.makeText(context, "ساعت بیداری روی ${DateTimeUtils.toPersianDigits(selectedWakeUpTime)} تنظیم شد ✓", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary)
                    ) {
                        Text("ذخیره ساعت بیداری", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showWakeUpTimePicker = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}

@Composable
private fun ReminderItemCard(reminder: Reminder) {
    val isCompleted = reminder.status == ReminderStatus.COMPLETED
    val timeFormatted = DateTimeUtils.formatTime(reminder.scheduledAt)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) Color(0xFFDCFCE7) else NooshSubtleBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (isCompleted) SuccessGreen else NooshPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "نوبت نوشیدن آب (${DateTimeUtils.toPersianDigits(reminder.amountMl.toString())} میل)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCompleted) "نوشیده شد ✓" else "زمان‌بندی شده برای ساعت $timeFormatted",
                        fontSize = 11.sp,
                        color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = DateTimeUtils.toPersianDigits(timeFormatted),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else NooshPrimary
            )
        }
    }
}
