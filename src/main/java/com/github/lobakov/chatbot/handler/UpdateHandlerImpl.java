package com.github.lobakov.chatbot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

@Component
public class UpdateHandlerImpl implements UpdateHandler {

    private Logger logger = LoggerFactory.getLogger(UpdateHandlerImpl.class);

    @Autowired
    private TelegramBot telegramBot;

    @Override
    public void handleUpdate(Update update) {
        Message message = update.message();

        logger.info(message.toString());

        Long chatId = 0l;
        String text = "";

        try {
            chatId = message.chat().id();
            text = message.text();
        } catch (Exception ex) {
            logger.info("EMPTY!!!");
            return;
        }

        logger.info("Chat id:" + chatId);
        logger.info("Message text : " + text);

        if (!(text.isBlank() || text.isEmpty())) {
            if (text.startsWith("/help")) {
                SendResponse response = telegramBot.execute(new SendMessage(chatId, "Ты пидор"));
                logger.info(response.message().toString());
            }
        }
    }
}
