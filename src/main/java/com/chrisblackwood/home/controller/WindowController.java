package com.chrisblackwood.home.controller;

import com.chrisblackwood.home.service.NotificationService;
import com.chrisblackwood.home.service.WindowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(method = RequestMethod.GET)
    public String helloWorld() {
        return notificationService.sendNotification();
    }
}
