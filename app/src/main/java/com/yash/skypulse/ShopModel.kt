package com.yash.skypulse

// This matches the JSON we will return from Spring Boot: /api/v1/shop/products
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // e.g., "Telescopes", "Apparel", "Art"
    val imageUrl: String,
    val stockQuantity: Int,
    val isBestseller: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val originalPrice: Double? = null
)

// Represents an item in the user's local cart
data class CartItem(
    val product: Product,
    var quantity: Int
)

// These match the JSON we will send to Spring Boot: POST /api/v1/shop/checkout
data class CheckoutRequest(
    val items: List<CartItemRequest>
)

data class CartItemRequest(
    val productId: String,
    val quantity: Int
)
