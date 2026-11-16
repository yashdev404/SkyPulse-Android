package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yash.skypulse.ui.theme.CelestialPurple
import com.yash.skypulse.ui.theme.DeepSpaceBackground
import com.yash.skypulse.ui.theme.ElevatedCardSurface
import com.yash.skypulse.ui.theme.NeonCyan
import com.yash.skypulse.ui.theme.RoseRed
import com.yash.skypulse.ui.theme.SurfaceBorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeFeedViewModel : ViewModel() {
    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems

    init {
        fetchFeedFromBackend()
    }

    private fun fetchFeedFromBackend() {
        viewModelScope.launch {
            try {
                // 1. Try DummyJSON API first for live posts stream
                val dummyResponse = DummyJsonInstance.api.getPosts(limit = 20)
                if (dummyResponse.isSuccessful && dummyResponse.body() != null) {
                    val livePosts = dummyResponse.body()!!.posts.map { it.toFeedItem() }
                    _feedItems.value = livePosts
                    return@launch
                }
            } catch (e: Exception) {
                println("DummyJSON API unreachable for posts, trying custom backend: ${e.message}")
            }

            try {
                // 2. Fallback to Spring Boot custom backend
                val response = BackendRetrofitInstance.api.getHomeFeed()
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    _feedItems.value = response.body()!!
                    return@launch
                }
            } catch (e: Exception) {
                println("Backend API error: ${e.message}")
            }

            // 3. Fallback to curated local cosmic feed items if offline
            _feedItems.value = fallbackFeedItems
        }
    }

    private val fallbackFeedItems = listOf(
        FeedItem("1", FeedType.POST, "Perseid Meteor Shower Peak Observation", "Explorer", "Senior Astrophysicist", "2h ago", "Observed over 60 meteors per hour from Palomar Observatory! Clear sky conditions tonight.", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800", likesCount = 142, commentsCount = 18),
        FeedItem("2", FeedType.ARTICLE, "James Webb Space Telescope Captures Cosmic Ring", "AstroPulse Observatory", "Observatory Lead", "5h ago", "New infrared spectrum reveals unprecedented details of stellar dust rings.", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800", likesCount = 289, commentsCount = 45),
        FeedItem("3", FeedType.LIVE, "Artemis Lunar Lander Telemetry Live Stream", "Commander Marcus", "Mission Lead", "12h ago", "Live telemetry updates from lunar orbit position test.", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800", likesCount = 512, commentsCount = 89)
    )

    fun addPost(title: String, content: String, imageUrl: String?, authorName: String = "Explorer", authorRole: String = "Commander 👨‍🚀") {
        val newPost = FeedItem(
            id = System.currentTimeMillis().toString(),
            type = FeedType.POST,
            title = title,
            author = authorName,
            authorRole = authorRole,
            timestamp = "Just now",
            content = content,
            imageUrl = if (!imageUrl.isNullOrBlank()) imageUrl else null,
            likesCount = 1,
            commentsCount = 0,
            likedByMe = true
        )
        _feedItems.value = listOf(newPost) + _feedItems.value
    }

    fun toggleLike(itemId: String) {
        _feedItems.value = _feedItems.value.map { item ->
            if (item.id == itemId) {
                val newLikeStatus = !item.likedByMe
                val newCount = if (newLikeStatus) item.likesCount + 1 else item.likesCount - 1
                item.copy(likedByMe = newLikeStatus, likesCount = newCount)
            } else {
                item
            }
        }
    }
}

@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel = viewModel(),
    currentUsername: String = "Explorer"
) {
    val feedItems by viewModel.feedItems.collectAsState()
    var showCreatePostDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SkyPulse", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Cosmic Social Feed", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElevatedCardSurface)
                        .border(1.dp, SurfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀", fontSize = 22.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FeaturedStory()
                }

                items(feedItems) { item ->
                    FeedCard(item = item, onLikeClick = { viewModel.toggleLike(item.id) })
                }
            }
        }

        // Floating Action Button (FAB) to Create Post
        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            containerColor = CelestialPurple,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Post")
        }

        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onPublish = { title, content, imageUrl ->
                    viewModel.addPost(
                        title = title,
                        content = content,
                        imageUrl = imageUrl,
                        authorName = currentUsername
                    )
                    showCreatePostDialog = false
                }
            )
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, content: String, imageUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val presetImages = listOf(
        Triple("Earth", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800", "🌍"),
        Triple("Nebula", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800", "🌌"),
        Triple("Satellite", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800", "🛰️"),
        Triple("Aurora", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800", "✨")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedCardSurface,
        title = { Text("Create Post 📝", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Post Title", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Share your stargazing update...", color = Color.White.copy(alpha = 0.7f)) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Attach Space Photo:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetImages) { (name, url, emoji) ->
                        val isSelected = selectedImageUrl == url
                        Surface(
                            color = if (isSelected) CelestialPurple else DeepSpaceBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SurfaceBorder),
                            modifier = Modifier.clickable {
                                selectedImageUrl = if (isSelected) null else url
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(name, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onPublish(title.ifBlank { "Deep Space Update" }, content, selectedImageUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CelestialPurple)
            ) {
                Text("Publish Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun FeaturedStory() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedCardSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(CelestialPurple.copy(alpha = 0.5f), DeepSpaceBackground)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = RoseRed,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("LIVE FEED 🛰️", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Artemis Lunar Base Camp Setup", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("12.5k watching live from Earth orbit", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FeedCard(item: FeedItem, onLikeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedCardSurface)
    ) {
        Column {
            // Author Header Row
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CelestialPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.author.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(text = item.author, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${item.authorRole} • ${item.timestamp}", color = NeonCyan, fontSize = 11.sp)
                }
                IconButton(onClick = { /* Menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White.copy(alpha = 0.5f))
                }
            }

            // Post Content
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    color = CelestialPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = item.type.name,
                        color = CelestialPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(text = item.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                if (item.content.isNotBlank()) {
                    Text(
                        text = item.content,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        maxLines = 4,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                    )
                }
            }

            // Image / Media Card
            if (item.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (item.type == FeedType.VIDEO) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).size(48.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }

            // Engagement Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (item.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (item.likedByMe) RoseRed else Color.White
                        )
                    }
                    Text(text = item.likesCount.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Comment Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Open Comments */ }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = Color.White)
                    }
                    Text(text = item.commentsCount.toString(), color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Share Button
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }
    }
}
