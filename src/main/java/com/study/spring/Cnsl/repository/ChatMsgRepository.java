package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Chat_Msg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMsgRepository extends JpaRepository<Chat_Msg, Integer> {
    List<Chat_Msg> findByCnslIdOrderByCreatedAtAsc(Integer cnslId);
}

