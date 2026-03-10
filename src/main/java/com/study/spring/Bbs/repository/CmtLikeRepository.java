package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Bbs.entity.Cmt_Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmtLikeRepository extends JpaRepository<Cmt_Like, Integer> {

    Optional<Cmt_Like> findByCmtIdAndMemberIdMemberId(Bbs_Comment cmt, String memberId);

    List<Cmt_Like> findByCmtId(Bbs_Comment cmt);
}
