package com.study.spring.chatbot.entity;

import com.study.spring.Member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bot_id", unique = true, nullable = false)
    private String botId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "msg_data", columnDefinition = "text")
    private String msgData;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

