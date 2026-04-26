package com.example.xinggui.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthDimensionTest {
    @Test
    fun dimensionsStayAtNineAndUseUniqueIds() {
        val entries = GrowthDimension.entries
        assertEquals(9, entries.size)
        assertEquals(entries.size, entries.map { it.id }.toSet().size)
        assertTrue(entries.all { it.displayName.isNotBlank() })
    }
}
