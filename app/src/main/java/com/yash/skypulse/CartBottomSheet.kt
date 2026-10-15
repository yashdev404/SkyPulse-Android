package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartItems: List<CartItem>,
    total: Double,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B2A47),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Cart", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty. It's like a black hole in here.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    cartItems.forEach { item ->
                        CartItemRow(item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(String.format("$%.2f", total), color = Color(0xFF4A90E2), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Checkout & Launch Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp)) // Safe area for bottom navigation
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.1f))) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$${item.product.price}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        
        Text("x${item.quantity}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
    }
}