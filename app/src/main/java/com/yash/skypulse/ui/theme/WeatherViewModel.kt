package com.yash.skypulse.ui.theme

import com.yash.skypulse.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yash.skypulse.ForecastResponse
import com.yash.skypulse.RetrofitInstance
import com.yash.skypulse.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(
        val weather : WeatherResponse,
        val forecast : ForecastResponse
    ) : WeatherState()
    data class Error(val message :String ): WeatherState()
}

class WeatherViewModel : ViewModel() {
    // This holds the data internally
    private val _uiState = MutableStateFlow<WeatherState>(WeatherState.Loading)

    // This lets the UI see the data but not change it
    val uiState: StateFlow<WeatherState> = _uiState

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherState.Loading
            try {
                val apiKey = BuildConfig.WEATHER_API_KEY

                val weatherResp = RetrofitInstance.api.getWeather(city, apiKey)
                val forecastResp = RetrofitInstance.api.getForecast(city, apiKey)

                if (weatherResp.isSuccessful && forecastResp.isSuccessful) {
                    val weather = weatherResp.body()
                    val forecast = forecastResp.body()

                    if (weather != null && forecast != null) {
                        _uiState.value = WeatherState.Success(weather, forecast)
                    } else {
                        _uiState.value = WeatherState.Error("Empty data received")
                    }
                } else {
                    _uiState.value = WeatherState.Error("Failed to fetch: ${weatherResp.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = WeatherState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
