package com.chrisblackwood.home.dto;

import java.util.List;

public record ForecastResponse(
        double latitude,
        double longitude,
        Daily daily
) {
    public record Daily(
            List<String> time,
            List<Double> temperature_2m_min,
            List<Double> wind_speed_10m_max,
            List<Double> relative_humidity_2m_mean
    ) {}
}
