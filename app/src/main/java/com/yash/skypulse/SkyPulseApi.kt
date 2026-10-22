package com.yash.skypulse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SkyPulseApi {
    
    // Auth Endpoints (Spring Security + JWT)
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Calls the Spring Boot FeedController
    @GET("/api/v1/feed")
    suspend fun getHomeFeed(): Response<List<FeedItem>>
    
    // Calls the Spring Boot ShopController (Fetch Products)
    @GET("/api/v1/shop/products")
    suspend fun getProducts(): Response<List<Product>>

    // Calls the Spring Boot ShopController (Checkout)
    @POST("/api/v1/shop/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Response<Any>

    // Calls the Spring Boot ChatController (Fetch Persistent Messages from MongoDB)
    @GET("/api/v1/rooms/{roomId}/messages")
    suspend fun getRoomHistory(@Path("roomId") roomId: String): Response<List<ChatMessage>>
}