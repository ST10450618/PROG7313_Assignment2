package com.budgetwise.app.utils

import java.text.SimpleDateFormat
import java.util.*

/** Centralised date/time formatting — ensures consistency across all screens. */
object DateUtils {
    private val dateFmt      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFmt = SimpleDateFormat("MMMM yyyy",  Locale.getDefault())

    fun formatDate(epochMs: Long): String = dateFmt.format(Date(epochMs))

    fun formatMonthYear(month: Int, year: Int): String {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        return monthYearFmt.format(cal.time)
    }

    fun startOfDay(epochMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMs
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfDay(epochMs: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMs
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59);       set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    fun startOfMonth(month: Int, year: Int): Long = Calendar.getInstance().apply {
        set(year, month - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfMonth(month: Int, year: Int): Long = Calendar.getInstance().apply {
        set(year, month - 1, 1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59);       set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun currentYear() : Int = Calendar.getInstance().get(Calendar.YEAR)
    fun todayMs()     : Long = System.currentTimeMillis()
}