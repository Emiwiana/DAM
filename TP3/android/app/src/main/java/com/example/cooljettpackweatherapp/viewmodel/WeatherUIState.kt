package com.example.cooljettpackweatherapp.viewmodel

data class WeatherUIState (
    val longitude: Float = 0f,
    val latitude: Float = 0f,
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Int = 0,
    val weathercode: Int = 0,
    val seaLevelPressure: Float = 0f,
    val time: String = "",
    val isDay: Boolean = true
)