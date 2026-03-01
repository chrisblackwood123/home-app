package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.AirQualityResponse;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AirQualityServiceTest {

    private static final String DOMAIN = "cams_europe";

    private MockRestServiceServer server;
    private AirQualityService airQualityService;

    private final double latitude = 48.51;
    private final double longitude = 2.17;
    private final double europeanAqi = 42.0;
    private final String timezone = "Europe/Paris";

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        airQualityService = new AirQualityService(builder, latitude, longitude, timezone, DOMAIN);
    }

    @Test
    void shouldGetAirQualityForecast() throws Exception {
        AirQualityResponse airQualityResponse = new AirQualityResponse(
                latitude,
                longitude,
                new AirQualityResponse.Hourly(List.of("2026-03-01T22:00"), List.of(europeanAqi))
        );

        server.expect(requestTo(containsString("/air-quality")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("latitude", String.valueOf(latitude)))
                .andExpect(queryParam("longitude", String.valueOf(longitude)))
                .andExpect(queryParam("hourly", "european_aqi"))
                .andExpect(queryParam("domains", DOMAIN))
                .andExpect(queryParam("timezone", timezone))
                .andRespond(withSuccess(mapper.writeValueAsString(airQualityResponse), MediaType.APPLICATION_JSON));

        AirQualityResponse response = airQualityService.getForecast();

        assertEquals(latitude, response.latitude());
        assertEquals(longitude, response.longitude());
        assertEquals(europeanAqi, response.hourly().european_aqi().getFirst());

        server.verify();
    }

    @Test
    void shouldFailFastWhenDomainMissing() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new AirQualityService(RestClient.builder(), latitude, longitude, timezone, "")
        );

        assertEquals("air-quality.domain must be configured", exception.getMessage());
    }
}
