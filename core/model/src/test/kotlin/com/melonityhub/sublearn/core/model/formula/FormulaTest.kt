package com.melonityhub.sublearn.core.model.formula

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FormulaTest {
    private val vars = mapOf("D" to 2_000.0, "M" to 1.5, "N" to 3.0)

    @Test
    fun evaluatesPrecedenceAndParentheses() {
        assertEquals(9.0, Formula.evaluate("(1 + 2) * 3", emptyMap()), 1e-9)
        assertEquals(7.0, Formula.evaluate("1 + 2 * 3", emptyMap()), 1e-9)
        assertEquals(3.0, Formula.evaluate("-2 + 5", emptyMap()), 1e-9)
        assertEquals(1.0, Formula.evaluate("7 % 3", emptyMap()), 1e-9)
    }

    @Test
    fun usesVariablesAndFunctions() {
        assertEquals(3_000.0, Formula.evaluate("M * D", vars), 1e-9)
        assertEquals(6_000.0, Formula.evaluate("D * N", vars), 1e-9)
        assertEquals(2_000.0, Formula.evaluate("min(3000, D)", vars), 1e-9)
        assertEquals(3.0, Formula.evaluate("clamp(5, 0, 3)", emptyMap()), 1e-9)
        assertEquals(5.0, Formula.evaluate("max(1, 5, 2)", emptyMap()), 1e-9)
    }

    @Test
    fun rejectsBadInput() {
        assertThrows(FormulaException::class.java) { Formula.evaluate("", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("D +", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("X * 2", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("1 / 0", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("system(1)", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("clamp(1, 5, 2)", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("1e308 * 10", vars) }
        assertThrows(FormulaException::class.java) { Formula.evaluate("1 2", vars) }
    }
}
