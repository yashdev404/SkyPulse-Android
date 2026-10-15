package com.yash.skypulse

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ShopCategory(val id: String, val name: String, val emoji: String)

class ShopViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    init {
        fetchProductsFromBackend()
    }
    
    private fun fetchProductsFromBackend() {
        viewModelScope.launch {
            try {
                val response = BackendRetrofitInstance.api.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    _products.value = response.body()!!
                } else {
                    println("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Network Error: ${e.message}")
            }
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }
        
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            currentCart.add(CartItem(product, 1))
        }
        _cart.value = currentCart
    }
    
    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.product.price * it.quantity }
    }
    
    fun checkout() {
        // Here we will eventually make a POST request to Spring Boot to create an Order
        _cart.value = emptyList() // Clear cart on successful checkout
    }
}

@Composable
fun ShopScreen(viewModel: ShopViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    var showCartSheet by remember { mutableStateOf(false) }

    val categories = listOf(
        ShopCategory("1", "Telescopes", "🔭"),
        ShopCategory("2", "Apparel", "👕"),
        ShopCategory("3", "Meteorites", "🪨"),
        ShopCategory("4", "Posters", "🖼️"),
        ShopCategory("5", "Tech", "🛰️")
    )

    var selectedCategory by remember { mutableStateOf(categories[0].id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        // 1. Store Header & Search
        ShopHeader(cartCount = cart.sumOf { it.quantity }, onCartClick = { showCartSheet = true })

        // 2. Promotional Sale Carousel
        PromoCarousel()

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
                ProductCard(product, onAddClick = { viewModel.addToCart(product) })
            }
        }
    }
    
    if (showCartSheet) {
        CartBottomSheet(
            cartItems = cart,
            total = viewModel.getCartTotal(),
            onDismiss = { showCartSheet = false },
            onCheckout = { 
                viewModel.checkout()
                showCartSheet = false
            }
        )
    }
}

@Composable
fun ShopHeader(cartCount: Int, onCartClick: () -> Unit) {
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
                .background(Color.White.copy(alpha = 0.1f))
                .clickable { onCartClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
            // Notification Badge
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cartCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PromoCarousel() {
    val banners = listOf(
        Triple("STELLAR SALE", "Up to 50% Off", "Telescopes & Gear"),
        Triple("NEW ARRIVAL", "Martian Soil", "Authentic Display Pieces"),
        Triple("LIMITED EDITION", "Webb Posters", "Signed by the team")
    )
    
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (page % 2 == 0) {
                                listOf(Color(0xFF4B0082), Color(0xFF000000)) // Deep purple
                            } else {
                                listOf(Color(0xFF0F2027), Color(0xFF203A43)) // Deep teal
                            }
                        )
                    )
            ) {
                Text("✨", modifier = Modifier.padding(16.dp).align(Alignment.TopEnd), fontSize = 24.sp)
                Text("🚀", modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd), fontSize = 32.sp)

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(24.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = banner.first,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = banner.second,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = banner.third,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Pager Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
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
fun ProductCard(product: Product, onAddClick: () -> Unit) {
    var isAdded by remember { mutableStateOf(false) }
    
    // Animation for the button color
    val buttonColor by animateColorAsState(
        targetValue = if (isAdded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "buttonColor"
    )

    // Reset the button after 2 seconds
    LaunchedEffect(isAdded) {
        if (isAdded) {
            kotlinx.coroutines.delay(2000)
            isAdded = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* View product details */ },
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
                    onClick = {
                        isAdded = true
                        onAddClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isAdded) "Added! ✔️" else "Add to Cart", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
