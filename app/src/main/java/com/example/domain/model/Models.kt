package com.example.domain.model

enum class UserRole(val faTitle: String) {
    DRIVER("راننده هلدینگ"),
    ADMIN("مسئول کارکرد / مدیر ناوگان"),
    FINANCE("مدیریت مالی و حسابداری")
}

enum class DailyStatus(val faTitle: String) {
    DRAFT("در حال ثبت"),
    PENDING_APPROVAL("در انتظار تأیید مدیریت"),
    FINALIZED("تأیید شده و نهایی"),
    REJECTED("رد شده / نیازمند اصلاح")
}

enum class PaymentStatus(val faTitle: String) {
    CALCULATING("محاسبه نشده"),
    PENDING_APPROVAL("در انتظار تأیید"),
    APPROVED("تأیید شده"),
    SENT_TO_FINANCE("ارسال به مالی"),
    PAID("پرداخت شده")
}

data class Driver(
    val id: String,
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

data class LocationItem(
    val id: Long,
    val name: String,
    val city: String,
    val isActive: Boolean = true
)

data class RouteItem(
    val id: String,
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

data class Trip(
    val id: String,
    val tripCode: String,
    val dailyWorkId: String,
    val driverId: String,
    val driverName: String,
    val routeId: String,
    val originTitle: String,
    val destinationTitle: String,
    val routeCode: String,
    val appliedPrice: Long, // Snapshot of the price at the time of trip registration!
    val tripJalaliDate: String,
    val startTime: String,
    val endTime: String? = null,
    val description: String = "",
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyWorkSummary(
    val id: String,
    val driverId: String,
    val driverName: String,
    val driverCode: String,
    val jalaliDate: String,
    val totalTrips: Int,
    val totalIncome: Long,
    val status: DailyStatus,
    val finalizedAt: Long? = null,
    val approvedBy: String? = null,
    val rejectionReason: String? = null,
    val notes: String = ""
)

data class PendingDailyApproval(
    val dailyWork: DailyWorkSummary,
    val driver: Driver?,
    val trips: List<Trip>
)

data class MonthlySettlementRow(
    val driverId: String,
    val driverName: String,
    val driverCode: String,
    val personnelCode: String,
    val workingDaysCount: Int,
    val totalTripsCount: Int,
    val finalizedIncome: Long,
    val draftIncome: Long,
    val unfinalizedDaysCount: Int,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING_APPROVAL
)

data class AuditLog(
    val id: Long,
    val operatorName: String,
    val operatorRole: String,
    val action: String,
    val entityTitle: String,
    val details: String,
    val jalaliTimestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)
