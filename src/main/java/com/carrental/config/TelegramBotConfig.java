package com.carrental.config;

import com.carrental.service.telegram.CarRentalTelegramBot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConditionalOnProperty(value = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(CarRentalTelegramBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
