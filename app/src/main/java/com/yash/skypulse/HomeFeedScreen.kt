package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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

enum class FeedType { VIDEO, ARTICLE, POST, LIVE }

data class FeedItem(
    val id: String,
    val type: FeedType,
    val title: String,
    val author: String,
    val timestamp: String,
    val content: String = "",
    val imageUrl: String? = null
)

class HomeFeedViewModel : ViewModel() {
    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems

    init {
        _feedItems.value = listOf(
            FeedItem("1", FeedType.LIVE, "Live: James Webb New Discovery", "NASA", "2m ago", imageUrl = "https://developer.android.com/static/images/brand/android-logo.png"),
            FeedItem("2", FeedType.ARTICLE, "Water Found on Mars Surface", "SpaceX", "1h ago", content = "Detailed analysis of recent rover data reveals potential subsurface water...", imageUrl = "https://developer.android.com/static/images/brand/android-logo.png"),
            FeedItem("3", FeedType.VIDEO, "The Future of Mars Colonies", "Elon Musk", "3h ago", imageUrl = "https://developer.android.com/static/images/brand/android-logo.png"),
            FeedItem("4", FeedType.POST, "Check out this beautiful nebula!", "Astronomy Daily", "5h ago", content = "Captured this from my backyard telescope last night. The colors are insane.", imageUrl = "https://developer.android.com/static/images/brand/android-logo.png")
        )
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SkyPulse", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Cosmic Feed", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 20.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeaturedStory()
            }
            
            items(feedItems) { item ->
                FeedCard(item)
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
            // Gradient placeholder
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
                Text("NASA Mars Mission Updates", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("12.5k watching now", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FeedCard(item: FeedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            if (item.imageUrl != null) {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
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
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.type.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = item.timestamp, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                
                Text(text = item.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                
                if (item.content.isNotBlank()) {
                    Text(
                        text = item.content,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
                    Text(text = item.author, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}
