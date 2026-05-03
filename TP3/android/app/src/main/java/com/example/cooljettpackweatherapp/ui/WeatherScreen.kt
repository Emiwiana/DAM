package com.example.cooljettpackweatherapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cooljettpackweatherapp.R
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
    val wIcon = context.resources.getIdentifier (wImage, "drawable", context . packageName )

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeatherIcon(wIcon)

        CoordinatesCard(
            latitude = latitude,
            longitude = longitude,
            onLatitudeChange = onLatitudeChange,
            onLongitudeChange = onLongitudeChange
        )

        WeatherDetailsCard(
            seaLevelPressure = seaLevelPressure,
            windDirection = windDirection,
            windSpeed = windSpeed,
            temperature = temperature,
            time = time
        )

        Spacer(modifier = Modifier.weight(1f))

        UpdateButton(onUpdateButtonClick = onUpdateButtonClick)
    }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIcon(
                wIcon = wIcon,
                modifier = Modifier.weight(1f)
            )

            CoordinatesCard(
                latitude = latitude,
                longitude = longitude,
                onLatitudeChange = onLatitudeChange,
                onLongitudeChange = onLongitudeChange,
                modifier = Modifier.weight(1.5f)
            )

            WeatherDetailsCard(
                seaLevelPressure = seaLevelPressure,
                windDirection = windDirection,
                windSpeed = windSpeed,
                temperature = temperature,
                time = time,
                modifier = Modifier.weight(1.5f)
            )
        }

        UpdateButton(
            onUpdateButtonClick = onUpdateButtonClick,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun CoordinatesCard(
    latitude: Float,
    longitude: Float,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.coordinates_text), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                //Icon(imageVector = Icons.Default.Public, contentDescription = "Globe Icon")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = latitude.toString(),
                onValueChange = onLatitudeChange,
                label = { Text(stringResource(R.string.latitude_text)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = longitude.toString(),
                onValueChange = onLongitudeChange,
                label = { Text(stringResource(R.string.longitude_text)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun WeatherIcon(wIcon: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = wIcon),
        contentDescription = "Weather Condition Icon",
        modifier = modifier
            .size(160.dp)
            .padding(16.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun WeatherDetailsCard(
    seaLevelPressure: Float,
    windDirection: Int,
    windSpeed: Float,
    temperature: Float,
    time: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(label = stringResource(R.string.pressure_text), value = "$seaLevelPressure hPa")
            DetailRow(label = stringResource(R.string.wind_text), value = "$windDirection°")
            DetailRow(label = stringResource(R.string.wind_speed_text), value = "$windSpeed km/h")
            DetailRow(label = stringResource(R.string.temperature_text), value = "$temperature°C")
            DetailRow(label = stringResource(R.string.time_text), value = time)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(text = value, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun UpdateButton(onUpdateButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onUpdateButtonClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(text = stringResource(R.string.update_weather_string), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
