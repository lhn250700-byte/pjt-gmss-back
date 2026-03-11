package com.study.spring.chatbot.repository;

import com.study.spring.chatbot.entity.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
    Optional<ChatbotSession> findByBotId(String botId);
}

