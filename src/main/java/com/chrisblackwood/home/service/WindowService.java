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

    @Autowired
    public WindowService(
            WeatherService weatherService,
            @Value("${window.five-minute-vent-max-temp}") double fiveMinuteVentMaxTemp,
            @Value("${window.ten-minute-vent-max-temp}") double tenMinuteVentMaxTemp,
            @Value("${window.ten-to-fifteen-minute-vent-and-crack-max-temp}") double tenToFifteenMinuteVentAndCrackMaxTemp,
            @Value("${window.crack-overnight-max-temp}") double crackOvernightMaxTemp,
            @Value("${window.open-overnight-max-temp}") double openOvernightMaxTemp
    ) {
        this.weatherService = weatherService;
        this.fiveMinuteVentMaxTemp = fiveMinuteVentMaxTemp;
        this.tenMinuteVentMaxTemp = tenMinuteVentMaxTemp;
        this.tenToFifteenMinuteVentAndCrackMaxTemp = tenToFifteenMinuteVentAndCrackMaxTemp;
        this.crackOvernightMaxTemp = crackOvernightMaxTemp;
        this.openOvernightMaxTemp = openOvernightMaxTemp;
    }

    public WindowDecision windowDecision() {
        return windowDecision(weatherService.getForecast());
    }

    public WindowRecommendation windowRecommendation() {
        WindowDecision decision = windowDecision();
        return new WindowRecommendation(decision, windowMessage(decision));
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
        if (tonightLow == null) {
            return WindowDecision.KEEP_CLOSED;
        }

        if (tonightLow <= fiveMinuteVentMaxTemp) {
            return WindowDecision.OPEN_FIVE_MINUTES_THEN_CLOSE;
        }

        if (tonightLow <= tenMinuteVentMaxTemp) {
            return WindowDecision.OPEN_TEN_MINUTES_THEN_CLOSE;
        }

        if (tonightLow <= tenToFifteenMinuteVentAndCrackMaxTemp) {
            return WindowDecision.OPEN_TEN_TO_FIFTEEN_MINUTES_THEN_CRACK_ONE_CM;
        }

        if (tonightLow <= crackOvernightMaxTemp) {
            return WindowDecision.CRACK_ONE_TO_THREE_CM_OVERNIGHT;
        }

        if (tonightLow <= openOvernightMaxTemp) {
            return WindowDecision.OPEN_OVERNIGHT;
        }

        return WindowDecision.OPEN_WIDE_OVERNIGHT;
    }

    private Double getTonightLow(ForecastResponse forecastResponse) {
        if (forecastResponse == null || forecastResponse.daily() == null) {
            return null;
        }

        List<Double> nightlyMinimums = forecastResponse.daily().temperature_2m_min();
        if (nightlyMinimums == null || nightlyMinimums.isEmpty()) {
            return null;
        }

        return nightlyMinimums.getFirst();
    }
}
