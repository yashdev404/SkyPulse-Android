package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope
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
                // Call the Spring Boot API
                val response = BackendRetrofitInstance.api.getHomeFeed()
                if (response.isSuccessful && response.body() != null) {
                    _feedItems.value = response.body()!!
                } else {
                    println("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Network Error: ${e.message}")
            }
        }
    }

    // This method will eventually make a Retrofit call to POST /api/v1/feed/{id}/like
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
fun HomeFeedScreen(viewModel: HomeFeedViewModel = viewModel()) {
    val feedItems by viewModel.feedItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // Deep space background
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SkyPulse", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Cosmic Feed", color = Color(0xFF4A90E2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 22.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp), // Extra bottom padding for floating nav
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
}

@Composable
fun FeaturedStory() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient placeholder for Featured Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF4682B4), Color(0xFF0D1B2A))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("LIVE", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Artemis Base Camp Setup", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("12.5k watching now", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FeedCard(item: FeedItem, onLikeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A47)) // Slightly lighter than background
    ) {
        Column {
            // User / Author Info Row
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF4A90E2)), contentAlignment = Alignment.Center) {
                    Text(item.author.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(text = item.author, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${item.authorRole} • ${item.timestamp}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                IconButton(onClick = { /* Menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White.copy(alpha = 0.5f))
                }
            }

            // Post Content
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = item.type.name,
                    color = Color(0xFF4A90E2),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(text = item.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                if (item.content.isNotBlank()) {
                    Text(
                        text = item.content,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 3,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }

            // Optional Image/Video
            if (item.imageUrl != null) {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (item.type == FeedType.VIDEO) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).size(50.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
            
            // Engagement Row (Like, Comment, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (item.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (item.likedByMe) Color.Red else Color.White
                        )
                    }
                    Text(text = item.likesCount.toString(), color = Color.White, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Comment Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Open Comments */ }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = Color.White)
                    }
                    Text(text = item.commentsCount.toString(), color = Color.White, fontSize = 14.sp)
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
