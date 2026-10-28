package com.example.finances

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun acceptsBothDecimalSeparatorsAndOneKopeck() {
        assertEquals(1050L, Money.parse("10,50"))
        assertEquals(1050L, Money.parse("10.50"))
        assertEquals(1L, Money.parse("0,01"))
        assertEquals(1200L, Money.parse(" 12 "))
    }

    @Test fun rejectsNonpositiveAmountsAndUnsupportedPrecision() {
        for (input in listOf("", "0", "0.00", "-10", "1.005", "1,2.3", "abc", "1e3", "1 000")) {
            assertNull(input, Money.parse(input))
        }
    }

    @Test fun validatesMaximumAmount() {
        assertEquals(99999999999L, Money.parse("999999999,99"))
        assertNull(Money.parse("1000000000"))
    }

    @Test fun addsMoneyWithoutFloatingPointRounding() {
        val total = Money.parse("0,10")!! + Money.parse("0,20")!!
        assertEquals(30L, total)
        assertEquals("0,30 ₽", Money.format(total))
        assertEquals("-0,30 ₽", Money.format(-total))
    }
}
