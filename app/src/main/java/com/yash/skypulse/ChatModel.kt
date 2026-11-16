package com.yash.skypulse

// This matches the JSON we will return from Spring Boot: /api/v1/rooms
data class ChatRoom(
    val id: String,
    val rfCode: String = "RF-01",
    val name: String,
    val description: String,
    val activeUsers: Int,
    val emojiIcon: String,
    val isPrivate: Boolean = false,
    val avatarType: String = "mars" // "mars", "nebula", "jwst", "alien", "meteorite", "orbit"
)

// This matches the JSON we will send/receive over Spring Boot WebSockets
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val roomId: String,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isMe: Boolean // Handled locally on Android to determine bubble side
)
