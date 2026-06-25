package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String, // Matches product ID
    val title: String,
    val price: Double,
    val imageResName: String, // Local resource name or category gradient key
    val category: String,
    val quantity: Int,
    val originalPrice: Double = 0.0,
    val rating: Float = 0.0f
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val id: String
)
