package com.github.lobakov.chatbot.handler;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.KickChatMember;
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
        logger.info(update.toString());
        Message message = update.message();

        if (message == null) {
            logger.info("No message in update.");
            return;
        }

        Long chatId = message.chat().id();
        User[] newChatMembers = message.newChatMembers();
        String text = message.text();

        logger.info("Chat id:" + chatId);

        if (newChatMembers != null) {
            logger.info("New users in chat: " + Arrays.toString(newChatMembers));
            for (User user: newChatMembers) {
                handleUser(chatId, user);
            }
        } else if (!(text == null || text.isBlank() || text.isEmpty())) {
            logger.info("Message text : " + text);
            handleText(chatId, text);
        }
    }

    private void handleUser(Long chatId, User user) {
        //telegramBot.execute(new KickChatMember(chatId, user.id()));
        telegramBot.execute(new SendMessage(chatId, "Превед"));
    }

    private void handleText(Long chatId, String text) {
        SendResponse response = null;

        if (text.startsWith("/help")) {
            response = telegramBot.execute(new SendMessage(chatId, "Ты пидор"));
            logger.info(response.message().toString());
        } else if (text.startsWith("/menu")) {
            //FOR UI TESTING PURPOSES
            String message = "test";
            String BUTTON_CB = "1";
            String BUTTON_EXCHANGE = "2";
            telegramBot.execute(new SendMessage(chatId, message)
                       .replyMarkup(new ReplyKeyboardMarkup(new String[]{BUTTON_CB}, new String[]{BUTTON_EXCHANGE})));
        }
    }
}
