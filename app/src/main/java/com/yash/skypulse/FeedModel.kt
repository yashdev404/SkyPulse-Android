package com.yash.skypulse

enum class FeedType { VIDEO, ARTICLE, POST, LIVE }

// Data Class for Feed Post Items
data class FeedItem(
    val id: String,
    val type: FeedType,
    val title: String,
    val author: String,
    val authorRole: String = "Space Enthusiast",
    val timestamp: String,
    val content: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedByMe: Boolean = false
)

// DummyJSON API Response Models for Posts
data class DummyPostResponse(
    val posts: List<DummyPostDto>,
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)

data class DummyPostDto(
    val id: Int,
    val title: String,
    val body: String,
    val userId: Int = 1,
    val tags: List<String> = emptyList(),
    val reactions: DummyReactions? = null,
    val views: Int = 0
)

data class DummyReactions(
    val likes: Int = 0,
    val dislikes: Int = 0
)

// Sample Space Images for visual enrichment
private val cosmicImageUrls = listOf(
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800", // Earth from space
    "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800", // Deep night sky
    "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800", // Satellite
    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800", // Aurora
    "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800"  // Nebula
)

private val authorNames = listOf(
    "Dr. Elena Vance", "Commander Marcus", "AstroPulse Observatory",
    "Sarah Jenkins", "Apollo Labs", "Dr. Alan Grant", "Nova Research"
)

private val authorRoles = listOf(
    "Senior Astrophysicist", "Mission Commander", "Observatory Lead",
    "Deep Space Photographer", "Space Systems Engineer", "Cosmologist"
)

// Mapper Function to convert DummyJSON DTO to SkyPulse FeedItem
fun DummyPostDto.toFeedItem(): FeedItem {
    val authorIndex = id % authorNames.size
    val imageIndex = id % cosmicImageUrls.size
    val likes = reactions?.likes ?: ((id * 17) % 250 + 12)
    val isVideo = (id % 4 == 0)

    return FeedItem(
        id = id.toString(),
        type = if (isVideo) FeedType.VIDEO else if (id % 3 == 0) FeedType.ARTICLE else FeedType.POST,
        title = title,
        author = authorNames[authorIndex],
        authorRole = authorRoles[authorIndex],
        timestamp = "${(id % 12) + 1}h ago",
        content = body,
        imageUrl = if (id % 2 == 0 || isVideo) cosmicImageUrls[imageIndex] else null,
        videoUrl = if (isVideo) "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" else null,
        likesCount = likes,
        commentsCount = (likes * 0.2).toInt() + 1,
        likedByMe = false
    )
}
