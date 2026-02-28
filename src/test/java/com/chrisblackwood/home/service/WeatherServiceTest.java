package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.ForecastResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class WeatherServiceTest {
    private MockRestServiceServer server;
    private WeatherService weatherService;

    private final double LATITUDE = 48.51;
    private final double LONGITUDE = 2.17;
    private final double TEMPERATURE = 2.5;
    private final double WIND_SPEED = 18.0;
    private final double HUMIDITY = 76.0;
    private final String TIMEZONE = "Europe/Paris";

    private final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        weatherService = new WeatherService(builder, LATITUDE, LONGITUDE, TIMEZONE) ;
    }

    @Test
    void shouldGetForecast() throws Exception {
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Daily
                (List.of("2026-03-01"), List.of(TEMPERATURE), List.of(WIND_SPEED), List.of(HUMIDITY)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("daily", "temperature_2m_min,wind_speed_10m_max,relative_humidity_2m_mean"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(LATITUDE, response.latitude());
        assertEquals(LONGITUDE, response.longitude());
        assertEquals(TEMPERATURE, response.daily().temperature_2m_min().getFirst());
        assertEquals(WIND_SPEED, response.daily().wind_speed_10m_max().getFirst());
        assertEquals(HUMIDITY, response.daily().relative_humidity_2m_mean().getFirst());

        server.verify();
    }

    @Test
    void shouldGetFirstNightForecast() throws Exception {
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Daily
                (List.of("2026-03-01", "2026-03-02"), List.of(TEMPERATURE, 2.6), List.of(WIND_SPEED, 12.0), List.of(HUMIDITY, 55.0)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("daily", "temperature_2m_min,wind_speed_10m_max,relative_humidity_2m_mean"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(TEMPERATURE, response.daily().temperature_2m_min().getFirst());
        assertEquals(WIND_SPEED, response.daily().wind_speed_10m_max().getFirst());
        assertEquals(HUMIDITY, response.daily().relative_humidity_2m_mean().getFirst());

        server.verify();
    }

    @Test
    void shouldHandleEmptyTemperatureList() throws Exception {
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Daily
                (List.of("2026-03-01", "2026-03-02"), List.of(), List.of(WIND_SPEED, 12.0), List.of(HUMIDITY, 55.0)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("daily", "temperature_2m_min,wind_speed_10m_max,relative_humidity_2m_mean"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(0, response.daily().temperature_2m_min().size());

        server.verify();
    }
}
