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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyStatus
import com.example.domain.model.Driver
import com.example.domain.model.MonthlySettlementRow
import com.example.domain.model.PendingDailyApproval
import com.example.domain.model.RouteItem
import com.example.domain.model.Trip
import com.example.ui.components.StatsMetricCard
import com.example.ui.theme.*
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@Composable
fun AdminDashboardScreen(
    drivers: List<Driver>,
    routes: List<RouteItem>,
    settlementRows: List<MonthlySettlementRow>,
    allApprovals: List<PendingDailyApproval>,
    onApproveDailyWork: (dailyWorkId: String, adminName: String) -> Unit,
    onRejectDailyWork: (dailyWorkId: String, reason: String, adminName: String) -> Unit,
    onUnlockDailyWork: (dailyWorkId: String, adminName: String) -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDrivers = drivers.size
    val activeDrivers = drivers.count { it.isActive }
    val totalTripsMonth = settlementRows.sumOf { it.totalTripsCount }
    val totalIncomeMonth = settlementRows.sumOf { it.finalizedIncome }
    val pendingCount = allApprovals.count { it.dailyWork.status == DailyStatus.PENDING_APPROVAL }

    var selectedApprovalFilter by remember { mutableStateOf("PENDING") } // "PENDING", "ALL", "FINALIZED", "REJECTED"
    var expandedWorkId by remember { mutableStateOf<String?>(null) }
    var itemToReject by remember { mutableStateOf<PendingDailyApproval?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    val filteredApprovals = remember(allApprovals, selectedApprovalFilter) {
        when (selectedApprovalFilter) {
            "PENDING" -> allApprovals.filter { it.dailyWork.status == DailyStatus.PENDING_APPROVAL }
            "FINALIZED" -> allApprovals.filter { it.dailyWork.status == DailyStatus.FINALIZED }
            "REJECTED" -> allApprovals.filter { it.dailyWork.status == DailyStatus.REJECTED }
            else -> allApprovals
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Executive Header Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            DarkGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_executive_banner"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "داشبورد مدیریت ناوگان هلدینگ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "واحد کنترل مأموریت‌ها، کارکرد و هزینه‌ها",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldPrimary
                        ) {
                            Text(
                                text = PersianDateHelper.getTodayJalali().formatReadable(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مجموع هزینه‌های مصوب ماه",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                            Text(
                                text = MoneyUtils.formatToman(totalIncomeMonth),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "تعداد کل مأموریت‌های ماه",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                            )
                            Text(
                                text = "${totalTripsMonth.toPersianDigits()} سفر",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Metrics Row 1: Drivers & Pending Approvals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsMetricCard(
                    title = "کارتابل در انتظار تأیید",
                    value = "${pendingCount.toPersianDigits()} کارکرد روز",
                    subtitle = if (pendingCount > 0) "نیازمند بررسی و تأیید ادمین" else "تمامی کارکردها تأیید شده‌اند",
                    icon = Icons.Default.FactCheck,
                    accentColor = if (pendingCount > 0) Color(0xFFE11D48) else EmeraldPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "metric_pending_approvals"
                )

                StatsMetricCard(
                    title = "ناوگان و رانندگان فعال",
                    value = "${activeDrivers.toPersianDigits()} از ${totalDrivers.toPersianDigits()}",
                    subtitle = "${routes.size.toPersianDigits()} مسیر با نرخ مصوب",
                    icon = Icons.Default.Groups,
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "metric_total_drivers"
                )
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // PENDING APPROVALS INBOX (Core Workflow Section)
        // ═════════════════════════════════════════════════════════════════════
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "کارتابل بررسی و تأیید کارکرد رانندگان",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }

                    if (pendingCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = "${pendingCount.toPersianDigits()} در انتظار تأیید",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedApprovalFilter == "PENDING",
                        onClick = { selectedApprovalFilter = "PENDING" },
                        label = { Text("در انتظار تأیید (${pendingCount.toPersianDigits()})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedApprovalFilter == "FINALIZED",
                        onClick = { selectedApprovalFilter = "FINALIZED" },
                        label = { Text("تأیید شده‌ها") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedApprovalFilter == "REJECTED",
                        onClick = { selectedApprovalFilter = "REJECTED" },
                        label = { Text("رد شده‌ها") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedApprovalFilter == "ALL",
                        onClick = { selectedApprovalFilter = "ALL" },
                        label = { Text("همه") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate700,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredApprovals.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = GlassBorderLight
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedApprovalFilter == "PENDING") {
                                "هیچ کارکردی در انتظار تأیید ادمین وجود ندارد."
                            } else {
                                "موردی با این فیلتر یافت نشد."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(filteredApprovals, key = { it.dailyWork.id }) { item ->
                val daily = item.dailyWork
                val isExpanded = expandedWorkId == daily.id

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approval_card_${daily.id}"),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = when (daily.status) {
                        DailyStatus.PENDING_APPROVAL -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                        DailyStatus.FINALIZED -> EmeraldAccent.copy(alpha = 0.35f)
                        DailyStatus.REJECTED -> Color(0xFFEF4444).copy(alpha = 0.35f)
                        DailyStatus.DRAFT -> GoldAccent.copy(alpha = 0.3f)
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(NavyPrimary.copy(alpha = 0.10f))
                                        .border(BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.2f)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = daily.driverName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "کد ${daily.driverCode}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                        )
                                        Text(text = "•", color = Slate400)
                                        Text(
                                            text = "تاریخ: ${daily.jalaliDate}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = NavyPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (daily.status) {
                                    DailyStatus.PENDING_APPROVAL -> Color(0xFF1E3A8A)
                                    DailyStatus.FINALIZED -> EmeraldPrimary
                                    DailyStatus.REJECTED -> Color(0xFFDC2626)
                                    DailyStatus.DRAFT -> GoldPrimary
                                }
                            ) {
                                Text(
                                    text = daily.status.faTitle,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Summary of the Work Log
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${daily.totalTrips.toPersianDigits()} سفر ثبت‌شده با نرخ مصوب",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = MoneyUtils.formatToman(daily.totalIncome),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                )
                            }
                        }

                        if (daily.status == DailyStatus.REJECTED && !daily.rejectionReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEE2E2),
                                border = BorderStroke(0.5.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "علت رد شده: ${daily.rejectionReason}",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB91C1C))
                                )
                            }
                        }

                        if (daily.status == DailyStatus.FINALIZED && !daily.approvedBy.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تأیید شده توسط: ${daily.approvedBy}",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trips Breakdown Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { expandedWorkId = if (isExpanded) null else daily.id }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "بستن ریز سفرها ▲" else "مشاهده جزئیات ریز سفرها (${item.trips.size.toPersianDigits()} سفر) ▼",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.trips.forEachIndexed { idx, trip ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(0.5.dp, Slate200),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${(idx + 1).toPersianDigits()}. ${trip.originTitle} ➔ ${trip.destinationTitle}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "ساعت: ${trip.startTime}${if (!trip.endTime.isNullOrBlank()) " تا ${trip.endTime}" else ""} | کد مسیر: ${trip.routeCode}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                                )
                                                if (trip.description.isNotBlank()) {
                                                    Text(
                                                        text = "توضیحات: ${trip.description}",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = MoneyUtils.formatToman(trip.appliedPrice),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldDark
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons for this daily work log
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (daily.status == DailyStatus.PENDING_APPROVAL || daily.status == DailyStatus.DRAFT || daily.status == DailyStatus.REJECTED) {
                                Button(
                                    onClick = { onApproveDailyWork(daily.id, "مسئول کارکرد ناوگان (ادمین)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("approve_btn_${daily.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "تأیید کارکرد روز",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                if (daily.status != DailyStatus.REJECTED) {
                                    OutlinedButton(
                                        onClick = {
                                            itemToReject = item
                                            rejectionReasonInput = ""
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("reject_btn_${daily.id}"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "عدم تأیید / بازگشت",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            } else if (daily.status == DailyStatus.FINALIZED) {
                                OutlinedButton(
                                    onClick = { onUnlockDailyWork(daily.id, "مسئول کارکرد ناوگان (ادمین)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("unlock_btn_${daily.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GoldPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "بازگشایی قفل جهت ویرایش مجدد راننده",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // Management Hub Modules Cards (Quick Navigation)
        // ═════════════════════════════════════════════════════════════════════
        item {
            Text(
                text = "ماژول‌های مدیریت و نظارت",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Route & Tariff Management Module Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToRoutes() }
                        .testTag("nav_to_routes_card"),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = GlassBorderLight
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavyPrimary.copy(alpha = 0.10f))
                                .border(BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "تعریف مسیر و تعرفه‌ها",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "مدیریت نرخ مصوب و تاریخچه",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }

                // Drivers Module Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToDrivers() }
                        .testTag("nav_to_drivers_card"),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = GlassBorderLight
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldPrimary.copy(alpha = 0.10f))
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "مدیریت رانندگان",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "پلاک، خودرو و وضعیت فعالیت",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Financial Settlement Module Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToFinance() }
                        .testTag("nav_to_finance_card"),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = GlassBorderLight
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.10f))
                                .border(BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "گزارشات مالی و تسویه",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "محاسبه، صدور فیش و خروجی اکسل",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }

                // Audit Logs Module Card
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAuditLogs() }
                        .testTag("nav_to_audit_logs_card"),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = GlassSurfacePure,
                    borderColor = GlassBorderLight
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.10f))
                                .border(BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f)), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "گزارش تغییرات (Audit)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "لاگ امنیت و تغییرات نرخ‌ها",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }
            }
        }

        // Live Driver Performance Overview Table
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "وضعیت کارکرد رانندگان در ماه جاری",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                TextButton(onClick = onNavigateToFinance) {
                    Text("مشاهده جزئیات کامل ❯")
                }
            }
        }

        items(settlementRows) { row ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.10f))
                                .border(BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.2f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = row.driverName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "کد ${row.driverCode} | ${row.workingDaysCount.toPersianDigits()} روز کاری | ${row.totalTripsCount.toPersianDigits()} سفر",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MoneyUtils.formatToman(row.finalizedIncome),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (row.paymentStatus.name) {
                                "PAID" -> EmeraldLight
                                "SENT_TO_FINANCE" -> Color(0xFFE0E7FF)
                                else -> GoldLight
                            }
                        ) {
                            Text(
                                text = row.paymentStatus.faTitle,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (row.paymentStatus.name) {
                                        "PAID" -> EmeraldDark
                                        "SENT_TO_FINANCE" -> NavyPrimary
                                        else -> GoldPrimary
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Rejection Feedback Modal
    if (itemToReject != null) {
        AlertDialog(
            onDismissRequest = { itemToReject = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Text(
                        text = "عدم تأیید کارکرد روز",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "علت عدم تأیید کارکرد تاریخ ${itemToReject?.dailyWork?.jalaliDate} برای راننده «${itemToReject?.dailyWork?.driverName}» را وارد فرمایید تا جهت اصلاح به وی اطلاع داده شود:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("علت عدم تأیید (مثال: عدم تطابق ساعت خروج / مأموریت اشتباه)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val workId = itemToReject?.dailyWork?.id ?: return@Button
                        val reason = if (rejectionReasonInput.isNotBlank()) rejectionReasonInput else "نیازمند بازنگری و اصلاح توسط راننده"
                        onRejectDailyWork(workId, reason, "مدیر ناوگان")
                        itemToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_reject_button")
                ) {
                    Text("ثبت عدم تأیید و بازگشت به راننده")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToReject = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}
