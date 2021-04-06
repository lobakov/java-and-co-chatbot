package com.github.lobakov.chatbot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

@SpringBootApplication
public class JavaAndCoChatBotApp implements CommandLineRunner {

    @Autowired
    private UpdateHandler updateHandler;

    @Autowired
    private TelegramBot telegramBot;

    public static void main(String[] args) {
        SpringApplication.run(JavaAndCoChatBotApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        telegramBot.setUpdatesListener(new UpdatesListener() {
                @Override
                public int process(List<Update> updates) {
                    updates.forEach(updateHandler::handleUpdate);
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
        });
    }
}
