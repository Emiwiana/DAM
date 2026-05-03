package com.example.cooljettpackweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cooljettpackweatherapp.data.WeatherApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    fun updateLatitude(newLatitude: Float) {
        newLatitude.coerceIn(-90f, 90f)
        _uiState.update { currentState -> currentState.copy(latitude = newLatitude) }
    }

    fun updateLongitude(newLongitude: Float) {
        newLongitude.coerceIn(-180f, 180f)
        _uiState.update { currentState -> currentState.copy(longitude = newLongitude) }
    }

    fun fetchWeather() {
        val longitude = _uiState.value.longitude
        val latitude = _uiState.value.latitude

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val weather = WeatherApiClient.getWeather(latitude, longitude)
            if (weather != null) {
                _uiState.update { currentState -> currentState.copy(
                    temperature = weather.current_weather.temperature,
                    windspeed = weather.current_weather.windspeed,
                    winddirection = weather.current_weather.winddirection,
                    weathercode = weather.current_weather.weathercode,
                    seaLevelPressure = weather.hourly.pressure_msl[12].toFloat(),
                    time = weather.current_weather.time
                ) }
                updateDay()
            }
        }
    }

    private fun updateDay(){
        val time = _uiState.value.time
        val hour = (time[11].toString() +  time[12].toString()).toInt()
        _uiState.update { currentState -> currentState.copy(isDay = hour in 6..18) }
    }
}
