package com.example

import com.example.core.utilities.FinancialUtils
import com.example.data.model.Currencies
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testParseToMinorUnits() {
        val currency = Currencies.getByCode("USD")
        val minorUnits1 = FinancialUtils.parseToMinorUnits("1250.50", currency.decimalDigits)
        assertEquals(125050L, minorUnits1)

        val minorUnits2 = FinancialUtils.parseToMinorUnits("500", currency.decimalDigits)
        assertEquals(50000L, minorUnits2)

        val minorUnits3 = FinancialUtils.parseToMinorUnits("0.05", currency.decimalDigits)
        assertEquals(5L, minorUnits3)
    }

    @Test
    fun testCurrencyFormatting() {
        val pkr = Currencies.getByCode("PKR")
        val formattedPkr = FinancialUtils.formatCurrency(150000L, pkr) // 1500.00
        assertEquals("Rs 1,500.00", formattedPkr)

        val usd = Currencies.getByCode("USD")
        val formattedUsd = FinancialUtils.formatCurrency(2450L, usd) // 24.50
        assertEquals("$24.50", formattedUsd)

        val negativeUsd = FinancialUtils.formatCurrency(-5000L, usd) // -50.00
        assertEquals("-$50.00", negativeUsd)
    }

    @Test
    fun testMinorUnitsToInputString() {
        val inputStr = FinancialUtils.minorUnitsToInputString(125050L, 2)
        assertEquals("1250.5", inputStr)
    }
}
