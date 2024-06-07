package it.ipzs.cieidsdk.util

import java.util.*

internal object FiscalCodeUtil {

    fun extractBirthDateFromFiscalCode(fiscalCode: String): String? {
        if (fiscalCode.length != 16) {
            return null
        }

        val lastTwoDigitYear = fiscalCode.substring(6, 8).toInt()
        val fullYear = getFullYear(lastTwoDigitYear)

        val monthLetter = fiscalCode.substring(8, 9)
        val month = getMonthFromLetter(monthLetter)

        var day = fiscalCode.substring(9, 11).toInt()
        if (day > 31) {
            day -= 40
        }

        return "$fullYear-$month-$day"
    }

    private fun getMonthFromLetter(monthLetter: String): String {
        return when (monthLetter) {
            "A" -> "01"
            "B" -> "02"
            "C" -> "03"
            "D" -> "04"
            "E" -> "05"
            "H" -> "06"
            "L" -> "07"
            "M" -> "08"
            "P" -> "09"
            "R" -> "10"
            "S" -> "11"
            "T" -> "12"
            else -> "01"
        }
    }

    private fun getFullYear(year: Int): Int {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val century = (currentYear/100) * 100
        val lastTwoDigits = currentYear - century
        return if (year > lastTwoDigits) {
            (century - 100) + year
        } else {
            century + year
        }
    }
}