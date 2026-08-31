package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FleetDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.DailyWorkLogEntity
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.FinancialPeriodEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.RouteEntity
import com.example.data.local.entity.RoutePriceHistoryEntity
import com.example.data.local.entity.TripEntity
import com.example.util.PersianDateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DriverEntity::class,
        LocationEntity::class,
        RouteEntity::class,
        RoutePriceHistoryEntity::class,
        DailyWorkLogEntity::class,
        TripEntity::class,
        FinancialPeriodEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fleetDao(): FleetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arman_entekhab_fleet.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial holding data on database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val dao = fleetDao()

        // 1. Check and seed Drivers if empty
        val existingDrivers = dao.getAllDriversList()
        if (existingDrivers.isEmpty()) {
            val drivers = listOf(
                DriverEntity(
                    id = "drv-101",
                    userId = "usr-101",
                    fullName = "علی رضایی",
                    driverCode = "D-101",
                    personnelCode = "AE-84012",
                    phoneNumber = "09121112233",
                    nationalId = "0012345678",
                    carModel = "پژو پارس سفید",
                    carPlate = "۱۲ ج ۳۴۵ ایران ۲۲",
                    joinDateJalali = "1403/02/15",
                    isActive = true,
                    description = "راننده ویژه مدیرعامل و مأموریت‌های درون‌شهری تهران"
                ),
                DriverEntity(
                    id = "drv-102",
                    userId = "usr-102",
                    fullName = "رضا محمدی",
                    driverCode = "D-102",
                    personnelCode = "AE-84019",
                    phoneNumber = "09123334455",
                    nationalId = "0023456789",
                    carModel = "سمند سورن پلاس خاکستری",
                    carPlate = "۷۷ د ۸۹۱ ایران ۱۱",
                    joinDateJalali = "1403/05/10",
                    isActive = true,
                    description = "راننده ترانسفر فرودگاه و مأموریت‌های کرج"
                ),
                DriverEntity(
                    id = "drv-103",
                    userId = "usr-103",
                    fullName = "حسین کریمی",
                    driverCode = "D-103",
                    personnelCode = "AE-84033",
                    phoneNumber = "09125556677",
                    nationalId = "0034567890",
                    carModel = "تارا اتوماتیک مشکی",
                    carPlate = "۴۵ ط ۶۱۲ ایران ۳۳",
                    joinDateJalali = "1404/01/20",
                    isActive = true,
                    description = "راننده مأموریت‌های برون‌شهری (قم، اصفهان، کاشان)"
                ),
                DriverEntity(
                    id = "drv-104",
                    userId = "usr-104",
                    fullName = "محمد احمدی",
                    driverCode = "D-104",
                    personnelCode = "AE-84048",
                    phoneNumber = "09127778899",
                    nationalId = "0045678901",
                    carModel = "دنا پلاس توربو سفید",
                    carPlate = "۳۳ ب ۷۴۵ ایران ۴۴",
                    joinDateJalali = "1404/06/01",
                    isActive = true,
                    description = "راننده پشتیبانی لجستیک و انبارهای مرکزی"
                ),
                DriverEntity(
                    id = "drv-105",
                    userId = "usr-105",
                    fullName = "مهدی کاظمی",
                    driverCode = "D-105",
                    personnelCode = "AE-84065",
                    phoneNumber = "09129990011",
                    nationalId = "0056789012",
                    carModel = "پژو ۲۰۷i خاکستری",
                    carPlate = "۶۸ ص ۲۱۱ ایران ۵۵",
                    joinDateJalali = "1404/09/15",
                    isActive = false,
                    description = "در حال مرخصی تحصیلی"
                )
            )
            dao.insertDrivers(drivers)
        }

        // 2. Initial Locations & 150+ CSV Holding Routes & Tariffs
        CsvFleetData.syncWithDatabase(
            dao = dao,
            operatorName = "راه‌اندازی خودکار نرخ‌های اکسل هلدینگ انتخاب"
        )

        // 4. Initial Route Price History
        dao.insertPriceHistory(
            RoutePriceHistoryEntity(
                routeId = "rt-001",
                routeCode = "AR-001",
                oldPrice = 250000,
                newPrice = 280000,
                changedBy = "مدیر سیستم (ادمین)",
                effectiveDateJalali = "1405/05/01"
            )
        )
        dao.insertPriceHistory(
            RoutePriceHistoryEntity(
                routeId = "rt-003",
                routeCode = "AR-003",
                oldPrice = 700000,
                newPrice = 750000,
                changedBy = "مدیر سیستم (ادمین)",
                effectiveDateJalali = "1405/05/10"
            )
        )

        // 5. Seed Realistic Trips for Today and Recent Days
        val today = PersianDateHelper.getTodayJalali()
        val todayStr = today.formatStandard()
        val yesterdayStr = String.format("%04d/%02d/%02d", today.year, today.month, if (today.day > 1) today.day - 1 else 1)

        val seedDailyWorkId = "dw-${todayStr.replace("/", "")}-101"
        dao.insertDailyWork(
            DailyWorkLogEntity(
                id = seedDailyWorkId,
                driverId = "drv-101",
                jalaliDate = todayStr,
                totalTrips = 2,
                totalIncome = 560000,
                status = "DRAFT",
                notes = "سفرهای صبح و بعدازظهر دفتر مرکزی"
            )
        )

        dao.insertTrips(
            listOf(
                TripEntity(
                    id = "trp-001",
                    tripCode = "TRP-${todayStr.replace("/", "")}-01",
                    dailyWorkId = seedDailyWorkId,
                    driverId = "drv-101",
                    driverName = "علی رضایی",
                    routeId = "rt-001",
                    originTitle = "تهران (دفتر مرکزی ملاصدرا)",
                    destinationTitle = "فرودگاه بین‌المللی امام خمینی (ره)",
                    routeCode = "AR-001",
                    appliedPrice = 280000,
                    tripJalaliDate = todayStr,
                    startTime = "07:45",
                    endTime = "09:00",
                    description = "استقبال از هیئت مدیره شرکت همکار"
                ),
                TripEntity(
                    id = "trp-002",
                    tripCode = "TRP-${todayStr.replace("/", "")}-02",
                    dailyWorkId = seedDailyWorkId,
                    driverId = "drv-101",
                    driverName = "علی رضایی",
                    routeId = "rt-008",
                    originTitle = "فرودگاه بین‌المللی امام خمینی (ره)",
                    destinationTitle = "تهران (دفتر مرکزی ملاصدرا)",
                    routeCode = "AR-008",
                    appliedPrice = 280000,
                    tripJalaliDate = todayStr,
                    startTime = "10:30",
                    endTime = "11:45",
                    description = "بازگشت به دفتر مرکزی ملاصدرا"
                )
            )
        )

        // Seed finalized work for yesterday for Driver 101 & 102
        val yestDailyWorkId = "dw-${yesterdayStr.replace("/", "")}-101"
        dao.insertDailyWork(
            DailyWorkLogEntity(
                id = yestDailyWorkId,
                driverId = "drv-101",
                jalaliDate = yesterdayStr,
                totalTrips = 3,
                totalIncome = 1250000,
                status = "FINALIZED",
                finalizedAt = System.currentTimeMillis() - 86400000,
                notes = "کارکرد نهایی شده روز گذشته"
            )
        )

        // Financial Period
        val monthKey = today.getYearMonthKey()
        dao.insertFinancialPeriod(
            FinancialPeriodEntity(
                jalaliYearMonth = monthKey,
                status = "PENDING_APPROVAL",
                totalAmount = 47800000,
                notes = "دوره محاسبه کارکرد رانندگان ناوگان هلدینگ"
            )
        )

        // Initial Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                operatorName = "مهندس حسینی (مدیر سیستم)",
                operatorRole = "SUPER_ADMIN",
                action = "INITIAL_SYSTEM_SETUP",
                entityTitle = "تنظیمات اولیه ناوگان",
                details = "راه‌اندازی پایگاه داده مسیرها و نرخ‌های مصوب هلدینگ آرمان انتخاب",
                jalaliTimestamp = today.formatReadable() + " - ساعت ۰۸:۰۰"
            )
        )
    }
}
