package com.chrisblackwood.home.notification;

import com.chrisblackwood.home.dto.PushoverRequest;
import com.chrisblackwood.home.dto.PushoverResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {

    private final RestClient restClient;
    private final String token;
    private final String user;

    @Autowired
    public NotificationService(RestClient.Builder restClientBuilder, @Value("${pushover.baseurl:https://api.pushover.net/1}") String baseUrl,
                               @Value("${pushover.token:}") String token, @Value("${pushover.user:}") String user) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.token = token;
        this.user = user;
    }

    public PushoverResponse sendNotification(String message) {

        PushoverRequest request = new PushoverRequest(token, user, message);

        return restClient.post()
                .uri("/messages.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PushoverResponse.class);
    }
}
