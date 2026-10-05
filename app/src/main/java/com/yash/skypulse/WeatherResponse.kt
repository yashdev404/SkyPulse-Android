package com.yash.skypulse

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class City(
    val name: String
)
