package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Chat_Msg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMsgRepository extends JpaRepository<Chat_Msg, Integer> {
    // chat_msg 테이블은 Supabase에서 관리하며, Spring에서는 더 이상 사용하지 않는다.
}

