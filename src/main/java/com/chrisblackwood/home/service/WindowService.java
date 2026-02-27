package com.chrisblackwood.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WindowService {

    private static final double LATITUDE = 51.543;
    private static final double LONGITUDE = -0.057;

    private final RestClient restClient;

    @Autowired
    public WindowService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String checkOvernightTemperature() {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=temperature_2m_min&timezone=Europe/London",
                LATITUDE,
                LONGITUDE
        );

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }
}
