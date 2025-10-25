package com.carrental.config;

import static org.mockito.Mockito.mock;

import com.carrental.service.telegram.CarRentalTelegramBot;
import com.carrental.service.telegram.OverdueRentalScheduler;
import com.carrental.service.telegram.TelegramNotificationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@TestConfiguration
public class TestConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        return mock(TelegramBotsApi.class);
    }

    @Bean
    public TelegramNotificationService telegramNotificationService() {
        return mock(TelegramNotificationService.class);
    }

    @Bean
    public CarRentalTelegramBot carRentalTelegramBot() {
        return mock(CarRentalTelegramBot.class);
    }

    @Bean
    public OverdueRentalScheduler overdueRentalScheduler() {
        return mock(OverdueRentalScheduler.class);
    }
}
