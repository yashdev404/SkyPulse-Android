package com.yash.skypulse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val DarkBackground = Color(0xFF0C141D)
private val SurfaceLowest = Color(0xFF070F18)
private val SurfaceLow = Color(0xFF151C26)
private val SurfaceContainerColor = Color(0xFF19202A)
private val SurfaceHigh = Color(0xFF232A34)
private val PrimaryCyan = Color(0xFF00F0FF)
private val OnPrimaryCyan = Color(0xFF00363A)
private val LightCyanText = Color(0xFFDBFCFF)
private val SecondaryCyan = Color(0xFF79D1FB)
private val OnSurfaceText = Color(0xFFDCE3F0)
private val OnSurfaceVariantText = Color(0xFFB9CACB)
private val OutlineText = Color(0xFF849495)
private val EmeraldGreen = Color(0xFF10B981)
private val RoseRed = Color(0xFFF43F5E)
private val AmberGold = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onBackClick: () -> Unit,
    onQuantityChange: (Product, Int) -> Unit,
    onOrderComplete: () -> Unit
) {
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var couponError by remember { mutableStateOf<String?>(null) }
    var couponSuccessMessage by remember { mutableStateOf<String?>(null) }

    var selectedPaymentMethod by remember { mutableIntStateOf(0) } // 0: Astro Card, 1: UPI, 2: Star Credits, 3: COD
    var isPlacingOrder by remember { mutableStateOf(false) }
    var showOrderConfirmedDialog by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }

    // Calculate coupon discount
    val couponDiscount = remember(appliedCoupon, subtotal) {
        val coupon = appliedCoupon ?: return@remember 0.0
        if (subtotal < coupon.minOrderValue) 0.0 else {
            when (coupon.type) {
                CouponType.PERCENTAGE -> (subtotal * coupon.discountValue / 100.0)
                CouponType.FLAT -> coupon.discountValue
            }
        }
    }

    // Additional Card Instant Discount if Astro Card is selected
    val bankDiscount = remember(selectedPaymentMethod, subtotal) {
        if (selectedPaymentMethod == 0) subtotal * 0.05 else 0.0 // 5% extra card discount
    }

    val finalTotal = (subtotal - couponDiscount - bankDiscount).coerceAtLeast(0.0)

    fun applyCouponCode(codeToApply: String) {
        val code = codeToApply.trim().uppercase()
        val foundCoupon = availableCoupons.find { it.code == code }
        if (foundCoupon == null) {
            couponError = "Invalid coupon or referral code!"
            couponSuccessMessage = null
        } else if (subtotal < foundCoupon.minOrderValue) {
            couponError = "Minimum order value of $${foundCoupon.minOrderValue.toInt()} required for ${foundCoupon.code}!"
            couponSuccessMessage = null
        } else {
            appliedCoupon = foundCoupon
            couponError = null
            val savingsStr = String.format(java.util.Locale.US, "$%.2f", if (foundCoupon.type == CouponType.PERCENTAGE) (subtotal * foundCoupon.discountValue / 100.0) else foundCoupon.discountValue)
            couponSuccessMessage = "Coupon ${foundCoupon.code} Applied! Saved $savingsStr"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar
            Surface(
                color = SurfaceContainerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 16.dp, top = 40.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightCyanText)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Checkout Manifest",
                                color = LightCyanText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ORBITAL DELIVERY PROTOCOL",
                                color = SecondaryCyan,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = SurfaceHigh,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("256-BIT SSL", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Scrollable Checkout Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                // Section A: Delivery Address
                item {
                    DeliveryAddressCard()
                }

                // Section B: Order Summary Items
                item {
                    Text(
                        text = "Order Items (${cartItems.sumOf { it.quantity }})",
                        color = OnSurfaceText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(cartItems) { item ->
                    CheckoutItemCard(
                        item = item,
                        onIncrease = { onQuantityChange(item.product, item.quantity + 1) },
                        onDecrease = { onQuantityChange(item.product, item.quantity - 1) }
                    )
                }

                // Section C: Coupons & Offers (Zomato / Amazon Style)
                item {
                    CouponsAndOffersSection(
                        couponInput = couponInput,
                        onCouponInputChange = { couponInput = it },
                        appliedCoupon = appliedCoupon,
                        couponError = couponError,
                        couponSuccessMessage = couponSuccessMessage,
                        onApplyClick = { applyCouponCode(couponInput) },
                        onSelectCoupon = { coupon ->
                            couponInput = coupon.code
                            applyCouponCode(coupon.code)
                        },
                        onRemoveCoupon = {
                            appliedCoupon = null
                            couponSuccessMessage = null
                            couponError = null
                        }
                    )
                }

                // Section D: Payment Methods
                item {
                    PaymentMethodsSection(
                        selectedMethod = selectedPaymentMethod,
                        onSelectMethod = { selectedPaymentMethod = it }
                    )
                }

                // Section E: Detailed Bill Summary
                item {
                    BillSummaryCard(
                        subtotal = subtotal,
                        couponDiscount = couponDiscount,
                        bankDiscount = bankDiscount,
                        finalTotal = finalTotal,
                        appliedCoupon = appliedCoupon
                    )
                }
            }
        }

        // Bottom Action Dock
        Surface(
            color = SurfaceContainerColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .border(1.dp, SurfaceHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount", color = OutlineText, fontSize = 11.sp)
                    Text(
                        text = String.format(java.util.Locale.US, "$%.2f", finalTotal),
                        color = PrimaryCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (couponDiscount > 0) {
                        Text(
                            text = String.format(java.util.Locale.US, "Saved $%.2f", couponDiscount + bankDiscount),
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        isPlacingOrder = true
                        showOrderConfirmedDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = OnPrimaryCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Place Order & Launch 🚀",
                        color = OnPrimaryCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showOrderConfirmedDialog) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = SurfaceLow,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Order Dispatched to Orbit! 🚀", color = OnSurfaceText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Telemetry locked! Your astronomical supplies are registered and in transit.",
                            color = OnSurfaceVariantText,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = SurfaceLowest,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("TRACKING CODE: ORBIT-2026-X942", color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("ESTIMATED ARRIVAL: 2 Orbital Days", color = SecondaryCyan, fontSize = 11.sp)
                                Text("TOTAL PAID: " + String.format(java.util.Locale.US, "$%.2f", finalTotal), color = LightCyanText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOrderConfirmedDialog = false
                            onOrderComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Text("Return to Cosmic Depot", color = OnPrimaryCyan, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun DeliveryAddressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Delivery Station", color = OnSurfaceText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = PrimaryCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text("DEFAULT", color = PrimaryCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Text("Node 7, Cislunar Station Alpha (Deck B, Bay 42)", color = OutlineText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            TextButton(onClick = { }) {
                Text("Change", color = SecondaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CheckoutItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceLowest)
            ) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = item.product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    color = OnSurfaceText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format(java.util.Locale.US, "$%.2f each", item.product.price),
                    color = OutlineText,
                    fontSize = 11.sp
                )
                Text(
                    text = String.format(java.util.Locale.US, "$%.2f", item.product.price * item.quantity),
                    color = PrimaryCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quantity Control Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerColor)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = LightCyanText, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    color = LightCyanText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = LightCyanText, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun CouponsAndOffersSection(
    couponInput: String,
    onCouponInputChange: (String) -> Unit,
    appliedCoupon: Coupon?,
    couponError: String?,
    couponSuccessMessage: String?,
    onApplyClick: () -> Unit,
    onSelectCoupon: (Coupon) -> Unit,
    onRemoveCoupon: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Coupons, Referral & Bank Offers", color = OnSurfaceText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Applied Coupon Box
            if (appliedCoupon != null) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(appliedCoupon.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("CODE: ${appliedCoupon.code}", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(appliedCoupon.title, color = OnSurfaceText, fontSize = 11.sp)
                            }
                        }

                        TextButton(onClick = onRemoveCoupon) {
                            Text("REMOVE", color = RoseRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponInput,
                    onValueChange = onCouponInputChange,
                    placeholder = { Text("Enter Coupon / Referral Code", color = OutlineText, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerColor,
                        unfocusedContainerColor = SurfaceContainerColor,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = OnSurfaceText,
                        unfocusedTextColor = OnSurfaceText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                )

                Button(
                    onClick = onApplyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Text("APPLY", color = OnPrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (couponSuccessMessage != null) {
                Text(couponSuccessMessage, color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
            if (couponError != null) {
                Text(couponError, color = RoseRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("AVAILABLE OFFER CODES", color = SecondaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Available Coupon Cards (Zomato Style)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableCoupons.forEach { coupon ->
                    val isApplied = appliedCoupon?.code == coupon.code
                    Surface(
                        color = SurfaceContainerColor,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isApplied) EmeraldGreen else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCoupon(coupon) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(coupon.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(coupon.code, color = LightCyanText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (coupon.isBankOffer) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = AmberGold.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                Text("BANK OFFER", color = AmberGold, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text(coupon.description, color = OutlineText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            Text(
                                text = if (isApplied) "APPLIED ✔" else "APPLY",
                                color = if (isApplied) EmeraldGreen else PrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsSection(selectedMethod: Int, onSelectMethod: (Int) -> Unit) {
    val paymentOptions = listOf(
        Triple("Astro Credit / Debit Card", "5% Instant Extra Card Offer", Icons.Default.CreditCard),
        Triple("Instant Quantum UPI", "Google Pay, PhonePe, Paytm", Icons.Default.QrCodeScanner),
        Triple("Star Credits & Crypto Beacon", "Pay with cosmic tokens", Icons.Default.Stars),
        Triple("Cash on Orbital Delivery", "Pay when manifest arrives", Icons.Default.Money)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Select Payment Gateway", color = OnSurfaceText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentOptions.forEachIndexed { index, (title, sub, icon) ->
                    val isSelected = selectedMethod == index
                    Surface(
                        color = SurfaceContainerColor,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryCyan else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMethod(index) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryCyan else OutlineText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(icon, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(title, color = OnSurfaceText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(sub, color = OutlineText, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillSummaryCard(
    subtotal: Double,
    couponDiscount: Double,
    bankDiscount: Double,
    finalTotal: Double,
    appliedCoupon: Coupon?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Detailed Bill Breakdown", color = OnSurfaceText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Item Subtotal", color = OutlineText, fontSize = 12.sp)
                Text(String.format(java.util.Locale.US, "$%.2f", subtotal), color = OnSurfaceText, fontSize = 12.sp)
            }

            if (appliedCoupon != null && couponDiscount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Coupon Savings (${appliedCoupon.code})", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(String.format(java.util.Locale.US, "-$%.2f", couponDiscount), color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (bankDiscount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Astro Card Offer (5% Instant)", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(String.format(java.util.Locale.US, "-$%.2f", bankDiscount), color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Atmospheric Delivery", color = OutlineText, fontSize = 12.sp)
                Text("FREE (Promotional)", color = SecondaryCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OutlineText.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Final Total", color = PrimaryCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(String.format(java.util.Locale.US, "$%.2f", finalTotal), color = PrimaryCyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}