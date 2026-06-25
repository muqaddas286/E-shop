package com.example.data

object ProductDataProvider {
    val categories = listOf(
        "Electronics",
        "Fashion",
        "Shoes",
        "Watches",
        "Beauty",
        "Home & Living"
    )

    val products = listOf(
        // Electronics
        Product(
            id = "elec_1",
            title = "AeroSound Max Pro Wireless Headphones",
            price = 129.99,
            originalPrice = 179.99,
            category = "Electronics",
            rating = 4.8f,
            reviewsCount = 312,
            description = "Experience rich, cinematic audio with active dynamic noise cancelling, 50-hour battery life, and ultra-plush memory foam earcups.",
            gradientIndex = 0,
            isFlashSale = true
        ),
        Product(
            id = "elec_2",
            title = "NanoBuds Active Earbuds",
            price = 69.99,
            originalPrice = 99.99,
            category = "Electronics",
            rating = 4.5f,
            reviewsCount = 184,
            description = "Sweatproof, ultra-compact true wireless earbuds designed with crystal clear audio and secure fit for rigorous workouts.",
            gradientIndex = 1
        ),
        Product(
            id = "elec_3",
            title = "Matrix RGB Mechanical Keyboard",
            price = 149.99,
            category = "Electronics",
            rating = 4.7f,
            reviewsCount = 95,
            description = "Premium clicky mechanical switches with customizable per-key RGB backlighting and aircraft-grade aluminum frame.",
            gradientIndex = 2
        ),

        // Fashion
        Product(
            id = "fash_1",
            title = "Urban Classic Woolen Trench Coat",
            price = 189.00,
            originalPrice = 249.00,
            category = "Fashion",
            rating = 4.6f,
            reviewsCount = 89,
            description = "A timeless wind-resistant woolen coat designed with double-breasted tailoring, adjustable waist belt, and deep inner utility pockets.",
            gradientIndex = 3
        ),
        Product(
            id = "fash_2",
            title = "EcoThread Premium Cotton Tee",
            price = 24.50,
            category = "Fashion",
            rating = 4.3f,
            reviewsCount = 420,
            description = "100% certified organic long-staple cotton tee. Ethically made, pre-shrunk, and incredibly soft for everyday styling.",
            gradientIndex = 4
        ),
        Product(
            id = "fash_3",
            title = "Streetwear Oversized Denim Jacket",
            price = 79.99,
            originalPrice = 110.00,
            category = "Fashion",
            rating = 4.5f,
            reviewsCount = 156,
            description = "Vintage-washed heavy denim jacket with distressed details, relaxed shoulders, and copper button closures.",
            gradientIndex = 5,
            isFlashSale = true
        ),

        // Shoes
        Product(
            id = "shoe_1",
            title = "Heritage Retro Leather Sneakers",
            price = 95.00,
            originalPrice = 125.00,
            category = "Shoes",
            rating = 4.7f,
            reviewsCount = 203,
            description = "Premium full-grain leather upper with retro colorways and cushioned EVA midsoles for all-day streetwear comfort.",
            gradientIndex = 0
        ),
        Product(
            id = "shoe_2",
            title = "Summit Trail Cushion Running Shoes",
            price = 119.99,
            category = "Shoes",
            rating = 4.4f,
            reviewsCount = 118,
            description = "Engineered mesh running shoes featuring high-rebound cushioning and multi-directional lugged rubber outsoles for peak grip.",
            gradientIndex = 1,
            isFlashSale = true
        ),
        Product(
            id = "shoe_3",
            title = "Vagabond Canvas Low Tops",
            price = 45.00,
            category = "Shoes",
            rating = 4.2f,
            reviewsCount = 310,
            description = "Breathable double-canvas lace-up sneakers, perfect for casual outings and warm summer days.",
            gradientIndex = 2
        ),

        // Watches
        Product(
            id = "wat_1",
            title = "Grand Chronograph Steel Watch",
            price = 349.00,
            originalPrice = 450.00,
            category = "Watches",
            rating = 4.9f,
            reviewsCount = 64,
            description = "A striking masterwork. Features solid surgical stainless steel casing, sapphire glass face, and high-precision Japanese movement.",
            gradientIndex = 3,
            isFlashSale = true
        ),
        Product(
            id = "wat_2",
            title = "Aura Hybrid Smartwatch Active",
            price = 179.99,
            originalPrice = 220.00,
            category = "Watches",
            rating = 4.5f,
            reviewsCount = 142,
            description = "Classic watch hands meet a hidden OLED screen. Track real-time heart rate, sleep scores, and smart notifications with 14-day battery.",
            gradientIndex = 4
        ),
        Product(
            id = "wat_3",
            title = "Nomad Minimalist Leather Watch",
            price = 115.00,
            category = "Watches",
            rating = 4.6f,
            reviewsCount = 78,
            description = "Ultra-thin sandblasted black dial paired with premium vegetable-tanned Italian leather straps.",
            gradientIndex = 5
        ),

        // Beauty
        Product(
            id = "beau_1",
            title = "Nectar Botanical Renewal Face Serum",
            price = 34.00,
            originalPrice = 48.00,
            category = "Beauty",
            rating = 4.8f,
            reviewsCount = 275,
            description = "Infused with rich herbal extracts and cold-pressed organic rosehip oil. Visibly brightens, plumps, and hydrates sensitive skin.",
            gradientIndex = 0
        ),
        Product(
            id = "beau_2",
            title = "HydraGlow Aloe Vera Moisture Cream",
            price = 18.50,
            category = "Beauty",
            rating = 4.6f,
            reviewsCount = 192,
            description = "Lightweight gel-cream formula. Floods skin with deep-cell hydration, leaving a fresh, dewy finish without being greasy.",
            gradientIndex = 1
        ),

        // Home & Living
        Product(
            id = "home_1",
            title = "Serene Soy Wax Aromatherapy Candles",
            price = 28.00,
            originalPrice = 38.00,
            category = "Home & Living",
            rating = 4.7f,
            reviewsCount = 143,
            description = "Three premium hand-poured soy candles infused with relaxing lavender, clean linen, and warm vanilla sandalwood scents.",
            gradientIndex = 2
        ),
        Product(
            id = "home_2",
            title = "Zen Handcrafted Ceramic Flower Vase",
            price = 39.99,
            category = "Home & Living",
            rating = 4.5f,
            reviewsCount = 57,
            description = "An artistic wabi-sabi inspired unglazed ceramic vase. Add dynamic minimalist sculpture to any mantel or table setup.",
            gradientIndex = 3
        ),
        Product(
            id = "home_3",
            title = "ErgoRest Premium Memory Foam Cushion",
            price = 49.99,
            category = "Home & Living",
            rating = 4.4f,
            reviewsCount = 94,
            description = "Orthopedic contour design wrapped in breathable cooling mesh, providing maximum lumbar and tailbone support for long workdays.",
            gradientIndex = 4,
            isFlashSale = true
        )
    )
}
