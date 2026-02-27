package com.chrisblackwood.home.controller;

import com.chrisblackwood.home.service.NotificationService;
import com.chrisblackwood.home.service.WindowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WindowController.class)
class WindowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WindowService windowService;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldReturnNotificationFromEndpoint() throws Exception {
        given(notificationService.sendNotification()).willReturn("ok");

        mockMvc.perform(get("/windows"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}
