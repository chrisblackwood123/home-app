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
    private final double RAIN_SUM = 0.4;
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
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Hourly
                (List.of("2026-03-01T22:00"), List.of(TEMPERATURE), List.of(WIND_SPEED), List.of(HUMIDITY), List.of(RAIN_SUM)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("hourly", "temperature_2m,wind_speed_10m,relative_humidity_2m,rain"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(LATITUDE, response.latitude());
        assertEquals(LONGITUDE, response.longitude());
        assertEquals(TEMPERATURE, response.hourly().temperature_2m().getFirst());
        assertEquals(WIND_SPEED, response.hourly().wind_speed_10m().getFirst());
        assertEquals(HUMIDITY, response.hourly().relative_humidity_2m().getFirst());
        assertEquals(RAIN_SUM, response.hourly().rain().getFirst());

        server.verify();
    }

    @Test
    void shouldGetFirstNightForecast() throws Exception {
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Hourly
                (List.of("2026-03-01T22:00", "2026-03-01T23:00"), List.of(TEMPERATURE, 2.6), List.of(WIND_SPEED, 12.0), List.of(HUMIDITY, 55.0), List.of(RAIN_SUM, 1.2)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("hourly", "temperature_2m,wind_speed_10m,relative_humidity_2m,rain"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(TEMPERATURE, response.hourly().temperature_2m().getFirst());
        assertEquals(WIND_SPEED, response.hourly().wind_speed_10m().getFirst());
        assertEquals(HUMIDITY, response.hourly().relative_humidity_2m().getFirst());
        assertEquals(RAIN_SUM, response.hourly().rain().getFirst());

        server.verify();
    }

    @Test
    void shouldHandleEmptyTemperatureList() throws Exception {
        ForecastResponse forecastResponse = new ForecastResponse(LATITUDE, LONGITUDE, new ForecastResponse.Hourly
                (List.of("2026-03-01T22:00", "2026-03-01T23:00"), List.of(), List.of(WIND_SPEED, 12.0), List.of(HUMIDITY, 55.0), List.of(RAIN_SUM, 1.2)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("hourly", "temperature_2m,wind_speed_10m,relative_humidity_2m,rain"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(0, response.hourly().temperature_2m().size());

        server.verify();
    }
}
