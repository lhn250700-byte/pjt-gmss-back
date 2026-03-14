package com.study.spring.Bot.repository;

import com.study.spring.Bot.entity.Bot_Msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotMsgRepository extends JpaRepository<Bot_Msg, Integer> {

    @Query("SELECT b FROM Bot_Msg b WHERE b.bot_id = :botId AND b.memberId.memberId = :memberId")
    Optional<Bot_Msg> findByBotIdAndMemberId(@Param("botId") Integer botId, @Param("memberId") String memberId);
}
