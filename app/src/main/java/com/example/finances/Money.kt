package com.example.finances

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object Money {
    // Внутри храним целое число копеек: 10,50 рубля = 1050 копеек.
    fun parse(text: String): Long? {
        val value = text.trim()
        if (!value.matches(Regex("[0-9]{1,9}([.,][0-9]{1,2})?"))) return null
        val kopecks = BigDecimal(value.replace(',', '.')).movePointRight(2).longValueExact()
        return kopecks.takeIf { it > 0 }
    }

    fun number(kopecks: Long): String = NumberFormat.getNumberInstance(Locale("ru", "RU")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(BigDecimal.valueOf(kopecks, 2))

    fun format(kopecks: Long) = number(kopecks) + " ₽"
}
