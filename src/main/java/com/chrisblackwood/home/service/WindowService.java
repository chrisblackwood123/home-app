package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.chrisblackwood.home.dto.WindowDecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WindowService {

    private final WeatherService weatherService;
    private final double openAllNightMaxTemp;
    private final double openBrieflyMaxTemp;

    @Autowired
    public WindowService(
            WeatherService weatherService,
            @Value("${window.open-all-night-max-temp}") double openAllNightMaxTemp,
            @Value("${window.open-briefly-max-temp}") double openBrieflyMaxTemp
    ) {
        this.weatherService = weatherService;
        this.openAllNightMaxTemp = openAllNightMaxTemp;
        this.openBrieflyMaxTemp = openBrieflyMaxTemp;
    }

    public WindowDecision windowDecision() {
        return windowDecision(weatherService.getForecast());
    }

    WindowDecision windowDecision(ForecastResponse forecastResponse) {
        Double tonightLow = getTonightLow(forecastResponse);
        if (tonightLow == null) {
            return WindowDecision.KEEP_CLOSED;
        }

        if (tonightLow < openBrieflyMaxTemp) {
            return WindowDecision.OPEN_BRIEFLY;
        }

        if (tonightLow > openAllNightMaxTemp) {
            return WindowDecision.OPEN_ALL_NIGHT;
        }

        return WindowDecision.OPEN_BRIEFLY;
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
