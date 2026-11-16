package com.yash.skypulse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DummyJsonInstance {
    private const val BASE_URL = "https://dummyjson.com/"

    val api: DummyJsonApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DummyJsonApi::class.java)
    }
}
