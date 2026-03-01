package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import com.chrisblackwood.home.dto.WindowRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class WindowService {

    private static final int BEDTIME_HOUR = 22;
    private static final int WAKE_HOUR = 8;

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
        OvernightMetrics overnightMetrics = overnightMetrics(forecastResponse);
        Double tonightLow = overnightMetrics.lowTemperature();
        Double maxWind = overnightMetrics.maxWind();
        Double meanHumidity = overnightMetrics.meanHumidity();
        Double rainSum = overnightMetrics.rainSum();
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
        OvernightMetrics overnightMetrics = overnightMetrics(forecastResponse);
        Double tonightLow = overnightMetrics.lowTemperature();
        Double maxWind = overnightMetrics.maxWind();
        Double meanHumidity = overnightMetrics.meanHumidity();
        Double rainSum = overnightMetrics.rainSum();
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

    private OvernightMetrics overnightMetrics(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.hourly() == null || forecastResponse.hourly().time() == null) {
            return OvernightMetrics.empty();
        }

        ForecastResponse.Hourly hourly = forecastResponse.hourly();
        LocalDateTime windowStart = findFirstBedtime(hourly.time());
        if (windowStart == null) {
            return OvernightMetrics.empty();
        }

        LocalDateTime windowEnd = windowStart.toLocalDate().plusDays(1).atTime(WAKE_HOUR, 0);
        Double lowestTemperature = null;
        Double maxWind = null;
        double humiditySum = 0.0;
        int humidityCount = 0;
        double rainSum = 0.0;
        int rainCount = 0;

        for (int index = 0; index < hourly.time().size(); index++) {
            LocalDateTime timestamp = parseTimestamp(hourly.time().get(index));
            if (timestamp == null || timestamp.isBefore(windowStart) || timestamp.isAfter(windowEnd)) {
                continue;
            }

            Double temperature = valueAt(hourly.temperature_2m(), index);
            if (temperature != null && (lowestTemperature == null || temperature < lowestTemperature)) {
                lowestTemperature = temperature;
            }

            Double wind = valueAt(hourly.wind_speed_10m(), index);
            if (wind != null && (maxWind == null || wind > maxWind)) {
                maxWind = wind;
            }

            Double humidity = valueAt(hourly.relative_humidity_2m(), index);
            if (humidity != null) {
                humiditySum += humidity;
                humidityCount++;
            }

            Double rain = valueAt(hourly.rain(), index);
            if (rain != null) {
                rainSum += rain;
                rainCount++;
            }
        }

        return new OvernightMetrics(
                lowestTemperature,
                maxWind,
                humidityCount == 0 ? null : humiditySum / humidityCount,
                rainCount == 0 ? null : rainSum
        );
    }

    private LocalDateTime findFirstBedtime(List<String> timestamps) {
        for (String timestampValue : timestamps) {
            LocalDateTime timestamp = parseTimestamp(timestampValue);
            if (timestamp != null && timestamp.getHour() == BEDTIME_HOUR) {
                return timestamp;
            }
        }

        return null;
    }

    private LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private Double valueAt(List<Double> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }

        return values.get(index);
    }

    private void validateThresholds() {
        if (!(fiveMinuteVentMaxTemp <= tenMinuteVentMaxTemp
                && tenMinuteVentMaxTemp <= tenToFifteenMinuteVentAndCrackMaxTemp
                && tenToFifteenMinuteVentAndCrackMaxTemp <= crackOvernightMaxTemp
                && crackOvernightMaxTemp <= openOvernightMaxTemp)) {
            throw new IllegalStateException("Window temperature thresholds must be ordered ascending");
        }
    }

    private record OvernightMetrics(
            Double lowTemperature,
            Double maxWind,
            Double meanHumidity,
            Double rainSum
    ) {
        private static OvernightMetrics empty() {
            return new OvernightMetrics(null, null, null, null);
        }
    }
}
