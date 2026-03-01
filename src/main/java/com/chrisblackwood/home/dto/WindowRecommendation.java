package com.chrisblackwood.home.dto;

public record WindowRecommendation(
        WindowDecision decision,
        String message,
        Double tonightLow,
        Double maxWind,
        Double meanHumidity,
        Double rainSum,
        Double effectiveNightLow,
        Double maxEuropeanAqi
) {
}
