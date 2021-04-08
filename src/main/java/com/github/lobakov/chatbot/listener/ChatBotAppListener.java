package com.github.lobakov.chatbot.listener;

import java.util.List;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class ChatBotAppListener implements ApplicationListener<ApplicationEvent> {

    private TelegramBot telegramBot;
    private UpdateHandler updateHandler;

    public ChatBotAppListener(TelegramBot telegramBot, UpdateHandler updateHandler) {
        this.telegramBot = telegramBot;
        this.updateHandler = updateHandler;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent) {
            runBot();
        }
    }

    private void runBot() {
        telegramBot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                System.out.println(updates.toString());
                updates.forEach(updateHandler::handleUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }
}
