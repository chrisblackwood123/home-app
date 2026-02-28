package com.chrisblackwood.home.controller;

import com.chrisblackwood.home.dto.PushoverResponse;
import com.chrisblackwood.home.dto.WindowRecommendation;
import com.chrisblackwood.home.notification.NotificationService;
import com.chrisblackwood.home.service.WindowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/windows")
public class WindowController {

    private final WindowService windowService;
    private final NotificationService notificationService;

    @Autowired
    public WindowController(WindowService windowService, NotificationService notificationService) {
        this.windowService = windowService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public WindowRecommendation getWindowDecision() {
        return windowService.windowRecommendation();
    }

    @PostMapping
    public PushoverResponse sendNotification(@RequestBody String message) {
        return notificationService.sendNotification(message);
    }
}
