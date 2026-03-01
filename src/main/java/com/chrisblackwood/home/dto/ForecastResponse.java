package com.chrisblackwood.home.dto;

import java.util.List;

public record ForecastResponse(
        double latitude,
        double longitude,
        Hourly hourly
) {
    public record Hourly(
            List<String> time,
            List<Double> temperature_2m,
            List<Double> wind_speed_10m,
            List<Double> relative_humidity_2m,
            List<Double> rain
    ) {}
}
