package com.github.lobakov.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.lobakov.chatbot.config.ChatBotConfig;
import com.github.lobakov.chatbot.handler.UpdateHandler;
import com.github.lobakov.chatbot.handler.UpdateHandlerImpl;
import com.github.lobakov.chatbot.listener.ChatBotAppListener;
import com.pengrad.telegrambot.TelegramBot;

@SpringBootApplication
public class JavaAndCoChatBotApp {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ChatBotConfig.class);
        TelegramBot telegramBot = context.getBean(TelegramBot.class);
        UpdateHandler updateHandler = context.getBean(UpdateHandlerImpl.class);

        SpringApplication springApplication = new SpringApplication(JavaAndCoChatBotApp.class);
        springApplication.addListeners(new ChatBotAppListener(telegramBot, updateHandler));
        springApplication.run(args);

        ((ConfigurableApplicationContext) context).close();
    }
}
