package com.github.lobakov.chatbot.handler;

import com.pengrad.telegrambot.model.ChatPermissions;

public class Permissions {

    public static final ChatPermissions MUTE = new ChatPermissions()
                                                        .canAddWebPagePreviews(false)
                                                        .canChangeInfo(false)
                                                        .canInviteUsers(false)
                                                        .canPinMessages(false)
                                                        .canSendMediaMessages(false)
                                                        .canSendMessages(false)
                                                        .canSendOtherMessages(false)
                                                        .canSendPolls(false);

    public static final ChatPermissions UNMUTE = new ChatPermissions()
                                                        .canAddWebPagePreviews(true)
                                                        .canSendMediaMessages(true)
                                                        .canSendMessages(true)
                                                        .canSendOtherMessages(true)
                                                        .canSendPolls(true);
}
