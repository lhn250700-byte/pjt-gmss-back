package com.study.spring.wallet.repository;

import com.study.spring.Member.entity.Member;
import com.study.spring.wallet.dto.PointHistoryDto;
import com.study.spring.wallet.entity.PointHistory;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

	
	List<PointHistory> findByMemberId_NicknameOrderByIdDesc(String nickname);

    void deleteByMemberId(Member member);
}
