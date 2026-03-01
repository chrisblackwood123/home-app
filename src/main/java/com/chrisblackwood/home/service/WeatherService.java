package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final double latitude;
    private final double longitude;
    private final String timezone;

    private final RestClient restClient;

    @Autowired
    public WeatherService(RestClient.Builder restClientBuilder, @Value("${weather.latitude:#{null}}") Double latitude,
                          @Value("${weather.longitude:#{null}}") Double longitude,
                          @Value("${weather.timezone:}") String timezone) {
        String baseUrl = "https://api.open-meteo.com/v1";
        this.latitude = requireCoordinate(latitude, "weather.latitude");
        this.longitude = requireCoordinate(longitude, "weather.longitude");
        this.timezone = requireText(timezone, "weather.timezone");
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public ForecastResponse getForecast() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", "temperature_2m,wind_speed_10m,relative_humidity_2m,rain")
                            .queryParam("timezone", timezone)
                            .build())
                    .retrieve()
                    .body(ForecastResponse.class);
        } catch (RestClientException exception) {
            log.error("Failed to fetch forecast from Open-Meteo");
            throw new IllegalStateException("Failed to fetch forecast from Open-Meteo", exception);
        }
    }

    private double requireCoordinate(Double value, String propertyName) {
        if (value == null) {
            throw new IllegalStateException(propertyName + " must be configured");
        }

        return value;
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured");
        }

        return value;
    }
}
