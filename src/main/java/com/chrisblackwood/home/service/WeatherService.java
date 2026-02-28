package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

    private final double latitude;
    private final double longitude;
    private final String timezone;

    private final RestClient restClient;

    @Autowired
    public WeatherService(RestClient.Builder restClientBuilder, @Value("${weather.latitude}") double latitude,
                          @Value("${weather.longitude}") double longitude, @Value("${weather.timezone}") String timezone) {
        String base_url = "https://api.open-meteo.com/v1";
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.restClient = restClientBuilder.baseUrl(base_url).build();
    }

    public ForecastResponse getForecast() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("daily", "temperature_2m_min,wind_speed_10m_max,relative_humidity_2m_mean")
                        .queryParam("timezone", timezone)
                        .build())
                .retrieve()
                .body(ForecastResponse.class);
    }
}
