package dam_A51635.coolweatherapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val updateButton : Button = findViewById(R.id.btnUpdate)
        val latitudeText : EditText = findViewById(R.id.etLatitude)
        val longitudeText : EditText = findViewById(R.id.etLongitude)

        updateButton.setOnClickListener {
            var latitude: Float = latitudeText.text.toString().toFloat()
            var longitude = longitudeText.text.toString().toFloat()

            if(latitude in -90f..<90f && longitude in -180f..<180f)
                fetchWeatherData(latitude, longitude).start()
        }
    }


    private fun WeatherAPI_Call ( lat: Float , long : Float ) : WeatherData {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }

        val url = URL(reqString);
        url.openStream().use {
            val request = Gson().fromJson (InputStreamReader(it, "UTF-8"), WeatherData :: class.java )
            return request
        }
    }

    private fun fetchWeatherData (lat: Float , long : Float ) : Thread {
        return Thread {
            val weather = WeatherAPI_Call (lat , long )
            updateUI(weather, lat, long)
        }
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables", "DiscouragedApi")
    private fun updateUI (request : WeatherData, lat: Float, long: Float) {
        runOnUiThread {
            val weatherImage : ImageView = findViewById(R.id.weatherImage )


            val pressure : TextView = findViewById(R.id.pressureValue)
            val windSpeed : TextView = findViewById(R.id.windSpeedValue)
            val windDirection : TextView = findViewById(R.id.windDirectionValue)
            val temperature : TextView = findViewById(R.id.temperatureValue)
            val time : TextView = findViewById(R.id.timeValue)

            val pressureText = request.hourly.pressure_msl[12].toString()+ " hPa"
            val windSpeedText = request.current_weather.windspeed.toString()+ " km/H"
            val windDirectionText = request.current_weather.winddirection.toString()
            val temperatureText = request.current_weather.temperature.toString()+ " ºC"
            var timeText = request.current_weather.time


            pressure.text = pressureText
            windSpeed.text = windSpeedText
            windDirection.text = windDirectionText
            temperature.text = temperatureText
            time.text = timeText

            val hour = (request.current_weather.time[11].toString() +  request.current_weather.time[12].toString()).toInt()
            val day = hour in 8..<20

            val mapt = getWeatherCodeMap();
            val wCode = mapt[request.current_weather.weathercode]
            val wImage = when (wCode) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY ->if(day) wCode.image +"day" else
                    wCode.image +"night"
                else -> wCode?.image
            }

            val res = getResources()
            val resID = res.getIdentifier (wImage , "drawable", packageName);
            if (resID != 0){
                val drawable = this.getDrawable(resID);
                weatherImage.setImageDrawable(drawable);
            }
        }
    }
}