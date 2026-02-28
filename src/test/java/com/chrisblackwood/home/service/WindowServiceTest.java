package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowServiceTest {

    private final WindowService windowService =
            new WindowService(null, 3.0, 7.0, 11.0, 15.0, 18.0, 20.0, 2.0, 80.0, 1.0);

    @Test
    void shouldOpenFiveMinutesThenCloseWhenTonightIsZeroToThreeDegrees() {
        ForecastResponse forecast = forecastWith(3.0);

        assertEquals(WindowDecision.OPEN_FIVE_MINUTES_THEN_CLOSE,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenTenMinutesThenCloseWhenTonightIsFourToSevenDegrees() {
        ForecastResponse forecast = forecastWith(7.0);

        assertEquals(WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenTenToFifteenMinutesThenCrackWhenTonightIsEightToElevenDegrees() {
        ForecastResponse forecast = forecastWith(11.0);

        assertEquals(WindowDecision.OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldCrackOneToThreeCentimetersOvernightWhenTonightIsTwelveToFifteenDegrees() {
        ForecastResponse forecast = forecastWith(15.0);

        assertEquals(WindowDecision.CRACK_ONE_TO_THREE_CM_OVERNIGHT,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenOvernightWhenTonightIsSixteenToEighteenDegrees() {
        ForecastResponse forecast = forecastWith(18.0);

        assertEquals(WindowDecision.OPEN_OVERNIGHT,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldOpenWideOvernightWhenTonightIsNineteenDegreesOrHigher() {
        ForecastResponse forecast = forecastWith(19.0);

        assertEquals(WindowDecision.OPEN_WIDE_OVERNIGHT,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldKeepClosedWhenForecastHasNoTemperatures() {
        ForecastResponse forecast = new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Daily(List.of("2026-03-01"), List.of(), List.of(10.0), List.of(60.0))
        );

        assertEquals(WindowDecision.KEEP_CLOSED,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldUseConfiguredThresholds() {
        WindowService customThresholdWindowService =
                new WindowService(null, 2.0, 6.0, 10.0, 14.0, 17.0, 18.0, 3.0, 75.0, 2.0);
        ForecastResponse forecast = forecastWith(18.0);

        assertEquals(WindowDecision.OPEN_WIDE_OVERNIGHT,
                customThresholdWindowService.windowDecision(forecast));
    }

    @Test
    void shouldBuildMessageFromDecision() {
        assertEquals("Leave the windows cracked (1-3cm) overnight",
                windowService.windowMessage(WindowDecision.CRACK_ONE_TO_THREE_CM_OVERNIGHT));
    }

    @Test
    void shouldBeMoreConservativeWhenWindIsStrong() {
        ForecastResponse forecast = forecastWith(12.0, 24.0, 60.0);

        assertEquals(WindowDecision.OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldBeMoreAggressiveWhenHumidityIsHigh() {
        ForecastResponse forecast = forecastWith(15.0, 10.0, 85.0);

        assertEquals(WindowDecision.OPEN_OVERNIGHT,
                windowService.windowDecision(forecast));
    }

    private ForecastResponse forecastWith(double tonightLow) {
        return forecastWith(tonightLow, 10.0, 60.0);
    }

    private ForecastResponse forecastWith(double tonightLow, double maxWind, double meanHumidity) {
        return new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Daily(
                        List.of("2026-03-01", "2026-03-02"),
                        List.of(tonightLow, 10.0),
                        List.of(maxWind, 10.0),
                        List.of(meanHumidity, 55.0)
                )
        );
    }
}
