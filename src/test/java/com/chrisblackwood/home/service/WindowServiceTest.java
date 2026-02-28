package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowServiceTest {

    private final WindowService windowService =
            new WindowService(null, 8.0, 15.0);

    @Test
    void shouldOpenBrieflyWhenTonightIsCold() {
        ForecastResponse forecast = forecastWith(4.0);

        assertEquals(WindowDecision.OPEN_BRIEFLY,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenBrieflyAtExactBriefThreshold() {
        ForecastResponse forecast = forecastWith(8.0);

        assertEquals(WindowDecision.OPEN_BRIEFLY,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenBrieflyWhenTonightIsMild() {
        ForecastResponse forecast = forecastWith(12.0);

        assertEquals(WindowDecision.OPEN_BRIEFLY,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenAllNightWhenTonightIsWarm() {
        ForecastResponse forecast = forecastWith(18.0);

        assertEquals(WindowDecision.OPEN_ALL_NIGHT,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenAllNightAboveAllNightThreshold() {
        ForecastResponse forecast = forecastWith(16.0);

        assertEquals(WindowDecision.OPEN_ALL_NIGHT,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldKeepClosedWhenForecastHasNoTemperatures() {
        ForecastResponse forecast = new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Daily(List.of("2026-03-01"), List.of())
        );

        assertEquals(WindowDecision.KEEP_CLOSED,
                windowService.windowDecision(forecast));
    }

    private ForecastResponse forecastWith(double tonightLow) {
        return new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Daily(
                        List.of("2026-03-01", "2026-03-02"),
                        List.of(tonightLow, 10.0)
                )
        );
    }
}