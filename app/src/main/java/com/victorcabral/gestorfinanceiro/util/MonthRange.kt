package com.victorcabral.gestorfinanceiro.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class MonthRange(
    val year: Int,
    val month: Int, // Calendar.MONTH style: 0 = January
    val startInclusive: Long,
    val endExclusive: Long,
) {
    fun labelPtBr(): String {
        val cal = Calendar.getInstance(TimeZone.getDefault(), Locale("pt", "BR"))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val formatter = java.text.SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
        return formatter.format(cal.time).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString()
        }
    }

    companion object {
        fun current(nowMs: Long = System.currentTimeMillis()): MonthRange {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = nowMs
            return of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }

        fun of(year: Int, month: Int): MonthRange {
            val start = Calendar.getInstance(TimeZone.getDefault()).apply {
                clear()
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val end = (start.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
            }
            return MonthRange(
                year = year,
                month = month,
                startInclusive = start.timeInMillis,
                endExclusive = end.timeInMillis,
            )
        }
    }
}
