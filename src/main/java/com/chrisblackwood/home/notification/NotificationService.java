package com.chrisblackwood.home.notification;

import com.chrisblackwood.home.dto.PushoverRequest;
import com.chrisblackwood.home.dto.PushoverResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RestClient restClient;
    private final String token;
    private final String user;

    @Autowired
    public NotificationService(RestClient.Builder restClientBuilder,
                               @Value("${pushover.token:}") String token, @Value("${pushover.user:}") String user) {
        String baseUrl = "https://api.pushover.net/1";
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.token = requireText(token, "pushover.token");
        this.user = requireText(user, "pushover.user");
    }

    public PushoverResponse sendNotification(String message) {

        PushoverRequest request = new PushoverRequest(token, user, message);

        try {
            return restClient.post()
                    .uri("/messages.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PushoverResponse.class);
        } catch (RestClientException exception) {
            log.error("Failed to send notification to Pushover");
            throw new IllegalStateException("Failed to send notification to Pushover", exception);
        }
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured");
        }

        return value;
    }
}
