package com.yash.skypulse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SkyPulseApi {
    
    // Auth Endpoints (Spring Security + JWT)
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @PUT("/api/v1/auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<String>

    @PUT("/api/v1/user/profile")
    suspend fun updateProfile(@Body profile: UserProfile): Response<UserProfile>

    // Feed Endpoints
    @GET("/api/v1/feed")
    suspend fun getHomeFeed(): Response<List<FeedItem>>

    @POST("/api/v1/feed/posts")
    suspend fun createFeedPost(@Body post: FeedItem): Response<FeedItem>
    
    // Shop Endpoints
    @GET("/api/v1/shop/products")
    suspend fun getProducts(): Response<List<Product>>

    @POST("/api/v1/shop/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Response<Any>

    @GET("/api/v1/shop/orders/{username}")
    suspend fun getUserOrders(@Path("username") username: String): Response<List<OrderResponse>>

    // Comms Channels Endpoints
    @GET("/api/v1/rooms/{roomId}/messages")
    suspend fun getRoomHistory(@Path("roomId") roomId: String): Response<List<ChatMessage>>
}