package com.example.smartinventarymanagement

import org.junit.Assert.*
import org.junit.Test

class InventoryRulesTest {
    @Test fun stockBoundariesRespectCustomThreshold() {
        assertEquals(StockStatus.OUT_OF_STOCK, InventoryRules.status(0, 5))
        assertEquals(StockStatus.LOW_STOCK, InventoryRules.status(4, 5))
        assertEquals(StockStatus.IN_STOCK, InventoryRules.status(5, 5))
        assertEquals(StockStatus.LOW_STOCK, InventoryRules.status(12, 20))
        assertEquals(StockStatus.IN_STOCK, InventoryRules.status(1, 0))
    }
    @Test fun rejectsInvalidInventoryValues() {
        assertTrue(InventoryRules.validValues("Rice", 0, 0.0, 5))
        assertFalse(InventoryRules.validValues(" ", 1, 2.0, 5))
        assertFalse(InventoryRules.validValues("Rice", -1, 2.0, 5))
        assertFalse(InventoryRules.validValues("Rice", 1, Double.NaN, 5))
        assertFalse(InventoryRules.validValues("Rice", 2, Double.MAX_VALUE, 5))
        assertFalse(InventoryRules.validValues("Rice", 1, 2.0, -1))
        assertFalse(InventoryRules.validValues("Rice", null, 2.0, 5))
    }
}
