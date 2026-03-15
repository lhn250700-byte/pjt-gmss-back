package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.dto.CommentListDto;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.dto.PostListDto;
import com.study.spring.Bbs.entity.Bbs;

import com.study.spring.Member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.entity.Bbs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsRepository extends JpaRepository<Bbs, Integer> {
    @Query(value= """
        select
        b.bbs_id as "bbsId", b.title as "title", b.content as "content", b.views as "views", b.created_at as "createdAt",
        COALESCE(count(distinct c.cmt_id), 0) as "commentCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = true) as "bbsLikeCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = false) as "bbsDisLikeCount",
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS "cmtLikeCount",
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) as "cmtDisLikeCount"
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(c.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        where COALESCE(b.del_yn, 'N') = 'N'
        and b.bbs_div <> 'NOTI'
        and (
            (:period = 'realtime'
                and b.created_at >= NOW() - INTERVAL '1 day')
        or
            (:period = 'week'
                and b.created_at >= NOW() - INTERVAL '7 days')
        or
            (:period = 'month'
                and b.created_at >= NOW() - INTERVAL '1 month')
        )
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.bbs_id
    """, nativeQuery = true)
    List<PopularPostDto> findPopularPosts(@Param("period") String period);

	/** 실시간 인기글: 최근 1일 (period 파라미터 비교 없이 고정 조건으로 바인딩 이슈 방지) */
	@Query(value = """
        select
        b.bbs_id as "bbsId", b.title as "title", b.content as "content", b.views as "views", b.created_at as "createdAt",
        COALESCE(count(distinct c.cmt_id), 0) as "commentCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = true) as "bbsLikeCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = false) as "bbsDisLikeCount",
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS "cmtLikeCount",
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) as "cmtDisLikeCount"
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(c.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        where COALESCE(b.del_yn, 'N') = 'N'
        and b.bbs_div <> 'NOTI'
        and b.created_at >= (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') - INTERVAL '1 day'
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.bbs_id
    """, nativeQuery = true)
	List<PopularPostDto> findPopularPostsRealtime();

	/** 주간 인기글: 최근 7일 */
	@Query(value = """
        select
        b.bbs_id as "bbsId", b.title as "title", b.content as "content", b.views as "views", b.created_at as "createdAt",
        COALESCE(count(distinct c.cmt_id), 0) as "commentCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = true) as "bbsLikeCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = false) as "bbsDisLikeCount",
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS "cmtLikeCount",
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) as "cmtDisLikeCount"
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(c.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        where COALESCE(b.del_yn, 'N') = 'N'
        and b.bbs_div <> 'NOTI'
        and b.created_at >= (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') - INTERVAL '7 days'
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.bbs_id
    """, nativeQuery = true)
	List<PopularPostDto> findPopularPostsWeekly();

	/** 월간 인기글: 최근 1개월 */
	@Query(value = """
        select
        b.bbs_id as "bbsId", b.title as "title", b.content as "content", b.views as "views", b.created_at as "createdAt",
        COALESCE(count(distinct c.cmt_id), 0) as "commentCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = true) as "bbsLikeCount",
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and bl.is_like = false) as "bbsDisLikeCount",
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS "cmtLikeCount",
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) as "cmtDisLikeCount"
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(c.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        where COALESCE(b.del_yn, 'N') = 'N'
        and b.bbs_div <> 'NOTI'
        and b.created_at >= NOW() - INTERVAL '1 month'
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.bbs_id
    """, nativeQuery = true)
	List<PopularPostDto> findPopularPostsMonthly();

	// 마이페이지 게시글 리스트
	@Query(value = """
			select
			b.bbs_id as bbsId,
			b.bbs_div as bbsDiv,
			b.title,
			b.views,
			b.created_at as createdAt,
			m.nickname,
			COUNT(bl.like_id) as like_count
			from bbs b
			left join member m on m.member_id = b.member_id
			left join bbs_like bl on bl.bbs_id = b.bbs_id
			where b.member_id = :memberId
			group by
			b.bbs_id,
			b.bbs_div,
			b.title,
			b.views,
			b.created_at,
			m.nickname
			order by b.created_at DESC
			""", countQuery = """
			select count(*)
			from bbs b
			where b.member_id = :memberId
			and (:keyword is null or :keyword = '' or b.title like concat('%', :keyword, '%'))
			""", nativeQuery = true)
	Page<PostListDto> getPostListByMemberId(@Param("memberId") String memberId, @Param("keyword") String keyword, Pageable pageable);

	@Query(value = """
			select
			bc.cmt_id as cmtId,
			b.bbs_div as bbsDiv,
			b.title,
			bc.content,
			m.nickname,
			bc.created_at as createdAt,
			count(bc.cmt_id) as cmt_count,
			count(cl.clike_id) as clike_count
			from bbs_comment bc
			left join bbs b on b.bbs_id = bc.bbs_id
			left join member m on m.member_id = bc.member_id
			left join cmt_like cl on cl.cmt_id = bc.cmt_id
			where bc.member_id = :memberId
			group by
			bc.cmt_id,
			b.bbs_div,
			b.title,
			bc.content,
			m.nickname,
			bc.created_at
			order by bc.created_at DESC
			""", countQuery = """
			select count(*)
			from bbs_comment bc
			where bc.member_id = :memberId
			""",nativeQuery = true)
	Page<CommentListDto> getCommentListByMemberId(@Param("memberId") String memberId, Pageable pageable);

	// 삭제 여부에 따른 게시글 목록 조회 (최신순). del_yn null은 미삭제로 간주해 목록에 포함
	@EntityGraph(attributePaths = {"memberId"})
	@Query("SELECT b FROM Bbs b WHERE (b.delYn = :delYn OR (b.delYn IS NULL AND :delYn = 'N')) ORDER BY b.created_at DESC")
	Page<Bbs> findByDelYnOrderByCreatedAtDesc(@Param("delYn") String delYn, Pageable pageable);

	// 게시물 분류 및 삭제 여부에 따른 조회 (del_yn null = 미삭제)
	@EntityGraph(attributePaths = {"memberId"})
	@Query("SELECT b FROM Bbs b WHERE b.bbs_div = :bbsDiv AND (b.delYn = :delYn OR (b.delYn IS NULL AND :delYn = 'N')) ORDER BY b.created_at DESC")
	Page<Bbs> findByBbsDivAndDelYnOrderByCreatedAtDesc(@Param("bbsDiv") String bbsDiv,
	                                                  @Param("delYn") String delYn,
	                                                  Pageable pageable);

	/** 게시글 상세: member 한 번에 로딩해 지연 로딩 추가 쿼리 방지 */
	@EntityGraph(attributePaths = {"memberId"})
	@Query("SELECT b FROM Bbs b WHERE b.bbsId = :id")
	java.util.Optional<Bbs> findByIdWithMember(@Param("id") Integer id);

    
//    // 삭제 여부에 따른 게시글 목록 조회 (최신순)
//    @Query("SELECT b FROM Bbs b WHERE b.delYn = :delYn ORDER BY b.created_at DESC")
//    Page<Bbs> findByDelYnOrderByCreatedAtDesc(@Param("delYn") String delYn, Pageable pageable);
//    
//    // 게시물 분류 및 삭제 여부에 따른 조회
//    @Query("SELECT b FROM Bbs b WHERE b.bbs_div = :bbsDiv AND b.delYn = :delYn ORDER BY b.created_at DESC")
//    Page<Bbs> findByBbsDivAndDelYnOrderByCreatedAtDesc(@Param("bbsDiv") String bbsDiv, @Param("delYn") String delYn, Pageable pageable);
    /** 인기글용: 삭제글 제외, 최근 200건만 조회 후 Java에서 점수 정렬해 상위 10건 사용 (전체 스캔 방지) */
    @Query(value= """
        select
        b.bbs_id, b.title, b.content, b.views, b.created_at,
        COALESCE(count(distinct c.cmt_id), 0) as commentCount,
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and is_like = true) bbsLikeCount,
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and is_like = false) bbsDisLikeCount,
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS cmtLikeCount,
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) cmtDisLikeCount
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(c.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        where COALESCE(b.del_yn, 'N') = 'N'
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.created_at DESC
        LIMIT 200
    """, nativeQuery = true)
    List<PopularPostDto> findPopularPosts();

	// 벡터(임베딩) 저장 — pgvector 확장 필요
	@Modifying
	@Query(value = "UPDATE bbs SET embedding = CAST(:embedding AS vector) WHERE bbs_id = :bbsId", nativeQuery = true)
	void updateEmbedding(@Param("bbsId") int bbsId, @Param("embedding") String embedding);

	// 유사도 순 게시글 ID 목록 (거리 오름차순, embedding IS NOT NULL인 글만)
	@Query(value = """
		SELECT bbs_id FROM bbs
		WHERE COALESCE(del_yn, 'N') = 'N' AND embedding IS NOT NULL
		ORDER BY embedding <-> CAST(:embedding AS vector)
		LIMIT :limit
		""", nativeQuery = true)
	List<Integer> findSimilarBbsIds(@Param("embedding") String embedding, @Param("limit") int limit);

	@Modifying(clearAutomatically = true)
	@Query("""
		UPDATE Bbs b
		SET b.memberId = :delMember
		WHERE b.memberId = :member
	""")
	void updateMember(@Param("member") Member member, @Param("delMember") Member deletedMember);
}
