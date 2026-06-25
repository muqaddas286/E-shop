package com.example.data

data class Product(
    val id: String,
    val title: String,
    val price: Double,
    val originalPrice: Double = 0.0,
    val category: String,
    val rating: Float,
    val reviewsCount: Int = 0,
    val description: String,
    val gradientIndex: Int,
    val isFlashSale: Boolean = false
) {
    val discountPercent: Int
        get() = if (originalPrice > price) {
            (((originalPrice - price) / originalPrice) * 100).toInt()
        } else {
            0
        }
}
