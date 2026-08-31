package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.UserRole
import com.example.ui.components.HoldingBrandHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminDriversScreen
import com.example.ui.screens.AdminRoutesScreen
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.DriverHomeScreen
import com.example.ui.screens.FinanceReportScreen
import com.example.ui.screens.RegisterTripScreen
import com.example.ui.theme.AmbientGlassBackdrop
import com.example.ui.theme.CanvasBgBottom
import com.example.ui.theme.CanvasBgTop
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.FleetViewModel

enum class ScreenRoute {
    DRIVER_HOME,
    DRIVER_REGISTER_TRIP,
    ADMIN_DASHBOARD,
    ADMIN_ROUTES,
    ADMIN_DRIVERS,
    ADMIN_FINANCE,
    ADMIN_AUDIT_LOGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: FleetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FleetApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun FleetApp(viewModel: FleetViewModel) {
    val context = LocalContext.current

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allDrivers by viewModel.allDrivers.collectAsStateWithLifecycle()
    val selectedDriver by viewModel.selectedDriver.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()

    val activeLocations by viewModel.activeLocations.collectAsStateWithLifecycle()
    val allRoutes by viewModel.allRoutes.collectAsStateWithLifecycle()

    val currentDailyWork by viewModel.currentDailyWork.collectAsStateWithLifecycle()
    val currentDailyTrips by viewModel.currentDailyTrips.collectAsStateWithLifecycle()
    val monthlySettlements by viewModel.monthlySettlements.collectAsStateWithLifecycle()
    val driverMonthlyIncome by viewModel.currentDriverMonthlyIncome.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val allApprovals by viewModel.allApprovals.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(ScreenRoute.DRIVER_HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HoldingBrandHeader(
                currentRole = currentRole,
                driverName = selectedDriver?.fullName ?: "راننده هلدینگ",
                onSwitchRole = { newRole ->
                    viewModel.setRole(newRole)
                    when (newRole) {
                        UserRole.DRIVER -> currentScreen = ScreenRoute.DRIVER_HOME
                        UserRole.ADMIN -> currentScreen = ScreenRoute.ADMIN_DASHBOARD
                        UserRole.FINANCE -> currentScreen = ScreenRoute.ADMIN_FINANCE
                    }
                }
            )
        }
    ) { innerPadding ->
        AmbientGlassBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    ScreenRoute.DRIVER_HOME -> {
                        val activeDriver = selectedDriver ?: allDrivers.firstOrNull()
                        if (activeDriver != null) {
                            DriverHomeScreen(
                                driver = activeDriver,
                                selectedDate = selectedDate,
                                dailyWork = currentDailyWork,
                                todayTrips = currentDailyTrips,
                                monthlyIncome = driverMonthlyIncome,
                                allDrivers = allDrivers,
                                onSelectDriver = { viewModel.selectDriver(it) },
                                onSelectDate = { viewModel.selectDate(it) },
                                onRegisterNewTripClick = { currentScreen = ScreenRoute.DRIVER_REGISTER_TRIP },
                                onFinalizeDayClick = {
                                    viewModel.submitDailyWorkForApproval(
                                        driverId = activeDriver.id,
                                        jalaliDate = selectedDate.formatStandard(),
                                        onSuccess = {
                                            Toast.makeText(context, "کارکرد روز جهت بررسی و تأیید برای مدیر ناوگان ارسال شد.", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onDeleteTripClick = { trip ->
                                    viewModel.deleteTrip(trip) { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = NavyPrimary)
                            }
                        }
                    }

                    ScreenRoute.DRIVER_REGISTER_TRIP -> {
                        val activeDriver = selectedDriver ?: allDrivers.firstOrNull()
                        if (activeDriver != null) {
                            RegisterTripScreen(
                                driver = activeDriver,
                                initialDate = selectedDate,
                                locations = activeLocations,
                                allRoutes = allRoutes,
                                onNavigateBack = { currentScreen = ScreenRoute.DRIVER_HOME },
                                onSubmitTrip = { driverId, jalaliDate, routeId, startTime, endTime, description ->
                                    viewModel.registerTrip(
                                        driverId = driverId,
                                        jalaliDate = jalaliDate,
                                        routeId = routeId,
                                        startTime = startTime,
                                        endTime = endTime,
                                        description = description,
                                        onSuccess = {
                                            Toast.makeText(context, "سفر با نرخ مصوب با موفقیت ثبت گردید.", Toast.LENGTH_SHORT).show()
                                            currentScreen = ScreenRoute.DRIVER_HOME
                                        },
                                        onError = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                    }

                    ScreenRoute.ADMIN_DASHBOARD -> {
                        AdminDashboardScreen(
                            drivers = allDrivers,
                            routes = allRoutes,
                            settlementRows = monthlySettlements,
                            allApprovals = allApprovals,
                            onApproveDailyWork = { dailyWorkId, adminName ->
                                viewModel.approveDailyWork(
                                    dailyWorkId = dailyWorkId,
                                    adminName = adminName,
                                    onSuccess = {
                                        Toast.makeText(context, "کارکرد روز راننده با موفقیت تأیید شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onRejectDailyWork = { dailyWorkId, reason, adminName ->
                                viewModel.rejectDailyWork(
                                    dailyWorkId = dailyWorkId,
                                    reason = reason,
                                    adminName = adminName,
                                    onSuccess = {
                                        Toast.makeText(context, "عدم تأیید کارکرد ثبت و به راننده بازگردانده شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onUnlockDailyWork = { dailyWorkId, adminName ->
                                viewModel.unlockDailyWork(
                                    dailyWorkId = dailyWorkId,
                                    adminName = adminName,
                                    onSuccess = {
                                        Toast.makeText(context, "کارکرد روز بازگشایی شد و راننده می‌تواند آن را ویرایش کند.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onNavigateToRoutes = { currentScreen = ScreenRoute.ADMIN_ROUTES },
                            onNavigateToDrivers = { currentScreen = ScreenRoute.ADMIN_DRIVERS },
                            onNavigateToFinance = { currentScreen = ScreenRoute.ADMIN_FINANCE },
                            onNavigateToAuditLogs = { currentScreen = ScreenRoute.ADMIN_AUDIT_LOGS }
                        )
                    }

                    ScreenRoute.ADMIN_ROUTES -> {
                        AdminRoutesScreen(
                            routes = allRoutes,
                            locations = activeLocations,
                            onNavigateBack = { currentScreen = ScreenRoute.ADMIN_DASHBOARD },
                            onSaveRoute = { routeId, routeCode, originId, originName, destId, destName, price, desc ->
                                viewModel.saveRoute(routeId, routeCode, originId, originName, destId, destName, price, desc)
                                Toast.makeText(context, "نرخ و مسیر مصوب با موفقیت ثبت شد.", Toast.LENGTH_SHORT).show()
                            },
                            onSyncCsv = { csvContent ->
                                viewModel.syncRoutesFromCsv(csvContent) { result ->
                                    result.onSuccess { count ->
                                        Toast.makeText(context, "تعداد $count مسیر و مقصد از اکسل با موفقیت بروزرسانی شد.", Toast.LENGTH_LONG).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "خطا در پردازش فایل: ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }

                    ScreenRoute.ADMIN_DRIVERS -> {
                        AdminDriversScreen(
                            drivers = allDrivers,
                            onNavigateBack = { currentScreen = ScreenRoute.ADMIN_DASHBOARD },
                            onSaveDriver = { driver ->
                                viewModel.saveDriver(driver)
                                Toast.makeText(context, "اطلاعات راننده ذخیره گردید.", Toast.LENGTH_SHORT).show()
                            },
                            onToggleActive = { driverId, currentStatus ->
                                viewModel.toggleDriverStatus(driverId, currentStatus)
                            }
                        )
                    }

                    ScreenRoute.ADMIN_FINANCE -> {
                        FinanceReportScreen(
                            currentYearMonth = selectedYearMonth,
                            settlementRows = monthlySettlements,
                            onNavigateBack = {
                                if (currentRole == UserRole.FINANCE) {
                                    viewModel.setRole(UserRole.ADMIN)
                                }
                                currentScreen = ScreenRoute.ADMIN_DASHBOARD
                            },
                            onYearMonthChange = { viewModel.selectYearMonth(it) },
                            onUpdatePaymentStatus = { newStatus ->
                                viewModel.updatePaymentStatus(newStatus)
                                Toast.makeText(context, "وضعیت پرداخت به «${newStatus.faTitle}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                            },
                            onExportCsv = { periodTitle, rows ->
                                viewModel.exportMonthlyCsv(periodTitle, rows)
                            }
                        )
                    }

                    ScreenRoute.ADMIN_AUDIT_LOGS -> {
                        AuditLogsScreen(
                            auditLogs = auditLogs,
                            onNavigateBack = { currentScreen = ScreenRoute.ADMIN_DASHBOARD }
                        )
                    }
                }
            }
        }
    }
}
