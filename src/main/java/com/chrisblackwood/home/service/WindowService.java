package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import com.chrisblackwood.home.dto.WindowRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WindowService {

    private final WeatherService weatherService;
    private final double fiveMinuteVentMaxTemp;
    private final double tenMinuteVentMaxTemp;
    private final double tenToFifteenMinuteVentAndCrackMaxTemp;
    private final double crackOvernightMaxTemp;
    private final double openOvernightMaxTemp;
    private final double strongWindThreshold;
    private final double strongWindCoolingAdjustment;
    private final double highHumidityThreshold;
    private final double highHumidityWarmingAdjustment;
    private final double lightRainThreshold;
    private final double heavyRainThreshold;

    @Autowired
    public WindowService(
            WeatherService weatherService,
            @Value("${window.five-minute-vent-max-temp}") double fiveMinuteVentMaxTemp,
            @Value("${window.ten-minute-vent-max-temp}") double tenMinuteVentMaxTemp,
            @Value("${window.ten-to-fifteen-minute-vent-and-crack-max-temp}") double tenToFifteenMinuteVentAndCrackMaxTemp,
            @Value("${window.crack-overnight-max-temp}") double crackOvernightMaxTemp,
            @Value("${window.open-overnight-max-temp}") double openOvernightMaxTemp,
            @Value("${window.strong-wind-threshold}") double strongWindThreshold,
            @Value("${window.strong-wind-cooling-adjustment}") double strongWindCoolingAdjustment,
            @Value("${window.high-humidity-threshold}") double highHumidityThreshold,
            @Value("${window.high-humidity-warming-adjustment}") double highHumidityWarmingAdjustment,
            @Value("${window.light-rain-threshold}") double lightRainThreshold,
            @Value("${window.heavy-rain-threshold}") double heavyRainThreshold
    ) {
        this.weatherService = weatherService;
        this.fiveMinuteVentMaxTemp = fiveMinuteVentMaxTemp;
        this.tenMinuteVentMaxTemp = tenMinuteVentMaxTemp;
        this.tenToFifteenMinuteVentAndCrackMaxTemp = tenToFifteenMinuteVentAndCrackMaxTemp;
        this.crackOvernightMaxTemp = crackOvernightMaxTemp;
        this.openOvernightMaxTemp = openOvernightMaxTemp;
        this.strongWindThreshold = strongWindThreshold;
        this.strongWindCoolingAdjustment = strongWindCoolingAdjustment;
        this.highHumidityThreshold = highHumidityThreshold;
        this.highHumidityWarmingAdjustment = highHumidityWarmingAdjustment;
        this.lightRainThreshold = lightRainThreshold;
        this.heavyRainThreshold = heavyRainThreshold;
        validateThresholds();
    }

    public WindowDecision windowDecision() {
        return windowDecision(weatherService.getForecast());
    }

    public WindowRecommendation windowRecommendation() {
        ForecastResponse forecastResponse = weatherService.getForecast();
        Double tonightLow = getTonightLow(forecastResponse);
        Double maxWind = getTonightMaxWind(forecastResponse);
        Double meanHumidity = getTonightMeanHumidity(forecastResponse);
        Double rainSum = getTonightRainSum(forecastResponse);
        Double effectiveNightLow = tonightLow == null ? null : effectiveNightLow(tonightLow, maxWind, meanHumidity);
        WindowDecision decision = windowDecision(effectiveNightLow, rainSum);
        return new WindowRecommendation(
                decision,
                windowMessage(decision),
                tonightLow,
                maxWind,
                meanHumidity,
                rainSum,
                effectiveNightLow
        );
    }

    public String windowMessage(WindowDecision decision) {
        if (decision == null) {
            return "Keep the windows closed tonight";
        }

        return switch (decision) {
            case OPEN_FIVE_MINUTES_THEN_CLOSE ->
                    "Open the windows for 5 minutes before bed, then close them fully overnight";
            case OPEN_TEN_MINUTES_THEN_CLOSE ->
                    "Open the windows for 10 minutes before bed, then close them overnight";
            case OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM ->
                    "Open the windows for 10-15 minutes before bed, then leave them slightly cracked (1cm) overnight";
            case CRACK_ONE_TO_THREE_CM_OVERNIGHT ->
                    "Leave the windows cracked (1-3cm) overnight";
            case OPEN_OVERNIGHT ->
                    "Leave the windows open overnight";
            case OPEN_WIDE_OVERNIGHT ->
                    "Open the windows wide overnight";
            case KEEP_CLOSED -> "Keep the windows closed tonight";
            default -> "Keep the windows closed tonight";
        };
    }

    WindowDecision windowDecision(ForecastResponse forecastResponse) {
        Double tonightLow = getTonightLow(forecastResponse);
        Double maxWind = getTonightMaxWind(forecastResponse);
        Double meanHumidity = getTonightMeanHumidity(forecastResponse);
        Double rainSum = getTonightRainSum(forecastResponse);
        Double effectiveNightLow = tonightLow == null ? null : effectiveNightLow(tonightLow, maxWind, meanHumidity);
        return windowDecision(effectiveNightLow, rainSum);
    }

    private WindowDecision windowDecision(Double effectiveNightLow, Double rainSum) {
        if (effectiveNightLow == null) {
            return WindowDecision.KEEP_CLOSED;
        }

        if (rainSum != null && rainSum >= heavyRainThreshold) {
            return WindowDecision.KEEP_CLOSED;
        }

        WindowDecision baseDecision = temperatureBandDecision(effectiveNightLow);

        if (rainSum != null && rainSum >= lightRainThreshold && opensOvernight(baseDecision)) {
            return WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE;
        }

        return baseDecision;
    }

    private double effectiveNightLow(double tonightLow, Double maxWind, Double meanHumidity) {
        double adjustedNightLow = tonightLow;

        if (maxWind != null && maxWind >= strongWindThreshold) {
            adjustedNightLow -= strongWindCoolingAdjustment;
        }

        if (meanHumidity != null && meanHumidity >= highHumidityThreshold) {
            adjustedNightLow += highHumidityWarmingAdjustment;
        }

        return adjustedNightLow;
    }

    private WindowDecision temperatureBandDecision(double effectiveNightLow) {
        if (effectiveNightLow <= fiveMinuteVentMaxTemp) {
            return WindowDecision.OPEN_FIVE_MINUTES_THEN_CLOSE;
        }

        if (effectiveNightLow <= tenMinuteVentMaxTemp) {
            return WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE;
        }

        if (effectiveNightLow <= tenToFifteenMinuteVentAndCrackMaxTemp) {
            return WindowDecision.OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM;
        }

        if (effectiveNightLow <= crackOvernightMaxTemp) {
            return WindowDecision.CRACK_ONE_TO_THREE_CM_OVERNIGHT;
        }

        if (effectiveNightLow <= openOvernightMaxTemp) {
            return WindowDecision.OPEN_OVERNIGHT;
        }

        return WindowDecision.OPEN_WIDE_OVERNIGHT;
    }

    private boolean opensOvernight(WindowDecision decision) {
        return decision == WindowDecision.OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM
                || decision == WindowDecision.CRACK_ONE_TO_THREE_CM_OVERNIGHT
                || decision == WindowDecision.OPEN_OVERNIGHT
                || decision == WindowDecision.OPEN_WIDE_OVERNIGHT;
    }

    private Double getTonightLow(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.daily() == null) {
            return null;
        }

        return getFirstDailyValue(forecastResponse.daily().temperature_2m_min());
    }

    private Double getTonightMaxWind(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.daily() == null) {
            return null;
        }

        return getFirstDailyValue(forecastResponse.daily().wind_speed_10m_max());
    }

    private Double getTonightMeanHumidity(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.daily() == null) {
            return null;
        }

        return getFirstDailyValue(forecastResponse.daily().relative_humidity_2m_mean());
    }

    private Double getTonightRainSum(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.daily() == null) {
            return null;
        }

        return getFirstDailyValue(forecastResponse.daily().rain_sum());
    }

    private Double getFirstDailyValue(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.getFirst();
    }

    private void validateThresholds() {
        if (!(fiveMinuteVentMaxTemp <= tenMinuteVentMaxTemp
                && tenMinuteVentMaxTemp <= tenToFifteenMinuteVentAndCrackMaxTemp
                && tenToFifteenMinuteVentAndCrackMaxTemp <= crackOvernightMaxTemp
                && crackOvernightMaxTemp <= openOvernightMaxTemp)) {
            throw new IllegalStateException("Window temperature thresholds must be ordered ascending");
        }
    }
}
