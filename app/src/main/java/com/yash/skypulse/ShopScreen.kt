package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class ShopCategory(val id: String, val name: String, val emoji: String)
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val isBestseller: Boolean = false
)

@Composable
fun ShopScreen() {
    val categories = listOf(
        ShopCategory("1", "Telescopes", "🔭"),
        ShopCategory("2", "Apparel", "👕"),
        ShopCategory("3", "Meteorites", "🪨"),
        ShopCategory("4", "Posters", "🖼️"),
        ShopCategory("5", "Tech", "🛰️")
    )

    val products = listOf(
        Product("1", "Orion SkyQuest XT8 Classic Dobsonian", 459.99, 499.99, 4.8, 1240, "https://developer.android.com/static/images/brand/android-logo.png", true),
        Product("2", "NASA Vintage Worm Logo Hoodie", 45.00, null, 4.9, 856, "https://developer.android.com/static/images/brand/android-logo.png", true),
        Product("3", "Authentic Campo del Cielo Meteorite", 120.00, 150.00, 4.7, 92, "https://developer.android.com/static/images/brand/android-logo.png"),
        Product("4", "James Webb Space Telescope Model", 85.50, 95.00, 4.6, 215, "https://developer.android.com/static/images/brand/android-logo.png")
    )

    var selectedCategory by remember { mutableStateOf(categories[0].id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        // 1. Store Header & Search
        ShopHeader()

        // 2. Promotional Sale Banner
        SaleBanner()

        // 3. Category Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(categories) { category ->
                CategoryPill(
                    category = category,
                    isSelected = selectedCategory == category.id,
                    onClick = { selectedCategory = category.id }
                )
            }
        }

        // 4. Product Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp), // Extra bottom padding for floating bar
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { product ->
                ProductCard(product)
            }
        }
    }
}

@Composable
fun ShopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cosmic Store",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Gear for the modern explorer",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
        
        // Cart Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
            // Notification Badge
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun SaleBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4B0082), Color(0xFF000000)) // Deep purple to black
                )
            )
    ) {
        // Starry decoration effect (simulated with emojis for now)
        Text("✨", modifier = Modifier.padding(16.dp).align(Alignment.TopEnd), fontSize = 24.sp)
        Text("🚀", modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd), fontSize = 32.sp)

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
        ) {
            Surface(
                color = Color(0xFFFFD700), // Gold
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "STELLAR SALE",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Up to 50% Off",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Telescopes & Gear",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CategoryPill(category: ShopCategory, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(50.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            // Product Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White) // White background makes product pop like Amazon
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                
                if (product.isBestseller) {
                    Surface(
                        color = Color(0xFFE67E22), // Amazon-like orange
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "Best Seller",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp) // Fixed height to keep grid aligned
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Text(
                        text = product.rating.toString(),
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = " (${product.reviewCount})",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pricing
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${product.price}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (product.originalPrice != null) {
                        Text(
                            text = "$${product.originalPrice}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Add to Cart Button
                Button(
                    onClick = { /* Add to cart */ },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
