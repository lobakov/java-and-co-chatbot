package com.github.lobakov.chatbot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class UpdateHandlerImpl implements UpdateHandler {

    private Logger logger = LoggerFactory.getLogger(UpdateHandlerImpl.class);
    private TelegramBot telegramBot;

    @Autowired
    public UpdateHandlerImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void handleUpdate(Update update) {
        Message message = update.message();

        if (message == null) {
            logger.info("No message in update.");
            return;
        }

        Long chatId = message.chat().id();
        String text = message.text();

        logger.info("Chat id:" + chatId);
        logger.info("Message text : " + text);

        if (!(text == null || text.isBlank() || text.isEmpty())) {
            SendResponse response = null;
            if (text.startsWith("/help")) {
                response = telegramBot.execute(new SendMessage(chatId, "Ты пидор"));
                logger.info(response.message().toString());
            } else {
                response = telegramBot.execute(new SendMessage(chatId, text));
            }
        }
    }
}
