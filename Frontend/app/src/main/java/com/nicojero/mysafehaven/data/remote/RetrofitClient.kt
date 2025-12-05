package com.nicojero.mysafehaven.data.remote

import com.nicojero.mysafehaven.data.local.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.76:5050/" // Para emulador Android
    // private const val BASE_URL = "http://localhost:5050/" // Para dispositivo físico usa tu IP local

    fun createApiService(authDataStore: AuthDataStore): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking {
                authDataStore.token.first()
            }

            val requestBuilder = chain.request().newBuilder()

            // Si hay token, agregarlo al header
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}