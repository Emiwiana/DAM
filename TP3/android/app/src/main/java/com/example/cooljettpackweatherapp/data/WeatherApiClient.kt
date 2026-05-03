package com.example.cooljettpackweatherapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


object WeatherApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getWeather(lat: Float, lon: Float): WeatherData? {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${lon}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }
        println("Getting URL: $reqString")

        return try {
            client.get(reqString).body<WeatherData>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
