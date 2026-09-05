package com.springboot.sb_chatgpt.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions")
public class ChatSessionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String mode;

    private String title;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastActiveAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ChatMessageEntity> messages = new ArrayList<>();

    protected ChatSessionEntity() {
    }

    public ChatSessionEntity(String id, String clientId, String mode, String title) {
        this.id = id;
        this.clientId = clientId;
        this.mode = SessionMode.normalize(mode);
        this.title = title;
        Instant now = Instant.now();
        this.createdAt = now;
        this.lastActiveAt = now;
    }

    public String getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMode() {
        return mode;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public List<ChatMessageEntity> getMessages() {
        return messages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }

    public void addMessage(ChatMessageEntity message) {
        messages.add(message);
        message.setSession(this);
    }
}
