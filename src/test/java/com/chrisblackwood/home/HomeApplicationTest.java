package com.chrisblackwood.home;

import com.chrisblackwood.home.dto.WindowDecision;
import com.chrisblackwood.home.notification.NotificationService;
import com.chrisblackwood.home.service.WindowService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HomeApplicationTest {

    @Test
    void shouldSendOpenWideOvernightNotificationMessage() {
        NotificationService notificationService = mock(NotificationService.class);
        WindowService windowService = mock(WindowService.class);
        when(windowService.windowDecision()).thenReturn(WindowDecision.OPEN_WIDE_OVERNIGHT);
        when(windowService.windowMessage(WindowDecision.OPEN_WIDE_OVERNIGHT))
                .thenReturn("Open the windows wide overnight");

        HomeApplication homeApplication = new HomeApplication(notificationService, windowService);

        homeApplication.run();

        verify(windowService).windowMessage(WindowDecision.OPEN_WIDE_OVERNIGHT);
        verify(notificationService).sendNotification("Open the windows wide overnight");
    }
}
