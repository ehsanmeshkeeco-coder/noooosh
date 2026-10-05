package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.DateTimeUtils
import com.example.presentation.theme.NooshPrimary
import kotlin.math.roundToInt

data class TourStep(
    val title: String,
    val description: String,
    val targetArea: TourTargetArea,
    val targetRect: Rect? = null
)

enum class TourTargetArea {
    WATER_RING,
    QUICK_ADD,
    BOTTOM_NAV
}

enum class TooltipPointerDirection {
    UP,
    DOWN
}

/**
 * Interactive spotlight tour overlay that smoothly travels to each target element,
 * illuminates it with a glowing animated spotlight, and moves the explanation tooltip
 * card directly adjacent to the element with RTL compliance and animated transitions.
 */
@Composable
fun InteractiveTourOverlay(
    activeStepIndex: Int,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit = {},
    onSkipTour: () -> Unit,
    ringBounds: Rect? = null,
    quickAddBounds: Rect? = null,
    bottomNavBounds: Rect? = null
) {
    val steps = remember(ringBounds, quickAddBounds, bottomNavBounds) {
        listOf(
            TourStep(
                title = "حلقه هوشمند پیشرفت",
                description = "مشاهده درصد و میزان آب مصرفی روزانه و ساعت یادآوری بعدی.",
                targetArea = TourTargetArea.WATER_RING,
                targetRect = ringBounds
            ),
            TourStep(
                title = "ثبت سریع آب",
                description = "ثبت فوری یک لیوان آب یا وارد کردن مقدار سفارشی با یک لمس.",
                targetArea = TourTargetArea.QUICK_ADD,
                targetRect = quickAddBounds
            ),
            TourStep(
                title = "نوار دسترسی سریع",
                description = "ورود به بخش همراه سلامت، نمودارها، یادآورها و تنظیمات.",
                targetArea = TourTargetArea.BOTTOM_NAV,
                targetRect = bottomNavBounds
            )
        )
    }

    if (activeStepIndex >= steps.size) return

    val currentStep = steps[activeStepIndex]
    val isLast = activeStepIndex == steps.size - 1
    val density = LocalDensity.current

    // Pulsing glowing animation around the spotlight hole
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("interactive_tour_overlay")
        ) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()

            // 1. Determine raw target rect for current step (directly reactive to bounds)
            val rawTargetRect = when (currentStep.targetArea) {
                TourTargetArea.WATER_RING -> ringBounds ?: currentStep.targetRect ?: Rect(
                    left = screenWidthPx * 0.12f,
                    top = screenHeightPx * 0.14f,
                    right = screenWidthPx * 0.88f,
                    bottom = screenHeightPx * 0.44f
                )
                TourTargetArea.QUICK_ADD -> quickAddBounds ?: currentStep.targetRect ?: Rect(
                    left = screenWidthPx * 0.04f,
                    top = screenHeightPx * 0.60f,
                    right = screenWidthPx * 0.96f,
                    bottom = screenHeightPx * 0.72f
                )
                TourTargetArea.BOTTOM_NAV -> bottomNavBounds ?: currentStep.targetRect ?: Rect(
                    left = 0f,
                    top = screenHeightPx * 0.88f,
                    right = screenWidthPx,
                    bottom = screenHeightPx
                )
            }

            val paddingPx = with(density) { 8.dp.toPx() }

            // 2. Smoothly animate spotlight cutout coordinates from one position to the next
            val animLeft by animateFloatAsState(
                targetValue = (rawTargetRect.left - paddingPx).coerceAtLeast(0f),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_left"
            )
            val animTop by animateFloatAsState(
                targetValue = (rawTargetRect.top - paddingPx).coerceAtLeast(0f),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_top"
            )
            val animRight by animateFloatAsState(
                targetValue = (rawTargetRect.right + paddingPx).coerceAtMost(screenWidthPx),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_right"
            )
            val animBottom by animateFloatAsState(
                targetValue = (rawTargetRect.bottom + paddingPx).coerceAtMost(screenHeightPx),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_bottom"
            )
            val animCornerRadius by animateFloatAsState(
                targetValue = if (currentStep.targetArea == TourTargetArea.BOTTOM_NAV) {
                    with(density) { 16.dp.toPx() }
                } else if (currentStep.targetArea == TourTargetArea.WATER_RING) {
                    ((rawTargetRect.bottom - rawTargetRect.top + paddingPx * 2) / 2f).coerceAtLeast(with(density) { 36.dp.toPx() })
                } else {
                    with(density) { 20.dp.toPx() }
                },
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_corner"
            )

            // 3. Spotlight Canvas: Dark overlay with the smoothly moving transparent hole and glowing ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val animatedHoleRect = Rect(
                    left = animLeft,
                    top = animTop,
                    right = animRight,
                    bottom = animBottom
                )

                val holePath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = animatedHoleRect,
                            cornerRadius = CornerRadius(animCornerRadius, animCornerRadius)
                        )
                    )
                }

                // Dimmed translucent scrim with transparent cut-out
                clipPath(holePath, clipOp = ClipOp.Difference) {
                    drawRect(
                        color = Color(0xDD090F1E), // Deep dark indigo backdrop (87% opacity)
                        size = Size(screenWidthPx, screenHeightPx)
                    )
                }

                // Glowing animated spotlight stroke around the target
                drawRoundRect(
                    color = NooshPrimary.copy(alpha = pulseAlpha),
                    topLeft = Offset(animatedHoleRect.left, animatedHoleRect.top),
                    size = Size(animatedHoleRect.width, animatedHoleRect.height),
                    cornerRadius = CornerRadius(animCornerRadius, animCornerRadius),
                    style = Stroke(width = with(density) { 3.dp.toPx() } + pulseGlow)
                )
            }

            // Interactive clickable hotspot directly over the spotlighted element
            Box(
                modifier = Modifier
                    .offset { IntOffset(animLeft.roundToInt(), animTop.roundToInt()) }
                    .size(
                        width = with(density) { (animRight - animLeft).coerceAtLeast(20f).toDp() },
                        height = with(density) { (animBottom - animTop).coerceAtLeast(20f).toDp() }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNextStep
                    )
            )

            // 4. Calculate where the tooltip card should float:
            // For step 2 (QUICK_ADD) or targets in the lower half, float above the target so it doesn't overlap
            val cardEstimatedHeight = with(density) { 190.dp.toPx() }
            val cardGapPx = with(density) { 12.dp.toPx() }

            val shouldFloatBelow = currentStep.targetArea == TourTargetArea.WATER_RING
            val pointerDirection = if (shouldFloatBelow) TooltipPointerDirection.UP else TooltipPointerDirection.DOWN

            val maxAllowedY = (screenHeightPx - cardEstimatedHeight - with(density) { 20.dp.toPx() }).coerceAtLeast(0f)
            val minAllowedY = with(density) { 20.dp.toPx() }

            val targetCardY = if (shouldFloatBelow) {
                // Float below water progress ring
                (rawTargetRect.bottom + cardGapPx).coerceIn(minAllowedY, maxAllowedY)
            } else {
                // Float above Quick Add buttons or Bottom Nav
                (rawTargetRect.top - cardEstimatedHeight - cardGapPx).coerceIn(minAllowedY, maxAllowedY)
            }

            // Smoothly animate the card Y position as it travels across the screen
            val animCardY by animateFloatAsState(
                targetValue = targetCardY,
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "tour_anim_card_y"
            )

            // Tooltip Card traveling to the exact target
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset { IntOffset(0, animCardY.roundToInt()) }
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tour_tooltip_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Direction pointer badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = NooshPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (pointerDirection == TooltipPointerDirection.UP) "👆 بخش مشخص‌شده در بالا" else "👇 بخش مشخص‌شده در پایین",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Step indicator (Persian digits)
                            Text(
                                text = "مرحله ${DateTimeUtils.toPersianDigits((activeStepIndex + 1).toString())} از ${DateTimeUtils.toPersianDigits(steps.size.toString())}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title & Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = NooshPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = currentStep.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description
                        Text(
                            text = currentStep.description,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Buttons Row (RTL Mirrored)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onSkipTour,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "رد شدن از تور",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (activeStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = onPreviousStep,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        // Back arrow points right in RTL
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "قبلی",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("قبلی", fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = onNextStep,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NooshPrimary),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isLast) "شروع استفاده از نوش 💧" else "متوجه شدم، بعدی",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    if (!isLast) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Forward arrow mirrors to point left in RTL
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
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
}
