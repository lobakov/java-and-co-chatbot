package com.github.lobakov.chatbot.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatBotRepository extends MongoRepository<ChatUser, Integer> {

    @Override
    public Optional<ChatUser> findById(Integer id);

    @Override
    @SuppressWarnings("unchecked")
    public ChatUser save(ChatUser chatUser);
}
