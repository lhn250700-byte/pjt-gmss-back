package com.study.spring.Bot.repository;

import com.study.spring.Bot.entity.Bot_Msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotMsgRepository extends JpaRepository<Bot_Msg, Integer> {

    Optional<Bot_Msg> findByBot_idAndMemberId_MemberId(Integer botId, String memberId);
}
