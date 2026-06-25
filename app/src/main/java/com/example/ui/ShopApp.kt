package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.ui.theme.CategoryGradients
import com.example.ui.theme.DiscountRose
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PremiumGold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopApp(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val priceRange by viewModel.priceRange.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val flashSaleSeconds by viewModel.flashSaleSeconds.collectAsStateWithLifecycle()
    val currentTestimonialIndex by viewModel.currentTestimonialIndex.collectAsStateWithLifecycle()
    val newsletterEmail by viewModel.newsletterEmail.collectAsStateWithLifecycle()
    val newsletterStatus by viewModel.newsletterStatus.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    var isCartOpen by remember { mutableStateOf(false) }
    var isLoginOpen by remember { mutableStateOf(false) }

    // Display temporary alert toasts
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StickyHeaderBar(
                cartCount = cartItems.sumOf { it.quantity },
                wishlistCount = wishlistItems.size,
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setTab(it) },
                onCartClick = { isCartOpen = true },
                onProfileClick = { isLoginOpen = true },
                isDarkMode = viewModel.isDarkMode.collectAsStateWithLifecycle().value,
                onThemeToggle = { viewModel.toggleTheme() },
                userProfile = userProfile
            )
        },
        bottomBar = {
            // Elegant modern bottom navigation as backup for mobile ergonomics
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                listOf(
                    TabItem.Home to Icons.Default.Home,
                    TabItem.Shop to Icons.Default.ShoppingBag,
                    TabItem.Categories to Icons.Default.Category,
                    TabItem.About to Icons.Default.Info,
                    TabItem.Wishlist to Icons.Default.Favorite
                ).forEach { (tab, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { viewModel.setTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.name) },
                        label = { Text(tab.name, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "MainScreenTransition"
            ) { tab ->
                when (tab) {
                    TabItem.Home -> HomeScreen(
                        viewModel = viewModel,
                        flashSaleSeconds = flashSaleSeconds,
                        currentTestimonialIndex = currentTestimonialIndex,
                        newsletterEmail = newsletterEmail,
                        newsletterStatus = newsletterStatus,
                        onShopNowClick = { viewModel.setTab(TabItem.Shop) },
                        onExploreDealsClick = {
                            viewModel.selectCategory(null)
                            viewModel.setTab(TabItem.Shop)
                            viewModel.showToast("Showing Hot Deals of the day!")
                        }
                    )
                    TabItem.Shop -> ShopScreen(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        priceRange = priceRange,
                        sortBy = sortBy,
                        wishlistIds = wishlistItems.map { it.id }.toSet()
                    )
                    TabItem.Categories -> CategoriesScreen(
                        onCategorySelect = { cat ->
                            viewModel.selectCategory(cat)
                            viewModel.setTab(TabItem.Shop)
                        }
                    )
                    TabItem.About -> AboutContactScreen()
                    TabItem.Wishlist -> WishlistScreen(
                        viewModel = viewModel,
                        wishlistItems = wishlistItems
                    )
                }
            }
        }
    }

    // Cart Sheet Overlay
    if (isCartOpen) {
        CartDrawerDialog(
            cartItems = cartItems,
            onClose = { isCartOpen = false },
            onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
            onRemove = { id -> viewModel.removeFromCart(id) },
            onCheckout = {
                viewModel.checkoutCart()
                isCartOpen = false
            }
        )
    }

    // Login/Profile Overlay Modal
    if (isLoginOpen) {
        ProfileLoginDialog(
            userProfile = userProfile,
            onClose = { isLoginOpen = false },
            onLogin = { name, email ->
                viewModel.login(name, email)
                isLoginOpen = false
            },
            onLogout = {
                viewModel.logout()
                isLoginOpen = false
            }
        )
    }

    // Product Detail Modal Dialog
    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            isWishlisted = wishlistItems.any { it.id == product.id },
            onClose = { viewModel.showProductDetails(null) },
            onToggleWishlist = { viewModel.toggleWishlist(product.id) },
            onAddToCart = { viewModel.addToCart(product) }
        )
    }
}

// -------------------------------------------------------------
// 1. Header Component (Sticky navigation with high fidelity)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickyHeaderBar(
    cartCount: Int,
    wishlistCount: Int,
    selectedTab: TabItem,
    onTabSelected: (TabItem) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    userProfile: UserProfile
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Professional Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTabSelected(TabItem.Home) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                append("E-")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = PremiumGold)) {
                                append("Shop")
                            }
                        },
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // Desktop-style top menu navigation (Hidden on smaller viewports / displayed elegantly)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Header Actions
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Wishlist Heart Icon
                    Box {
                        IconButton(
                            onClick = { onTabSelected(TabItem.Wishlist) },
                            modifier = Modifier.testTag("wishlist_header_icon")
                        ) {
                            Icon(
                                imageVector = if (wishlistCount > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (wishlistCount > 0) DiscountRose else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (wishlistCount > 0) {
                            BadgeCount(wishlistCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                    }

                    // Cart Shopping Bag
                    Box {
                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.testTag("cart_header_button")
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (cartCount > 0) {
                            BadgeCount(cartCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Profile / Login Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable { onProfileClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (userProfile.isLoggedIn) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userProfile.username,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 60.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCount(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = 2.dp, y = (-2).dp)
            .background(DiscountRose, shape = CircleShape)
            .size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -------------------------------------------------------------
// 2. Home Tab Screen (Rich banners, countdown, newsletter, etc)
// -------------------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    flashSaleSeconds: Long,
    currentTestimonialIndex: Int,
    newsletterEmail: String,
    newsletterStatus: String?,
    onShopNowClick: () -> Unit,
    onExploreDealsClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Banner Section
        HeroBannerSection(onShopNowClick, onExploreDealsClick)

        // Categories Grid-breaking Cards Section
        HomeCategoriesSection(onCategorySelect = { cat ->
            viewModel.selectCategory(cat)
            viewModel.setTab(TabItem.Shop)
        })

        // Deals & Flash Sale Countdown Section
        FlashSaleDealsSection(
            viewModel = viewModel,
            flashSaleSeconds = flashSaleSeconds
        )

        // Testimonials Slider Section
        TestimonialsSection(currentIndex = currentTestimonialIndex)

        // Newsletter Signup Card Section
        NewsletterSection(
            email = newsletterEmail,
            status = newsletterStatus,
            onEmailChange = { viewModel.updateNewsletterEmail(it) },
            onSubscribe = { viewModel.subscribeNewsletter() }
        )

        // Detailed Multi-Column Footer
        FooterSection()
    }
}

@Composable
fun HeroBannerSection(
    onShopNowClick: () -> Unit,
    onExploreDealsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Render generated high-quality banner from local assets folder
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner_1782375622742),
            contentDescription = "Hero Promotional Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Overlay for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Banner Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(PremiumGold, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SUMMER FLASH SALE",
                    fontSize = 11.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Upgrade Your Lifestyle\nPremium Gear & Fashion",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enjoy massive up to 45% discount, premium worldwide shipping and dynamic refund protection services.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.widthIn(max = 320.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onShopNowClick,
                    modifier = Modifier.testTag("shop_now_cta"),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Shop Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onExploreDealsClick,
                    modifier = Modifier.testTag("explore_deals_cta"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Explore Deals", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun HomeCategoriesSection(onCategorySelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Browse Premium Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(ProductDataProvider.categories) { category ->
                val icon = when (category) {
                    "Electronics" -> Icons.Default.DeveloperBoard
                    "Fashion" -> Icons.Default.Checkroom
                    "Shoes" -> Icons.Default.DirectionsRun
                    "Watches" -> Icons.Default.Watch
                    "Beauty" -> Icons.Default.AutoAwesome
                    else -> Icons.Default.HomeWork // Home & Living
                }

                Card(
                    onClick = { onCategorySelect(category) },
                    modifier = Modifier
                        .width(110.dp)
                        .height(120.dp)
                        .testTag("category_card_${category.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = category,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashSaleDealsSection(
    viewModel: ShopViewModel,
    flashSaleSeconds: Long
) {
    val flashSaleProducts = ProductDataProvider.products.filter { it.isFlashSale }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(DiscountRose, shape = CircleShape)
                        .size(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lightning Flash Deals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Beautiful Monospace Countdown Timer
            Box(
                modifier = Modifier
                    .background(Color.Black, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val hours = flashSaleSeconds / 3600
                val minutes = (flashSaleSeconds % 3600) / 60
                val seconds = flashSaleSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds),
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal List of special deals
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(flashSaleProducts) { product ->
                ProductCardMini(
                    product = product,
                    onProductClick = { viewModel.showProductDetails(product) },
                    onAddToCartClick = { viewModel.addToCart(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCardMini(
    product: Product,
    onProductClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onProductClick() }
            .testTag("deal_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(CategoryGradients[product.gradientIndex]))
                )
                // Product category overlay icon placeholder
                Icon(
                    imageVector = when (product.category) {
                        "Electronics" -> Icons.Default.DeveloperBoard
                        "Fashion" -> Icons.Default.Checkroom
                        "Shoes" -> Icons.Default.DirectionsRun
                        "Watches" -> Icons.Default.Watch
                        "Beauty" -> Icons.Default.AutoAwesome
                        else -> Icons.Default.HomeWork
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.Center)
                )

                // Discount Badge overlay
                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(DiscountRose, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = PremiumGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.rating.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${product.price}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.originalPrice > 0) {
                            Text(
                                text = "$${product.originalPrice}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
                            .clickable { onAddToCartClick() }
                            .padding(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestimonialsSection(currentIndex: Int) {
    val currentTestimonial = TestimonialProvider.testimonials[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "What Our Customers Say",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("testimonial_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Brush.linearGradient(CategoryGradients[currentTestimonial.avatarColorIndex]),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentTestimonial.author.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentTestimonial.author,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentTestimonial.role,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // 5-Star Rating layout
                    Row {
                        repeat(5) { starIndex ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIndex < currentTestimonial.rating.toInt()) PremiumGold else Color.LightGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = "Quote",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = currentTestimonial.comment,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                // Carousel Indicator dots
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (idx == currentIndex) 10.dp else 6.dp)
                                .background(
                                    color = if (idx == currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsletterSection(
    email: String,
    status: String?,
    onEmailChange: (String) -> Unit,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("newsletter_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = PremiumGold,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Subscribe to Our Newsletter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Get real-time flash deal alerts, special coupon vouchers, and new premium brand product releases directly in your inbox.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Text field and button row
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("newsletter_email_input"),
                placeholder = { Text("Enter your email address", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PremiumGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubscribe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("newsletter_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Subscribe Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            status?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = if (it.contains("Success")) MintGreen else Color.Yellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FooterSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "E-Shop Store",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "The ultimate native digital marketplace delivering elite-quality electronics, apparel, accessories, and home items at unmatched price tags.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            // Links Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("QUICK LINKS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Home", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Shop Catalog", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Categories", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Customer Support", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column {
                    Text("OFFICES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("San Francisco, CA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("New York, NY", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Singapore Hub", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            // Social Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    Icons.Default.Share to "Facebook",
                    Icons.Default.Send to "Telegram",
                    Icons.Default.Public to "Website"
                ).forEach { (icon, desc) ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = desc,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "© 2026 E-Shop premium Store. All rights reserved.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// 3. Shop Tab Screen (Full catalog, live filtering & sorting)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    searchQuery: String,
    selectedCategory: String?,
    priceRange: ClosedFloatingPointRange<Float>,
    sortBy: SortOption,
    wishlistIds: Set<String>
) {
    // Dynamically filter and sort products list in memory
    val filteredProducts = remember(searchQuery, selectedCategory, priceRange, sortBy) {
        ProductDataProvider.products.filter { prod ->
            val matchQuery = prod.title.contains(searchQuery, ignoreCase = true) ||
                    prod.description.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || prod.category == selectedCategory
            val matchPrice = prod.price >= priceRange.start && prod.price <= priceRange.endInclusive
            matchQuery && matchCategory && matchPrice
        }.let { list ->
            when (sortBy) {
                SortOption.None -> list
                SortOption.PriceAsc -> list.sortedBy { it.price }
                SortOption.PriceDesc -> list.sortedByDescending { it.price }
                SortOption.RatingDesc -> list.sortedByDescending { it.rating }
            }
        }
    }

    var showFilterSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Live Search Input Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input"),
                    placeholder = { Text("Search electronic, clothing, beauty, watches...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Filter Category Row
                    Text(
                        text = selectedCategory ?: "All Catalog Items",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("filter_options_toggle"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Horizontal scrolling category tags for instant filter access
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(bottom = 10.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All Products") }
                )
            }
            items(ProductDataProvider.categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category) },
                    modifier = Modifier.testTag("filter_tag_${category.lowercase()}")
                )
            }
        }

        // Active filter status summary
        if (selectedCategory != null || searchQuery.isNotEmpty() || priceRange.start > 0f || priceRange.endInclusive < 400f || sortBy != SortOption.None) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} results matched filters",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Text(
                    text = "Clear All Filters",
                    fontSize = 11.sp,
                    color = DiscountRose,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.clearAllFilters() }
                        .padding(4.dp)
                )
            }
        }

        // Main Product Grid list
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No products match your criteria",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Try resetting search filters or checking for typos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductGridCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onProductClick = { viewModel.showProductDetails(product) },
                        onWishlistClick = { viewModel.toggleWishlist(product.id) },
                        onAddToCartClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }
    }

    // Modal Sheet of detailed advanced filters
    if (showFilterSheet) {
        Dialog(onDismissRequest = { showFilterSheet = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Refine Catalog",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Price Range Slider
                    Text(
                        text = "Price Budget Range: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RangeSlider(
                        value = priceRange,
                        onValueChange = { viewModel.setPriceRange(it) },
                        valueRange = 0f..400f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sorting Options
                    Text("Sort Results By:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            SortOption.None to "Recommended",
                            SortOption.PriceAsc to "Price: Low to High",
                            SortOption.PriceDesc to "Price: High to Low",
                            SortOption.RatingDesc to "Top Customer Rated"
                        ).forEach { (option, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (sortBy == option) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setSortOption(option) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sortBy == option,
                                    onClick = { viewModel.setSortOption(option) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Text("Reset All", color = DiscountRose)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = { showFilterSheet = false }) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Background Gradient visual block
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(CategoryGradients[product.gradientIndex]))
                )

                // Category overlay icon
                Icon(
                    imageVector = when (product.category) {
                        "Electronics" -> Icons.Default.DeveloperBoard
                        "Fashion" -> Icons.Default.Checkroom
                        "Shoes" -> Icons.Default.DirectionsRun
                        "Watches" -> Icons.Default.Watch
                        "Beauty" -> Icons.Default.AutoAwesome
                        else -> Icons.Default.HomeWork
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                )

                // Top Actions overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Discount tag
                    if (product.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .background(DiscountRose, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${product.discountPercent}% OFF",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Wishlist Heart Fab
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = CircleShape)
                            .clickable { onWishlistClick() }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) DiscountRose else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Star Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${product.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewsCount})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Add Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "$${product.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.originalPrice > 0) {
                            Text(
                                text = "$${product.originalPrice}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Button(
                        onClick = onAddToCartClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_grid_${product.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Categories Tab Screen (Interactive detailed listings)
// -------------------------------------------------------------
@Composable
fun CategoriesScreen(onCategorySelect: (String) -> Unit) {
    val items = listOf(
        Triple("Electronics", "Smart headphones, gaming keyboards, high-fidelity earbuds", 0),
        Triple("Fashion", "Premium winter woolen trench coats, minimalist organic tees", 1),
        Triple("Shoes", "Retro leather sneakers, hiking boots, active running shoes", 2),
        Triple("Watches", "Grand chronograph steel dials, hybrid smartwatches", 3),
        Triple("Beauty", "Renewable plant face serums, moisturizers, aloe gels", 4),
        Triple("Home & Living", "Aromatherapy wax candles, sculptural vases, orthopedic pillows", 5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Our Custom Departments",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tap on any card to see instant premium catalog items available.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { (cat, desc, gradIdx) ->
                Card(
                    onClick = { onCategorySelect(cat) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("category_row_${cat.lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Side Gradient Block
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight()
                                .background(Brush.verticalGradient(CategoryGradients[gradIdx]))
                        )

                        // Info Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(14.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Arrow indicator
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. About & Contact Tab Screen (Corporate & forms)
// -------------------------------------------------------------
@Composable
fun AboutContactScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Corporate Intro
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Our E-Shop Philosophy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Launched with a passionate vision of accessibility and sustainable luxury, E-Shop connects master designers and top-tier electronics fabricators straight to your doorstep. We exclude unnecessary wholesale layers to provide unbeatable pricing with pristine Material 3 craftsmanship.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Contact Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Support Desk & Queries",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Have an issue or bulk-shipping inquiry? Send our customer success agents a direct desk request.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inputs
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_email_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("contact_message_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty() && message.isNotEmpty()) {
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_submit_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Service ticket", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Office Info & map placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Contact Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("100 Market St, San Francisco, CA 94105", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+1 (555) 019-2834", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("support@eshop-premium.com", fontSize = 12.sp)
                }
            }
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ticket Submitted!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Thank you, $name. We have successfully registered your query. A dedicated customer executive will email you back within 6 hours.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            name = ""
                            email = ""
                            message = ""
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Awesome")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Wishlist Screen Component
// -------------------------------------------------------------
@Composable
fun WishlistScreen(
    viewModel: ShopViewModel,
    wishlistItems: List<WishlistItem>
) {
    val savedProducts = remember(wishlistItems) {
        ProductDataProvider.products.filter { prod ->
            wishlistItems.any { it.id == prod.id }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Private Wishlist",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Review items you've bookmarked. Add them straight to cart or remove anytime.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (savedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your Wishlist is Empty",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Bookmark products in the Shop section to save them here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.setTab(TabItem.Shop) }) {
                        Text("Browse Catalog")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedProducts) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wishlist_item_${product.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail Gradient
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        Brush.linearGradient(CategoryGradients[product.gradientIndex]),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (product.category) {
                                        "Electronics" -> Icons.Default.DeveloperBoard
                                        "Fashion" -> Icons.Default.Checkroom
                                        "Shoes" -> Icons.Default.DirectionsRun
                                        "Watches" -> Icons.Default.Watch
                                        "Beauty" -> Icons.Default.AutoAwesome
                                        else -> Icons.Default.HomeWork
                                    },
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${product.price}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DiscountRose)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = { viewModel.addToCart(product) }) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = MintGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Shopping Cart Dialog Overlay (Drawer styled)
// -------------------------------------------------------------
@Composable
fun CartDrawerDialog(
    cartItems: List<CartItem>,
    onClose: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Shopping Cart Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Your Shopping Cart is Empty",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Start exploring some premium gear to populate this space.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cartItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("cart_item_row_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Gradient block
                                    val gradIndex = item.imageResName.removePrefix("gradient_").toIntOrNull() ?: 0
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                Brush.linearGradient(CategoryGradients[gradIndex]),
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocalMall,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.4f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "$${item.price}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Quantity Controllers
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color.White, shape = RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    ) {
                                        Text(
                                            "-",
                                            modifier = Modifier
                                                .clickable { onUpdateQuantity(item.id, item.quantity - 1) }
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            item.quantity.toString(),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "+",
                                            modifier = Modifier
                                                .clickable { onUpdateQuantity(item.id, item.quantity + 1) }
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(onClick = { onRemove(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DiscountRose)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculations
                    val subtotal = cartItems.sumOf { it.price * it.quantity }
                    val shipping = if (subtotal > 150.0) 0.0 else 15.0
                    val tax = subtotal * 0.08
                    val grandTotal = subtotal + shipping + tax

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cart Subtotal:", fontSize = 12.sp)
                                Text(String.format(Locale.US, "$%.2f", subtotal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Shipping & Handling:", fontSize = 12.sp)
                                Text(if (shipping == 0.0) "FREE" else String.format(Locale.US, "$%.2f", shipping), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (shipping == 0.0) MintGreen else MaterialTheme.colorScheme.onBackground)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Tax (8%):", fontSize = 12.sp)
                                Text(String.format(Locale.US, "$%.2f", tax), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Price:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(String.format(Locale.US, "$%.2f", grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PremiumGold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("checkout_cart_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Place Secure Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. Login Profile Dialog Component
// -------------------------------------------------------------
@Composable
fun ProfileLoginDialog(
    userProfile: UserProfile,
    onClose: () -> Unit,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (userProfile.isLoggedIn) "Your Account Details" else "Customer Gateway",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (userProfile.isLoggedIn) {
                    // Show logged in detail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(PremiumGold, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(userProfile.username, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(userProfile.email, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Order History:", fontSize = 12.sp)
                                Text("${userProfile.ordersCount} Completed Orders", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MintGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscountRose),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Out Account", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Show login forms
                    Text(
                        "Login to access personalized tracking, save coupons, and check your digital invoice history.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Enter Username") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Enter Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLogin(username, email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Login securely", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. Product Details Dialog Popup Component
// -------------------------------------------------------------
@Composable
fun ProductDetailDialog(
    product: Product,
    isWishlisted: Boolean,
    onClose: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top close & title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Product Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Media Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(CategoryGradients[product.gradientIndex])),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (product.category) {
                            "Electronics" -> Icons.Default.DeveloperBoard
                            "Fashion" -> Icons.Default.Checkroom
                            "Shoes" -> Icons.Default.DirectionsRun
                            "Watches" -> Icons.Default.Watch
                            "Beauty" -> Icons.Default.AutoAwesome
                            else -> Icons.Default.HomeWork
                        },
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(100.dp)
                    )

                    // Wishlist Float action overlay
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(Color.White, CircleShape)
                            .clickable { onToggleWishlist() }
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) DiscountRose else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.uppercase(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} / 5.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Prices
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${product.price}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.originalPrice > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$${product.originalPrice}",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(DiscountRose, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAVE ${product.discountPercent}%",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Description details:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic stock status indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MintGreen, shape = CircleShape)
                            .size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "In Stock — Ships within 24 hours",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onAddToCart()
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("add_to_cart_detail_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Shopping Bag", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
