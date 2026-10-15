package com.yash.skypulse

// This matches the JSON we will return from Spring Boot: /api/v1/users/me
data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val dateOfBirth: String,
    val bio: String,
    val profilePictureUrl: String,
    val spaceRank: String, // e.g., "Astronaut", "Explorer", "Commander"
    val joinedDate: String
)

// Represents a mock order from the Shop
data class OrderHistoryItem(
    val orderId: String,
    val productName: String,
    val date: String,
    val price: String,
    val status: String
)
