package com.yash.skypulse

import retrofit2.Response
import retrofit2.http.GET

interface SkyPulseApi {
    
    // Calls the Spring Boot FeedController
    @GET("/api/v1/feed")
    suspend fun getHomeFeed(): Response<List<FeedItem>>
    
    // Calls the Spring Boot ShopController
    @GET("/api/v1/shop/products")
    suspend fun getProducts(): Response<List<Product>>
}