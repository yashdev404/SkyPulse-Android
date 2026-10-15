package com.yash.skypulse

enum class FeedType { VIDEO, ARTICLE, POST, LIVE }

// This maps to the MongoDB Document we will fetch via Spring Boot: /api/v1/feed
data class FeedItem(
    val id: String,
    val type: FeedType,
    val title: String,
    val author: String,
    val authorRole: String = "Space Enthusiast",
    val timestamp: String,
    val content: String = "",
    val imageUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedByMe: Boolean = false
)
