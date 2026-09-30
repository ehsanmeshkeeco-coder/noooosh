package com.example.presentation.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.util.DateTimeUtils
import com.example.data.remote.clerk.AuthState
import com.example.domain.model.StreakInfo
import com.example.presentation.components.HydrationGoalCalculatorDialog
import com.example.presentation.components.HydrationStreakCard
import com.example.presentation.theme.NooshPrimary
import com.example.presentation.theme.NooshSubtleBlue
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.viewmodel.MainViewModel

@Composable
fun ProfileSettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    val profile = dashboardState?.profile
    var currentGoal by remember(profile) { mutableIntStateOf(profile?.dailyWaterGoalMl ?: 2000) }
    var graceDayEnabled by remember(profile) { mutableStateOf(profile?.graceDayEnabled ?: true) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    viewModel.uploadAvatar(bytes, uri.toString())
                    Toast.makeText(context, "تصویر پروفایل ذخیره شد ✓", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در پردازش تصویر", Toast.LENGTH_SHORT).show()
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("profile_settings_screen"),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "پروفایل و تنظیمات کاربری",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مدیریت هدف روزانه آب، حساب کاربری و ظاهر برنامه",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // User Identity Card
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
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile?.profileImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = profile?.profileImageUrl,
                                        contentDescription = "تصویر پروفایل",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "پروفایل",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                // Camera overlay icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "تغییر تصویر",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                val userName = when (val a = authState) {
                                    is AuthState.Authenticated -> a.user.firstName
                                    else -> profile?.name ?: "کاربر گرامی"
                                }
                                val userEmail = when (val a = authState) {
                                    is AuthState.Authenticated -> a.user.email
                                    else -> profile?.email ?: "noosh@user.app"
                                }
                                Text(
                                    text = userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "حساب کاربری", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Streak & Badges Card
            item {
                val streakInfo = dashboardState?.streak ?: StreakInfo(currentStreak = 1, longestStreak = 1, isGraceDayUsed = false)
                HydrationStreakCard(
                    streakInfo = streakInfo,
                    graceDayEnabled = graceDayEnabled
                )
            }

            // Daily Water Goal Card
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
                                text = "هدف روزانه مصرف آب",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${DateTimeUtils.toPersianDigits(currentGoal.toString())} میلی‌لیتر (${DateTimeUtils.toPersianDigits((currentGoal / 250).toString())} لیوان)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = NooshPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // RTL Slider: Fills from Right to Left
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Slider(
                                value = currentGoal.toFloat(),
                                onValueChange = {
                                    currentGoal = (it / 250).toInt() * 250
                                    viewModel.updateDailyGoal(currentGoal)
                                },
                                valueRange = 1000f..4000f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = NooshPrimary,
                                    activeTrackColor = NooshPrimary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Goal Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1500, 2000, 2500, 3000).forEach { goal ->
                                val isSelected = currentGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        currentGoal = goal
                                        viewModel.updateDailyGoal(goal)
                                    },
                                    label = { Text(text = "${DateTimeUtils.toPersianDigits((goal / 250).toString())} لیوان", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NooshPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showCalculatorDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = NooshPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "محاسبه هوشمند بر اساس وزن و تحرک بدنی",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NooshPrimary
                            )
                        }
                    }
                }
            }

            // Biometric Profile Overview Card
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
                                text = "مشخصات فیزیکی شما",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = onNavigateToOnboarding,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NooshSubtleBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("ویرایش مشخصات", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NooshPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("وزن", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${DateTimeUtils.toPersianDigits((profile?.weightKg?.toInt() ?: 70).toString())} کیلوگرم", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("قد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${DateTimeUtils.toPersianDigits((profile?.heightCm?.toInt() ?: 170).toString())} سانت", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("سن", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${DateTimeUtils.toPersianDigits((profile?.age ?: 25).toString())} سال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("جنسیت", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val genderLabel = if (profile?.gender == "female") "خانم" else "آقا"
                                Text(genderLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Streak Grace Day Card
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
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "روز استراحت زنجیره (امان‌نامه)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "جلوگیری از قطع شدن زنجیره روزانه در صورت فراموشی موقت",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = graceDayEnabled,
                                onCheckedChange = { checked ->
                                    graceDayEnabled = checked
                                    profile?.let { viewModel.updateProfile(it.copy(graceDayEnabled = checked)) }
                                }
                            )
                        }
                    }
                }
            }

            // Theme Mode Card
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
                            text = "ظاهر برنامه (حالت روز و شب)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val selectedTheme = profile?.themeMode ?: "system"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("light", "روز (روشن)", "☀️"),
                                Triple("dark", "شب (تاریک)", "🌙"),
                                Triple("system", "خودکار", "⚙️")
                            ).forEach { (mode, label, icon) ->
                                val isSelected = selectedTheme == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(text = "$icon $label", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NooshPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalculatorDialog) {
        HydrationGoalCalculatorDialog(
            initialWeightKg = profile?.weightKg ?: 70f,
            initialActivityLevel = profile?.activityLevel ?: "moderate",
            initialClimate = profile?.climate ?: "temperate",
            onApply = { targetGoalMl, weightKg, activityLevel, climate ->
                currentGoal = targetGoalMl
                viewModel.updateDailyGoal(targetGoalMl)
                profile?.let {
                    viewModel.updateProfile(
                        it.copy(
                            dailyWaterGoalMl = targetGoalMl,
                            weightKg = weightKg,
                            activityLevel = activityLevel,
                            climate = climate
                        )
                    )
                }
                showCalculatorDialog = false
                Toast.makeText(
                    context,
                    "هدف مصرف روزانه به ${DateTimeUtils.toPersianDigits(targetGoalMl.toString())} میلی‌لیتر تنظیم شد!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onDismiss = { showCalculatorDialog = false }
        )
    }
}
