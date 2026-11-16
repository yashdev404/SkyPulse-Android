package com.yash.skypulse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DummyJsonApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 30
    ): Response<DummyProductResponse>

    @GET("posts")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20
    ): Response<DummyPostResponse>
}
