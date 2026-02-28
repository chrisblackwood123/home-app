package com.chrisblackwood.home.service;

import com.chrisblackwood.home.dto.PushoverRequest;
import com.chrisblackwood.home.dto.PushoverResponse;
import com.chrisblackwood.home.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class NotificationServiceTest {

    private static final String BASE_URL = "https://api.pushover.net/1";

    private MockRestServiceServer server;
    private NotificationService notificationService;

    private final String TOKEN = "token";
    private final String USER = "user";

    private final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        notificationService = new NotificationService(builder, TOKEN, USER);
    }

    @Test
    void shouldSentNotification() throws Exception {
        String message = "Hi Tester";
        String uri = BASE_URL + "/messages.json";

        PushoverRequest pushoverRequest = new PushoverRequest(TOKEN, USER, message);
        PushoverResponse pushoverResponse = new PushoverResponse
                (1, "425235", null, null, null);

        server.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(MAPPER.writeValueAsString(pushoverRequest)))
                .andRespond(withSuccess(MAPPER.writeValueAsString(pushoverResponse), MediaType.APPLICATION_JSON));

        assertEquals(pushoverResponse, notificationService.sendNotification(message));

    }

    @Test
    void shouldHandleNoMessage() throws Exception {
        String message = "Hi Tester";
        String uri = BASE_URL + "/messages.json";
        ArrayList<String> errors = new ArrayList<>();
        errors.add("message cannot be blank");

        PushoverRequest pushoverRequest = new PushoverRequest(TOKEN, USER, message);
        PushoverResponse pushoverErrorResponse = new PushoverResponse
                (0, "425235", errors, "message cannot be blank", null);

        server.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(MAPPER.writeValueAsString(pushoverRequest)))
                .andRespond(withSuccess(MAPPER.writeValueAsString(pushoverErrorResponse), MediaType.APPLICATION_JSON));

        assertEquals(pushoverErrorResponse, notificationService.sendNotification(message));

    }

    @Test
    void shouldHandleNoResponseFromServer() throws Exception {
        String message = "Hi Tester";
        String uri = BASE_URL + "/messages.json";

        PushoverRequest pushoverRequest = new PushoverRequest(TOKEN, USER, message);

        server.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(MAPPER.writeValueAsString(pushoverRequest)))
                .andRespond(withResourceNotFound());

        assertThrows(RestClientResponseException.class,
                () -> notificationService.sendNotification(message));
    }

}
