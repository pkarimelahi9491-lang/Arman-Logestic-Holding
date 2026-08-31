package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drivers",
    indices = [
        Index(value = ["driverCode"], unique = true),
        Index(value = ["personnelCode"], unique = true)
    ]
)
data class DriverEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val driverCode: String,
    val personnelCode: String,
    val phoneNumber: String,
    val nationalId: String,
    val carModel: String,
    val carPlate: String,
    val joinDateJalali: String,
    val isActive: Boolean = true,
    val description: String = ""
)

@Entity(
    tableName = "locations",
    indices = [Index(value = ["name"], unique = true)]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val isActive: Boolean = true
)

@Entity(
    tableName = "routes",
    indices = [
        Index(value = ["routeCode"], unique = true),
        Index(value = ["originId", "destinationId"], unique = true)
    ]
)
data class RouteEntity(
    @PrimaryKey val id: String,
    val routeCode: String,
    val originId: Long,
    val originName: String,
    val destinationId: Long,
    val destinationName: String,
    val currentPrice: Long,
    val currency: String = "TOMAN",
    val isActive: Boolean = true,
    val description: String = "",
    val updatedAtJalali: String = ""
)

@Entity(
    tableName = "route_price_history",
    indices = [Index(value = ["routeId"])]
)
data class RoutePriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeCode: String,
    val oldPrice: Long,
    val newPrice: Long,
    val changedBy: String,
    val effectiveDateJalali: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_work_logs",
    indices = [
        Index(value = ["driverId", "jalaliDate"], unique = true),
        Index(value = ["jalaliDate"])
    ]
)
data class DailyWorkLogEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val jalaliDate: String,
    val totalTrips: Int = 0,
    val totalIncome: Long = 0,
    val status: String = "DRAFT", // "DRAFT", "PENDING_APPROVAL", "FINALIZED", "REJECTED"
    val finalizedAt: Long? = null,
    val approvedBy: String? = null,
    val rejectionReason: String? = null,
    val notes: String = ""
)

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["tripCode"], unique = true),
        Index(value = ["driverId", "tripJalaliDate"]),
        Index(value = ["dailyWorkId"])
    ]
)
data class TripEntity(
    @PrimaryKey val id: String,
    val tripCode: String,
    val dailyWorkId: String,
    val driverId: String,
    val driverName: String,
    val routeId: String,
    val originTitle: String,
    val destinationTitle: String,
    val routeCode: String,
    val appliedPrice: Long, // Immutable Snapshot
    val currency: String = "TOMAN",
    val tripJalaliDate: String,
    val startTime: String,
    val endTime: String? = null,
    val description: String = "",
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "financial_periods",
    indices = [Index(value = ["jalaliYearMonth"], unique = true)]
)
data class FinancialPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jalaliYearMonth: String, // e.g. "1405-05"
    val status: String = "PENDING_APPROVAL", // "CALCULATING", "PENDING_APPROVAL", "APPROVED", "SENT_TO_FINANCE", "PAID"
    val approvedBy: String? = null,
    val paidAtJalali: String? = null,
    val totalAmount: Long = 0,
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operatorName: String,
    val operatorRole: String,
    val action: String,
    val entityTitle: String,
    val details: String,
    val jalaliTimestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)
