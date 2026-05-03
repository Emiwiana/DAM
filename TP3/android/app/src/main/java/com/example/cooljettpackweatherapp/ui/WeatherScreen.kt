package com.example.cooljettpackweatherapp.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cooljettpackweatherapp.data.WMO_WeatherCode
import com.example.cooljettpackweatherapp.data.getWeatherCodeMap
import com.example.cooljettpackweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherUI ( weatherViewModel : WeatherViewModel = viewModel()) {
    val weatherUIState by weatherViewModel.uiState.collectAsState()
    val latitude = weatherUIState.latitude
    val longitude = weatherUIState.longitude
    val temperature = weatherUIState.temperature
    val windSpeed = weatherUIState.windspeed
    val windDirection = weatherUIState.winddirection
    val weathercode = weatherUIState.weathercode
    val seaLevelPressure = weatherUIState.seaLevelPressure
    val time = weatherUIState.time
    val day = weatherUIState.isDay

    val configuration = LocalConfiguration.current

    val mapt = getWeatherCodeMap ();
    val wImage = when (val wCode = mapt[weathercode]) {
        WMO_WeatherCode.CLEAR_SKY ,
        WMO_WeatherCode.MAINLY_CLEAR ,
        WMO_WeatherCode.PARTLY_CLOUDY -> if (day) wCode.image + "day"
        else wCode.image + "night"
        else -> wCode ?. image
    }
    val context = LocalContext.current
    val wIcon = context . resources . getIdentifier (wImage , "drawable", context . packageName )

    if ( configuration . orientation == Configuration. ORIENTATION_LANDSCAPE ) {
        LandscapeWeatherUI(
            wIcon ,
            latitude ,
            longitude ,
            temperature ,
            windSpeed ,
            windDirection ,
            weathercode ,
            seaLevelPressure ,
            time ,
            onLatitudeChange = {
                    newValue -> newValue.toFloatOrNull()?.let {
                weatherViewModel.updateLatitude(it) }
            },
            onLongitudeChange = {
                    newValue -> newValue.toFloatOrNull()?.let {
                weatherViewModel.updateLongitude(it) }
            },
            onUpdateButtonClick = {
                weatherViewModel.fetchWeather()
            }
        )
    } else {
        PortraitWeatherUI (
            wIcon ,
            latitude ,
            longitude ,
            temperature ,
            windSpeed ,
            windDirection ,
            weathercode ,
            seaLevelPressure ,
            time ,
            onLatitudeChange = {
                    newValue ->
                newValue . toFloatOrNull () ?. let {
                    weatherViewModel . updateLatitude (it) }
            },
            onLongitudeChange = {
                    newValue ->
                newValue . toFloatOrNull () ?. let {
                    weatherViewModel . updateLongitude (it) }
            },
            onUpdateButtonClick = {
                weatherViewModel.fetchWeather()
            }
        )
    }
}

@Composable
fun PortraitWeatherUI(
    wIcon : Int ,
    latitude : Float ,
    longitude : Float ,
    temperature : Float ,
    windSpeed : Float ,
    windDirection : Int ,
    weathercode : Int ,
    seaLevelPressure : Float ,
    time : String ,
    onLatitudeChange : ( String ) -> Unit ,
    onLongitudeChange : ( String ) -> Unit ,
    onUpdateButtonClick : () -> Unit ,
) {
    TODO("Not yet implemented")
}

@Composable
fun LandscapeWeatherUI(
    wIcon : Int ,
    latitude : Float ,
    longitude : Float ,
    temperature : Float ,
    windSpeed : Float ,
    windDirection : Int ,
    weathercode : Int ,
    seaLevelPressure : Float ,
    time : String ,
    onLatitudeChange : ( String ) -> Unit ,
    onLongitudeChange : ( String ) -> Unit ,
    onUpdateButtonClick : () -> Unit ,
) {
    TODO("Not yet implemented")
}
