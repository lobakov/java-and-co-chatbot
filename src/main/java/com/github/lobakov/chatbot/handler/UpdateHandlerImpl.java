package com.github.lobakov.chatbot.handler;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.lobakov.chatbot.persistence.ChatBotRepository;
import com.github.lobakov.chatbot.persistence.ChatUser;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetChatResponse;

@Component
public class UpdateHandlerImpl implements UpdateHandler {

    private static final int UNBAN_INDEX = 0;
    private static final int PARENT_INDEX = 1;
    private static final int CHAT_ID_OFFSET = 4;
    private static final int START_LENGTH = 6;
    private static final String ALREADY_UNBANNED = "Тебя уже однажды разбанили.";
    private static final String DELIMITER = ":";
    private static final String HELP = "/help";
    private static final String INSULT = "Ты пидор";
    private static final String NL = System.lineSeparator();
    private static final String START_PATTERN = "^/start -?\\d+$";
    private static final String TELEGRAM_PREFIX = "https://t.me/c/";
    private static final String UNBAN_BUTTON = "Разбаньте меня";
    private static final String UNBAN_REQUEST = "UB";
    private static final String UNBANNED = "Вы были разбанены";
    private static final String WARNING = "Вы точно прочитали <a href=\"%s\">правила</a>?" + NL
            + "Если да - то нажимайте \"Разбаньте меня\", но не выебывайтесь за нарушения.";

    @Autowired
    private ChatBotRepository chatBotRepository;

    private Logger logger = LoggerFactory.getLogger(UpdateHandlerImpl.class);
    private TelegramBot telegramBot;

    @Autowired
    public UpdateHandlerImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void handleUpdate(Update update) {
        logger.debug("Update received: " + update.toString());
        Optional<Message> optMessage = Optional.ofNullable(update.message());
        Optional<CallbackQuery> optCallbackQuery = Optional.ofNullable(update.callbackQuery());

        if (optCallbackQuery.isPresent()) {
            logger.debug("CallbackQuery received: " + optCallbackQuery.get().toString());
            unbanUser(optCallbackQuery.get(), optCallbackQuery.get().message().messageId());
        } else if (optMessage.isPresent()) {
            restrictNewUsers(optMessage.get());
            handleMessage(optMessage.get());
        }
    }

    private void restrictNewUsers(Message message) {
        User[] newChatMembers = message.newChatMembers();
        Long chatId = message.chat().id();

        if (!(null == newChatMembers)) {
            for (User user: newChatMembers) {
                telegramBot.execute(new RestrictChatMember(chatId, user.id(), Permissions.MUTE));
            }
        }
    }

    private void handleMessage(Message message) {
        Integer userId = message.from().id();
        Long chatId = message.chat().id();
        String text = message.text();
        if (!(null == text || text.isBlank() || text.isEmpty())) {
            handleText(chatId, userId, text);
        }
    }

    private void handleText(Long chatId, int userId, String text) {
        Pattern pattern = Pattern.compile(START_PATTERN);

        if (text.startsWith(HELP)) {
            telegramBot.execute(new SendMessage(chatId, INSULT));
        } else if (pattern.matcher(text).matches()) {
            Optional<ChatUser> optChatMember = chatBotRepository.findById(userId);
            if (optChatMember.isPresent()) {
                telegramBot.execute(new SendMessage(chatId, ALREADY_UNBANNED));
                return;
            }
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
        confirmUnban(parentChatId, chatId, userId, message);
    }

    private String composeRulesUrl(Long parentChatId, int rulesId) {
        String chatId = parentChatId.toString().substring(CHAT_ID_OFFSET);
        return TELEGRAM_PREFIX + chatId + "/" + rulesId;
    }

    private void confirmUnban(Long parentChatId, Long chatId, int userId, String message) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        joiner.add(UNBAN_REQUEST)
              .add(parentChatId.toString());

        telegramBot.execute(new SendMessage(chatId, message)
                   .parseMode(ParseMode.HTML)
                   .disableWebPagePreview(true)
                   .replyMarkup(new InlineKeyboardMarkup(
                        new InlineKeyboardButton[] {
                                new InlineKeyboardButton(UNBAN_BUTTON).callbackData(joiner.toString())
                        })));
    }

    private void unbanUser(CallbackQuery callbackQuery, Integer buttonId) {
        String[] callback = callbackQuery.data().split(DELIMITER);
        if (!callback[UNBAN_INDEX].equals(UNBAN_REQUEST)) {
            return;
        }
        Long parentChatId = Long.valueOf(callback[PARENT_INDEX]);
        Long chatId = callbackQuery.message().chat().id();
        int userId = callbackQuery.from().id();
        telegramBot.execute(new RestrictChatMember(parentChatId, userId, Permissions.UNMUTE));
        chatBotRepository.save(new ChatUser(userId));
        telegramBot.execute(new DeleteMessage(chatId, buttonId));
        telegramBot.execute(new SendMessage(chatId, UNBANNED));
    }
}
