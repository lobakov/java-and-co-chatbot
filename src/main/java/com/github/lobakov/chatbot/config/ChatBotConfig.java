package com.github.lobakov.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.github.lobakov.chatbot.handler.UpdateHandlerImpl;
import com.pengrad.telegrambot.TelegramBot;

@Configuration
public class ChatBotConfig {

    @Value("${BOT}")
    private String token;
    private TelegramBot telegramBot;

    @Bean
    public TelegramBot telegramBot() {
        this.telegramBot = new TelegramBot(token);
        return telegramBot;
    }

    @Bean
    public UpdateHandler updateHandler() {
        return new UpdateHandlerImpl(telegramBot);
    }
}
