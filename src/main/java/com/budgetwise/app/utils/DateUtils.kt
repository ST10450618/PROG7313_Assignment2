package com.budgetwise.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Date and time utility functions used throughout BudgetWise.
 *
 * CRITICAL: All Calendar operations use Calendar.getInstance() which respects the
 * device's local timezone. Never use UTC-based operations (e.g. System.currentTimeMillis()
 * modulo 86400000) for date boundaries — the BETWEEN SQL filter will be wrong for
 * users in non-UTC timezones.
 *
 * Convention:
 *   - "epoch ms" = Long value from System.currentTimeMillis() — milliseconds since Unix epoch
 *   - "startOfDay" = local midnight (00:00:00.000) on a calendar day
 *   - "endOfDay"   = 23:59:59.999 on a calendar day
 */
object DateUtils {

    // -------------------------------------------------------------------------
    // Day boundary helpers (used for BETWEEN SQL queries)
    // -------------------------------------------------------------------------

    /**
     * Returns the epoch ms at the start of the calendar day containing [epochMs].
     * Sets the time to 00:00:00.000 in the device's local timezone.
     *
     * Used when storing an expense's date: always store startOfDay(pickedMs) so the
     * BETWEEN filter works correctly regardless of when the expense was entered.
     */
    fun startOfDay(epochMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE,      0)
        cal.set(Calendar.SECOND,      0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Returns the epoch ms at the end of the calendar day containing [epochMs].
     * Sets the time to 23:59:59.999 in the device's local timezone.
     *
     * Used as the endDate parameter in DAO queries so expenses on the last picked day
     * are included in the result set.
     */
    fun endOfDay(epochMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE,      59)
        cal.set(Calendar.SECOND,      59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    // -------------------------------------------------------------------------
    // Month boundary helpers (default filter = current month)
    // -------------------------------------------------------------------------

    /**
     * Returns the epoch ms at the start of the first day of the given month.
     * Uses getActualMaximum to correctly handle Feb (28/29 days), 30-day months, etc.
     *
     * @param month 1-based month number (1 = January, 12 = December).
     * @param year  4-digit year (e.g. 2026).
     */
    fun startOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR,         year)
        cal.set(Calendar.MONTH,        month - 1)  // Calendar months are 0-based
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return startOfDay(cal.timeInMillis)
    }

    /**
     * Returns the epoch ms at the end of the last day of the given month.
     * getActualMaximum(DAY_OF_MONTH) gives the correct last day for the month
     * (28, 29, 30, or 31 depending on month and leap year).
     *
     * @param month 1-based month number.
     * @param year  4-digit year.
     */
    fun endOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR,  year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return endOfDay(cal.timeInMillis)
    }

    // -------------------------------------------------------------------------
    // Display formatting
    // -------------------------------------------------------------------------

    /**
     * Format an epoch ms value as "dd/MM/yyyy" (e.g. "01/04/2026").
     * Uses the device's default locale for number formatting.
     */
    fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(epochMs)
    }

    /**
     * Format a month/year pair as "MMMM yyyy" (e.g. "April 2026").
     * Month is 1-based (1 = January).
     */
    fun formatMonthYear(month: Int, year: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.YEAR,  year)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }

    // -------------------------------------------------------------------------
    // Current date helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the current calendar month as a 1-based integer (1–12).
     * January = 1, December = 12.
     */
    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    /** Returns the current 4-digit calendar year (e.g. 2026). */
    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    /** Returns the current epoch ms from the system clock. */
    fun todayMs(): Long = System.currentTimeMillis()

    /** Returns the epoch ms at the start of today (local midnight). */
    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    /** Returns the epoch ms at the end of today (23:59:59.999 local time). */
    fun endOfToday(): Long = endOfDay(System.currentTimeMillis())
}
