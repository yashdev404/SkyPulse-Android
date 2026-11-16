package com.yash.skypulse

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SlateOnyxBackground = Color(0xFF080C14)
private val SurfaceLow = Color(0xFF151C26)
private val SurfaceHigh = Color(0xFF232A34)
private val PrimaryCyan = Color(0xFF00F0FF)
private val OnPrimaryCyan = Color(0xFF00363A)
private val SecondaryCyan = Color(0xFF79D1FB)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8B95A5)
private val EmeraldGreen = Color(0xFF10B981)

data class VaultBadgeItem(
    val title: String,
    val description: String,
    val timestampSol: String,
    val status: String = "Unlocked",
    val isComplete: Boolean = true,
    val iconType: String = "solar"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeIntelVaultScreen(
    onBackClick: () -> Unit,
    commanderName: String = "Commander Vanguard",
    rankInfo: String = "Tier 2 Pioneer • Clearance Level 4"
) {
    BackHandler {
        onBackClick()
    }

    val vaultBadges = remember {
        listOf(
            VaultBadgeItem(
                title = "Solar Flare Observer",
                description = "Captured 50 Ha-alpha prominence events in full solar spectrum.",
                timestampSol = "Nov 12, 2026 • Sol 402",
                iconType = "solar"
            ),
            VaultBadgeItem(
                title = "Pioneer Orbital",
                description = "Completed first 100 deep-sky observation sessions.",
                timestampSol = "Oct 28, 2026 • Sol 388",
                iconType = "orbital"
            ),
            VaultBadgeItem(
                title = "Deep Field Cartographer",
                description = "Mapped 25 deep-sky nebulae across NGC catalog fields.",
                timestampSol = "Nov 05, 2026 • Sol 395",
                iconType = "cartographer"
            ),
            VaultBadgeItem(
                title = "Telemetry Relay Node",
                description = "Maintained 99.9% uptime across orbital relay nodes.",
                timestampSol = "Nov 15, 2026 • Sol 405",
                iconType = "telemetry"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateOnyxBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Header Bar
            Surface(
                color = SlateOnyxBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Badge Intel Vault",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                // 2. Commander Header Deck
                item {
                    Column {
                        Text(
                            text = commanderName,
                            color = TextWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 38.sp
                        )

                        Text(
                            text = rankInfo,
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // View Telemetry Button
                        Surface(
                            color = PrimaryCyan,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .height(44.dp)
                                .clickable { }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View Telemetry",
                                    color = OnPrimaryCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = OnPrimaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 3. 4 HUD Metric Stat Cards (2x2 Grid)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VaultHUDCard(
                                label = "POSTS",
                                value = "12",
                                modifier = Modifier.weight(1f)
                            )
                            VaultHUDCard(
                                label = "GEAR",
                                value = "4",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VaultHUDCard(
                                label = "BADGES",
                                value = "100%",
                                modifier = Modifier.weight(1f)
                            )
                            VaultHUDCard(
                                label = "SYNC",
                                value = "100%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Badge Intel Cards List
                items(vaultBadges) { badge ->
                    VaultBadgeCard(badge)
                }
            }
        }
    }
}

@Composable
fun VaultHUDCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, SurfaceHigh, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VaultBadgeCard(badge: VaultBadgeItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Badge Icon Sphere
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when (badge.iconType) {
                                    "solar" -> Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0284C7)))
                                    "orbital" -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF4338CA)))
                                    "cartographer" -> Brush.linearGradient(listOf(Color(0xFF7E22CE), Color(0xFFBE185D)))
                                    else -> Brush.linearGradient(listOf(Color(0xFF15803D), Color(0xFF0284C7)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon: ImageVector = when (badge.iconType) {
                            "solar" -> Icons.Default.WbSunny
                            "orbital" -> Icons.Default.Radar
                            "cartographer" -> Icons.Default.Shield
                            else -> Icons.Default.Sensors
                        }
                        Icon(icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = badge.title,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Unlocked Badge Pill
                        Surface(
                            color = SurfaceHigh,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = badge.status,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Completion Indicator
                if (badge.isComplete) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Complete",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description Text
            Text(
                text = badge.description,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamp Sol
            Text(
                text = badge.timestampSol,
                color = SecondaryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cyan Progress Bar Accent Line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(PrimaryCyan)
            )
        }
    }
}