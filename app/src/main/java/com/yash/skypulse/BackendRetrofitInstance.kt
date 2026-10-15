package com.yash.skypulse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendRetrofitInstance {
    
    // 10.0.2.2 is the magic IP that lets the Android Emulator talk to your computer's localhost!
    private const val BASE_URL = "http://10.0.2.2:8080"

    val api: SkyPulseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkyPulseApi::class.java)
    }
}