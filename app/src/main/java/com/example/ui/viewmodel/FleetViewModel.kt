package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AuditLogEntity
import com.example.data.repository.FleetRepository
import com.example.domain.model.PendingDailyApproval
import com.example.domain.model.DailyWorkSummary
import com.example.domain.model.Driver
import com.example.domain.model.LocationItem
import com.example.domain.model.MonthlySettlementRow
import com.example.domain.model.PaymentStatus
import com.example.domain.model.RouteItem
import com.example.domain.model.Trip
import com.example.domain.model.UserRole
import com.example.util.PersianDateHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FleetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FleetRepository(db.fleetDao())

    // Active User Role Switcher (DRIVER, ADMIN, FINANCE)
    private val _currentRole = MutableStateFlow(UserRole.DRIVER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Drivers Flow
    val allDrivers: StateFlow<List<Driver>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // All Daily Work Approvals for Admin Inbox
    val allApprovals: StateFlow<List<PendingDailyApproval>> = repository.observeAllDailyWorkApprovals()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected Driver for Driver Mode
    private val _selectedDriver = MutableStateFlow<Driver?>(null)
    val selectedDriver: StateFlow<Driver?> = _selectedDriver.asStateFlow()

    // Selected Jalali Date for Driver Mode
    private val _selectedDate = MutableStateFlow(PersianDateHelper.getTodayJalali())
    val selectedDate: StateFlow<PersianDateHelper.JalaliDate> = _selectedDate.asStateFlow()

    // Selected Year-Month for Financial and Admin Mode
    private val _selectedYearMonth = MutableStateFlow(PersianDateHelper.getTodayJalali().getYearMonthKey())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    // Locations and Routes
    val activeLocations: StateFlow<List<LocationItem>> = repository.activeLocations
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allRoutes: StateFlow<List<RouteItem>> = repository.allRoutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected Driver Daily Work Summary
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDailyWork: StateFlow<DailyWorkSummary?> = kotlinx.coroutines.flow.combine(
        _selectedDriver,
        _selectedDate
    ) { driver, date ->
        Pair(driver?.id ?: "drv-101", date.formatStandard())
    }.flatMapLatest { (driverId, dateStr) ->
        repository.observeDailyWork(driverId, dateStr)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Selected Driver Daily Trips List
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDailyTrips: StateFlow<List<Trip>> = kotlinx.coroutines.flow.combine(
        _selectedDriver,
        _selectedDate
    ) { driver, date ->
        Pair(driver?.id ?: "drv-101", date.formatStandard())
    }.flatMapLatest { (driverId, dateStr) ->
        repository.getTripsForDriverAndDate(driverId, dateStr)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Monthly settlement calculations
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySettlements: StateFlow<List<MonthlySettlementRow>> = _selectedYearMonth
        .flatMapLatest { yearMonth ->
            repository.getMonthlySettlementReport(yearMonth)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Current driver monthly earnings
    val currentDriverMonthlyIncome: StateFlow<Long> = kotlinx.coroutines.flow.combine(
        _selectedDriver,
        monthlySettlements
    ) { driver, settlements ->
        val driverId = driver?.id ?: "drv-101"
        settlements.find { it.driverId == driverId }?.finalizedIncome ?: 0L
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // Audit logs
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            db.seedInitialData()
        }
        // Automatically default selected driver to first driver once loaded
        viewModelScope.launch {
            allDrivers.collect { list ->
                if (_selectedDriver.value == null && list.isNotEmpty()) {
                    _selectedDriver.value = list.first()
                }
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectDriver(driver: Driver) {
        _selectedDriver.value = driver
    }

    fun selectDate(date: PersianDateHelper.JalaliDate) {
        _selectedDate.value = date
    }

    fun selectYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun registerTrip(
        driverId: String,
        jalaliDate: String,
        routeId: String,
        startTime: String,
        endTime: String?,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerTrip(driverId, jalaliDate, routeId, startTime, endTime, description)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "خطا در ثبت سفر") }
        }
    }

    fun deleteTrip(trip: Trip, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTrip(trip.id, trip.driverId, trip.tripJalaliDate)
            result.onFailure { onError(it.message ?: "خطا در حذف سفر") }
        }
    }

    fun submitDailyWorkForApproval(driverId: String, jalaliDate: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.submitDailyWorkForApproval(driverId, jalaliDate)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "خطا در ارسال جهت تأیید") }
        }
    }

    fun approveDailyWork(dailyWorkId: String, adminName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.approveDailyWork(dailyWorkId, adminName)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "خطا در تأیید کارکرد") }
        }
    }

    fun rejectDailyWork(dailyWorkId: String, reason: String, adminName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.rejectDailyWork(dailyWorkId, reason, adminName)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "خطا در رد کارکرد") }
        }
    }

    fun unlockDailyWork(dailyWorkId: String, adminName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.unlockDailyWork(dailyWorkId, adminName)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "خطا در بازگشایی کارکرد") }
        }
    }

    fun finalizeDay(driverId: String, jalaliDate: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        submitDailyWorkForApproval(driverId, jalaliDate, onSuccess, onError)
    }

    fun saveRoute(
        routeId: String?,
        routeCode: String,
        originId: Long,
        originName: String,
        destinationId: Long,
        destinationName: String,
        price: Long,
        description: String
    ) {
        viewModelScope.launch {
            repository.saveRoute(
                routeId = routeId,
                routeCode = routeCode,
                originId = originId,
                originName = originName,
                destinationId = destinationId,
                destinationName = destinationName,
                price = price,
                description = description,
                operatorName = "مسئول کارکرد ناوگان"
            )
        }
    }

    fun saveDriver(driver: Driver) {
        viewModelScope.launch {
            repository.saveDriver(driver, operatorName = "مدیر سیستم")
        }
    }

    fun toggleDriverStatus(driverId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleDriverActiveStatus(driverId, currentStatus, operatorName = "مدیر سیستم")
        }
    }

    fun updatePaymentStatus(newStatus: PaymentStatus) {
        viewModelScope.launch {
            repository.updateFinancialPeriodStatus(
                yearMonthKey = _selectedYearMonth.value,
                newStatus = newStatus,
                operatorName = "واحد مالی هلدینگ"
            )
        }
    }

    fun exportMonthlyCsv(periodTitle: String, rows: List<MonthlySettlementRow>): String {
        return repository.generateExcelCsvContent(periodTitle, rows)
    }

    fun syncRoutesFromCsv(
        csvText: String,
        onResult: (Result<Int>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.syncLocationsAndRoutesFromCsv(
                csvText = csvText,
                operatorName = "مدیر ناوگان هلدینگ"
            )
            onResult(result)
        }
    }
}
