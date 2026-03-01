package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.AirQualityResponse;
import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import com.chrisblackwood.home.dto.WindowRecommendation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WindowServiceTest {

    private final WindowService windowService =
            new WindowService(null, null, 3.0, 7.0, 11.0, 15.0, 18.0, 20.0, 2.0, 80.0, 1.0, 0.5, 3.0, 60.0);

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
                new ForecastResponse.Hourly(
                        List.of("2026-03-01T22:00", "2026-03-02T02:00", "2026-03-02T08:00"),
                        List.of(),
                        List.of(10.0, 9.0, 8.0),
                        List.of(60.0, 60.0, 60.0),
                        List.of(0.0, 0.0, 0.0)
                )
        );

        assertEquals(WindowDecision.KEEP_CLOSED,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldUseConfiguredThresholds() {
        WindowService customThresholdWindowService =
                new WindowService(null, null, 2.0, 6.0, 10.0, 14.0, 17.0, 18.0, 3.0, 75.0, 2.0, 0.3, 2.0, 60.0);
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
    void shouldBuildRecommendationPayload() {
        ForecastResponse forecast = forecastWith(15.0, 10.0, 85.0);
        WeatherService weatherService = mock(WeatherService.class);
        AirQualityService airQualityService = mock(AirQualityService.class);
        when(weatherService.getForecast()).thenReturn(forecast);
        when(airQualityService.getForecast()).thenReturn(airQualityWith(35.0));
        WindowService recommendationWindowService =
                new WindowService(weatherService, airQualityService, 3.0, 7.0, 11.0, 15.0, 18.0, 20.0, 2.0, 80.0, 1.0, 0.5, 3.0, 60.0);

        WindowRecommendation recommendation = recommendationWindowService.windowRecommendation();

        assertEquals(WindowDecision.OPEN_OVERNIGHT, recommendation.decision());
        assertEquals("Leave the windows open overnight", recommendation.message());
        assertEquals(15.0, recommendation.tonightLow());
        assertEquals(10.0, recommendation.maxWind());
        assertEquals(85.0, recommendation.meanHumidity());
        assertEquals(0.0, recommendation.rainSum());
        assertEquals(16.0, recommendation.effectiveNightLow());
        assertEquals(35.0, recommendation.maxEuropeanAqi());
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

    @Test
    void shouldAvoidOvernightOpeningWhenLightRainIsExpected() {
        ForecastResponse forecast = forecastWith(15.0, 10.0, 60.0, 1.0);

        assertEquals(WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldKeepClosedWhenHeavyRainIsExpected() {
        ForecastResponse forecast = forecastWith(15.0, 10.0, 60.0, 4.0);

        assertEquals(WindowDecision.KEEP_CLOSED,
                windowService.windowDecision(forecast));
    }

    @Test
    void shouldAvoidOvernightOpeningWhenAirQualityIsPoor() {
        ForecastResponse forecast = forecastWith(15.0, 10.0, 60.0, 0.0);
        AirQualityResponse airQuality = airQualityWith(65.0);

        assertEquals(WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE,
                windowService.windowDecision(forecast, airQuality));
    }

    @Test
    void shouldIgnoreConditionsOutsideTheSleepWindow() {
        ForecastResponse forecast = new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Hourly(
                        List.of("2026-03-01T20:00", "2026-03-01T22:00", "2026-03-02T02:00", "2026-03-02T08:00", "2026-03-02T09:00"),
                        List.of(1.0, 19.0, 20.0, 19.5, 1.0),
                        List.of(5.0, 8.0, 9.0, 7.0, 5.0),
                        List.of(50.0, 60.0, 60.0, 60.0, 50.0),
                        List.of(0.0, 0.0, 0.0, 0.0, 0.0)
                )
        );

        assertEquals(WindowDecision.OPEN_WIDE_OVERNIGHT,
                windowService.windowDecision(forecast));
    }

    private ForecastResponse forecastWith(double tonightLow) {
        return forecastWith(tonightLow, 10.0, 60.0, 0.0);
    }

    private ForecastResponse forecastWith(double tonightLow, double maxWind, double meanHumidity) {
        return forecastWith(tonightLow, maxWind, meanHumidity, 0.0);
    }

    private ForecastResponse forecastWith(double tonightLow, double maxWind, double meanHumidity, double rainSum) {
        double rainPerHour = rainSum / 5.0;

        return new ForecastResponse(
                48.51,
                2.17,
                new ForecastResponse.Hourly(
                        List.of(
                                "2026-03-01T21:00",
                                "2026-03-01T22:00",
                                "2026-03-01T23:00",
                                "2026-03-02T02:00",
                                "2026-03-02T05:00",
                                "2026-03-02T08:00",
                                "2026-03-02T09:00"
                        ),
                        List.of(20.0, tonightLow + 2.0, tonightLow + 1.0, tonightLow, tonightLow + 0.5, tonightLow + 1.5, 19.0),
                        List.of(4.0, maxWind - 2.0, maxWind, maxWind - 3.0, maxWind - 4.0, maxWind - 5.0, 4.0),
                        List.of(50.0, meanHumidity, meanHumidity, meanHumidity, meanHumidity, meanHumidity, 50.0),
                        List.of(0.0, rainPerHour, rainPerHour, rainPerHour, rainPerHour, rainPerHour, 0.0)
                )
        );
    }

    private AirQualityResponse airQualityWith(double overnightMaxEuropeanAqi) {
        return new AirQualityResponse(
                48.51,
                2.17,
                new AirQualityResponse.Hourly(
                        List.of(
                                "2026-03-01T21:00",
                                "2026-03-01T22:00",
                                "2026-03-01T23:00",
                                "2026-03-02T02:00",
                                "2026-03-02T05:00",
                                "2026-03-02T08:00",
                                "2026-03-02T09:00"
                        ),
                        List.of(30.0, overnightMaxEuropeanAqi - 5.0, overnightMaxEuropeanAqi, overnightMaxEuropeanAqi - 10.0, overnightMaxEuropeanAqi - 15.0, overnightMaxEuropeanAqi - 20.0, 25.0)
                )
        );
    }
}
