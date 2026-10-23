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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyPulseProfileScreen(
    authResponse: AuthResponse? = null,
    onLogout: () -> Unit = {}
) {
    val userProfile = remember(authResponse) {
        if (authResponse != null) {
            UserProfile(
                id = authResponse.userId,
                username = authResponse.username,
                email = authResponse.email,
                dateOfBirth = "1998-05-12",
                bio = "Commander of SkyPulse Station",
                profilePictureUrl = "",
                spaceRank = "Commander 👨‍🚀",
                joinedDate = "Oct 2026"
            )
        } else {
            UserProfile(
                id = "USR-1001",
                username = "Explorer",
                email = "explorer@skypulse.app",
                dateOfBirth = "2000-01-01",
                bio = "Stargazer & Software Engineer.",
                profilePictureUrl = "",
                spaceRank = "Cadet 🚀",
                joinedDate = "Oct 2026"
            )
        }
    }

    val realOrders = remember { mutableStateListOf<OrderResponse>() }

    // Fetch real PostgreSQL orders whenever Profile Screen opens
    LaunchedEffect(userProfile.username) {
        try {
            val response = BackendRetrofitInstance.api.getUserOrders(userProfile.username)
            if (response.isSuccessful && response.body() != null) {
                realOrders.clear()
                realOrders.addAll(response.body()!!)
            }
        } catch (e: Exception) {
            println("Failed to fetch user orders: ${e.message}")
        }
    }

    val userPosts = remember { mutableStateListOf<String>() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Orders", "Badges")

    var showPasswordDialog by remember { mutableStateOf(false) }

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
            ProfileHeader(userProfile)

            // 2. Action Buttons (Edit Profile, Change Password, & Logout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Password 🔑", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Log Out", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            if (realOrders.isEmpty()) {
                                Text(
                                    text = "No orders launched yet. Visit the Cosmic Shop!",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                realOrders.forEach { order ->
                                    OrderItemRow(order)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
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

    if (showPasswordDialog) {
        ChangePasswordDialog(
            username = userProfile.username,
            onDismiss = { showPasswordDialog = false }
        )
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
fun OrderItemRow(order: OrderResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF4A90E2))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${order.items.size} Space Item(s)", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Order ID: ${order.id.take(8)}...", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format(java.util.Locale.US, "%.2f", order.totalAmount)}", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                Text(order.status, color = if(order.status == "PAID") Color.Green else Color.Yellow, fontSize = 12.sp)
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

@Composable
fun ChangePasswordDialog(username: String, onDismiss: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B2A47),
        title = { Text("Change Password", color = Color.White) },
        text = {
            Column {
                Text("Update credentials for $username", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                if (statusMessage != null) {
                    Text(statusMessage!!, color = if (statusMessage!!.contains("success", ignoreCase = true)) Color.Green else Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = BackendRetrofitInstance.api.changePassword(
                                ChangePasswordRequest(username, oldPassword, newPassword)
                            )
                            if (response.isSuccessful) {
                                statusMessage = "Password updated successfully!"
                            } else {
                                statusMessage = "Failed: Invalid current password."
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}
