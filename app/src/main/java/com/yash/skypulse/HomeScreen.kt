package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yash.skypulse.ui.theme.SkyPulseTheme
import com.yash.skypulse.ui.theme.WeatherState
import com.yash.skypulse.ui.theme.WeatherViewModel

@Composable
fun HomeScreen(viewModel: WeatherViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchWeather("Delhi")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEEB),
                        Color(0xFF4682B4),
                        Color(0xFF0D1B2A),
                    ),
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search Region", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchText.isNotBlank()) {
                                viewModel.fetchWeather(searchText)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is WeatherState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is WeatherState.Success -> {
                    WeatherDetails(state.weather, state.forecast)
                }
                is WeatherState.Error -> {
                    BarrenMarsErrorScreen(
                        message = state.message,
                        onRetry = {
                            if (searchText.isNotBlank()) {
                                viewModel.fetchWeather(searchText)
                            } else {
                                viewModel.fetchWeather("Delhi")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(weather: WeatherResponse, forecast: ForecastResponse) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Current Weather Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "📍 ${weather.name}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${weather.main.temp.toInt()}°C",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = weather.weather[0].description.replaceFirstChar { it.uppercase() },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(label = "Humidity", value = "${weather.main.humidity}%", icon = "💧")
                WeatherDetailItem(label = "Condition", value = weather.weather[0].description, icon = "☁️")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Forecast Section
        Text(
            text = "Next Few Days",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter forecast to show one entry per day
        val dailyForecast = forecast.list.filterIndexed { index, _ -> (index % 8) == 0 }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(dailyForecast) { item ->
                ForecastCard(item)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cosmic Events Placeholder
        CosmicEventsCard()
    }
}

@Composable
fun CosmicEventsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("🔭", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cosmic Events",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Mock Event 1
        EventItem(
            title = "Perseid Meteor Shower",
            date = "Tonight, 2:00 AM",
            description = "Peak visibility expected in clear skies."
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Mock Event 2
        EventItem(
            title = "Super Blue Moon",
            date = "Tomorrow, 8:45 PM",
            description = "The largest and brightest moon of the year."
        )
    }
}

@Composable
fun EventItem(title: String, date: String, description: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = date, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun ForecastCard(item: ForecastItem) {
    val date = item.dt_txt.substring(5, 10)
    val time = item.dt_txt.substring(11, 16)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = date, color = Color.White, fontSize = 14.sp)
            Text(text = time, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "🌤️", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${item.main.temp.toInt()}°C", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarrenMarsErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF8B3A3A)), // Deep rusty red for Mars
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Visual element: A simple representation of Mars/Error
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCD5C5C)), // Lighter red
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏜️", 
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connection Lost",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We've lost the signal to the weather satellites. \n$message",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF8B3A3A)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Transmission", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SkyPulseTheme {
        HomeScreen()
    }
}
