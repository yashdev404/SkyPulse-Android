package com.yash.skypulse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendRetrofitInstance {
    
    // 10.0.2.2 is the magic IP that lets the Android Emulator talk to your computer's localhost!
    private const val BASE_URL = "http://10.0.2.2:8080"

    private var retrofitApi: SkyPulseApi? = null

    fun getApi(context: android.content.Context): SkyPulseApi {
        if (retrofitApi == null) {
            val tokenManager = TokenManager(context)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .build()

            retrofitApi = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SkyPulseApi::class.java)
        }
        return retrofitApi!!
    }

    // Fallback property for direct access
    val api: SkyPulseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkyPulseApi::class.java)
    }
}