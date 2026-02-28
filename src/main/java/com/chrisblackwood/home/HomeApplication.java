
package com.chrisblackwood.home;

import com.chrisblackwood.home.dto.WindowDecision;
import com.chrisblackwood.home.notification.NotificationService;
import com.chrisblackwood.home.service.WindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(HomeApplication.class);

	private final NotificationService notificationService;
	private final WindowService windowService;

    public HomeApplication(NotificationService notificationService, WindowService windowService) {
		this.notificationService = notificationService;
		this.windowService = windowService;
	}

	public static void main(String[] args) {
		SpringApplication.run(HomeApplication.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			WindowDecision decision = windowService.windowDecision();
			notificationService.sendNotification(windowService.windowMessage(decision));
			log.info("Window recommendation sent successfully: {}", decision);
		} catch (Exception exception) {
			log.error("Nightly window recommendation run failed", exception);
			throw exception;
		}
	}
}
