package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyStatus
import com.example.domain.model.DailyWorkSummary
import com.example.domain.model.Driver
import com.example.domain.model.Trip
import com.example.ui.components.PersianDatePickerModal
import com.example.ui.components.StatsMetricCard
import com.example.ui.components.TripItemCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldGlassSurface
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldGlassSurface
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.GlassCard
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.GlassSurfacePure
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassHighlight
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@Composable
fun DriverHomeScreen(
    driver: Driver,
    selectedDate: PersianDateHelper.JalaliDate,
    dailyWork: DailyWorkSummary?,
    todayTrips: List<Trip>,
    monthlyIncome: Long,
    allDrivers: List<Driver>,
    onSelectDriver: (Driver) -> Unit,
    onSelectDate: (PersianDateHelper.JalaliDate) -> Unit,
    onRegisterNewTripClick: () -> Unit,
    onFinalizeDayClick: () -> Unit,
    onDeleteTripClick: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showFinalizeDialog by remember { mutableStateOf(false) }
    var showDriverSwitchDialog by remember { mutableStateOf(false) }

    val status = dailyWork?.status ?: DailyStatus.DRAFT
    val isLocked = status == DailyStatus.PENDING_APPROVAL || status == DailyStatus.FINALIZED
    val isToday = selectedDate == PersianDateHelper.getTodayJalali()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("driver_home_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Driver Identity Card (Glass Dark Surface)
            DarkGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_profile_card"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldPrimary.copy(alpha = 0.9f), GoldAccent)
                                    )
                                )
                                .border(BorderStroke(1.dp, GlassHighlight), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = driver.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x333B82F6),
                                    border = BorderStroke(0.5.dp, Color(0x4D60A5FA))
                                ) {
                                    Text(
                                        text = "کد راننده: ${driver.driverCode}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF93C5FD),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "پرسنلی: ${driver.personnelCode.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Switch driver profile button
                    OutlinedButton(
                        onClick = { showDriverSwitchDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        modifier = Modifier.testTag("switch_driver_btn")
                    ) {
                        Text(
                            text = "تعویض",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Persian Calendar Bar (Day Selector Glass Card)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persian_calendar_bar"),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            // Previous day
                            val maxDays = if (selectedDate.month <= 6) 31 else if (selectedDate.month <= 11) 30 else 29
                            val newDay = if (selectedDate.day > 1) selectedDate.day - 1 else 1
                            onSelectDate(selectedDate.copy(day = newDay))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight, // In RTL, Right is Previous
                            contentDescription = "روز قبل",
                            tint = Slate700
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isToday) GoldGlassSurface else Color(0x0A0F172A),
                        border = if (isToday) BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)) else BorderStroke(0.5.dp, Color(0x10000000)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isToday) GoldPrimary else NavyPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = selectedDate.formatWithDayName(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 13.sp
                                )
                            )
                            if (isToday) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldPrimary
                                ) {
                                    Text(
                                        text = "امروز",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            // Next day
                            val maxDays = if (selectedDate.month <= 6) 31 else if (selectedDate.month <= 11) 30 else 29
                            val newDay = if (selectedDate.day < maxDays) selectedDate.day + 1 else maxDays
                            onSelectDate(selectedDate.copy(day = newDay))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft, // In RTL, Left is Next
                            contentDescription = "روز بعد",
                            tint = Slate700
                        )
                    }
                }
            }
        }

        // Daily Work Status Banner (Clean Frosted Glass Banner)
        item {
            val bannerBg = when (status) {
                DailyStatus.FINALIZED -> EmeraldGlassSurface.copy(alpha = 0.15f)
                DailyStatus.PENDING_APPROVAL -> Color(0xFF1E3A8A).copy(alpha = 0.12f)
                DailyStatus.REJECTED -> Color(0xFFDC2626).copy(alpha = 0.12f)
                DailyStatus.DRAFT -> GoldGlassSurface.copy(alpha = 0.12f)
            }
            val bannerBorder = when (status) {
                DailyStatus.FINALIZED -> EmeraldAccent.copy(alpha = 0.4f)
                DailyStatus.PENDING_APPROVAL -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                DailyStatus.REJECTED -> Color(0xFFEF4444).copy(alpha = 0.4f)
                DailyStatus.DRAFT -> GoldAccent.copy(alpha = 0.35f)
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_status_banner"),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = bannerBg,
                borderColor = bannerBorder,
                borderWidth = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (status) {
                                DailyStatus.FINALIZED -> Icons.Default.CheckCircle
                                DailyStatus.PENDING_APPROVAL -> Icons.Default.Schedule
                                DailyStatus.REJECTED -> Icons.Default.Warning
                                DailyStatus.DRAFT -> Icons.Default.PlaylistAddCheck
                            },
                            contentDescription = null,
                            tint = when (status) {
                                DailyStatus.FINALIZED -> EmeraldDark
                                DailyStatus.PENDING_APPROVAL -> NavyPrimary
                                DailyStatus.REJECTED -> Color(0xFFB91C1C)
                                DailyStatus.DRAFT -> GoldPrimary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = when (status) {
                                    DailyStatus.FINALIZED -> "کارکرد روز تأیید و نهایی شده است"
                                    DailyStatus.PENDING_APPROVAL -> "کارکرد به مدیریت ارسال شده (در انتظار تأیید)"
                                    DailyStatus.REJECTED -> "کارکرد رد شده و نیازمند اصلاح است"
                                    DailyStatus.DRAFT -> "کارکرد این روز در حال ثبت می‌باشد"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (status) {
                                        DailyStatus.FINALIZED -> EmeraldDark
                                        DailyStatus.PENDING_APPROVAL -> NavyDark
                                        DailyStatus.REJECTED -> Color(0xFF991B1B)
                                        DailyStatus.DRAFT -> Color(0xFF92400E)
                                    }
                                )
                            )
                            Text(
                                text = when (status) {
                                    DailyStatus.FINALIZED -> if (!dailyWork?.approvedBy.isNullOrBlank()) "تأیید شده توسط ${dailyWork?.approvedBy} جهت تسویه مالی" else "تأیید نهایی جهت صدور فیش و تسویه حساب"
                                    DailyStatus.PENDING_APPROVAL -> "در صف کارتابل مدیر ناوگان جهت بررسی و صدور تأییدیه"
                                    DailyStatus.REJECTED -> "علت: ${dailyWork?.rejectionReason ?: "نامشخص"} - لطفاً اصلاح و مجدداً ارسال فرمایید."
                                    DailyStatus.DRAFT -> "سفرهای خود را ثبت کرده و سپس دکمه ارسال به مدیریت را بزنید."
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when (status) {
                                        DailyStatus.FINALIZED -> EmeraldDark.copy(alpha = 0.8f)
                                        DailyStatus.PENDING_APPROVAL -> Slate700
                                        DailyStatus.REJECTED -> Color(0xFFDC2626)
                                        DailyStatus.DRAFT -> Color(0xFFB45309)
                                    }
                                ),
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (status) {
                            DailyStatus.FINALIZED -> EmeraldPrimary
                            DailyStatus.PENDING_APPROVAL -> NavyPrimary
                            DailyStatus.REJECTED -> Color(0xFFDC2626)
                            DailyStatus.DRAFT -> GoldPrimary
                        }
                    ) {
                        Text(
                            text = status.faTitle,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        // Metrics Grid (Today's Trips, Today's Income, Month Income)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsMetricCard(
                    title = "تعداد سفرهای امروز",
                    value = "${todayTrips.size.toPersianDigits()} سفر",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "metric_today_trips"
                )

                StatsMetricCard(
                    title = "درآمد امروز",
                    value = MoneyUtils.formatTomanCompact(todayTrips.sumOf { it.appliedPrice }),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "metric_today_income"
                )
            }
        }

        item {
            StatsMetricCard(
                title = "مجموع درآمد ماه جاری (${PersianDateHelper.PERSIAN_MONTHS[selectedDate.month - 1]})",
                value = MoneyUtils.formatToman(monthlyIncome),
                subtitle = "شامل کلیه سفرهای مصوب هلدینگ در این ماه",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = GoldPrimary,
                modifier = Modifier.fillMaxWidth(),
                testTag = "metric_month_income"
            )
        }

        // Action Buttons: "ثبت سفر جدید" and "ارسال به مدیریت جهت تأیید"
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Register Trip Button
                Button(
                    onClick = onRegisterNewTripClick,
                    enabled = !isLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_new_trip_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        disabledContainerColor = Slate300
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isLocked) "کارکرد قفل شده (امکان ثبت جدید نیست)" else "ثبت سفر جدید",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                // Submit to Admin / Finalize Shift Button
                if (!isLocked && todayTrips.isNotEmpty()) {
                    Button(
                        onClick = { showFinalizeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("finalize_day_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == DailyStatus.REJECTED) Color(0xFFD97706) else EmeraldPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAddCheck,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (status == DailyStatus.REJECTED) {
                                    "ارسال مجدد به مدیریت جهت بررسی (${todayTrips.size.toPersianDigits()} سفر)"
                                } else {
                                    "ارسال به مدیریت جهت تأیید کارکرد (${todayTrips.size.toPersianDigits()} سفر)"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Daily Trips List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لیست سفرهای ${selectedDate.formatReadable()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "${todayTrips.size.toPersianDigits()} سفر ثبت‌شده",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
            }
        }

        if (todayTrips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Slate300,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هنوز هیچ سفری برای این تاریخ ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                        )
                        if (!isLocked) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onRegisterNewTripClick,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ثبت اولین سفر این روز")
                            }
                        }
                    }
                }
            }
        } else {
            items(todayTrips, key = { it.id }) { trip ->
                TripItemCard(
                    trip = trip,
                    canDelete = !isLocked,
                    onDelete = { onDeleteTripClick(trip) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        PersianDatePickerModal(
            initialDate = selectedDate,
            onDateSelected = { newDate ->
                onSelectDate(newDate)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Finalize / Send to Admin Approval Confirmation Modal
    if (showFinalizeDialog) {
        AlertDialog(
            onDismissRequest = { showFinalizeDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAddCheck,
                        contentDescription = null,
                        tint = EmeraldDark
                    )
                    Text(
                        text = "ارسال کارکرد روز به پنل مدیریت",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "آیا از ارسال کارکرد تاریخ ${selectedDate.formatReadable()} جهت بررسی و تأیید ادمین ناوگان اطمینان دارید؟",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "• تعداد کل سفرها: ${todayTrips.size.toPersianDigits()} سفر",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "• مجموع ارزش سفرهای روز: ${MoneyUtils.formatToman(todayTrips.sumOf { it.appliedPrice })}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                        }
                    }

                    Text(
                        text = "📋 توجه: پس از ارسال، کارکرد در کارتابل مسئول ناوگان قرار می‌گیرد و تا زمان تأیید توسط مدیریت، سفرها به صورت موقت قفل خواهند شد.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1E40AF))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalizeDialog = false
                        onFinalizeDayClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("confirm_finalize_button")
                ) {
                    Text("ارسال جهت تأیید مدیریت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Driver Switch Dialog (for testing multi-driver scenarios)
    if (showDriverSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showDriverSwitchDialog = false },
            title = {
                Text(
                    text = "انتخاب پروفایل راننده",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allDrivers) { item ->
                        val isSelected = item.id == driver.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GoldLight else Slate100,
                            border = if (isSelected) BorderStroke(1.dp, GoldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectDriver(item)
                                    showDriverSwitchDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.fullName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Text(
                                        text = "کد ${item.driverCode} | ${item.carModel}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDriverSwitchDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}
