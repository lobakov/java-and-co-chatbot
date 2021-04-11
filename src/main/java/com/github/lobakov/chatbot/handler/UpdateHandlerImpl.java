package com.github.lobakov.chatbot.handler;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.response.GetChatResponse;

public class UpdateHandlerImpl implements UpdateHandler {

    private static final int CHAT_ID_OFFSET = 4;
    private static final int START_LENGTH = 6;
    private static final String NL = System.lineSeparator();
    private static final String START_PATTERN = "^/start -?\\d+$";
    private static final String TELEGRAM_PREFIX = "https://t.me/c/";
    private static final String UNBAN_BUTTON = "Разбаньте меня";
    private static final String WARNING = "Вы точно прочитали <a href=\"%s\">правила</a>?" + NL
            + "Если да - то нажимайте \"Разбаньте меня\", но не выебывайтесь за нарушения.";

    private TelegramBot telegramBot;

    @Autowired
    public UpdateHandlerImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void handleUpdate(Update update) {
        Message message = update.message();

        if (message != null) {
            banNewUsers(message);
            handleMessage(message);
        }
    }

    private void banNewUsers(Message message) {
        User[] newChatMembers = message.newChatMembers();
        Long chatId = message.chat().id();

        if (newChatMembers != null) {
            for (User user: newChatMembers) {
                telegramBot.execute(new KickChatMember(chatId, user.id()));
            }
        }
    }

    private void handleMessage(Message message) {
        String text = message.text();
        Long chatId = message.chat().id();
        int userId = message.from().id();

        if (!(text == null || text.isBlank() || text.isEmpty())) {
            handleText(chatId, userId, text);
        }
    }

    private void handleText(Long chatId, int userId, String text) {
        Pattern pattern = Pattern.compile(START_PATTERN);
        if (text.startsWith("/help")) {
            telegramBot.execute(new SendMessage(chatId, "Ты пидор"));
        } else if (pattern.matcher(text).matches()) {
            String parentChat = text.substring(START_LENGTH).trim();
            Long parentChatId = Long.valueOf(parentChat);
            handleUnban(parentChatId, chatId, userId);
        }
    }

    private void handleUnban(Long parentChatId, Long chatId, int userId) {
        GetChatResponse chat = telegramBot.execute(new GetChat(parentChatId));
        int rulesId = chat.chat().pinnedMessage().messageId();
        String rulesUrl = composeRulesUrl(parentChatId, rulesId);
        String message = String.format(WARNING, rulesUrl);
        String inviteLink = chat.chat().inviteLink();
        unbanUser(chatId, userId, message, inviteLink);
    }

    private String composeRulesUrl(Long parentChatId, int rulesId) {
        String chatId = parentChatId.toString().substring(CHAT_ID_OFFSET);
        return TELEGRAM_PREFIX + chatId + "/" + rulesId;
    }

    private void unbanUser(Long chatId, int userId, String message, String inviteLink) {
        telegramBot.execute(new UnbanChatMember(chatId, userId).onlyIfBanned(true));
        telegramBot.execute(new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(new InlineKeyboardMarkup(
                        new InlineKeyboardButton[] {
                                new InlineKeyboardButton(UNBAN_BUTTON).url(inviteLink)
                        })));
    }
}
