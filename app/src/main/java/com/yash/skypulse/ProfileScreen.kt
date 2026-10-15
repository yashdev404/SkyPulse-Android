package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyPulseProfileScreen() {
    // MOCK DATA: This simulates what our Spring Boot backend will return via Retrofit!
    val mockUser = remember {
        UserProfile(
            id = "USR-1001",
            username = "Yash Shukla",
            email = "yash@skypulse.app",
            dateOfBirth = "1998-05-12",
            bio = "Stargazer & Software Engineer. Waiting for the Mars launch.",
            profilePictureUrl = "",
            spaceRank = "Commander 👨‍🚀",
            joinedDate = "Oct 2026"
        )
    }

    val mockOrders = remember {
        listOf(
            OrderHistoryItem("ORD-77A", "NASA Apollo Hoodie", "Oct 04, 2026", "$45.00", "Shipped"),
            OrderHistoryItem("ORD-77B", "James Webb Telescope Poster", "Sep 28, 2026", "$15.00", "Delivered")
        )
    }

    val userPosts = remember { mutableStateListOf<String>() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Orders", "Badges")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { /* Back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B2A))
            )
        },
        containerColor = Color(0xFF0D1B2A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Profile Header (Space Themed)
            ProfileHeader(mockUser)

            // 2. Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Edit Profile */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", color = Color.White, fontSize = 14.sp)
                }
            }

            // 3. Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 4. Content Area based on Tab
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> { // POSTS TAB
                        if (userPosts.isEmpty()) {
                            DinosaurEmptyState(onLaunchClick = {
                                userPosts.addAll(listOf("Orion Nebula", "My Telescope Setup", "Mars Rover Update"))
                            })
                        } else {
                            UserContentGrid(userPosts)
                        }
                    }
                    1 -> { // ORDERS TAB
                        Column(modifier = Modifier.padding(16.dp)) {
                            mockOrders.forEach { order ->
                                OrderItemRow(order)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    2 -> { // BADGES TAB
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(64.dp))
                            Text("Pioneer Badge Unlocked", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B2A47)),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user.username,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.spaceRank,
                    color = Color(0xFF4A90E2), // Space blue
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = user.email, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bio Section
        Text(
            text = user.bio,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
             Text(text = "DOB: ${user.dateOfBirth}", color = Color.Gray, fontSize = 12.sp)
             Text(text = "Joined: ${user.joinedDate}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun OrderItemRow(order: OrderHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(order.productName, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Order: ${order.orderId} • ${order.date}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(order.price, color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                Text(order.status, color = if(order.status == "Delivered") Color.Green else Color.Yellow, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DinosaurEmptyState(onLaunchClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔭", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No transmissions yet...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Share your first astrophotography or space thought.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLaunchClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))) {
                Text("Launch Your First Post")
            }
        }
    }
}

@Composable
fun UserContentGrid(posts: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().heightIn(min = 300.dp), // Need a min height inside Scrollable column
        contentPadding = PaddingValues(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(posts.size) { index ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                 Text(text = "🌌", fontSize = 32.sp)
            }
        }
    }
}
