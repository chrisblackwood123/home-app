
package com.chrisblackwood.home;

import com.chrisblackwood.home.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeApplication implements CommandLineRunner {

	private final NotificationService notificationService;

    public HomeApplication(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public static void main(String[] args) {
		SpringApplication.run(HomeApplication.class, args);
	}

	@Override
	public void run(String... args) {
		notificationService.sendNotification();
	}
}
