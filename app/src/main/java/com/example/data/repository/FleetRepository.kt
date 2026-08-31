package com.example.data.repository

import com.example.data.local.dao.FleetDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.DailyWorkLogEntity
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.FinancialPeriodEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.RouteEntity
import com.example.data.local.entity.RoutePriceHistoryEntity
import com.example.data.local.entity.TripEntity
import com.example.domain.model.DailyStatus
import com.example.domain.model.DailyWorkSummary
import com.example.domain.model.Driver
import com.example.domain.model.LocationItem
import com.example.domain.model.MonthlySettlementRow
import com.example.domain.model.PaymentStatus
import com.example.domain.model.PendingDailyApproval
import com.example.domain.model.RouteItem
import com.example.domain.model.Trip
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class FleetRepository(private val dao: FleetDao) {

    // ── Drivers ──────────────────────────────────────────────────────────
    val allDrivers: Flow<List<Driver>> = dao.getAllDrivers().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    suspend fun getDriverById(id: String): Driver? = withContext(Dispatchers.IO) {
        dao.getDriverById(id)?.toDomain()
    }

    suspend fun saveDriver(driver: Driver, operatorName: String) = withContext(Dispatchers.IO) {
        val entity = driver.toEntity()
        dao.insertDriver(entity)
        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = operatorName,
                operatorRole = "ADMIN",
                action = "SAVE_DRIVER",
                entityTitle = driver.fullName,
                details = "ثبت / به‌روزرسانی راننده با کد ${driver.driverCode} و کد پرسنلی ${driver.personnelCode}",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
    }

    suspend fun toggleDriverActiveStatus(driverId: String, currentStatus: Boolean, operatorName: String) = withContext(Dispatchers.IO) {
        val existing = dao.getDriverById(driverId) ?: return@withContext
        val updated = existing.copy(isActive = !currentStatus)
        dao.updateDriver(updated)
        val statusText = if (updated.isActive) "فعال" else "غیرفعال"
        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = operatorName,
                operatorRole = "ADMIN",
                action = "TOGGLE_DRIVER_STATUS",
                entityTitle = updated.fullName,
                details = "تغییر وضعیت راننده به $statusText",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
    }

    // ── Locations ────────────────────────────────────────────────────────
    val activeLocations: Flow<List<LocationItem>> = dao.getAllActiveLocations().map { entities ->
        entities.map { LocationItem(it.id, it.name, it.city, it.isActive) }
    }.flowOn(Dispatchers.IO)

    suspend fun addLocation(name: String, city: String, operatorName: String): Long = withContext(Dispatchers.IO) {
        val id = dao.insertLocation(LocationEntity(name = name, city = city, isActive = true))
        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = operatorName,
                operatorRole = "ADMIN",
                action = "ADD_LOCATION",
                entityTitle = name,
                details = "ایجاد مکان مصوب جدید در شهر $city",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
        id
    }

    // ── Routes & Tariffs ──────────────────────────────────────────────────
    val allRoutes: Flow<List<RouteItem>> = dao.getAllRoutes().map { entities ->
        entities.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    suspend fun findRoute(originId: Long, destinationId: Long): RouteItem? = withContext(Dispatchers.IO) {
        dao.findRouteByEndpoints(originId, destinationId)?.toDomain()
    }

    suspend fun saveRoute(
        routeId: String?,
        routeCode: String,
        originId: Long,
        originName: String,
        destinationId: Long,
        destinationName: String,
        price: Long,
        description: String,
        operatorName: String
    ) = withContext(Dispatchers.IO) {
        val todayStr = PersianDateHelper.getTodayJalali().formatStandard()
        val id = routeId ?: "rt-${UUID.randomUUID().toString().take(8)}"
        val existing = dao.getRouteById(id)

        val newEntity = RouteEntity(
            id = id,
            routeCode = routeCode.trim(),
            originId = originId,
            originName = originName,
            destinationId = destinationId,
            destinationName = destinationName,
            currentPrice = price,
            currency = "TOMAN",
            isActive = true,
            description = description,
            updatedAtJalali = todayStr
        )
        dao.insertRoute(newEntity)

        // If price changed or new route, record in RoutePriceHistory
        if (existing != null && existing.currentPrice != price) {
            dao.insertPriceHistory(
                RoutePriceHistoryEntity(
                    routeId = id,
                    routeCode = routeCode,
                    oldPrice = existing.currentPrice,
                    newPrice = price,
                    changedBy = operatorName,
                    effectiveDateJalali = todayStr
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    operatorName = operatorName,
                    operatorRole = "ADMIN",
                    action = "UPDATE_ROUTE_PRICE",
                    entityTitle = "مسیر $routeCode ($originName ← $destinationName)",
                    details = "تغییر نرخ مصوب از ${MoneyUtils.formatToman(existing.currentPrice)} به ${MoneyUtils.formatToman(price)}",
                    jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
                )
            )
        } else if (existing == null) {
            dao.insertPriceHistory(
                RoutePriceHistoryEntity(
                    routeId = id,
                    routeCode = routeCode,
                    oldPrice = 0,
                    newPrice = price,
                    changedBy = operatorName,
                    effectiveDateJalali = todayStr
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    operatorName = operatorName,
                    operatorRole = "ADMIN",
                    action = "CREATE_ROUTE",
                    entityTitle = "مسیر جدید $routeCode",
                    details = "تعریف مسیر $originName به $destinationName با نرخ مصوب ${MoneyUtils.formatToman(price)}",
                    jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
                )
            )
        }
    }

    fun getPriceHistory(routeId: String): Flow<List<RoutePriceHistoryEntity>> {
        return dao.getPriceHistoryForRoute(routeId)
    }

    suspend fun syncLocationsAndRoutesFromCsv(
        csvText: String,
        primaryOrigin: String = com.example.data.local.CsvFleetData.DEFAULT_PRIMARY_ORIGIN,
        operatorName: String = "مدیر سیستم"
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val count = com.example.data.local.CsvFleetData.syncWithDatabase(
                dao = dao,
                csvText = csvText,
                primaryOriginName = primaryOrigin,
                operatorName = operatorName
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    operatorName = operatorName,
                    operatorRole = "ADMIN",
                    action = "IMPORT_CSV_ROUTES",
                    entityTitle = "بروزرسانی مسیرها با اکسل/CSV",
                    details = "همگام‌سازی $count مسیر و مکان مصوب با موفقیت انجام شد",
                    jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
                )
            )
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Trips & Daily Registration ────────────────────────────────────────
    fun observeDailyWork(driverId: String, jalaliDate: String): Flow<DailyWorkSummary?> {
        return combine(
            dao.observeDailyWork(driverId, jalaliDate),
            dao.getTripsForDriverAndDate(driverId, jalaliDate)
        ) { dailyEntity, trips ->
            val driver = dao.getDriverById(driverId)
            val driverName = driver?.fullName ?: "راننده هلدینگ"
            val driverCode = driver?.driverCode ?: ""
            val totalIncome = trips.sumOf { it.appliedPrice }
            val count = trips.size
            val status = when (dailyEntity?.status) {
                "PENDING_APPROVAL" -> DailyStatus.PENDING_APPROVAL
                "FINALIZED" -> DailyStatus.FINALIZED
                "REJECTED" -> DailyStatus.REJECTED
                else -> DailyStatus.DRAFT
            }

            DailyWorkSummary(
                id = dailyEntity?.id ?: "dw-${jalaliDate.replace("/", "")}-$driverId",
                driverId = driverId,
                driverName = driverName,
                driverCode = driverCode,
                jalaliDate = jalaliDate,
                totalTrips = count,
                totalIncome = totalIncome,
                status = status,
                finalizedAt = dailyEntity?.finalizedAt,
                approvedBy = dailyEntity?.approvedBy,
                rejectionReason = dailyEntity?.rejectionReason,
                notes = dailyEntity?.notes ?: ""
            )
        }.flowOn(Dispatchers.IO)
    }

    fun observeAllDailyWorkApprovals(): Flow<List<PendingDailyApproval>> {
        return combine(
            dao.getAllDailyWorkLogs(),
            dao.getAllDrivers(),
            dao.getRecentTrips()
        ) { dailyLogs, drivers, trips ->
            val driversMap = drivers.associateBy { it.id }
            dailyLogs.map { log ->
                val driver = driversMap[log.driverId]
                val logTrips = trips.filter { it.dailyWorkId == log.id || (it.driverId == log.driverId && it.tripJalaliDate == log.jalaliDate) }
                val status = when (log.status) {
                    "PENDING_APPROVAL" -> DailyStatus.PENDING_APPROVAL
                    "FINALIZED" -> DailyStatus.FINALIZED
                    "REJECTED" -> DailyStatus.REJECTED
                    else -> DailyStatus.DRAFT
                }
                val summary = DailyWorkSummary(
                    id = log.id,
                    driverId = log.driverId,
                    driverName = driver?.fullName ?: "راننده هلدینگ",
                    driverCode = driver?.driverCode ?: "",
                    jalaliDate = log.jalaliDate,
                    totalTrips = if (log.totalTrips > 0) log.totalTrips else logTrips.size,
                    totalIncome = if (log.totalIncome > 0) log.totalIncome else logTrips.sumOf { it.appliedPrice },
                    status = status,
                    finalizedAt = log.finalizedAt,
                    approvedBy = log.approvedBy,
                    rejectionReason = log.rejectionReason,
                    notes = log.notes
                )
                PendingDailyApproval(
                    dailyWork = summary,
                    driver = driver?.toDomain(),
                    trips = logTrips.map { it.toDomain() }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getTripsForDriverAndDate(driverId: String, jalaliDate: String): Flow<List<Trip>> {
        return dao.getTripsForDriverAndDate(driverId, jalaliDate).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun registerTrip(
        driverId: String,
        jalaliDate: String,
        routeId: String,
        startTime: String,
        endTime: String?,
        description: String
    ): Result<Trip> = withContext(Dispatchers.IO) {
        val route = dao.getRouteById(routeId) ?: return@withContext Result.failure(Exception("مسیر معتبر نیست."))
        val driver = dao.getDriverById(driverId) ?: return@withContext Result.failure(Exception("راننده یافت نشد."))

        val dailyWork = dao.getDailyWork(driverId, jalaliDate)
        if (dailyWork?.status == "FINALIZED") {
            return@withContext Result.failure(Exception("کارکرد این تاریخ توسط ادمین تأیید و قفل شده است."))
        }
        if (dailyWork?.status == "PENDING_APPROVAL") {
            return@withContext Result.failure(Exception("کارکرد این تاریخ به مدیریت ارسال شده و در انتظار تأیید است."))
        }

        val dailyWorkId = dailyWork?.id ?: "dw-${jalaliDate.replace("/", "")}-$driverId"
        if (dailyWork == null) {
            dao.insertDailyWork(
                DailyWorkLogEntity(
                    id = dailyWorkId,
                    driverId = driverId,
                    jalaliDate = jalaliDate,
                    totalTrips = 0,
                    totalIncome = 0,
                    status = "DRAFT"
                )
            )
        }

        val tripId = "trp-${UUID.randomUUID().toString().take(8)}"
        val tripCode = "TRP-${jalaliDate.replace("/", "")}-${(100..999).random()}"

        val tripEntity = TripEntity(
            id = tripId,
            tripCode = tripCode,
            dailyWorkId = dailyWorkId,
            driverId = driverId,
            driverName = driver.fullName,
            routeId = route.id,
            originTitle = route.originName,
            destinationTitle = route.destinationName,
            routeCode = route.routeCode,
            appliedPrice = route.currentPrice, // Critical: Immutable Price Snapshot
            currency = route.currency,
            tripJalaliDate = jalaliDate,
            startTime = startTime,
            endTime = endTime,
            description = description,
            createdAt = System.currentTimeMillis()
        )

        dao.insertTrip(tripEntity)

        // Update Daily Work Totals
        val existingDaily = dao.getDailyWork(driverId, jalaliDate)
        if (existingDaily != null) {
            val updatedCount = existingDaily.totalTrips + 1
            val updatedIncome = existingDaily.totalIncome + route.currentPrice
            dao.updateDailyWork(existingDaily.copy(totalTrips = updatedCount, totalIncome = updatedIncome, status = "DRAFT"))
        }

        Result.success(tripEntity.toDomain())
    }

    suspend fun deleteTrip(tripId: String, driverId: String, jalaliDate: String): Result<Unit> = withContext(Dispatchers.IO) {
        val dailyWork = dao.getDailyWork(driverId, jalaliDate)
        if (dailyWork?.status == "FINALIZED") {
            return@withContext Result.failure(Exception("امکان حذف سفر در کارکرد تأیید شده وجود ندارد."))
        }
        if (dailyWork?.status == "PENDING_APPROVAL") {
            return@withContext Result.failure(Exception("امکان حذف سفر در وضعیت در انتظار تأیید وجود ندارد."))
        }
        dao.deleteTripById(tripId)
        Result.success(Unit)
    }

    suspend fun submitDailyWorkForApproval(driverId: String, jalaliDate: String): Result<Unit> = withContext(Dispatchers.IO) {
        val daily = dao.getDailyWork(driverId, jalaliDate)
        val dailyId = daily?.id ?: "dw-${jalaliDate.replace("/", "")}-$driverId"
        val driver = dao.getDriverById(driverId)

        val updated = DailyWorkLogEntity(
            id = dailyId,
            driverId = driverId,
            jalaliDate = jalaliDate,
            totalTrips = daily?.totalTrips ?: 0,
            totalIncome = daily?.totalIncome ?: 0,
            status = "PENDING_APPROVAL",
            finalizedAt = null,
            approvedBy = null,
            rejectionReason = null,
            notes = daily?.notes ?: ""
        )
        dao.insertDailyWork(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = driver?.fullName ?: "راننده",
                operatorRole = "DRIVER",
                action = "SUBMIT_FOR_APPROVAL",
                entityTitle = "کارکرد روز $jalaliDate (${driver?.fullName})",
                details = "ارسال کارکرد با ${updated.totalTrips} سفر به ارزش ${MoneyUtils.formatToman(updated.totalIncome)} جهت بررسی و تأیید ادمین",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
        Result.success(Unit)
    }

    suspend fun approveDailyWork(dailyWorkId: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getDailyWorkById(dailyWorkId) ?: return@withContext Result.failure(Exception("کارکرد یافت نشد."))
        val driver = dao.getDriverById(existing.driverId)
        val updated = existing.copy(
            status = "FINALIZED",
            finalizedAt = System.currentTimeMillis(),
            approvedBy = adminName,
            rejectionReason = null
        )
        dao.updateDailyWork(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = adminName,
                operatorRole = "ADMIN",
                action = "APPROVE_DAILY_WORK",
                entityTitle = "تأیید کارکرد روز ${existing.jalaliDate} (${driver?.fullName ?: ""})",
                details = "تأیید نهایی کارکرد با ${existing.totalTrips} سفر و ارزش ${MoneyUtils.formatToman(existing.totalIncome)} توسط $adminName",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
        Result.success(Unit)
    }

    suspend fun rejectDailyWork(dailyWorkId: String, reason: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getDailyWorkById(dailyWorkId) ?: return@withContext Result.failure(Exception("کارکرد یافت نشد."))
        val driver = dao.getDriverById(existing.driverId)
        val updated = existing.copy(
            status = "REJECTED",
            rejectionReason = reason,
            approvedBy = adminName
        )
        dao.updateDailyWork(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = adminName,
                operatorRole = "ADMIN",
                action = "REJECT_DAILY_WORK",
                entityTitle = "عدم تأیید کارکرد روز ${existing.jalaliDate} (${driver?.fullName ?: ""})",
                details = "عدم تأیید کارکرد به علت: «$reason» توسط $adminName",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
        Result.success(Unit)
    }

    suspend fun unlockDailyWork(dailyWorkId: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getDailyWorkById(dailyWorkId) ?: return@withContext Result.failure(Exception("کارکرد یافت نشد."))
        val driver = dao.getDriverById(existing.driverId)
        val updated = existing.copy(
            status = "DRAFT",
            finalizedAt = null,
            approvedBy = null,
            rejectionReason = null
        )
        dao.updateDailyWork(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = adminName,
                operatorRole = "ADMIN",
                action = "UNLOCK_DAILY_WORK",
                entityTitle = "بازگشایی کارکرد ${existing.jalaliDate} (${driver?.fullName ?: ""})",
                details = "مجوز ویرایش مجدد کارکرد توسط مدیر ناوگان صادر شد",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
        Result.success(Unit)
    }

    // Deprecated alias for backwards compatibility
    suspend fun finalizeDailyWork(driverId: String, jalaliDate: String): Result<Unit> {
        return submitDailyWorkForApproval(driverId, jalaliDate)
    }

    // ── Monthly & Financial Calculations ─────────────────────────────────
    fun getMonthlySettlementReport(yearMonthKey: String): Flow<List<MonthlySettlementRow>> {
        return combine(
            dao.getAllDrivers(),
            dao.getTripsForMonth(yearMonthKey),
            dao.getDailyWorkLogsForMonth(yearMonthKey)
        ) { drivers, trips, dailyLogs ->
            val period = dao.getFinancialPeriod(yearMonthKey)
            val paymentStatus = when (period?.status) {
                "CALCULATING" -> PaymentStatus.CALCULATING
                "APPROVED" -> PaymentStatus.APPROVED
                "SENT_TO_FINANCE" -> PaymentStatus.SENT_TO_FINANCE
                "PAID" -> PaymentStatus.PAID
                else -> PaymentStatus.PENDING_APPROVAL
            }

            drivers.map { driver ->
                val driverTrips = trips.filter { it.driverId == driver.id }
                val driverDailyLogs = dailyLogs.filter { it.driverId == driver.id }

                val workingDays = driverDailyLogs.map { it.jalaliDate }.distinct().size
                val totalTrips = driverTrips.size
                val finalizedIncome = driverDailyLogs.filter { it.status == "FINALIZED" }.sumOf { it.totalIncome }
                val draftIncome = driverDailyLogs.filter { it.status == "DRAFT" }.sumOf { it.totalIncome }
                val unfinalizedCount = driverDailyLogs.count { it.status == "DRAFT" }

                MonthlySettlementRow(
                    driverId = driver.id,
                    driverName = driver.fullName,
                    driverCode = driver.driverCode,
                    personnelCode = driver.personnelCode,
                    workingDaysCount = if (workingDays == 0 && totalTrips > 0) 1 else workingDays,
                    totalTripsCount = totalTrips,
                    finalizedIncome = if (finalizedIncome > 0) finalizedIncome else driverTrips.sumOf { it.appliedPrice },
                    draftIncome = draftIncome,
                    unfinalizedDaysCount = unfinalizedCount,
                    paymentStatus = paymentStatus
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateFinancialPeriodStatus(yearMonthKey: String, newStatus: PaymentStatus, operatorName: String) = withContext(Dispatchers.IO) {
        val existing = dao.getFinancialPeriod(yearMonthKey)
        val todayStr = PersianDateHelper.getTodayJalali().formatStandard()

        val updated = FinancialPeriodEntity(
            id = existing?.id ?: 0,
            jalaliYearMonth = yearMonthKey,
            status = newStatus.name,
            approvedBy = if (newStatus == PaymentStatus.APPROVED || newStatus == PaymentStatus.SENT_TO_FINANCE) operatorName else existing?.approvedBy,
            paidAtJalali = if (newStatus == PaymentStatus.PAID) todayStr else existing?.paidAtJalali,
            totalAmount = existing?.totalAmount ?: 0,
            notes = "به‌روزرسانی وضعیت به ${newStatus.faTitle}"
        )
        dao.insertFinancialPeriod(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = operatorName,
                operatorRole = "FINANCE",
                action = "UPDATE_FINANCIAL_STATUS",
                entityTitle = "دوره مالی $yearMonthKey",
                details = "تغییر وضعیت تسویه‌حساب به: ${newStatus.faTitle}",
                jalaliTimestamp = PersianDateHelper.getTodayJalali().formatReadable() + " - " + PersianDateHelper.getCurrentTimeString()
            )
        )
    }

    // ── Audit Logs ────────────────────────────────────────────────────────
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    // ── Excel CSV Generator with UTF-8 BOM ────────────────────────────────
    fun generateExcelCsvContent(periodTitle: String, rows: List<MonthlySettlementRow>): String {
        val builder = StringBuilder()
        builder.append("\uFEFF") // UTF-8 BOM for Microsoft Excel compatibility with Persian/RTL characters
        builder.append("گزارش کارکرد و تسویه‌حساب مالی رانندگان هلدینگ آرمان انتخاب\n")
        builder.append("دوره مالی: $periodTitle\n")
        builder.append("تاریخ خروجی: ${PersianDateHelper.getTodayJalali().formatReadable()}\n\n")
        builder.append("ردیف,نام و نام خانوادگی,کد راننده,کد پرسنلی,تعداد روز کاری,تعداد کل سفرها,مبلغ قابل پرداخت (تومان),وضعیت پرداخت\n")

        var totalSum: Long = 0
        rows.forEachIndexed { index, row ->
            totalSum += row.finalizedIncome
            builder.append("${index + 1},")
            builder.append("\"${row.driverName}\",")
            builder.append("${row.driverCode},")
            builder.append("${row.personnelCode},")
            builder.append("${row.workingDaysCount},")
            builder.append("${row.totalTripsCount},")
            builder.append("${row.finalizedIncome},")
            builder.append("\"${row.paymentStatus.faTitle}\"\n")
        }

        builder.append("\n,,,جمع کل مبالغ قابل پرداخت (تومان),,$totalSum,\n")
        return builder.toString()
    }
}

// ── Extension Mappings ─────────────────────────────────────────────────────

fun DriverEntity.toDomain() = Driver(
    id = id,
    userId = userId,
    fullName = fullName,
    driverCode = driverCode,
    personnelCode = personnelCode,
    phoneNumber = phoneNumber,
    nationalId = nationalId,
    carModel = carModel,
    carPlate = carPlate,
    joinDateJalali = joinDateJalali,
    isActive = isActive,
    description = description
)

fun Driver.toEntity() = DriverEntity(
    id = id,
    userId = userId,
    fullName = fullName,
    driverCode = driverCode,
    personnelCode = personnelCode,
    phoneNumber = phoneNumber,
    nationalId = nationalId,
    carModel = carModel,
    carPlate = carPlate,
    joinDateJalali = joinDateJalali,
    isActive = isActive,
    description = description
)

fun RouteEntity.toDomain() = RouteItem(
    id = id,
    routeCode = routeCode,
    originId = originId,
    originName = originName,
    destinationId = destinationId,
    destinationName = destinationName,
    currentPrice = currentPrice,
    currency = currency,
    isActive = isActive,
    description = description,
    updatedAtJalali = updatedAtJalali
)

fun TripEntity.toDomain() = Trip(
    id = id,
    tripCode = tripCode,
    dailyWorkId = dailyWorkId,
    driverId = driverId,
    driverName = driverName,
    routeId = routeId,
    originTitle = originTitle,
    destinationTitle = destinationTitle,
    routeCode = routeCode,
    appliedPrice = appliedPrice,
    tripJalaliDate = tripJalaliDate,
    startTime = startTime,
    endTime = endTime,
    description = description,
    isCancelled = isCancelled,
    createdAt = createdAt
)
