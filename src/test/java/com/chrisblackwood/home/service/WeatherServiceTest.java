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
                (List.of("2026-03-01"), List.of(TEMPERATURE)));


        server.expect(requestTo(containsString("/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(LATITUDE)))
                .andExpect(queryParam("longitude", String.valueOf(LONGITUDE)))
                .andExpect(queryParam("daily", "temperature_2m_min"))
                .andExpect(queryParam("timezone", TIMEZONE))
                .andRespond(withSuccess(MAPPER.writeValueAsString(forecastResponse), MediaType.APPLICATION_JSON));

        ForecastResponse response = weatherService.getForecast();

        assertEquals(LATITUDE, response.latitude());
        assertEquals(LONGITUDE, response.longitude());
        assertEquals(TEMPERATURE, response.daily().temperature_2m_min().getFirst());

        server.verify();

    }
}
