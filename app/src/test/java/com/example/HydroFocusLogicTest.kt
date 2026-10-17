package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HydroFocusLogicTest {

    @Test
    fun testPuzzleUnlockCalculation_3x3Grid() {
        val dimension = 3
        val totalPieces = dimension * dimension // 9
        val dailyGoal = 2000

        // 0 ml -> 0 pieces
        var intake = 0
        var progress = intake.toFloat() / dailyGoal
        var unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(0, unlocked)

        // 500 ml (25%) -> 2 pieces
        intake = 500
        progress = intake.toFloat() / dailyGoal
        unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(2, unlocked)

        // 1000 ml (50%) -> 4 pieces
        intake = 1000
        progress = intake.toFloat() / dailyGoal
        unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(4, unlocked)

        // 2000 ml (100%) -> all 9 pieces
        intake = 2000
        progress = intake.toFloat() / dailyGoal
        unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(9, unlocked)

        // 2500 ml (>100%) -> all 9 pieces
        intake = 2500
        progress = intake.toFloat() / dailyGoal
        unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(9, unlocked)
    }

    @Test
    fun testPuzzleUnlockCalculation_4x4Grid() {
        val dimension = 4
        val totalPieces = dimension * dimension // 16
        val dailyGoal = 2000

        val intake = 1000 // 50%
        val progress = intake.toFloat() / dailyGoal
        val unlocked = if (progress >= 1.0f) totalPieces else (progress * totalPieces).toInt()
        assertEquals(8, unlocked)
    }
}
