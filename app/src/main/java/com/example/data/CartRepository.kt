package com.example.data

import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
    val wishlistItems: Flow<List<WishlistItem>> = cartDao.getWishlistItems()

    suspend fun insertCartItem(item: CartItem) {
        cartDao.insertCartItem(item)
    }

    suspend fun deleteCartItem(id: String) {
        cartDao.deleteCartItem(id)
    }

    suspend fun updateQuantity(id: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(id)
        } else {
            cartDao.updateQuantity(id, quantity)
        }
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    suspend fun insertWishlistItem(id: String) {
        cartDao.insertWishlistItem(WishlistItem(id))
    }

    suspend fun deleteWishlistItem(id: String) {
        cartDao.deleteWishlistItem(id)
    }
}
