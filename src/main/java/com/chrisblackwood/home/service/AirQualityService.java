package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.AirQualityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AirQualityService {

    private static final Logger log = LoggerFactory.getLogger(AirQualityService.class);

    private final double latitude;
    private final double longitude;
    private final String timezone;
    private final String domain;
    private final RestClient restClient;

    @Autowired
    public AirQualityService(
            RestClient.Builder restClientBuilder,
            @Value("${weather.latitude:#{null}}") Double latitude,
            @Value("${weather.longitude:#{null}}") Double longitude,
            @Value("${weather.timezone:}") String timezone,
            @Value("${air-quality.domain:}") String domain
    ) {
        String baseUrl = "https://air-quality-api.open-meteo.com/v1";
        this.latitude = requireCoordinate(latitude, "weather.latitude");
        this.longitude = requireCoordinate(longitude, "weather.longitude");
        this.timezone = requireText(timezone, "weather.timezone");
        this.domain = requireText(domain, "air-quality.domain");
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public AirQualityResponse getForecast() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/air-quality")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", "european_aqi")
                            .queryParam("domains", domain)
                            .queryParam("timezone", timezone)
                            .build())
                    .retrieve()
                    .body(AirQualityResponse.class);
        } catch (RestClientException exception) {
            log.error("Failed to fetch air quality from Open-Meteo");
            throw new IllegalStateException("Failed to fetch air quality from Open-Meteo", exception);
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
