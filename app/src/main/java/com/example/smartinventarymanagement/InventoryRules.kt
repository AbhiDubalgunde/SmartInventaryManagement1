package com.example.smartinventarymanagement

enum class StockStatus { OUT_OF_STOCK, LOW_STOCK, IN_STOCK }

object InventoryRules {
    fun status(quantity: Int, threshold: Int): StockStatus = when {
        quantity <= 0 -> StockStatus.OUT_OF_STOCK
        quantity < threshold -> StockStatus.LOW_STOCK
        else -> StockStatus.IN_STOCK
    }

    fun validValues(name: String, quantity: Int?, price: Double?, threshold: Int?): Boolean =
        name.isNotBlank() && quantity != null && quantity >= 0 &&
            price != null && price.isFinite() && price >= 0 && (price * quantity).isFinite() &&
            threshold != null && threshold >= 0
}
