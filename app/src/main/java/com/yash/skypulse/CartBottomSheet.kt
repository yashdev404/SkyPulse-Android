package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val SurfaceLowest = Color(0xFF070F18)
private val SurfaceContainerColor = Color(0xFF19202A)
private val SurfaceHigh = Color(0xFF232A34)
private val PrimaryCyan = Color(0xFF00F0FF)
private val OnPrimaryCyan = Color(0xFF00363A)
private val SecondaryCyan = Color(0xFF79D1FB)
private val OnSurfaceText = Color(0xFFDCE3F0)
private val OutlineText = Color(0xFF849495)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartItems: List<CartItem>,
    total: Double,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    val totalCount = cartItems.sumOf { it.quantity }

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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = PrimaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Orbital Manifest",
                        color = PrimaryCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PrimaryCyan,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$totalCount items",
                            color = OnPrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerColor)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = OutlineText, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your orbital manifest is empty. It's like a black hole in here.",
                        color = OutlineText,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Itemized Cart List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(item)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = OutlineText.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Price Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = OutlineText, fontSize = 13.sp)
                    Text(String.format(java.util.Locale.US, "$%.2f", total), color = OnSurfaceText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Atmospheric Shipping", color = OutlineText, fontSize = 13.sp)
                    Text("Free (Promotional)", color = SecondaryCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Manifest", color = PrimaryCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(String.format(java.util.Locale.US, "$%.2f", total), color = PrimaryCyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Checkout Button
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure Checkout",
                        tint = OnPrimaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Secure Quantum Checkout • " + String.format(java.util.Locale.US, "$%.2f", total),
                        color = OnPrimaryCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    color = OnSurfaceText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Qty: ${item.quantity}",
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
        }
    }
}