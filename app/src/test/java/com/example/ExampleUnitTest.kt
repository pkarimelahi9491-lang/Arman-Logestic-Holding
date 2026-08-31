package com.example

import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPersianDigitsConversion() {
        val latin = "1405/05/20"
        val expected = "۱۴۰۵/۰۵/۲۰"
        assertEquals(expected, latin.toPersianDigits())
    }

    @Test
    fun testTomanFormatting() {
        val amount = 350000L
        val formatted = MoneyUtils.formatToman(amount)
        assertEquals("۳۵۰,۰۰۰ تومان", formatted)
    }

    @Test
    fun testJalaliDateGeneration() {
        val today = PersianDateHelper.getTodayJalali()
        assertTrue(today.year >= 1400)
        assertTrue(today.month in 1..12)
        assertTrue(today.day in 1..31)
    }
}
