package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.DailyWorkLogEntity
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.FinancialPeriodEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.RouteEntity
import com.example.data.local.entity.RoutePriceHistoryEntity
import com.example.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {

    // Drivers
    @Query("SELECT * FROM drivers ORDER BY fullName ASC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers ORDER BY fullName ASC")
    suspend fun getAllDriversList(): List<DriverEntity>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverById(id: String): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    // Locations
    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    // Routes
    @Query("SELECT * FROM routes ORDER BY routeCode ASC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE originId = :originId AND destinationId = :destinationId AND isActive = 1 LIMIT 1")
    suspend fun findRouteByEndpoints(originId: Long, destinationId: Long): RouteEntity?

    @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
    suspend fun getRouteById(id: String): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Update
    suspend fun updateRoute(route: RouteEntity)

    // Price History
    @Query("SELECT * FROM route_price_history WHERE routeId = :routeId ORDER BY timestamp DESC")
    fun getPriceHistoryForRoute(routeId: String): Flow<List<RoutePriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: RoutePriceHistoryEntity)

    // Daily Work Logs
    @Query("SELECT * FROM daily_work_logs WHERE id = :id LIMIT 1")
    suspend fun getDailyWorkById(id: String): DailyWorkLogEntity?

    @Query("SELECT * FROM daily_work_logs WHERE driverId = :driverId AND jalaliDate = :jalaliDate LIMIT 1")
    suspend fun getDailyWork(driverId: String, jalaliDate: String): DailyWorkLogEntity?

    @Query("SELECT * FROM daily_work_logs WHERE driverId = :driverId AND jalaliDate = :jalaliDate LIMIT 1")
    fun observeDailyWork(driverId: String, jalaliDate: String): Flow<DailyWorkLogEntity?>

    @Query("SELECT * FROM daily_work_logs ORDER BY jalaliDate DESC, id DESC")
    fun getAllDailyWorkLogs(): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE status = :status ORDER BY jalaliDate DESC")
    fun getDailyWorkLogsByStatus(status: String): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE driverId = :driverId ORDER BY jalaliDate DESC")
    fun getDailyWorkLogsForDriver(driverId: String): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE jalaliDate = :jalaliDate")
    fun getDailyWorkLogsByDate(jalaliDate: String): Flow<List<DailyWorkLogEntity>>

    @Query("SELECT * FROM daily_work_logs WHERE jalaliDate LIKE :yearMonthPrefix || '%'")
    fun getDailyWorkLogsForMonth(yearMonthPrefix: String): Flow<List<DailyWorkLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWork(dailyWork: DailyWorkLogEntity)

    @Update
    suspend fun updateDailyWork(dailyWork: DailyWorkLogEntity)

    // Trips
    @Query("SELECT * FROM trips WHERE driverId = :driverId AND tripJalaliDate = :jalaliDate ORDER BY createdAt ASC")
    fun getTripsForDriverAndDate(driverId: String, jalaliDate: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE tripJalaliDate = :jalaliDate ORDER BY createdAt DESC")
    fun getAllTripsForDate(jalaliDate: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE tripJalaliDate LIKE :yearMonthPrefix || '%' ORDER BY createdAt DESC")
    fun getTripsForMonth(yearMonthPrefix: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getAllTripsForDriver(driverId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY createdAt DESC LIMIT 100")
    fun getRecentTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)

    // Financial Periods
    @Query("SELECT * FROM financial_periods WHERE jalaliYearMonth = :yearMonth LIMIT 1")
    suspend fun getFinancialPeriod(yearMonth: String): FinancialPeriodEntity?

    @Query("SELECT * FROM financial_periods ORDER BY jalaliYearMonth DESC")
    fun getAllFinancialPeriods(): Flow<List<FinancialPeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialPeriod(period: FinancialPeriodEntity)

    @Update
    suspend fun updateFinancialPeriod(period: FinancialPeriodEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
