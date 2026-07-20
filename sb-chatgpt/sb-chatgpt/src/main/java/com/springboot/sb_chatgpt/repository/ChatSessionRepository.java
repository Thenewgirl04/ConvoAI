package com.springboot.sb_chatgpt.repository;

import com.springboot.sb_chatgpt.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, String> {

    List<ChatSessionEntity> findByClientIdOrderByLastActiveAtDesc(String clientId);

    Optional<ChatSessionEntity> findByIdAndClientId(String id, String clientId);
}
