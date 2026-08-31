package com.example.util

import java.util.Calendar
import java.util.Locale

/**
 * Robust mathematical converter for Persian (Jalali) Calendar.
 * Supports bidirectional conversions, day of week names, month names, and formatted strings.
 */
object PersianDateHelper {

    val PERSIAN_MONTHS = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val WEEK_DAYS_FA = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    data class JalaliDate(
        val year: Int,
        val month: Int, // 1 to 12
        val day: Int    // 1 to 31
    ) {
        fun formatStandard(): String {
            return String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
        }

        fun formatReadable(): String {
            val monthName = if (month in 1..12) PERSIAN_MONTHS[month - 1] else ""
            return "${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"
        }

        fun formatWithDayName(): String {
            val dayName = getDayOfWeekName(this)
            val monthName = if (month in 1..12) PERSIAN_MONTHS[month - 1] else ""
            return "$dayName ${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"
        }

        fun getYearMonthKey(): String {
            return String.format(Locale.US, "%04d-%02d", year, month)
        }
    }

    /**
     * Converts current Gregorian system time to JalaliDate.
     */
    fun getTodayJalali(): JalaliDate {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getCurrentTimeString(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    fun parseJalali(dateStr: String): JalaliDate {
        return try {
            val parts = dateStr.replace("-", "/").split("/")
            JalaliDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (e: Exception) {
            getTodayJalali()
        }
    }

    /**
     * Converts Gregorian Year, Month, Day to Jalali
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)

        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1

        return JalaliDate(jy, jm + 1, jd)
    }

    fun getDayOfWeekName(jalaliDate: JalaliDate): String {
        // Approximate calculation for day of week based on reference date
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val cal = Calendar.getInstance()
        cal.set(gy, gm - 1, gd)
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> "شنبه"
        }
    }

    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        var jy = jYear - 979
        var jm = jMonth - 1
        var jd = jDay - 1

        var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
        for (i in 0 until jm) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        val gd = gDayNo + 1

        return Triple(gy, gm + 1, gd)
    }
}

/**
 * Extension function to convert Latin digits to Persian digits
 */
fun Int.toPersianDigits(): String {
    return this.toString().toPersianDigits()
}

fun Long.toPersianDigits(): String {
    return this.toString().toPersianDigits()
}

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder()
    for (char in this) {
        if (char in '0'..'9') {
            builder.append(persianDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

object MoneyUtils {
    /**
     * Formats an amount with 3-digit comma separation and Persian digits.
     * e.g. 1250000 -> "۱,۲۵۰,۰۰۰ تومان"
     */
    fun formatToman(amount: Long): String {
        val formattedNumber = String.format(Locale.US, "%,d", amount)
        return "${formattedNumber.toPersianDigits()} تومان"
    }

    fun formatTomanCompact(amount: Long): String {
        return if (amount >= 1_000_000) {
            val millions = amount / 1_000_000.0
            val formatted = String.format(Locale.US, "%.1f", millions).removeSuffix(".0")
            "${formatted.toPersianDigits()} میلیون تومان"
        } else if (amount >= 1_000) {
            val thousands = amount / 1_000.0
            val formatted = String.format(Locale.US, "%.0f", thousands)
            "${formatted.toPersianDigits()} هزار تومان"
        } else {
            formatToman(amount)
        }
    }
}
