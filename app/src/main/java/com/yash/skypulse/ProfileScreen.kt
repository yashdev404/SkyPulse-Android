package com.yash.skypulse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// Slate Onyx Theme Palette
private val SlateOnyxBackground = Color(0xFF080C14)
private val SurfaceLowest = Color(0xFF0C121D)
private val SurfaceLow = Color(0xFF151C26)
private val SurfaceContainerColor = Color(0xFF19202A)
private val SurfaceHigh = Color(0xFF232A34)
private val PrimaryCyan = Color(0xFF00F0FF)
private val OnPrimaryCyan = Color(0xFF00363A)
private val SecondaryCyan = Color(0xFF79D1FB)
private val TextWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFFDEE2F1)
private val TextMuted = Color(0xFF8B95A5)
private val TextDim = Color(0xFF43474E)
private val RoseRed = Color(0xFFF43F5E)

data class AvatarSticker(val name: String, val url: String)

data class EquipmentItem(
    val name: String,
    val category: String,
    val specs: String,
    val icon: ImageVector
)

data class BadgeItem(
    val title: String,
    val description: String,
    val tierInfo: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyPulseProfileScreen(
    authResponse: AuthResponse? = null,
    feedViewModel: HomeFeedViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    var userProfile by remember(authResponse) {
        mutableStateOf(
            UserProfile(
                id = authResponse?.userId ?: "YSH-9805",
                username = authResponse?.username ?: "yash",
                email = authResponse?.email ?: "yash@gmail.com",
                dateOfBirth = "1998-05-12",
                bio = "Deep Space Observer & Astrophysicist. Mapping faint transients across high-declination stellar fields.",
                profilePictureUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=400",
                spaceRank = "Commander Vance Thorne",
                joinedDate = "Oct 2026"
            )
        )
    }

    // Load saved profile details from TokenManager on launch
    LaunchedEffect(Unit) {
        val (savedBio, savedRank, savedAvatar) = tokenManager.getSavedProfileDetails()
        if (savedBio != null || savedRank != null || savedAvatar != null) {
            userProfile = userProfile.copy(
                bio = savedBio ?: userProfile.bio,
                spaceRank = savedRank ?: userProfile.spaceRank,
                profilePictureUrl = savedAvatar ?: userProfile.profilePictureUrl
            )
        }
    }

    val allFeedItems by feedViewModel.feedItems.collectAsState()
    val myUserPosts = remember(allFeedItems, userProfile.username) {
        allFeedItems.filter { it.author.equals(userProfile.username, ignoreCase = true) || it.author.contains("Vance", ignoreCase = true) }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Equipment, 2: Badges
    var showMenu by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showBadgeVault by remember { mutableStateOf(false) }

    if (showBadgeVault) {
        BadgeIntelVaultScreen(
            onBackClick = { showBadgeVault = false },
            commanderName = if (userProfile.spaceRank.contains("Vance", ignoreCase = true)) userProfile.spaceRank else "Commander Vanguard"
        )
    } else {
        Scaffold(
        topBar = {
            OnyxTopHeader(
                title = "Slate Onyx Variant",
                onMenuClick = { showMenu = true }
            )
        },
        containerColor = SlateOnyxBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Dropdown Menu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(end = 16.dp)
            ) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceContainerColor).border(1.dp, SurfaceHigh)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile & Avatar", color = TextPrimary, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            showEditProfileDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change Password", color = TextPrimary, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            showPasswordDialog = true
                        }
                    )
                    HorizontalDivider(color = SurfaceHigh)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = RoseRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Log Out", color = RoseRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onLogout()
                        }
                    )
                }
            }

            // 1. Slate Onyx Hero Section (Moonlit Ridge Background & Stats)
            SlateOnyxHeroHeader(
                user = userProfile,
                postsCount = if (myUserPosts.isNotEmpty()) myUserPosts.size else 24,
                onEditClick = { showEditProfileDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Telemetry Metrics Section (2x2 HUD Grid)
            SlateHUDMetricsSection(
                postsCount = if (myUserPosts.isNotEmpty()) myUserPosts.size else 24
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Segmented Navigation Tabs
            OnyxSegmentedTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Tab Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 110.dp)
            ) {
                when (selectedTab) {
                    0 -> { // MY POSTS TAB
                        ObservationLogFeed(posts = myUserPosts)
                    }
                    1 -> { // EQUIPMENT TAB
                        ActiveEquipmentPanel()
                    }
                    2 -> { // BADGES TAB
                        UnlockedBadgesGrid(onViewAllClick = { showBadgeVault = true })
                    }
                }
            }
        }

            if (showEditProfileDialog) {
                EditProfileDialog(
                    currentProfile = userProfile,
                    onDismiss = { showEditProfileDialog = false },
                    onSave = { updated ->
                        userProfile = updated
                        tokenManager.saveProfileDetails(updated.bio, updated.spaceRank, updated.profilePictureUrl)
                        scope.launch {
                            try {
                                BackendRetrofitInstance.api.updateProfile(updated)
                            } catch (e: Exception) {
                                println("Profile update backend API error: ${e.message}")
                            }
                        }
                        showEditProfileDialog = false
                    }
                )
            }

            if (showPasswordDialog) {
                ChangePasswordDialog(
                    username = userProfile.username,
                    onDismiss = { showPasswordDialog = false }
                )
            }
        }
    }
}

@Composable
fun OnyxTopHeader(title: String, onMenuClick: () -> Unit) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Tune, contentDescription = "Menu", tint = TextMuted)
            }
        }
    }
}

@Composable
fun SlateOnyxHeroHeader(
    user: UserProfile,
    postsCount: Int,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Moonlit Ridge Hero Image Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800",
                contentDescription = "Moonlit Landscape",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                SlateOnyxBackground.copy(alpha = 0.6f),
                                SlateOnyxBackground
                            )
                        )
                    )
            )

            // Centered Circular Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimaryCyan, CircleShape)
                    .background(SurfaceLowest),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = user.username,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Commander Title & Subtitle
        Text(
            text = if (user.spaceRank.contains("Vance", ignoreCase = true)) user.spaceRank else "Commander Vance Thorne",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sector Alpha • Clearance Level 4",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stat Counter Row (Posts, Tier 2 Rank, Gear)
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$postsCount", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Posts", color = TextMuted, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(SurfaceHigh)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = PrimaryCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("4", color = PrimaryCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                }
                Text("Tier 2", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Rank", color = TextMuted, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(SurfaceHigh)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("6", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Gear", color = TextMuted, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button: Edit Profile Pill
        Surface(
            color = PrimaryCyan,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .width(180.dp)
                .height(40.dp)
                .clickable { onEditClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Edit Profile",
                    color = OnPrimaryCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SlateHUDMetricsSection(postsCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Posts Published & Pioneer Rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("POSTS PUBLISHED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("$postsCount", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).height(40.dp).background(SurfaceHigh))

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("PIONEER RANK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Tier 2", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceHigh)
            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Active Equipment & Badges Unlocked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ACTIVE EQUIPMENT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("6", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).height(40.dp).background(SurfaceHigh))

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("BADGES UNLOCKED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("5", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OnyxSegmentedTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OnyxTabButton(
            title = "My Posts",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        OnyxTabButton(
            title = "Equipment",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
        OnyxTabButton(
            title = "Badges",
            isSelected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OnyxTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) PrimaryCyan else SurfaceLow,
        shape = RoundedCornerShape(50.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryCyan else SurfaceHigh),
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = if (isSelected) OnPrimaryCyan else SecondaryCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ObservationLogFeed(
    posts: List<FeedItem>
) {
    val samplePosts = remember(posts) {
        if (posts.isNotEmpty()) posts else listOf(
            FeedItem(
                id = "1",
                type = FeedType.POST,
                title = "Veil Nebula Capture",
                author = "Commander Vance Thorne",
                authorRole = "Sector Alpha Lead",
                timestamp = "2h",
                content = "Completed a three-hour narrowband capture of the Veil Nebula using the Celestron EdgeHD 1100 and ZWO ASI6200MM Pro setup. Seeing conditions were exceptional at 1.2 arcseconds.",
                imageUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800",
                likesCount = 42,
                commentsCount = 8
            ),
            FeedItem(
                id = "2",
                type = FeedType.POST,
                title = "EQ6-R Pro Calibration",
                author = "Commander Vance Thorne",
                authorRole = "Sector Alpha Lead",
                timestamp = "1d",
                content = "Calibrated the EQ6-R Pro tracking mount ahead of tonight's transit observations. Guiding error dropped to a steady 0.35 RMS.",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800",
                likesCount = 29,
                commentsCount = 5
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        samplePosts.forEach { post ->
            OnyxPostCard(post)
        }
    }
}

@Composable
fun OnyxPostCard(post: FeedItem) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableIntStateOf(post.likesCount) }
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceHigh, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.author, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${post.authorRole} • ${post.timestamp}", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Surface(
                    color = SurfaceHigh,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (post.id == "2") "EQUIPMENT" else "TELEMETRY",
                        color = SecondaryCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Hero Photo Frame
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceLowest)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Engagement Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            if (isLiked) likesCount++ else likesCount--
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) RoseRed else TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$likesCount", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.commentsCount}", color = TextWhite, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Share",
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { isBookmarked = !isBookmarked },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) PrimaryCyan else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Text
            Text(
                text = post.content,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun ActiveEquipmentPanel() {
    val equipmentList = listOf(
        EquipmentItem(
            name = "Celestron EdgeHD 1100",
            category = "Primary Optical Tube Assembly",
            specs = "Aperture 280mm • Focal Length 2800mm • Calibrated",
            icon = Icons.Default.Tag
        ),
        EquipmentItem(
            name = "ZWO ASI6200MM Pro",
            category = "Deep Sky Monochrome Camera",
            specs = "Full Frame • Back-Illuminated • Active Cooling",
            icon = Icons.Default.CameraAlt
        ),
        EquipmentItem(
            name = "Sky-Watcher EQ6-R Pro",
            category = "Computerized Equatorial Mount",
            specs = "Payload 20kg • Belt Driven • Connected",
            icon = Icons.Default.Radar
        ),
        EquipmentItem(
            name = "QHYCCD CFW3-L",
            category = "Motorized Filter Wheel",
            specs = "7-Position • 50mm Unmounted • Standby",
            icon = Icons.Default.Filter
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        equipmentList.forEach { equip ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceHigh, RoundedCornerShape(12.dp))
                    .clickable { },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(equip.icon, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(equip.name, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(equip.category, color = SecondaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(equip.specs, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UnlockedBadgesGrid(onViewAllClick: () -> Unit = {}) {
    val badges = listOf(
        BadgeItem(
            title = "Solar Flare",
            description = "M-class telemetry recorded.",
            tierInfo = "Tier 1 • Nov 12",
            icon = Icons.Default.WbSunny
        ),
        BadgeItem(
            title = "Pioneer Orbital",
            description = "100 sessions completed.",
            tierInfo = "Tier 2 • Nov 08",
            icon = Icons.Default.Radar
        ),
        BadgeItem(
            title = "Cartographer",
            description = "NGC 2237 mapped.",
            tierInfo = "Tier 3 • Oct 29",
            icon = Icons.Default.Shield
        ),
        BadgeItem(
            title = "Telemetry Relay",
            description = "99.9% uplink maintained.",
            tierInfo = "Tier 1 • Oct 15",
            icon = Icons.Default.Sensors
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OnyxBadgeCard(badge = badges[0], modifier = Modifier.weight(1f))
            OnyxBadgeCard(badge = badges[1], modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OnyxBadgeCard(badge = badges[2], modifier = Modifier.weight(1f))
            OnyxBadgeCard(badge = badges[3], modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onViewAllClick() }
        ) {
            Text(
                text = "See all 24 unlocked badges",
                color = PrimaryCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun OnyxBadgeCard(badge: BadgeItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, SurfaceHigh, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(badge.icon, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(badge.title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(badge.description, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(badge.tierInfo, color = SecondaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var newBio by remember { mutableStateOf(currentProfile.bio) }
    var newRank by remember { mutableStateOf(currentProfile.spaceRank) }
    var selectedAvatarUrl by remember { mutableStateOf(currentProfile.profilePictureUrl) }

    val presetStickerAvatars = listOf(
        AvatarSticker("Deep Space", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=400"),
        AvatarSticker("Earth Orbit", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=400"),
        AvatarSticker("Satellite", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=400"),
        AvatarSticker("Aurora", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLow,
        title = { Text("Edit Profile & Avatar", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select Space Avatar Sticker:", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(presetStickerAvatars) { sticker ->
                        val isSelected = selectedAvatarUrl == sticker.url
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(SurfaceLowest)
                                .border(2.dp, if (isSelected) PrimaryCyan else SurfaceHigh, CircleShape)
                                .clickable { selectedAvatarUrl = sticker.url },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = sticker.url,
                                contentDescription = sticker.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newRank,
                    onValueChange = { newRank = it },
                    label = { Text("Commander Display Rank", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newBio,
                    onValueChange = { newBio = it },
                    label = { Text("Bio", color = TextMuted) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentProfile.copy(bio = newBio, spaceRank = newRank, profilePictureUrl = selectedAvatarUrl))
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
            ) {
                Text("Save Changes", color = OnPrimaryCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

@Composable
fun ChangePasswordDialog(username: String, onDismiss: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLow,
        title = { Text("Change Password", color = TextWhite) },
        text = {
            Column {
                Text("Update credentials for $username", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                if (statusMessage != null) {
                    Text(statusMessage!!, color = if (statusMessage!!.contains("success", ignoreCase = true)) Color.Green else RoseRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
            ) {
                Text("Update Password", color = OnPrimaryCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}
