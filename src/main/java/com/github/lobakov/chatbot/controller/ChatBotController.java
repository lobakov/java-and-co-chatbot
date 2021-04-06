package com.github.lobakov.chatbot.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

@RestController
public class ChatBotController {

    private Logger logger = LoggerFactory.getLogger(ChatBotController.class);

    @Autowired
    private UpdateHandler handler;

    @Autowired
    private TelegramBot telegramBot;

    @GetMapping("/poll")
    public void pollUpdates() {
        telegramBot.setUpdatesListener(updates -> {
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        GetUpdatesResponse getUpdatesResponse = telegramBot.execute(getUpdates);

        List<Update> updates = getUpdatesResponse.updates();
        logger.info(updates.toString());

        updates.forEach(this::processUpdate);
    }

    @PostMapping("/process")
    public void processUpdate(@RequestBody Update update) {
        handler.handleUpdate(update);
    }
}
