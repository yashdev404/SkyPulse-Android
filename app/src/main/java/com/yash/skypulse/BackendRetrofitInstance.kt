package com.yash.skypulse

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendRetrofitInstance {
    
    private const val BASE_URL = "http://10.0.2.2:8080"
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        if (tokenManager == null) {
            tokenManager = TokenManager(context.applicationContext)
        }
    }

    val api: SkyPulseApi by lazy {
        val okHttpClientBuilder = OkHttpClient.Builder()
        
        // If tokenManager is initialized, attach AuthInterceptor!
        tokenManager?.let {
            okHttpClientBuilder.addInterceptor(AuthInterceptor(it))
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkyPulseApi::class.java)
    }
}