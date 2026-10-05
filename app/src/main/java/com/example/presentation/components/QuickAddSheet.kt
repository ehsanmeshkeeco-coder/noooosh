package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.util.DateTimeUtils
import com.example.presentation.theme.NooshPrimary

data class BeverageType(
    val id: String,
    val name: String,
    val emoji: String,
    val hydrationFactor: Float, // e.g. 1.0 for water, 0.85 for tea, etc.
    val description: String
)

val BeverageOptions = listOf(
    BeverageType("pure_water", "آب خالص", "💧", 1.0f, "۱۰۰٪ ارزش هیدراتاسیون"),
    BeverageType("tea", "چای کمرنگ", "🍵", 0.85f, "۸۵٪ ارزش آب‌رسانی"),
    BeverageType("coffee", "قهوه / نسکافه", "☕", 0.70f, "۷۰٪ ارزش آب‌رسانی"),
    BeverageType("herbal", "دمنوش گیاهی", "🫖", 0.90f, "۹۰٪ ارزش آب‌رسانی"),
    BeverageType("milk", "شیر", "🥛", 0.88f, "۸۸٪ ارزش آب‌رسانی"),
    BeverageType("juice", "آبمیوه طبیعی", "🧃", 0.80f, "۸۰٪ ارزش آب‌رسانی")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAddWater: (amountMl: Int, rescheduleMinutes: Int?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedAmount by remember { mutableIntStateOf(250) }
    var selectedBeverage by remember { mutableStateOf(BeverageOptions[0]) }
    var selectedRescheduleMinutes by remember { mutableIntStateOf(0) } // 0 = normal schedule

    // Calculate effective water hydration amount
    val effectiveAmount = (selectedAmount * selectedBeverage.hydrationFactor).toInt()

    val presets = listOf(
        150 to "۱۵۰ میلی‌لیتر (نصف لیوان)",
        250 to "۲۵۰ میلی‌لیتر (یک لیوان)",
        350 to "۳۵۰ میلی‌لیتر (یک ماگ)",
        500 to "۵۰۰ میلی‌لیتر (بطری نیم‌لیتری)"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("quick_add_sheet")
            ) {
                Text(
                    text = "ثبت مایعات مصرفی 💧",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Beverage Type Selector Chips
                Text(
                    text = "نوع نوشیدنی:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BeverageOptions.forEach { beverage ->
                        val isSelected = selectedBeverage.id == beverage.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBeverage = beverage },
                            label = { Text("${beverage.emoji} ${beverage.name}", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NooshPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets List
                Text(
                    text = "حجم نوشیدنی:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (ml, _) ->
                        val isSelected = selectedAmount == ml
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAmount = ml },
                            label = { Text("${DateTimeUtils.toPersianDigits(ml.toString())} میل", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NooshPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Slider in RTL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مقدار دقیق: ${DateTimeUtils.toPersianDigits(selectedAmount.toString())} میلی‌لیتر",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedBeverage.hydrationFactor < 1.0f) {
                        Text(
                            text = "(ارزش آبی: ${DateTimeUtils.toPersianDigits(effectiveAmount.toString())} میل)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NooshPrimary
                        )
                    }
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Slider(
                        value = selectedAmount.toFloat(),
                        onValueChange = { selectedAmount = (it / 50).toInt() * 50 },
                        valueRange = 50f..1000f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("amount_slider")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smart Reminder rescheduling option
                Text(
                    text = "فاصله تا یادآور بعدی:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "عادی",
                        30 to "۳۰ دقیقه",
                        60 to "۶۰ دقیقه",
                        90 to "۹۰ دقیقه"
                    ).forEach { (minutes, label) ->
                        val isSelected = selectedRescheduleMinutes == minutes
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRescheduleMinutes = minutes },
                            label = { Text(text = label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        onAddWater(
                            effectiveAmount,
                            if (selectedRescheduleMinutes > 0) selectedRescheduleMinutes else null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_water_intake_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "ثبت ${selectedBeverage.emoji} ${selectedBeverage.name} (${DateTimeUtils.toPersianDigits(effectiveAmount.toString())} میل آب)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
