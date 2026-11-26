package com.yash.skypulse

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Tailored Cosmic Store Theme Palette (Matching Tailwind Config & Figma Spec)
private val DarkBackground = Color(0xFF0C141D)
private val SurfaceLowest = Color(0xFF070F18)
private val SurfaceLow = Color(0xFF151C26)
private val SurfaceContainerColor = Color(0xFF19202A)
private val SurfaceHigh = Color(0xFF232A34)
private val SurfaceVariantColor = Color(0xFF2E3540)
private val PrimaryCyan = Color(0xFF00F0FF)
private val OnPrimaryCyan = Color(0xFF00363A)
private val LightCyanText = Color(0xFFDBFCFF)
private val SecondaryCyan = Color(0xFF79D1FB)
private val DeepCyanContainer = Color(0xFF007BA1)
private val OnSecondaryContainerText = Color(0xFFF2F9FF)
private val OnSurfaceText = Color(0xFFDCE3F0)
private val OnSurfaceVariantText = Color(0xFFB9CACB)
private val OutlineText = Color(0xFF849495)
private val EmeraldGreen = Color(0xFF10B981)
private val RoseRed = Color(0xFFF43F5E)

data class ShopCategory(val id: String, val name: String, val icon: ImageVector)

data class SalesBanner(
    val title: String,
    val headline: String,
    val subtitle: String,
    val promoCode: String,
    val ctaText: String,
    val imageUrl: String,
    val tagIcon: ImageVector = Icons.Default.RocketLaunch
)

class ShopViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _selectedSort = MutableStateFlow(SortOption.POPULARITY)
    val selectedSort: StateFlow<SortOption> = _selectedSort

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    init {
        fetchProductsFromBackend()
    }

    private fun fetchProductsFromBackend() {
        viewModelScope.launch {
            try {
                // 1. Try Spring Boot PostgreSQL Backend API first
                val response = BackendRetrofitInstance.api.getProducts()
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    _products.value = response.body()!!
                    return@launch
                }
            } catch (e: Exception) {
                println("PostgreSQL Spring Boot API error: ${e.message}")
            }

            try {
                // 2. Try DummyJSON API fallback
                val dummyResponse = DummyJsonInstance.api.getProducts(limit = 30)
                if (dummyResponse.isSuccessful && dummyResponse.body() != null) {
                    val liveProducts = dummyResponse.body()!!.products.map { it.toProduct() }
                    _products.value = liveProducts
                    return@launch
                }
            } catch (e: Exception) {
                println("DummyJSON API unreachable: ${e.message}")
            }

            // 3. Local Static Fallback
            _products.value = fallbackProducts
        }
    }

    private val fallbackProducts = listOf(
        Product(
            id = "1",
            name = "Celestron AstroMaster 130EQ Reflector",
            description = "High precision reflector telescope mounted on a steel German equatorial tripod.",
            price = 299.99,
            category = "Telescopes",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBzO7y5AsmXuRZP1AmfmtRlgcjiawDQrbuup-ewbJE_wBsAGrlT4BJJ2owHQP1mfi_mKoU1yD_Kiks4Zg6vSLOECiwmUi5f5hA_WkhTYffe-ksNEJH-AJ7E9lNsF_Z2C3ZebhmiQo1QSpYnnu7vCtFPTMhGpLsZUZGV-TKvuoxE2wd5_HRmxIeAtUATtYJhMnKP_LjhAL7PgKm5X0GdeERSZT2YRFJQ5J16to1jRL0",
            stockQuantity = 5,
            isBestseller = true,
            rating = 4.9,
            reviewCount = 142,
            originalPrice = 349.99,
            badge = "BEST SELLER",
            is3D = true
        ),
        Product(
            id = "2",
            name = "Authentic Campo del Cielo Meteorite (240g)",
            description = "Certified authentic nickel-iron meteorite specimen resting on a dark obsidian stand.",
            price = 149.00,
            category = "Meteorites",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCjXPyzVJLWfYJvGvaZhgMqpDd6fs03w4NZd4o27GhEb5PW4TBxIf4t1rOS1PRglM4AnRv9G-lS0pAjd8a3D_p4isTYPhMJWADL-fEUnkMUEe9dr6Db1NWMT6fGTEVLRK4_qx5GEmYYdHian4ZMZTyIl0nCxmc8ZzX0ASIoo0Vw_7LlJ1WvhnqcUiPzdUFaNRDf9aF0pzNPet80PknLumbMLC5MfjxCyqVhLPcs-TM",
            stockQuantity = 12,
            isBestseller = true,
            rating = 4.95,
            reviewCount = 88,
            originalPrice = 185.00,
            badge = "CERTIFIED"
        ),
        Product(
            id = "3",
            name = "James Webb Deep Field Nebula Hoodie",
            description = "Premium heavyweight matte black hoodie featuring an iridescent James Webb Cosmic Cliffs nebula graphic.",
            price = 68.00,
            category = "Apparel",
               imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDgN4VzhtAfWcw9IPXX-4As-Eu7FgYSeuE71SRp4K3qDLOFP8pHYs0FiN-YKAeRmouPnvce4Hm3jBeKUCjk0UXB_Xdvj9DaIrvDRDfKz3b_bBhMNVdZHf0FnfHAylENKn0lseW4kNG6eP186IWc10dNr2_ENA3PVg5dz4ws0q3oxr72Wnv1MKXKqbaf0lWazH1_ZH-TdEIq_p3_6dB7X4CO2Rxtp5TG8cOrKnav7ac",
            stockQuantity = 20,
            isBestseller = false,
             rating = 4.8,
            reviewCount = 96,
            originalPrice = null,
            badge = "LIMITED",
            subtag = "Cosmic Ed."
        ),
        Product(
            id = "4",
            name = "Lunar Surface High-Res Art Print (24x36”)",
            description = "High resolution detailed monochrome lunar crater photography art print in an ultra-slim black aluminum frame.",
            price = 42.00,
            category = "Tech & Posters",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuASjq6P9DEzUQgW_Ate-SB-6KuHCPWBt9covktkNfe-8MD6dnQO2KnZsvLhsJxsGRMGKru5o7i_N9rl-1UZgHxW-Osa96UbZ37HUV8f358wOv9DWawoIgeW_uPV-cKnzC7-H10J6UbXi37tIoWBxTKVxf-xvQ3SfRfF7bY_MEKdC3GCRrZRat9PN1faWy-I94aAp9HZpVHh0FdBtiEHWEK0mLf-SxKbjo9DPR4s6UE",
            stockQuantity = 15,
            isBestseller = false,
            rating = 4.7,
            reviewCount = 54,
            originalPrice = 55.00,
            badge = "MUSEUM"
        )
    )

    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }

        if (existingIndex != -1) {
            val currentItem = currentCart[existingIndex]
            currentCart[existingIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
        } else {
            currentCart.add(CartItem(product, 1))
        }
        _cart.value = currentCart
    }

    fun updateCartQuantity(product: Product, newQuantity: Int) {
        val currentCart = _cart.value.toMutableList()
        if (newQuantity <= 0) {
            currentCart.removeAll { it.product.id == product.id }
        } else {
            val index = currentCart.indexOfFirst { it.product.id == product.id }
            if (index != -1) {
                currentCart[index] = currentCart[index].copy(quantity = newQuantity)
            } else {
                currentCart.add(CartItem(product, newQuantity))
            }
        }
        _cart.value = currentCart
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.product.price * it.quantity }
    }

    fun setSortOption(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun setFilterState(filter: FilterState) {
        _filterState.value = filter
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun checkout(username: String = "yash") {
        viewModelScope.launch {
            try {
                val requestItems = _cart.value.map {
                    CartItemRequest(productId = it.product.id, quantity = it.quantity)
                }
                val request = CheckoutRequest(username = username, items = requestItems)
                BackendRetrofitInstance.api.checkout(request)
            } catch (e: Exception) {
                println("Checkout network error: ${e.message}")
            } finally {
                _cart.value = emptyList()
            }
        }
    }
}

@Composable
fun ShopScreen(
    viewModel: ShopViewModel = viewModel(),
    currentUsername: String = "yash"
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var isCheckoutActive by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedDetailProduct by remember { mutableStateOf<Product?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("0") }

    val categories = listOf(
        ShopCategory("0", "All Gear", Icons.Default.Public),
        ShopCategory("1", "Telescopes", Icons.Default.TravelExplore),
        ShopCategory("2", "Meteorites", Icons.Default.Autorenew),
        ShopCategory("3", "Apparel", Icons.Default.Checkroom),
        ShopCategory("4", "Tech & Posters", Icons.Default.PhotoCameraBack)
    )

    val filteredProducts = remember(products, selectedCategory, searchQuery, selectedSort, filterState) {
        products.filter { product ->
            val matchesCategory = if (selectedCategory == "0") true else {
                val targetName = categories.find { it.id == selectedCategory }?.name ?: ""
                product.category.contains(targetName, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true) || product.description.contains(searchQuery, ignoreCase = true)
            val matchesPrice = product.price >= filterState.minPrice && product.price <= filterState.maxPrice
            val matchesRating = product.rating >= filterState.minRating
            val matchesBadge = filterState.selectedBadge == null || product.badge.equals(filterState.selectedBadge, ignoreCase = true)
            val matchesStock = !filterState.inStockOnly || product.stockQuantity > 0

            matchesCategory && matchesSearch && matchesPrice && matchesRating && matchesBadge && matchesStock
        }.let { list ->
            when (selectedSort) {
                SortOption.POPULARITY -> list.sortedByDescending { it.rating * (if (it.reviewCount > 0) it.reviewCount else 10) }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
                SortOption.RATING -> list.sortedByDescending { it.rating }
                SortOption.TRENDING -> list.sortedByDescending { (if (it.isBestseller) 10.0 else 0.0) + it.rating }
            }
        }
    }

    if (isCheckoutActive) {
        CheckoutScreen(
            cartItems = cart,
            currentUsername = currentUsername,
            onBackClick = { isCheckoutActive = false },
            onQuantityChange = { product, q -> viewModel.updateCartQuantity(product, q) },
            onOrderComplete = {
                viewModel.checkout(currentUsername)
                isCheckoutActive = false
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Header Bar
                ShopHeader(
                    cartCount = cart.sumOf { it.quantity },
                    onCartClick = { showCartSheet = true }
                )

                // Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // 2. Search & Telemetry Filter Bar (With 5-sec cycling typewriter hint)
                    SearchBarAndFilter(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onFilterClick = { showFilterSheet = true }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Feed Layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Column {
                                // 3. Multi-Banner Sales Carousel (4 Banners)
                                MultiBannerSalesCarousel(
                                    onExploreClick = {
                                        showCartSheet = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // 4. Horizontal Scrollable Category Pills
                                CategoryPillRow(
                                    categories = categories,
                                    selectedCategory = selectedCategory,
                                    onSelectCategory = { selectedCategory = it }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // 5. Section Header & Sort Trigger
                                SectionHeaderAndSort(
                                    count = filteredProducts.size,
                                    selectedSort = selectedSort,
                                    onSortClick = { showSortSheet = true }
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // 6. 2x2 Product Grid Cards
                        items(filteredProducts) { product ->
                            val cartItem = cart.find { it.product.id == product.id }
                            val quantity = cartItem?.quantity ?: 0
                            ProductCard(
                                product = product,
                                cartQuantity = quantity,
                                onQuantityChange = { newQ ->
                                    viewModel.updateCartQuantity(product, newQ)
                                },
                                onProductClick = {
                                    selectedDetailProduct = product
                                }
                            )
                        }

                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                // 7. Telemetry Certification Strip
                                TelemetryCertificationStrip()
                            }
                        }
                    }
                }
            }

            // Sticky/Floating Bottom Checkout Dock
            if (cart.isNotEmpty()) {
                Surface(
                    color = SurfaceContainerColor,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 80.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cart.sumOf { it.quantity }}",
                                    color = OnPrimaryCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Total Manifest", color = OutlineText, fontSize = 10.sp)
                                Text(
                                    text = String.format(java.util.Locale.US, "$%.2f", viewModel.getCartTotal()),
                                    color = PrimaryCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { isCheckoutActive = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Proceed to Checkout", color = OnPrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = OnPrimaryCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Cart Drawer Modal
        if (showCartSheet) {
            CartBottomSheet(
                cartItems = cart,
                total = viewModel.getCartTotal(),
                onDismiss = { showCartSheet = false },
                onCheckout = {
                    showCartSheet = false
                    isCheckoutActive = true
                }
            )
        }

        // Sort Selection Modal Sheet
        if (showSortSheet) {
            SortSelectionBottomSheet(
                selectedSort = selectedSort,
                onSelectSort = { sort ->
                    viewModel.setSortOption(sort)
                    showSortSheet = false
                },
                onDismiss = { showSortSheet = false }
            )
        }

        // Advanced Filter Modal Sheet
        if (showFilterSheet) {
            AdvancedFilterBottomSheet(
                filterState = filterState,
                onApplyFilter = { newFilter ->
                    viewModel.setFilterState(newFilter)
                    showFilterSheet = false
                },
                onReset = {
                    viewModel.resetFilters()
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        if (selectedDetailProduct != null) {
            val currentDetail = selectedDetailProduct!!
            val cartItem = cart.find { it.product.id == currentDetail.id }
            val quantity = cartItem?.quantity ?: 0
            ProductDetailBottomSheet(
                product = currentDetail,
                cartQuantity = quantity,
                onQuantityChange = { newQ ->
                    viewModel.updateCartQuantity(currentDetail, newQ)
                },
                onDismiss = { selectedDetailProduct = null }
            )
        }
    }
}

@Composable
fun ShopHeader(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsating Cyan Glow Dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dotAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryCyan.copy(alpha = alpha))
                        .shadow(4.dp, CircleShape, spotColor = PrimaryCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Shop",
                    color = LightCyanText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "LIVE GEAR & ASTRONOMICAL SUPPLIES",
                color = OnSurfaceVariantText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerColor)
                    .clickable { onCartClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = "Cart",
                    tint = OnSurfaceText,
                    modifier = Modifier.size(20.dp)
                )
                if (cartCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartCount.toString(),
                            color = OnPrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(LightCyanText),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OnPrimaryCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchBarAndFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    // 5-second cycling placeholder typewriter hints
    val hints = remember {
        listOf(
            "Search telescopes & reflectors...",
            "Search Campo del Cielo meteorites...",
            "Search James Webb hoodies...",
            "Search lunar crater art prints...",
            "Search astrophotography gear..."
        )
    }
    var currentHintIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentHintIndex = (currentHintIndex + 1) % hints.size
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = SurfaceLow,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f)),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SecondaryCyan,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        AnimatedContent(
                            targetState = hints[currentHintIndex],
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                            },
                            label = "SearchHintAnimation"
                        ) { hintText ->
                            Text(
                                text = hintText,
                                color = OutlineText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = OnSurfaceText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(PrimaryCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Surface(
            color = SurfaceLow,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(52.dp)
                .clickable { onFilterClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter Catalog",
                    tint = SecondaryCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun MultiBannerSalesCarousel(onExploreClick: () -> Unit) {
    val banners = remember {
        listOf(
            SalesBanner(
                title = "STELLAR DISCOUNTS",
                headline = "Up to 50% Off",
                subtitle = "Deep space reflectors & calibrated astrophotography sensors",
                promoCode = "STELLAR50",
                ctaText = "Explore Depot",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBjdAR1HpjwYAfo-a00PU1UPp3rsxyERJfdRerUHw9kxU2w04kGOFRgVPUJrOxF8qkEPZXXE1Y1DJeu58Sb-ZHm_aw5D7BEGAw5Yosqlm8nmACC6PeqAW9VusfUv86Y19Yk-0I58FhEPe6_2v5Zc_xdWyOhDermBxNLrLgKGgNT_E_ID7IjhI7CO7k-6dXrezKWGPIVTEJtq5HbGp_Aef1KyLwHRznibME9iOvPzxk",
                tagIcon = Icons.Default.RocketLaunch
            ),
            SalesBanner(
                title = "NEW MOON COSMIC SALE",
                headline = "Extra 30% Off",
                subtitle = "Celebrate full moon alignment with discount code NEWMON30",
                promoCode = "NEWMON30",
                ctaText = "Claim 30% Off",
                imageUrl = "https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=800",
                tagIcon = Icons.Default.NightsStay
            ),
            SalesBanner(
                title = "WINTER SOLSTICE CLEARANCE",
                headline = "Flat $50 Off",
                subtitle = "Apply code WINTERSOL on all orders above $150",
                promoCode = "WINTERSOL",
                ctaText = "Shop Clearance",
                imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800",
                tagIcon = Icons.Default.AcUnit
            ),
            SalesBanner(
                title = "ORBITAL LAUNCH SPECIAL",
                headline = "20% Off Launch Deal",
                subtitle = "Welcome code ORBITLAUNCH for certified space crew",
                promoCode = "ORBITLAUNCH",
                ctaText = "Launch Deal",
                imageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800",
                tagIcon = Icons.Default.Explore
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-advance carousel every 4 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .border(1.dp, SurfaceHigh, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerColor)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SurfaceContainerColor.copy(alpha = 0.95f),
                                        SurfaceContainerColor.copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Tag Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SurfaceLowest.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(PrimaryCyan))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = banner.title,
                                        color = PrimaryCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(banner.tagIcon, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(banner.promoCode, color = SecondaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Headline
                        Column {
                            Text(
                                text = banner.headline,
                                color = LightCyanText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = banner.subtitle,
                                color = OnSurfaceVariantText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // CTA Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = PrimaryCyan,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { onExploreClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = banner.ctaText,
                                        color = OnPrimaryCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = OnPrimaryCyan, modifier = Modifier.size(13.dp))
                                }
                            }

                            // Dot Pager Indicators
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(banners.size) { iteration ->
                                    val isSelected = pagerState.currentPage == iteration
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 18.dp else 5.dp, 5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) PrimaryCyan else SurfaceVariantColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPillRow(
    categories: List<ShopCategory>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category.id
            Surface(
                color = if (isSelected) PrimaryCyan else SurfaceContainerColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onSelectCategory(category.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = if (isSelected) OnPrimaryCyan else SecondaryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        color = if (isSelected) OnPrimaryCyan else OnSurfaceVariantText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeaderAndSort(
    count: Int,
    selectedSort: SortOption,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Stellar Artifacts",
                color = OnSurfaceText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = SurfaceHigh,
                shape = CircleShape
            ) {
                Text(
                    text = "$count available",
                    color = SecondaryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSortClick() }
        ) {
            Text(
                text = "Sort: ${selectedSort.displayName}",
                color = SecondaryCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Sort",
                tint = SecondaryCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    cartQuantity: Int,
    onQuantityChange: (Int) -> Unit,
    onProductClick: () -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp))
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Product Media Frame (1:1 Aspect Ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLowest)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Left Overlay Badges
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.badge != null) {
                        val badgeBg = when (product.badge) {
                            "BEST SELLER" -> DeepCyanContainer
                            "CERTIFIED" -> SurfaceHigh
                            "LIMITED" -> SurfaceVariantColor
                            "MUSEUM" -> DeepCyanContainer
                            else -> SurfaceHigh
                        }
                        val badgeTextColor = if (product.badge == "CERTIFIED") PrimaryCyan else OnSecondaryContainerText

                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = product.badge,
                                color = badgeTextColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (product.is3D) {
                        Surface(
                            color = SurfaceLowest.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ViewInAr, contentDescription = "3D", tint = PrimaryCyan, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "3D",
                                    color = PrimaryCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Top Right Favorite Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceLowest.copy(alpha = 0.7f))
                        .clickable { isFavorite = !isFavorite },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) RoseRed else OnSurfaceVariantText,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = product.rating.toString(),
                    color = OnSurfaceText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "(${product.reviewCount})",
                    color = OutlineText,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Title
            Text(
                text = product.name,
                color = OnSurfaceText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Pricing Row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "$%.2f", product.price),
                    color = PrimaryCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (product.originalPrice != null) {
                    Text(
                        text = String.format(java.util.Locale.US, "$%.2f", product.originalPrice),
                        color = OutlineText,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                } else if (product.subtag != null) {
                    Text(
                        text = product.subtag,
                        color = SecondaryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart Button OR In-Card Quantity Stepper
            if (cartQuantity <= 0) {
                Button(
                    onClick = {
                        onQuantityChange(1)
                        Toast.makeText(context, "Added ${product.name} to cart!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = OnPrimaryCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Cart",
                        color = OnPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    color = PrimaryCyan,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onQuantityChange(cartQuantity - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = OnPrimaryCyan, modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = "$cartQuantity",
                            color = OnPrimaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        IconButton(
                            onClick = { onQuantityChange(cartQuantity + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = OnPrimaryCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryCertificationStrip() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Calibrated & Authenticated",
                        color = OnSurfaceText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spectrometric verification certificate included",
                        color = OutlineText,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield",
                tint = SecondaryCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSelectionBottomSheet(
    selectedSort: SortOption,
    onSelectSort: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceHigh,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(OutlineText.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("Sort Stellar Catalog", color = PrimaryCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            SortOption.entries.forEach { option ->
                val isSelected = selectedSort == option
                Surface(
                    color = if (isSelected) SurfaceContainerColor else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSort(option) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.displayName,
                            color = if (isSelected) PrimaryCyan else OnSurfaceText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterBottomSheet(
    filterState: FilterState,
    onApplyFilter: (FilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var maxPrice by remember { mutableFloatStateOf(filterState.maxPrice) }
    var minRating by remember { mutableFloatStateOf(filterState.minRating) }
    var selectedBadge by remember { mutableStateOf(filterState.selectedBadge) }
    var inStockOnly by remember { mutableStateOf(filterState.inStockOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceHigh,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(OutlineText.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Catalog Telemetry", color = PrimaryCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    onReset()
                    maxPrice = 2000f
                    minRating = 0f
                    selectedBadge = null
                    inStockOnly = false
                }) {
                    Text("Reset All", color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Range Slider
            Text(
                text = "Maximum Price: $${maxPrice.toInt()}",
                color = OnSurfaceText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                valueRange = 50f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryCyan,
                    activeTrackColor = PrimaryCyan,
                    inactiveTrackColor = SurfaceContainerColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rating Filter
            Text("Minimum Rating", color = OnSurfaceText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0f to "All", 4.0f to "4.0★ +", 4.5f to "4.5★ +").forEach { (ratingVal, label) ->
                    val isSelected = minRating == ratingVal
                    Surface(
                        color = if (isSelected) PrimaryCyan else SurfaceContainerColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { minRating = ratingVal }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) OnPrimaryCyan else OnSurfaceVariantText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badge Filter
            Text("Product Classification Badge", color = OnSurfaceText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val badges = listOf(null to "All", "BEST SELLER" to "Best Seller", "CERTIFIED" to "Certified", "LIMITED" to "Limited", "MUSEUM" to "Museum")
                items(badges) { (bVal, bLabel) ->
                    val isSelected = selectedBadge == bVal
                    Surface(
                        color = if (isSelected) PrimaryCyan else SurfaceContainerColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { selectedBadge = bVal }
                    ) {
                        Text(
                            text = bLabel,
                            color = if (isSelected) OnPrimaryCyan else OnSurfaceVariantText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // In Stock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("In Stock Items Only", color = OnSurfaceText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = inStockOnly,
                    onCheckedChange = { inStockOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCyan, checkedTrackColor = SurfaceContainerColor)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Apply Button
            Button(
                onClick = {
                    onApplyFilter(
                        FilterState(
                            minPrice = 0f,
                            maxPrice = maxPrice,
                            minRating = minRating,
                            selectedBadge = selectedBadge,
                            inStockOnly = inStockOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Catalog Filters", color = OnPrimaryCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    product: Product,
    cartQuantity: Int,
    onQuantityChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = product.category.uppercase(),
                        color = PrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurfaceText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Product Media Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceLowest)
                    .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (product.badge != null) {
                    Surface(
                        color = PrimaryCyan,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = product.badge,
                            color = OnPrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.name,
                color = OnSurfaceText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${product.rating}",
                    color = OnSurfaceText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${product.reviewCount} reviews)",
                    color = OutlineText,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "In Stock (${product.stockQuantity} left)",
                    color = EmeraldGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.description,
                color = OnSurfaceVariantText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Price & Cart Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PRICE", color = OutlineText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(java.util.Locale.US, "$%.2f", product.price),
                        color = PrimaryCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (cartQuantity <= 0) {
                    Button(
                        onClick = {
                            onQuantityChange(1)
                            Toast.makeText(context, "Added ${product.name} to cart!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = OnPrimaryCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", color = OnPrimaryCyan, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryCyan)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = { onQuantityChange(cartQuantity - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove", tint = OnPrimaryCyan)
                        }
                        Text(
                            text = "$cartQuantity in cart",
                            color = OnPrimaryCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { onQuantityChange(cartQuantity + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = OnPrimaryCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}