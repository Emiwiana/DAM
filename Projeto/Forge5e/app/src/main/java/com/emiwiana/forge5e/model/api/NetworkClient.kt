package com.emiwiana.forge5e.model.api
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://www.dnd5eapi.co/"

    // 1. Configure the JSON deserializer
    private val jsonConfiguration = Json {
        ignoreUnknownKeys = true // Crucial: ignores unmapped JSON properties from the API
        coerceInputValues = true // Uses default property values if JSON returns invalid/mismatched values
    }

    // 2. Configure OkHttpClient with logging and network timeouts
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Logs request and response lines, headers, and bodies (ideal for development debug logs)
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // 3. Build the primary Retrofit instance
    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonConfiguration.asConverterFactory(contentType))
            .build()
    }

    // 4. Provide a public singleton access point to your endpoints
    val srdApiService: SrdApiService by lazy {
        retrofit.create(SrdApiService::class.java)
    }
}