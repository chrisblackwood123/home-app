package com.chrisblackwood.home.dto;

import java.util.List;

public record AirQualityResponse(
        double latitude,
        double longitude,
        Hourly hourly
) {
    public record Hourly(
            List<String> time,
            List<Double> european_aqi
    ) {}
}
