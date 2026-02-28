package com.chrisblackwood.home.controller;

import com.chrisblackwood.home.dto.PushoverResponse;
import com.chrisblackwood.home.notification.NotificationService;
import com.chrisblackwood.home.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/windows")
public class WindowController {

    private final WeatherService weatherService;
    private final NotificationService notificationService;

    @Autowired
    public WindowController(WeatherService weatherService, NotificationService notificationService) {
        this.weatherService = weatherService;
        this.notificationService = notificationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public PushoverResponse sendNotification(@RequestBody String message) {
        return notificationService.sendNotification(message);
    }
}
