package com.chrisblackwood.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {

    private final RestClient restClient;

    @Value("${pushover.token:}")
    private String token;

    @Value("${pushover.user:}")
    private String user;

    @Autowired
    public NotificationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String sendNotification() {

        String jsonRequest = String.format("""
            {"token": "%s", "user": "%s", "message": "Hi Chris"}
            """, token, user);

        return restClient.post()
                .uri("https://api.pushover.net/1/messages.json")
                .body(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
}
