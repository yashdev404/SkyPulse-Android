package com.yash.skypulse

// Product Data Class for E-commerce Shop
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
    val originalPrice: Double? = null,
    val badge: String? = null,
    val is3D: Boolean = false,
    val subtag: String? = null
)

enum class SortOption(val displayName: String) {
    POPULARITY("Popularity"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    RATING("Highest Rated"),
    TRENDING("Trending & Bestsellers")
}

enum class CouponType {
    PERCENTAGE,
    FLAT
}

data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val type: CouponType,
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val icon: String = "🏷️",
    val isBankOffer: Boolean = false
)

data class FilterState(
    val minPrice: Float = 0f,
    val maxPrice: Float = 2000f,
    val minRating: Float = 0f,
    val selectedBadge: String? = null,
    val inStockOnly: Boolean = false
)

val availableCoupons = listOf(
    Coupon("NEWMON30", "New Moon Cosmic Sale", "30% OFF on all astronomical gear & telescopes", CouponType.PERCENTAGE, 30.0, minOrderValue = 50.0, icon = "🌙"),
    Coupon("WINTERSOL", "Winter Solstice Clearance", "Flat $50 OFF on orders above $150", CouponType.FLAT, 50.0, minOrderValue = 150.0, icon = "❄️"),
    Coupon("ORBITLAUNCH", "Orbital Launch Special", "20% OFF for new space explorers", CouponType.PERCENTAGE, 20.0, minOrderValue = 0.0, icon = "🚀"),
    Coupon("ASTROCARD", "Astro Card Instant Offer", "15% Instant Discount with Astro Credit/Debit Cards", CouponType.PERCENTAGE, 15.0, minOrderValue = 0.0, icon = "💳", isBankOffer = true),
    Coupon("YASHREF50", "Commander Referral Code", "Flat $50 Referral Reward for verified crew", CouponType.FLAT, 50.0, minOrderValue = 100.0, icon = "🎁")
)

// DummyJSON API Response Models
data class DummyProductResponse(
    val products: List<DummyProductDto>,
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)

data class DummyProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val category: String = "Gear",
    val thumbnail: String = "",
    val images: List<String> = emptyList()
)

// Mapper Function to convert DummyJSON DTO to SkyPulse Product
fun DummyProductDto.toProduct(): Product {
    val origPrice = if (discountPercentage > 0) {
        String.format(java.util.Locale.US, "%.2f", price / (1 - discountPercentage / 100)).toDoubleOrNull()
    } else null

    val badgeText = when {
        rating >= 4.8 -> "BEST SELLER"
        stock < 10 -> "LIMITED"
        category.contains("gear", ignoreCase = true) -> "CERTIFIED"
        else -> null
    }

    return Product(
        id = id.toString(),
        name = title,
        description = description,
        price = price,
        category = category.replaceFirstChar { it.uppercase() },
        imageUrl = if (images.isNotEmpty()) images.first() else thumbnail,
        stockQuantity = stock,
        isBestseller = rating >= 4.5,
        rating = rating,
        reviewCount = (rating * 12).toInt() + 5,
        originalPrice = origPrice,
        badge = badgeText
    )
}

// Represents an item in the user's local cart
data class CartItem(
    val product: Product,
    var quantity: Int
)

// Matches POST /api/v1/shop/checkout
data class CheckoutRequest(
    val username: String = "YashCommander",
    val items: List<CartItemRequest>
)

data class CartItemRequest(
    val productId: String,
    val quantity: Int
)

// Matches PostgreSQL Order response
data class OrderResponse(
    val id: String,
    val customerName: String,
    val totalAmount: Double,
    val status: String,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: String,
    val productId: String,
    val quantity: Int,
    val priceAtPurchase: Double
)
