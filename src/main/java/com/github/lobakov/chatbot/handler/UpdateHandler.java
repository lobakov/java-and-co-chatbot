package com.github.lobakov.chatbot.handler;

import com.pengrad.telegrambot.model.Update;

public interface UpdateHandler {

    void handleUpdate(Update update);
}
