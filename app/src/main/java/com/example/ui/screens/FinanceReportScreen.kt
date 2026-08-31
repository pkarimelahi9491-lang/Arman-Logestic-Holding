package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlySettlementRow
import com.example.domain.model.PaymentStatus
import com.example.ui.components.FinancialSlipDialog
import com.example.ui.components.StatsMetricCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
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
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@Composable
fun FinanceReportScreen(
    currentYearMonth: String,
    settlementRows: List<MonthlySettlementRow>,
    onNavigateBack: () -> Unit,
    onYearMonthChange: (String) -> Unit,
    onUpdatePaymentStatus: (newStatus: PaymentStatus) -> Unit,
    onExportCsv: (periodTitle: String, rows: List<MonthlySettlementRow>) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val parts = currentYearMonth.split("-")
    var selectedYear by remember { mutableIntStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 1405) }
    var selectedMonth by remember { mutableIntStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 5) }

    var selectedRowForSlip by remember { mutableStateOf<MonthlySettlementRow?>(null) }
    var showWorkflowStatusDialog by remember { mutableStateOf(false) }

    val monthName = PersianDateHelper.PERSIAN_MONTHS.getOrElse(selectedMonth - 1) { "مرداد" }
    val periodTitle = "$monthName ${selectedYear.toPersianDigits()}"

    val totalHoldingsPayable = settlementRows.sumOf { it.finalizedIncome }
    val totalTrips = settlementRows.sumOf { it.totalTripsCount }
    val currentPeriodStatus = settlementRows.firstOrNull()?.paymentStatus ?: PaymentStatus.PENDING_APPROVAL

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("finance_report_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = NavyPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "گزارش مالی و تسویه‌حساب ماهانه",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "محاسبه دقیق کارکرد و فرآیند صدور پرداخت",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }
            }
        }

        // Persian Month & Year Selector
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonth > 1) {
                                selectedMonth--
                            } else {
                                selectedMonth = 12
                                selectedYear--
                            }
                            onYearMonthChange(String.format("%04d-%02d", selectedYear, selectedMonth))
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "ماه قبل", tint = Slate700)
                    }

                    Text(
                        text = "دوره مالی: $periodTitle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 15.sp
                        )
                    )

                    IconButton(
                        onClick = {
                            if (selectedMonth < 12) {
                                selectedMonth++
                            } else {
                                selectedMonth = 1
                                selectedYear++
                            }
                            onYearMonthChange(String.format("%04d-%02d", selectedYear, selectedMonth))
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "ماه بعد", tint = Slate700)
                    }
                }
            }
        }

        // Executive Financial Summary Banner
        item {
            DarkGlassCard(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "جمع کل کارکرد ناوگان در دوره",
                            style = MaterialTheme.typography.labelMedium.copy(color = Slate400)
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (currentPeriodStatus) {
                                PaymentStatus.PAID -> EmeraldPrimary
                                PaymentStatus.SENT_TO_FINANCE -> Color(0xFF3B82F6)
                                PaymentStatus.APPROVED -> GoldPrimary
                                else -> Slate700
                            }
                        ) {
                            Text(
                                text = currentPeriodStatus.faTitle,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = MoneyUtils.formatToman(totalHoldingsPayable),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Change Workflow Status Button
                        Button(
                            onClick = { showWorkflowStatusDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.weight(1f).testTag("change_status_flow_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تغییر وضعیت تسویه")
                        }

                        // Export Excel CSV
                        OutlinedButton(
                            onClick = {
                                val csv = onExportCsv(periodTitle, settlementRows)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "ارسال فایل گزارش اکسل")
                                context.startActivity(shareIntent)
                                Toast.makeText(context, "فایل اکسل با استاندارد UTF-8 تولید شد.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Slate300),
                            modifier = Modifier.weight(1f).testTag("export_excel_csv_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("خروجی اکسل")
                        }
                    }
                }
            }
        }

        // Section: Driver Settlement List
        item {
            Text(
                text = "جدول تفکیکی کارکرد و تسویه رانندگان",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
        }

        items(settlementRows) { row ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("finance_driver_row_${row.driverCode}"),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = row.driverName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
                            )
                            Text(
                                text = "کد راننده: ${row.driverCode} | پرسنلی: ${row.personnelCode.toPersianDigits()}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                            )
                        }

                        Button(
                            onClick = { selectedRowForSlip = row },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("view_slip_btn_${row.driverCode}")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("فیش تسویه", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("روزهای کاری", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                            Text("${row.workingDaysCount.toPersianDigits()} روز", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }

                        Column {
                            Text("تعداد کل سفرها", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                            Text("${row.totalTripsCount.toPersianDigits()} سفر", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("مبلغ قابل پرداخت", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                            Text(
                                text = MoneyUtils.formatToman(row.finalizedIncome),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldDark
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

    // Financial Slip Preview Dialog
    if (selectedRowForSlip != null) {
        val slipRow = selectedRowForSlip!!
        FinancialSlipDialog(
            periodTitle = periodTitle,
            driverName = slipRow.driverName,
            personnelCode = slipRow.personnelCode,
            driverCode = slipRow.driverCode,
            workingDays = slipRow.workingDaysCount,
            totalTrips = slipRow.totalTripsCount,
            totalAmount = slipRow.finalizedIncome,
            paymentStatus = slipRow.paymentStatus.faTitle,
            onDismiss = { selectedRowForSlip = null }
        )
    }

    // Payment Workflow Status Selector Dialog
    if (showWorkflowStatusDialog) {
        AlertDialog(
            onDismissRequest = { showWorkflowStatusDialog = false },
            title = {
                Text(
                    text = "تغییر وضعیت فرآیند مالی دوره $periodTitle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentStatus.values().forEach { status ->
                        val isSelected = currentPeriodStatus == status
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GoldLight else Slate100,
                            border = if (isSelected) BorderStroke(1.dp, GoldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onUpdatePaymentStatus(status)
                                    showWorkflowStatusDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = status.faTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWorkflowStatusDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}
