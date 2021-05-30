package com.github.lobakov.chatbot.runner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

@Component
public class ChatBotRunner {

    private Logger logger = LoggerFactory.getLogger(ChatBotRunner.class);
    private TelegramBot telegramBot;
    private UpdateHandler updateHandler;

    @Autowired
    public ChatBotRunner(TelegramBot telegramBot, UpdateHandler updateHandler) {
        this.telegramBot = telegramBot;
        this.updateHandler = updateHandler;
    }

    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        runBot();
    }

    public void runBot() {
        logger.info("Running bot...");
        telegramBot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                logger.debug(updates.toString());
                updates.forEach(updateHandler::handleUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }
}
