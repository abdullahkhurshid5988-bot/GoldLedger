package com.example.core.utilities

import com.example.data.model.SupportedCurrency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FinancialUtils {

    /**
     * Converts a user entered string (e.g. "1250.50" or "500") to integer minor units (Long)
     * using exact BigDecimal arithmetic.
     */
    fun parseToMinorUnits(input: String, decimalDigits: Int = 2): Long {
        if (input.isBlank()) return 0L
        val clean = input.replace(",", "").trim()
        return try {
            val bd = BigDecimal(clean)
            val multiplier = BigDecimal.TEN.pow(decimalDigits)
            bd.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Converts minor units (Long) back to decimal string for text input editing (e.g. 125050L -> "1250.50")
     */
    fun minorUnitsToInputString(amountMinorUnits: Long, decimalDigits: Int = 2): String {
        if (amountMinorUnits == 0L) return ""
        val bd = BigDecimal(amountMinorUnits).divide(BigDecimal.TEN.pow(decimalDigits), decimalDigits, RoundingMode.HALF_UP)
        return bd.stripTrailingZeros().toPlainString()
    }

    /**
     * Formats minor units for display, e.g. "$1,250.50" or "Rs 1,250.50"
     */
    fun formatCurrency(
        amountMinorUnits: Long,
        currency: SupportedCurrency,
        includeSymbol: Boolean = true,
        includeSign: Boolean = false
    ): String {
        val isNegative = amountMinorUnits < 0
        val absUnits = kotlin.math.abs(amountMinorUnits)
        val divisor = BigDecimal.TEN.pow(currency.decimalDigits)
        val value = BigDecimal(absUnits).divide(divisor, currency.decimalDigits, RoundingMode.HALF_UP)

        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = currency.decimalDigits
            maximumFractionDigits = currency.decimalDigits
        }
        val formattedNumber = formatter.format(value)

        val prefix = if (isNegative) "-" else if (includeSign && amountMinorUnits > 0) "+" else ""
        return if (includeSymbol) {
            if (currency.symbol.length >= 2) {
                "$prefix${currency.symbol} $formattedNumber"
            } else {
                "$prefix${currency.symbol}$formattedNumber"
            }
        } else {
            "$prefix$formattedNumber"
        }
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    fun getStartOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getEndOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    fun getStartOfYear(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    fun formatTime(timestamp: Long, pattern: String = "hh:mm a"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}
