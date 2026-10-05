package com.yash.skypulse

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yash.skypulse.ui.theme.SkyPulseTheme
import kotlin.math.roundToInt
// import com.airbnb.lottie.compose.*

sealed class AppScreen(val icon: ImageVector, val label: String) {
    object Feed : AppScreen(Icons.Default.RssFeed, "Feed")
    object Rooms : AppScreen(Icons.Default.Groups, "Rooms")
    object Stargazer : AppScreen(Icons.Default.AutoAwesome, "Stargazer")
    object Shop : AppScreen(Icons.Default.ShoppingBag, "Shop")
    object Profile : AppScreen(Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Feed) }

    // Logic for hiding bar on scroll
    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx + delta
                bottomBarOffsetHeightPx = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    // Simulate initial loading
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // Consistent deep space background
            .nestedScroll(nestedScrollConnection)
    ) {
        if (isLoading) {
            AsteroidLoadingScreen()
        } else {
            // Main Content Area
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(500),
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.Feed -> HomeFeedScreen()
                        AppScreen.Rooms -> RoomsScreen()
                        AppScreen.Stargazer -> HomeScreen()
                        AppScreen.Shop -> ShopScreen()
                        AppScreen.Profile -> SkyPulseProfileScreen()
                    }
                }
            }

            // Custom Floating Pill Bottom Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.roundToInt()) }
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val screens = listOf(
                            AppScreen.Feed,
                            AppScreen.Rooms,
                            AppScreen.Stargazer,
                            AppScreen.Shop,
                            AppScreen.Profile
                        )
                        screens.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val animatedAlpha by animateFloatAsState(
                                if (isSelected) 1f else 0.5f,
                                label = "IconAlpha"
                            )
                            
                            IconButton(
                                onClick = { 
                                    currentScreen = screen
                                    // Reset bar position when switching tabs
                                    bottomBarOffsetHeightPx = 0f 
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        modifier = Modifier.size(24.dp).graphicsLayer(alpha = animatedAlpha)
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = screen.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 2.dp)
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
}

@Composable
fun AsteroidLoadingScreen() {
    // We'll use a standard indicator until you add the 'asteroid.json' to res/raw
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)), // Deep space blue
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
        Text(
            text = "Entering Orbit...",
            color = Color.White,
            modifier = Modifier.padding(top = 100.dp),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun ShopScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Exquisite Space Shop (Coming Soon)")
    }
}

@Composable
fun MainScreenPreview() {
    SkyPulseTheme {
        MainScreen()
    }
}
