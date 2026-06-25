package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CartItem
import com.example.data.CartRepository
import com.example.data.Product
import com.example.data.ProductDataProvider
import com.example.data.WishlistItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TabItem {
    Home, Shop, Categories, About, Wishlist
}

enum class SortOption {
    None, PriceAsc, PriceDesc, RatingDesc
}

data class UserProfile(
    val username: String,
    val email: String,
    val ordersCount: Int = 0,
    val isLoggedIn: Boolean = false
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CartRepository(database.cartDao())

    // Cart and Wishlist reactively observed from database
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI States
    private val _selectedTab = MutableStateFlow(TabItem.Home)
    val selectedTab: StateFlow<TabItem> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _priceRange = MutableStateFlow(0f..400f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.None)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _flashSaleSeconds = MutableStateFlow(14255L) // 3h 57m 35s
    val flashSaleSeconds: StateFlow<Long> = _flashSaleSeconds.asStateFlow()

    private val _currentTestimonialIndex = MutableStateFlow(0)
    val currentTestimonialIndex: StateFlow<Int> = _currentTestimonialIndex.asStateFlow()

    private val _newsletterEmail = MutableStateFlow("")
    val newsletterEmail: StateFlow<String> = _newsletterEmail.asStateFlow()

    private val _newsletterStatus = MutableStateFlow<String?>(null)
    val newsletterStatus: StateFlow<String?> = _newsletterStatus.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile("", "", 0, false))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var timerJob: Job? = null
    private var testimonialJob: Job? = null

    init {
        startCountdownTimer()
        startTestimonialCarousel()
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_flashSaleSeconds.value > 0) {
                    _flashSaleSeconds.value -= 1
                } else {
                    _flashSaleSeconds.value = 14400 // Reset to 4 hours if done
                }
            }
        }
    }

    private fun startTestimonialCarousel() {
        testimonialJob?.cancel()
        testimonialJob = viewModelScope.launch {
            while (true) {
                delay(6000) // Swap testimonial card every 6s
                val nextIndex = (_currentTestimonialIndex.value + 1) % 4
                _currentTestimonialIndex.value = nextIndex
            }
        }
    }

    // Tab Navigation
    fun setTab(tab: TabItem) {
        _selectedTab.value = tab
    }

    // Theme toggle
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Search and Filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun setSortOption(option: SortOption) {
        _sortBy.value = option
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _priceRange.value = 0f..400f
        _sortBy.value = SortOption.None
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.id == product.id }
            if (existing != null) {
                repository.updateQuantity(product.id, existing.quantity + quantity)
            } else {
                repository.insertCartItem(
                    CartItem(
                        id = product.id,
                        title = product.title,
                        price = product.price,
                        imageResName = "gradient_${product.gradientIndex}",
                        category = product.category,
                        quantity = quantity,
                        originalPrice = product.originalPrice,
                        rating = product.rating
                    )
                )
            }
            showToast("Added '${product.title}' to Cart")
        }
    }

    fun updateCartQuantity(id: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateQuantity(id, quantity)
        }
    }

    fun removeFromCart(id: String) {
        viewModelScope.launch {
            val item = cartItems.value.find { it.id == id }
            repository.deleteCartItem(id)
            item?.let { showToast("Removed '${it.title}' from Cart") }
        }
    }

    fun checkoutCart() {
        viewModelScope.launch {
            if (cartItems.value.isNotEmpty()) {
                repository.clearCart()
                showToast("Order placed successfully! Thank you for shopping with us.")
                if (_userProfile.value.isLoggedIn) {
                    _userProfile.value = _userProfile.value.copy(
                        ordersCount = _userProfile.value.ordersCount + 1
                    )
                }
            }
        }
    }

    // Wishlist Operations
    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            val isWishlisted = wishlistItems.value.any { it.id == productId }
            val product = ProductDataProvider.products.find { it.id == productId }
            if (isWishlisted) {
                repository.deleteWishlistItem(productId)
                product?.let { showToast("Removed '${it.title}' from Wishlist") }
            } else {
                repository.insertWishlistItem(productId)
                product?.let { showToast("Added '${it.title}' to Wishlist") }
            }
        }
    }

    // Newsletter Operation
    fun updateNewsletterEmail(email: String) {
        _newsletterEmail.value = email
        _newsletterStatus.value = null
    }

    fun subscribeNewsletter() {
        val email = _newsletterEmail.value.trim()
        if (email.isEmpty()) {
            _newsletterStatus.value = "Please enter an email address."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _newsletterStatus.value = "Please enter a valid email address."
            return
        }
        _newsletterStatus.value = "Success! Thank you for subscribing."
        showToast("Subscribed with: $email")
        _newsletterEmail.value = ""
    }

    // User Authentication
    fun login(username: String, email: String) {
        if (username.trim().isEmpty() || email.trim().isEmpty()) {
            showToast("Please enter a username and email.")
            return
        }
        _userProfile.value = UserProfile(
            username = username.trim(),
            email = email.trim(),
            ordersCount = 0,
            isLoggedIn = true
        )
        showToast("Welcome back, ${username.trim()}!")
    }

    fun logout() {
        _userProfile.value = UserProfile("", "", 0, false)
        showToast("Logged out successfully.")
    }

    // Product Popup Details
    fun showProductDetails(product: Product?) {
        _selectedProduct.value = product
    }

    // Toast message handling
    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        testimonialJob?.cancel()
    }
}
