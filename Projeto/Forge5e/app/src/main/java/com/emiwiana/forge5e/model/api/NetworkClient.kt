package com.emiwiana.forge5e.model.api
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object that manages network configuration and provides access to API services.
 */
object NetworkClient {
    private const val BASE_URL = "https://www.dnd5eapi.co/"

    /**
     * JSON configuration for Kotlinx Serialization.
     * Configured to ignore unknown keys and coerce input values for better stability with external APIs.
     */
    private val jsonConfiguration = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Configured OkHttpClient with logging and network timeouts.
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Primary Retrofit instance for the D&D 5e API.
     */
    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonConfiguration.asConverterFactory(contentType))
            .build()
    }

    /**
     * Singleton instance of the [SrdApiService].
     */
    val srdApiService: SrdApiService by lazy {
        retrofit.create(SrdApiService::class.java)
    }
}
