package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.DateTimeUtils
import com.example.domain.model.AlertFilterPolicy
import com.example.domain.model.AlertSeverity
import com.example.domain.model.CompanionConnectionStatus
import com.example.domain.model.CompanionMonitoringData
import com.example.domain.model.EventDeliveryStatus
import com.example.domain.model.HealthAlertEvent
import com.example.domain.model.HealthCompanionConnection
import com.example.domain.model.HealthCompanionStatus
import com.example.domain.model.HealthEventType
import com.example.domain.model.HealthStatusEvaluation
import com.example.presentation.theme.NooshPrimary
import com.example.presentation.theme.NooshSubtleBlue
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.viewmodel.MainViewModel

@Composable
fun HealthCompanionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val companionStatus by viewModel.companionStatus.collectAsState()
    val recentEvents by viewModel.companionEvents.collectAsState()
    val activeCompanion by viewModel.activeCompanion.collectAsState()
    val companionMonitoring by viewModel.companionMonitoring.collectAsState()
    val isRefreshingMonitoring by viewModel.isRefreshingMonitoring.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: مانیتورینگ همراه من, 1: وضعیت و اتاق من
    var showJoinDialog by remember { mutableStateOf(false) }
    var generatedRoomCode by remember { mutableStateOf<String?>(null) }
    var isCreatingRoom by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MonitorHeart,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "همراه سلامت و مانیتورینگ مراقبت",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پایش زنده وضعیت مصرف آب همراه سلامت، اشتراک داده‌ها و یادآوری هوشمند",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabPillButton(
                    title = "مانیتورینگ همراه من",
                    icon = Icons.Filled.People,
                    isSelected = selectedTab == 0,
                    badgeCount = if (activeCompanion != null) "۱" else null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedTab = 0
                        viewModel.refreshCompanionMonitoring()
                    }
                )
                TabPillButton(
                    title = "وضعیت و اتاق من",
                    icon = Icons.Filled.Person,
                    isSelected = selectedTab == 1,
                    badgeCount = null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedTab = 1
                        viewModel.refreshCompanionStatus()
                    }
                )
            }
        }

        // =====================================================================
        // TAB 0: COMPANION MONITORING (پایش همراه سلامت)
        // =====================================================================
        if (selectedTab == 0) {
            if (activeCompanion != null) {
                item {
                    CompanionMonitoringLiveCard(
                        data = companionMonitoring,
                        activeCompanion = activeCompanion!!,
                        isRefreshing = isRefreshingMonitoring,
                        onRefresh = { viewModel.refreshCompanionMonitoring() },
                        onSendNudge = {
                            viewModel.sendCompanionNudge { success ->
                                if (success) {
                                    Toast.makeText(context, "سقلمه یادآوری آب برای همراه ارسال شد ✓", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "یادآوری در صف ارسال قرار گرفت", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSendCheer = {
                            viewModel.sendCompanionCheer { success ->
                                if (success) {
                                    Toast.makeText(context, "پیام تشویق و آفرین برای همراه ارسال شد 🎉", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "پیام تشویق در صف ذخیره شد", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                // Companion Recent Intakes Timeline
                item {
                    CompanionRecentIntakesSection(data = companionMonitoring)
                }

                // Companion Health Alerts
                item {
                    CompanionAlertsSection(data = companionMonitoring)
                }
            } else {
                // Empty state when no companion is connected yet
                item {
                    CompanionEmptyMonitoringCard(
                        onCreateRoom = {
                            isCreatingRoom = true
                            viewModel.createCompanionRoom { code ->
                                generatedRoomCode = code
                                isCreatingRoom = false
                                selectedTab = 1
                                Toast.makeText(context, "کد اتاق ایجاد شد! آن را برای همراه خود بفرستید", Toast.LENGTH_LONG).show()
                            }
                        },
                        onJoinRoom = { showJoinDialog = true }
                    )
                }
            }
        }

        // =====================================================================
        // TAB 1: MY STATUS & ROOMS (وضعیت و اتاق من)
        // =====================================================================
        if (selectedTab == 1) {
            // Section 1: Health Status Card
            item {
                companionStatus?.let { status ->
                    CompanionStatusCard(status = status)
                }
            }

            // Section 2: Room Creation & Invite Code (Supabase)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "اتاق همراه سلامت و کد دعوت",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای همراه خود اتاق بسازید و کد ۶ رقمی را ارسال کنید، یا با داشتن کد دعوت به اتاق همراه ملحق شوید.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeCompanion != null && activeCompanion?.status == CompanionConnectionStatus.CONNECTED) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "همراه متصل: ${activeCompanion?.companionName}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                        Text(
                                            text = "اتصال با سرور فعال است ✓",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { activeCompanion?.let { viewModel.disconnectCompanion(it.id) } },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Text("قطع ارتباط", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isCreatingRoom = true
                                        viewModel.createCompanionRoom { code ->
                                            generatedRoomCode = code
                                            isCreatingRoom = false
                                            Toast.makeText(context, "کد اتاق با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCreatingRoom) "در حال ساخت..." else "ساخت اتاق",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showJoinDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ورود با کد دعوت",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            generatedRoomCode?.let { code ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "کد دعوت اختصاصی اتاق شما:",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = DateTimeUtils.toPersianDigits(code),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 4.sp
                                            )
                                        }

                                        IconButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("کد دعوت نوش", code)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "کد اتاق کپی شد", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "کپی کد",
                                                tint = NooshPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Companion Policy Settings
            item {
                activeCompanion?.let { connection ->
                    CompanionConnectionCard(
                        connection = connection,
                        onTogglePause = { viewModel.togglePauseCompanion(connection.id, connection.status == CompanionConnectionStatus.CONNECTED) },
                        onPolicyChange = { viewModel.updateCompanionPolicy(connection.id, it) }
                    )
                }
            }

            // Section 4: Audit Trail of Health Events
            item {
                Text(
                    text = "سوابق هشدارهای سلامت من",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "هنوز رویدادی ثبت نشده است. سوابق مصرف آب به صورت خودکار در این بخش ثبت می‌شود.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recentEvents) { event ->
                    HealthEventItem(event = event)
                }
            }
        }
    }

    if (showJoinDialog) {
        JoinCompanionRoomDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code, companionName ->
                viewModel.joinCompanionRoom(code, companionName) { success ->
                    if (success) {
                        Toast.makeText(context, "با موفقیت به اتاق همراه پیوستید ✓", Toast.LENGTH_SHORT).show()
                        selectedTab = 0
                    } else {
                        Toast.makeText(context, "اتصال انجام شد و در صف ذخیره گردید", Toast.LENGTH_SHORT).show()
                    }
                }
                showJoinDialog = false
            }
        )
    }
}

@Composable
private fun TabPillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeCount: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NooshPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            badgeCount?.let { count ->
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DateTimeUtils.toPersianDigits(count),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =============================================================================
// COMPANION LIVE MONITORING COMPONENTS
// =============================================================================

@Composable
private fun CompanionMonitoringLiveCard(
    data: CompanionMonitoringData?,
    activeCompanion: HealthCompanionConnection,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSendNudge: () -> Unit,
    onSendCheer: () -> Unit
) {
    val companionName = data?.companionName ?: activeCompanion.companionName
    val todayWater = data?.todayWaterMl ?: 0
    val goal = data?.dailyGoalMl ?: 2000
    val percentage = data?.goalPercentage ?: if (goal > 0) ((todayWater.toFloat() / goal) * 100).toInt() else 0
    val evaluation = data?.evaluation ?: HealthStatusEvaluation.ON_TRACK

    val (statusText, statusBg, statusColor) = when (evaluation) {
        HealthStatusEvaluation.GOAL_REACHED -> Triple("هدف روزانه محقق شد 🎉", Color(0xFFDCFCE7), Color(0xFF16A34A))
        HealthStatusEvaluation.ON_TRACK -> Triple("هیدراتاسیون مطلوب ✓", Color(0xFFE0F2FE), Color(0xFF0284C7))
        HealthStatusEvaluation.BEHIND -> Triple("عقب‌تر از برنامه ⚠️", Color(0xFFFEE2E2), Color(0xFFDC2626))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Companion Info + Live Status Badge + Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = companionName.take(1),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = companionName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                        }
                        Text(
                            text = "پایش زنده و ابری فعال است",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "به‌روزرسانی داده‌ها",
                                tint = NooshPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hydration Progress Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "مصرف آب امروز همراه",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${DateTimeUtils.toPersianDigits(todayWater.toString())} / ${DateTimeUtils.toPersianDigits(goal.toString())} میلی‌لیتر",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${DateTimeUtils.toPersianDigits(percentage.toString())}٪",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NooshPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (percentage.coerceIn(0, 100) / 100f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = NooshPrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Summary Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompanionMetricItem(
                    label = "آخرین نوشیدن",
                    value = if (data?.lastDrinkTimeAgoMinutes != null) {
                        "${DateTimeUtils.toPersianDigits(data.lastDrinkTimeAgoMinutes.toString())} دقیقه پیش"
                    } else {
                        "هنوز ثبت نشده"
                    },
                    modifier = Modifier.weight(1f)
                )

                CompanionMetricItem(
                    label = "زنجیره پایدار",
                    value = "${DateTimeUtils.toPersianDigits((data?.streakDays ?: 1).toString())} روز متوالی",
                    modifier = Modifier.weight(1f)
                )

                CompanionMetricItem(
                    label = "آخرین حجم",
                    value = if (data?.lastDrinkAmountMl != null) {
                        "${DateTimeUtils.toPersianDigits(data.lastDrinkAmountMl.toString())} میل"
                    } else {
                        "—"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Companion Actions: Nudge & Cheer
            Text(
                text = "اقدامات مراقبتی و تعامل با همراه:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSendNudge,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سقلمه آب 💧", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSendCheer,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Celebration,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تشویق و آفرین 🎉", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CompanionMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CompanionRecentIntakesSection(data: CompanionMonitoringData?) {
    Column {
        Text(
            text = "آخرین نوبت‌های ثبت‌شده همراه",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        val intakes = data?.recentIntakes ?: emptyList()
        if (intakes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "نوبت جدیدی در سرور ثبت نشده است. به محض ثبت مصرف آب توسط همراه، در اینجا نمایش داده می‌شود.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    intakes.forEachIndexed { index, intake ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = NooshPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${DateTimeUtils.toPersianDigits(intake.amountMl.toString())} میلی‌لیتر",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val minutesAgo = ((System.currentTimeMillis() - intake.consumedAt) / 60000).coerceAtLeast(0)
                            Text(
                                text = if (minutesAgo < 60) "${DateTimeUtils.toPersianDigits(minutesAgo.toString())} دقیقه پیش" else "امروز",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < intakes.size - 1) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionAlertsSection(data: CompanionMonitoringData?) {
    val alerts = data?.alerts ?: emptyList()
    if (alerts.isNotEmpty()) {
        Column {
            Text(
                text = "هشدارهای اخیر پایش همراه",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            alerts.forEach { alert ->
                HealthEventItem(event = alert)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CompanionEmptyMonitoringCard(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(NooshSubtleBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = null,
                    tint = NooshPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "هنوز همراه سلامتی متصل نکرده‌اید",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "با اتصال به اتاق همراه (پدر، مادر، فرزند یا دوست خود)، می‌توانید به صورت زنده میزان مصرف آب، درصد تحقق هدف و هشدارهای او را بررسی کرده و با ارسال یادآوری دوستانه، هوای سلامت او را داشته باشید!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onJoinRoom,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ورود با کد همراه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCreateRoom,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ساخت اتاق مراقبت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// MY STATUS & PRE-EXISTING COMPONENTS
// =============================================================================

@Composable
private fun CompanionStatusCard(status: HealthCompanionStatus) {
    val (statusText, statusBg, statusColor) = when (status.evaluation) {
        HealthStatusEvaluation.ON_TRACK -> Triple("وضعیت مطلوب", Color(0xFFDCFCE7), Color(0xFF16A34A))
        HealthStatusEvaluation.BEHIND -> Triple("عقب‌تر از برنامه", Color(0xFFFEE2E2), Color(0xFFDC2626))
        HealthStatusEvaluation.GOAL_REACHED -> Triple("هدف روزانه محقق شد", Color(0xFFE0F2FE), Color(0xFF0284C7))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "وضعیت جاری مصرف آب من",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricMiniItem(
                    label = "مصرف امروز من",
                    value = "${DateTimeUtils.toPersianDigits(status.todayWaterMl.toString())} / ${DateTimeUtils.toPersianDigits(status.dailyGoalMl.toString())} میلی‌لیتر",
                    sub = "${DateTimeUtils.toPersianDigits(status.goalPercentage.toString())}٪ هدف محقق شده"
                )
                MetricMiniItem(
                    label = "آخرین نوبت نوشیدن",
                    value = if (status.lastDrinkTimeAgoMinutes != null) {
                        "${DateTimeUtils.toPersianDigits(status.lastDrinkTimeAgoMinutes.toString())} دقیقه پیش"
                    } else {
                        "هنوز ثبت نشده"
                    },
                    sub = "بر اساس زمان واقعی"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricMiniItem(
                    label = "یادآورهای امروز",
                    value = "${DateTimeUtils.toPersianDigits(status.completedReminders.toString())} انجام‌شده",
                    sub = "${DateTimeUtils.toPersianDigits(status.missedReminders.toString())} بدون پاسخ"
                )
                MetricMiniItem(
                    label = "زنجیره پایدار",
                    value = "${DateTimeUtils.toPersianDigits(status.currentStreak.toString())} روز متوالی",
                    sub = "زنجیره ثبت مداوم"
                )
            }
        }
    }
}

@Composable
private fun MetricMiniItem(label: String, value: String, sub: String) {
    Column(modifier = Modifier.width(150.dp)) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun CompanionConnectionCard(
    connection: HealthCompanionConnection,
    onTogglePause: () -> Unit,
    onPolicyChange: (AlertFilterPolicy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تنظیمات حریم خصوصی و اعلان‌های همراه",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "سطح حساسیت ارسال رویدادها را برای همراه تعیین کنید.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (connection.status == CompanionConnectionStatus.CONNECTED) "ارسال گزارش: فعال" else "ارسال گزارش: متوقف",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (connection.status == CompanionConnectionStatus.CONNECTED) Color(0xFF16A34A) else Color(0xFFF59E0B)
                )

                OutlinedButton(
                    onClick = onTogglePause,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (connection.status == CompanionConnectionStatus.CONNECTED) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (connection.status == CompanionConnectionStatus.CONNECTED) "توقف موقت" else "فعال‌سازی مجدد",
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "سیاست فیلتر رویدادها:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PolicyChip(
                    title = "همه رویدادها",
                    selected = connection.alertPolicy == AlertFilterPolicy.ALL,
                    onClick = { onPolicyChange(AlertFilterPolicy.ALL) }
                )
                PolicyChip(
                    title = "متوسط و بحرانی",
                    selected = connection.alertPolicy == AlertFilterPolicy.MEDIUM_AND_HIGH,
                    onClick = { onPolicyChange(AlertFilterPolicy.MEDIUM_AND_HIGH) }
                )
                PolicyChip(
                    title = "فقط بحرانی",
                    selected = connection.alertPolicy == AlertFilterPolicy.HIGH_ONLY,
                    onClick = { onPolicyChange(AlertFilterPolicy.HIGH_ONLY) }
                )
            }
        }
    }
}

@Composable
private fun PolicyChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun HealthEventItem(event: HealthAlertEvent) {
    val (typeTitle, typeColor) = when (event.eventType) {
        HealthEventType.WATER_CONSUMED -> "مصرف آب" to Color(0xFF0284C7)
        HealthEventType.REMINDER_TRIGGERED -> "ارسال یادآور" to Color(0xFF64748B)
        HealthEventType.REMINDER_MISSED -> "یادآور بی‌پاسخ" to Color(0xFFF59E0B)
        HealthEventType.REMINDER_SNOOZED -> "تعویق یادآور" to Color(0xFF6B7280)
        HealthEventType.GOAL_REACHED -> "تحقق هدف روزانه" to Color(0xFF16A34A)
        HealthEventType.LONG_INACTIVITY -> "عدم فعالیت طولانی" to Color(0xFFDC2626)
        HealthEventType.DAILY_GOAL_MISSED -> "عدم تحقق هدف روزانه" to Color(0xFFEF4444)
        HealthEventType.LOW_DAILY_PROGRESS -> "پیشرفت کم آب" to Color(0xFFF97316)
    }

    val (sevTitle, sevBg, sevColor) = when (event.severity) {
        AlertSeverity.LOW -> Triple("عادی", Color(0xFFF1F5F9), Color(0xFF64748B))
        AlertSeverity.MEDIUM -> Triple("متوسط", Color(0xFFFEF3C7), Color(0xFFD97706))
        AlertSeverity.HIGH -> Triple("بحرانی", Color(0xFFFEE2E2), Color(0xFFDC2626))
    }

    val statusTitle = when (event.deliveryStatus) {
        EventDeliveryStatus.PENDING -> "در صف ارسال"
        EventDeliveryStatus.SENT -> "ارسال شده"
        EventDeliveryStatus.ACKNOWLEDGED -> "تأیید شده"
        EventDeliveryStatus.FAILED -> "تلاش مجدد"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(typeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = typeTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sevBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = sevTitle, fontSize = 9.sp, color = sevColor, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "مصرف: ${DateTimeUtils.toPersianDigits(event.currentWaterMl.toString())} از ${DateTimeUtils.toPersianDigits(event.dailyGoalMl.toString())} میلی‌لیتر (${DateTimeUtils.toPersianDigits(event.goalPercentage.toString())}٪)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusTitle,
                    fontSize = 10.sp,
                    color = if (event.deliveryStatus == EventDeliveryStatus.ACKNOWLEDGED || event.deliveryStatus == EventDeliveryStatus.SENT) Color(0xFF16A34A) else Color(0xFFF59E0B),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateTimeUtils.toPersianDigits(event.date),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun JoinCompanionRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, companionName: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var companionName by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "ورود به اتاق همراه سلامت", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "کد ۶ رقمی اتاق و نام همراه (مثلاً دکتر، مربی یا همسر) را وارد کنید:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = { Text("کد ۶ رقمی دعوت") },
                        placeholder = { Text("مثلاً 123456") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = companionName,
                        onValueChange = { companionName = it },
                        label = { Text("نام همراه سلامت") },
                        placeholder = { Text("مثلاً دکتر احمدی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && companionName.isNotBlank()) {
                            onConfirm(code.trim(), companionName.trim())
                        }
                    },
                    enabled = code.isNotBlank() && companionName.isNotBlank()
                ) {
                    Text("اتصال به اتاق")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("انصراف")
                }
            }
        )
    }
}
