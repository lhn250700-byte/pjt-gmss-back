package com.study.spring.Cnsl.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.lang.String;

import com.study.spring.Cnsl.dto.*;
import com.study.spring.Member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Cnsl.entity.Cnsl_Reg;

@Repository
public interface CnslRepository extends JpaRepository<Cnsl_Reg, Long> {
	@Query(value = """
			select case when count(*) > 0 then 'Y' else 'N' end isCounselingYn
			from cnsl_reg cr
			where cr.del_yn = 'N'
			and cr.cnsl_stat in ('A', 'B', 'C')
			and cr.cnsl_dt = :cnslDt
			and cr.cnsl_start_time = :cnslStartTime
			and (
			      cr.cnsler_id = :cnslerId
			   or cr.member_id = :memberId
			)
			""", nativeQuery = true)
	Optional<IsCnslDto> isCounseling(@Param("memberId") String memberId, @Param("cnslerId") String cnslerId,
			@Param("cnslDt") LocalDate cnslDt, @Param("cnslStartTime") LocalTime cnslStartTime);

    @Query(value = """
                SELECT
               cr.cnsl_dt AS cnslDt,
               cr.cnsl_start_time AS cnslStartTime, 
               m.nickname AS nickname
                FROM cnsl_reg cr
                JOIN member m ON cr.cnsler_id = m.member_id
                WHERE cr.del_yn = 'N'
                  AND cr.cnsl_stat <> 'X'      
                  AND cr.cnsler_id = :cnslerId
                  AND cr.cnsl_dt = :cnslDt
                ORDER BY cr.cnsl_start_time
            """, nativeQuery = true)
    List<CnslerDateDto> getReservedInfo(@Param("cnslerId") String cnslerId, @Param("cnslDt") LocalDate cnslDt);

	@Query(value = """
			select
			 date_trunc('month', cr.cnsl_dt)::date AS month_start,
			 COUNT(*) AS total_cnt,
			 sum(case when cr.cnsl_stat = 'A' then 1 else 0 end) reserved_cnt,
			 sum(case when cr.cnsl_stat = 'D' then 1 else 0 end) completed_cnt
			 from cnsl_reg cr
			 where cr.cnsler_id = :cnslerId
			 and coalesce(cr.del_yn, 'N') = 'N'
			 and cr.cnsl_stat in ('A', 'D')
			 group by month_start
			 order by month_start desc
			""", nativeQuery = true)
	List<CnslDatePerMonthDto> getCnslDatePerMonthList(@Param("cnslerId") String cnslerId);

	@Query(value = """
				select
			    count(*) as total_cnt,
			    sum(case when cr.cnsl_stat = 'A' then 1 else 0 end) reserved_cnt,
			    sum(case when cr.cnsl_stat = 'D' then 1 else 0 end) completed_cnt
				from cnsl_reg cr
				WHERE cr.cnsler_id = :cnslerId
				and cr.del_yn = 'N'
			""", nativeQuery = true)
	Optional<CnslSumDto> getCnslTotalCount(@Param("cnslerId") String cnslerId);

    // [상담 내역 전체 : stat으로 구분됨]
    @Query(value = """
            	select
            	cr.cnsl_id,
            	cr.cnsl_title,
            	cr.cnsl_content,
            	m.nickname,
            	case when cr.cnsl_stat = 'C' and cr.cnsl_todo_yn = 'Y'
            	  then '답변 필요'
            	  else '!'	
            	end as respYn,  
            	case when cr.cnsl_stat = 'D'
            	  then to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_end_time, 'HH24:MI')
            	  else to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_start_time, 'HH24:MI')
            	end as dt_time,
                case 
                    when cr.cnsl_stat = 'B' then '상담 예정'
                    when cr.cnsl_stat = 'C' then '상담 진행 중'
                    when cr.cnsl_stat = 'D' then '상담 완료'
                    else '!'
                end as statusText,
				get_code_nm('cnsl_tp', cr.cnsl_tp) as type
			  from cnsl_reg cr
			  join member m
              on cr.member_id = m.member_id
              where cr.del_yn = 'N'
              and cr.cnsler_Id = :cnslerId
              and (cr.cnsl_stat is null or cr.cnsl_stat = :status)
              order by cr.cnsl_dt desc, cr.cnsl_start_time desc
            """, nativeQuery = true)
    Page<cnslListDto> findCounselingsByCounselor(@Param("status") String status, Pageable pageable, @Param("cnslerId") String cnslerId);
    
    // [전체 상담 내역]
    @Query(value="""
    		select
            	cr.cnsl_id,
            	cr.cnsl_title,
            	cr.cnsl_content,
            	m.nickname,
            	case when cr.cnsl_stat = 'C' and cr.cnsl_todo_yn = 'Y'
            	  then '답변 필요'
            	  else '!'	
            	end as respYn,  
            	case when cr.cnsl_stat = 'D'
            	  then to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_end_time, 'HH24:MI')
            	  else to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_start_time, 'HH24:MI')
            	end as dt_time,
                case 
                    when cr.cnsl_stat = 'B' then '상담 예정'
                    when cr.cnsl_stat = 'C' then '상담 진행 중'
                    when cr.cnsl_stat = 'D' then '상담 완료'
                    else '!'
                end as statusText
            	from cnsl_reg cr
            	join member m
              on cr.member_id = m.member_id
              where cr.del_yn = 'N'
              and cr.cnsler_Id = :cnslerId
              and cr.cnsl_stat NOT IN('A', 'X')
              ORDER BY
		      CASE 
		        WHEN cr.cnsl_stat = 'B' THEN 1
		        WHEN cr.cnsl_stat = 'C' AND cr.cnsl_todo_yn = 'Y' THEN 2
		        WHEN cr.cnsl_stat = 'C' AND cr.cnsl_todo_yn != 'Y' THEN 3
		        WHEN cr.cnsl_stat = 'D' THEN 4
		        ELSE 5
		      END,
		      cr.cnsl_dt,       
		      cr.cnsl_start_time
    		""", nativeQuery = true)
    Page<cnslListDto> findAllCounselingsByCounselor(Pageable pageable, @Param("cnslerId") String cnslerId);

    // [상담 예약 관리(수락 전)]
    @Query(value = """
            select
                cr.cnsl_id,
                cr.cnsl_title,
                cr.cnsl_content,
                m.nickname, 
                case when cr.cnsl_stat = 'D'
                  then to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_end_time, 'HH24:MI')
                  else to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_start_time, 'HH24:MI')
                end as dt_time,
                get_code_nm('cnsl_tp', cr.cnsl_tp) as type
             from cnsl_reg cr
             join member m
             on cr.member_id = m.member_id
             where cr.del_yn = 'N'
             and cr.cnsl_stat = 'A'
			 and cr.cnsler_id = :cnslerId
             order by cr.cnsl_dt desc, cr.cnsl_start_time desc
            """, nativeQuery = true)
    Page<cnslListWithoutStatusDto> findPendingReservations(Pageable pageable, @Param("cnslerId") String cnslerId);

	@Query(value = """
			    select
			        r.cnsler_id,
			        m.nickname,
			       sum(case when r.cnsl_stat in ('A','B','C') then 1 else 0 end) as cnslReqCnt,
			       sum(case when r.cnsl_stat in ('D') then 1 else 0 end) as cnslDoneCnt,
			       count(*)
			    from cnsl_reg r
			    join member m on r.cnsler_id = m.member_id
			    where cnsl_stat not in ('X')
			    and cnsl_dt between :startDate and :endDate
			    and r.cnsler_id = :cnslerId
			    group by cnsler_id, m.nickname
			""", nativeQuery = true)
	ConsultationStatusCountDto findConsultationStatusCounts(@Param("cnslerId") String cnslerId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			    select
			          c.code,
			          c.code_name,
			          to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnslPriceSum,
			          to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnslPriceCmsn,
			          to_char(coalesce(b.cnsl_price_sum, 0)
			                - coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnslExctAmt,
			          coalesce(b.cnsl_count, 0) cnslCount
			   from code c
			   left join (
			            select
			               cr.cnsler_id,
			               m.nickname,
			               cr.cnsl_tp,
			               get_code_nm('cnsl_tp',cr.cnsl_tp) cnsl_tp_nm,
			               sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
			               trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
			               count(*) cnsl_count
			            from cnsl_reg cr
			            join member m on m.member_id = cr.cnsler_id
			            left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
			            where cr.cnsl_stat not in ('X') -- 상담취소제외
			            and cr.cnsler_id = :cnslerId
			            and cr.cnsl_dt between :startDate and :endDate
			            group by cr.cnsl_dt, cr.cnsler_id, m.nickname, cr.cnsl_tp ) b
			            on c.code = b.cnsl_tp
			   where c.col_id = 'cnsl_tp'
			   order by c.code
			""", nativeQuery = true)
	List<ConsultationCategoryCountDto> findConsultationCategoryCounts(@Param("cnslerId") String cnslerId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			    select
			       r.cnsl_dt,
			       r.cnsler_id,
			       m.nickname,
			       sum(case when r.cnsl_stat in ('A','B','C') then 1 else 0 end) cnslReqCnt,
			       sum(case when r.cnsl_stat in ('D') then 1 else 0 end) cnslDoneCnt,
			       count(*)
			    from cnsl_reg r
			    join member m on r.cnsler_id = m.member_id
			    where cnsl_stat not in ('X')
			    and cnsl_dt between :startDate and :endDate
			    and r.cnsler_id = :cnslerId
			    group by r.cnsl_dt, cnsler_id, m.nickname
			    order by r.cnsl_dt desc
			""", nativeQuery = true)
	List<ConsultationStatusDailyDto> findDailyReservationCompletionTrend(@Param("cnslerId") String cnslerId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			        select cr.cnsler_id,
			        	   m.nickname,
			        	   '기간별수익합' tp,
			               to_char(sum(coalesce(ci.cnsl_price,0)), '999,999,999,999') cnslPriceSum,
			               to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslPriceCmsn , -- 10자리에서 버림
			               to_char(sum(coalesce(ci.cnsl_price,0))
			        			- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslExctAmt,
			               count(*) cnslCount
			        from cnsl_reg cr
			        join member m on m.member_id = cr.cnsler_id
			        left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
			        where cr.cnsl_stat not in ('X') -- 상담취소제외
			        and cr.cnsler_id = :cnslerId
			        and cr.cnsl_dt between :startDate and :endDate
			        group by cr.cnsler_id, m.nickname
			        union all
			        select cr.cnsler_id,
			        	   m.nickname,
			        	   '3개월별수익합' tp,
			               to_char(sum(coalesce(ci.cnsl_price,0)), '999,999,999,999') cnslPriceSum,
			               to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslPriceCmsn , -- 10자리에서 버림
			               to_char(sum(coalesce(ci.cnsl_price,0))
			        			- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslExctAmt,
			               count(*) cnslCount
			        from cnsl_reg cr
			        join member m on m.member_id = cr.cnsler_id
			        left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
			        where cr.cnsl_stat not in ('X') -- 상담취소제외
			        and cr.cnsler_id = :cnslerId
			        and cr.cnsl_dt between current_date - INTERVAL '3 MONTH' and current_date --current_date
			        group by cr.cnsler_id, m.nickname
			""", nativeQuery = true)
	List<MyRevenueSummaryDto> findMyRevenueSummary(@Param("cnslerId") String cnslerId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			    select
			           cr.cnsler_id,
			           m.nickname,
			           cr.cnsl_tp,
			           get_code_nm('cnsl_tp',cr.cnsl_tp) cnslTpNm,
			           count(*) cnslCount
			    from cnsl_reg cr
			    join member m on m.member_id = cr.cnsler_id
			    left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
			    where cr.cnsl_stat not in ('X') -- 상담취소제외
			    and cr.cnsler_id = :cnslerId
			    and cr.cnsl_tp <> '1'
			    and cr.cnsl_dt between :startDate and :endDate
			    group by cr.cnsler_id, m.nickname, cr.cnsl_tp
			    order by count(*) desc, cr.cnsl_tp
			    limit 1
			""", nativeQuery = true)
	MostConsultedTypeDto findMostConsultedType(@Param("cnslerId") String cnslerId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value = """
			    select to_char(r.created_at, 'YYYY-MM-DD HH24:MI:SS') risk_date,
			           r.table_id,
			           r.bbs_div,
			           get_code_nm('bbs_div', bbs_div) bbs_div_nm,
			           r.member_id,
			           m.nickname,
			           r.content,
			           r.action
			    from bbs_risk r
			    join  member m on r.member_id = m.member_id
			    order by r.created_at desc
			""", nativeQuery = true)
	List<RealtimeRiskDetectionStatusDto> findRealtimeRiskDetectionStatus();

    @Query(value=
            """
        select cr.cnsl_dt,
               cr.cnsler_id,
               m.nickname,
               to_char(sum(coalesce(ci.cnsl_price,0)),'999,999,999,999') cnsl_price_sum,
               to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1),'999,999,999,999') cnsl_price_cmsn , -- 10자리에서 버림
               to_char(sum(coalesce(ci.cnsl_price,0))
               	- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1),'999,999,999,999') cnsl_exct_sum,
               '완료' exct_stat,
               count(*) cnsl_count
        from cnsl_reg cr
        join member m on m.member_id = cr.cnsler_id
        left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
        where cr.cnsl_stat not in ('X') -- 상담취소제외
        and cr.cnsl_dt between current_date - 7 and current_date
        group by cr.cnsl_dt, cr.cnsler_id, m.nickname
        order by cr.cnsl_dt desc , sum(coalesce(ci.cnsl_price,0)) desc, cr.cnsler_id
    """, nativeQuery = true)
    Page<CounselorRevenueLatestlyDto> findLatestlyCounselorRevenue(Pageable pageable);
	@Query(value = """
		   select c.code,
		   c.code_name,
		   to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnsl_price_sum,
		   to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnsl_price_cmsn,
		   to_char(coalesce(b.cnsl_exct_sum,0),'999,999,999,999') cnsl_exct_sum,
		   coalesce(b.cnsl_count,0) cnsl_count,
		   coalesce(avg_cnsl_time, '00:00:00') avg_cnsl_time
		   from code c
		   left join ( 
				select cr.cnsl_cate,
				sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
				trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
				sum(coalesce(ci.cnsl_price,0)) - trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_exct_sum,
				to_char(avg(cnsl_end_time - cnsl_start_time), 'HH24:MI:SS') avg_cnsl_time,
				count(*) cnsl_count
				from cnsl_reg cr
				join member m on m.member_id = cr.cnsler_id
				left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
				where cr.cnsl_stat not in ('X') -- 상담취소제외
				and cr.cnsl_dt between :startDate and :endDate
				group by cr.cnsl_cate ) b on c.code = b.cnsl_cate
			where c.col_id = 'cnsl_cate'
			order by c.code
	""", nativeQuery = true)
	List<CategoryRevenueStatisticsDto> findCategoryRevenueStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value= """
            select c.code,
                   c.code_name,
                   to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnsl_price_sum,
                   to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnsl_price_cmsn,
                   to_char(coalesce(b.cnsl_exct_sum,0),'999,999,999,999') cnsl_exct_sum,
                   coalesce(b.cnsl_count,0) cnsl_count,
                   coalesce(avg_cnsl_time, '00:00:00') avg_cnsl_time
            from code c
            left join ( select cr.cnsl_tp,
            			       sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
            			       trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
            			       sum(coalesce(ci.cnsl_price,0))
            			       	- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_exct_sum,
            			       to_char(avg(cnsl_end_time - cnsl_start_time), 'HH24:MI:SS') avg_cnsl_time,
            			       count(*) cnsl_count
            			from cnsl_reg cr
            			join member m on m.member_id = cr.cnsler_id
            			left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
            			where cr.cnsl_stat not in ('X') -- 상담취소제외
            			and cr.cnsl_dt between :startDate and :endDate
						and cr.del_yn = 'N'
            			group by cr.cnsl_tp ) b on c.code = b.cnsl_tp
            where c.col_id = 'cnsl_tp'
            order by c.code
    """, nativeQuery = true)
    List<CategoryRevenueStatisticsDto> findTypeRevenueStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT
          r.cnsl_dt as cnslDt, 
          r.cnsl_title,
          r.cnsl_content,
          CASE
              WHEN r.cnsl_stat = 'A' THEN '상담 신청'
              WHEN r.cnsl_stat = 'B' THEN '상담 예정'
              WHEN r.cnsl_stat = 'C' THEN '상담 진행 중'
          WHEN r.cnsl_stat = 'D' THEN '상담 완료'
          ELSE '!'
      END AS cnsl_stat_nm,
      m1.nickname,
      m1.mbti,
      get_code_nm('gender', m1.gender) || '성' AS gender,
      '만 ' || EXTRACT(YEAR FROM age(current_date, m1.birth)) ||'세' AS age,
      m1.text,
      m1.img_url AS imgUrl
    from cnsl_reg r
    join member m1 on m1.member_id = r.member_id
    join member m2 on m2.member_id = r.cnsler_id
    where m2.member_id = :email
        and r.cnsl_id = :cnslId
    	and r.del_yn = 'N'
    """, nativeQuery = true)
    CounselDetailDto getCounselDetail(@Param("cnslId") Long cnslId, @Param("email") String email);

    /** 상담사 목록 (진입 시 기본): cnslCate/cnslTp 바인딩 제거하여 500 방지. 가격 필터만 적용. */
    @Query(value = """
        select
            m.member_id      AS memberId,
            m.nickname       AS nickname,
            m.profile        AS profile,
            m.text           AS text,
            m.img_url        AS imgUrl,
            a.cate_1_cnt     AS cate1Cnt,
            a.cate_2_cnt     AS cate2Cnt,
            a.cate_3_cnt     AS cate3Cnt,
            a.cnsl_cnt       AS cnslCnt,
            a.review_cnt     AS reviewCnt,
            a.avg_eval_pt    AS avgEvalPt,
            ci.cnsl_1_price  AS cnsl1Price,
            ci.cnsl_2_price  AS cnsl2Price,
            ci.cnsl_3_price  AS cnsl3Price,
            ci.cnsl_4_price  AS cnsl4Price,
            ci.cnsl_5_price  AS cnsl5Price,
            ci.cnsl_6_price  AS cnsl6Price
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join (
            select member_id,
                max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price,
                max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price,
                max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price,
                max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price,
                max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price,
                max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price
            from cnsl_info group by member_id
        ) ci on m.member_id = ci.member_id
        left join (
            select crg.cnsler_id,
                sum(case when crg.cnsl_cate = '1' then 1 else 0 end) cate_1_cnt,
                sum(case when crg.cnsl_cate = '2' then 1 else 0 end) cate_2_cnt,
                sum(case when crg.cnsl_cate = '3' then 1 else 0 end) cate_3_cnt,
                count(crg.cnsl_id) cnsl_cnt,
                count(crw.review_id) review_cnt,
                coalesce(avg(crw.eval_pt), 0) avg_eval_pt
            from cnsl_reg crg
            left join cnsl_review crw on crg.cnsl_id = crw.cnsl_id
            where coalesce(crg.del_yn,'N') = 'N'
            group by crg.cnsler_id
        ) a on a.cnsler_id = m.member_id
        where 1=1
        and (:minPrice is null or (
            (ci.cnsl_1_price between :minPrice and :maxPrice) or (ci.cnsl_2_price between :minPrice and :maxPrice)
            or (ci.cnsl_3_price between :minPrice and :maxPrice) or (ci.cnsl_4_price between :minPrice and :maxPrice)
            or (ci.cnsl_5_price between :minPrice and :maxPrice) or (ci.cnsl_6_price between :minPrice and :maxPrice)
        ))
        order by a.cnsl_cnt desc, a.avg_eval_pt desc, m.member_id
    """, countQuery = """
        select count(m.member_id)
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join (
            select member_id,
                max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price,
                max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price,
                max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price,
                max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price,
                max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price,
                max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price
            from cnsl_info group by member_id
        ) ci on m.member_id = ci.member_id
        left join ( select cnsler_id from cnsl_reg where coalesce(del_yn,'N') = 'N' group by cnsler_id ) a on a.cnsler_id = m.member_id
        where 1=1
        and (:minPrice is null or ( (coalesce(ci.cnsl_1_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_2_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_3_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_4_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_5_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_6_price,0) between :minPrice and :maxPrice) ) )
    """, nativeQuery = true)
    Page<CounselorListDto> getCounselorList(Pageable pageable, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);

    /** 상담 유형(cnslCate)·상담 방식(cnslTp) 필터 적용. cnslCate/cnslTp는 null이 아닐 때만 호출하며, 서비스에서 null이면 전체값으로 채워서 전달 */
    @Query(value = """
        select
            m.member_id      AS memberId,
            m.nickname       AS nickname,
            m.profile        AS profile,
            m.text           AS text,
            m.img_url        AS imgUrl,
            a.cate_1_cnt     AS cate1Cnt,
            a.cate_2_cnt     AS cate2Cnt,
            a.cate_3_cnt     AS cate3Cnt,
            a.cnsl_cnt       AS cnslCnt,
            a.review_cnt     AS reviewCnt,
            a.avg_eval_pt    AS avgEvalPt,
            ci.cnsl_1_price  AS cnsl1Price,
            ci.cnsl_2_price  AS cnsl2Price,
            ci.cnsl_3_price  AS cnsl3Price,
            ci.cnsl_4_price  AS cnsl4Price,
            ci.cnsl_5_price  AS cnsl5Price,
            ci.cnsl_6_price  AS cnsl6Price
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join (
            select member_id,
                max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price,
                max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price,
                max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price,
                max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price,
                max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price,
                max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price
            from cnsl_info group by member_id
        ) ci on m.member_id = ci.member_id
        left join (
            select crg.cnsler_id,
                sum(case when crg.cnsl_cate = '1' then 1 else 0 end) cate_1_cnt,
                sum(case when crg.cnsl_cate = '2' then 1 else 0 end) cate_2_cnt,
                sum(case when crg.cnsl_cate = '3' then 1 else 0 end) cate_3_cnt,
                count(crg.cnsl_id) cnsl_cnt,
                count(crw.review_id) review_cnt,
                coalesce(avg(crw.eval_pt), 0) avg_eval_pt
            from cnsl_reg crg
            left join cnsl_review crw on crg.cnsl_id = crw.cnsl_id
            where coalesce(crg.del_yn,'N') = 'N'
            group by crg.cnsler_id
        ) a on a.cnsler_id = m.member_id
        where 1=1
        and ( exists ( select 1 from cnsl_reg crg2 where crg2.cnsler_id = m.member_id and crg2.cnsl_cate in (:cnslCate) and coalesce(crg2.del_yn,'N') = 'N' ) )
        and ( exists ( select 1 from cnsl_info ci2 where ci2.member_id = m.member_id and ci2.cnsl_tp in (:cnslTp) ) )
        and (:minPrice is null or ( (ci.cnsl_1_price between :minPrice and :maxPrice) or (ci.cnsl_2_price between :minPrice and :maxPrice) or (ci.cnsl_3_price between :minPrice and :maxPrice) or (ci.cnsl_4_price between :minPrice and :maxPrice) or (ci.cnsl_5_price between :minPrice and :maxPrice) or (ci.cnsl_6_price between :minPrice and :maxPrice) ) )
        order by a.cnsl_cnt desc, a.avg_eval_pt desc, m.member_id
    """, countQuery = """
        select count(m.member_id)
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join ( select member_id, max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price, max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price, max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price, max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price, max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price, max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price from cnsl_info group by member_id ) ci on m.member_id = ci.member_id
        left join ( select cnsler_id from cnsl_reg where coalesce(del_yn,'N') = 'N' group by cnsler_id ) a on a.cnsler_id = m.member_id
        where 1=1
        and ( exists ( select 1 from cnsl_reg crg2 where crg2.cnsler_id = m.member_id and crg2.cnsl_cate in (:cnslCate) and coalesce(crg2.del_yn,'N') = 'N' ) )
        and ( exists ( select 1 from cnsl_info ci2 where ci2.member_id = m.member_id and ci2.cnsl_tp in (:cnslTp) ) )
        and (:minPrice is null or ( (coalesce(ci.cnsl_1_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_2_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_3_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_4_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_5_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_6_price,0) between :minPrice and :maxPrice) ) )
    """, nativeQuery = true)
    Page<CounselorListDto> getCounselorListWithFilters(Pageable pageable, @Param("cnslCate") List<String> cnslCate, @Param("cnslTp") List<String> cnslTp, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);

    /** 상담 방식(cnslTp)만 필터. 상담 유형 미선택·상담 방식만 선택 시 사용 (cnsl_reg exists 없음) */
    @Query(value = """
        select
            m.member_id      AS memberId,
            m.nickname       AS nickname,
            m.profile        AS profile,
            m.text           AS text,
            m.img_url        AS imgUrl,
            a.cate_1_cnt     AS cate1Cnt,
            a.cate_2_cnt     AS cate2Cnt,
            a.cate_3_cnt     AS cate3Cnt,
            a.cnsl_cnt       AS cnslCnt,
            a.review_cnt     AS reviewCnt,
            a.avg_eval_pt    AS avgEvalPt,
            ci.cnsl_1_price  AS cnsl1Price,
            ci.cnsl_2_price  AS cnsl2Price,
            ci.cnsl_3_price  AS cnsl3Price,
            ci.cnsl_4_price  AS cnsl4Price,
            ci.cnsl_5_price  AS cnsl5Price,
            ci.cnsl_6_price  AS cnsl6Price
        from member m
        join member_role_list ml
          on m.member_id = ml.member_member_id
         and ml.member_role_list = 1
        left join (
            select member_id,
                   max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price,
                   max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price,
                   max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price,
                   max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price,
                   max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price,
                   max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price
            from cnsl_info
            group by member_id
        ) ci on m.member_id = ci.member_id
        left join (
            select crg.cnsler_id,
                   sum(case when crg.cnsl_cate = '1' then 1 else 0 end) cate_1_cnt,
                   sum(case when crg.cnsl_cate = '2' then 1 else 0 end) cate_2_cnt,
                   sum(case when crg.cnsl_cate = '3' then 1 else 0 end) cate_3_cnt,
                   count(crg.cnsl_id)          cnsl_cnt,
                   count(crw.review_id)        review_cnt,
                   coalesce(avg(crw.eval_pt), 0) avg_eval_pt
            from cnsl_reg crg
            left join cnsl_review crw
              on crg.cnsl_id = crw.cnsl_id
            where coalesce(crg.del_yn,'N') = 'N'
            group by crg.cnsler_id
        ) a on a.cnsler_id = m.member_id
        where exists (
            select 1
            from cnsl_info ci2
            where ci2.member_id = m.member_id
              and ci2.cnsl_tp in (:cnslTp)
        )
        and (
            :minPrice is null
            or (
                (ci.cnsl_1_price between :minPrice and :maxPrice)
             or (ci.cnsl_2_price between :minPrice and :maxPrice)
             or (ci.cnsl_3_price between :minPrice and :maxPrice)
             or (ci.cnsl_4_price between :minPrice and :maxPrice)
             or (ci.cnsl_5_price between :minPrice and :maxPrice)
             or (ci.cnsl_6_price between :minPrice and :maxPrice)
            )
        )
        order by a.cnsl_cnt desc, a.avg_eval_pt desc, m.member_id
    """, countQuery = """
        select count(m.member_id) from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join ( select member_id, max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price, max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price, max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price, max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price, max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price, max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price from cnsl_info group by member_id ) ci on m.member_id = ci.member_id
        left join ( select cnsler_id from cnsl_reg where coalesce(del_yn,'N') = 'N' group by cnsler_id ) a on a.cnsler_id = m.member_id
        where exists ( select 1 from cnsl_info ci2 where ci2.member_id = m.member_id and ci2.cnsl_tp in (:cnslTp) )
        and (:minPrice is null or ( (coalesce(ci.cnsl_1_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_2_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_3_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_4_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_5_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_6_price,0) between :minPrice and :maxPrice) ) )
    """, nativeQuery = true)
    Page<CounselorListDto> getCounselorListByMethod(Pageable pageable, @Param("cnslTp") List<String> cnslTp, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);

    /** 해시태그(취업/커리어) 필터 포함. hashTags는 null이 아닐 때만 호출 (null 시 getCounselorList 사용하여 cast 오류 방지) */
    @Query(value = """
        select
            m.member_id      AS memberId,
            m.nickname       AS nickname,
            m.profile        AS profile,
            m.text           AS text,
            m.img_url        AS imgUrl,
            a.cate_1_cnt     AS cate1Cnt,
            a.cate_2_cnt     AS cate2Cnt,
            a.cate_3_cnt     AS cate3Cnt,
            a.cnsl_cnt       AS cnslCnt,
            a.review_cnt     AS reviewCnt,
            a.avg_eval_pt    AS avgEvalPt,
            ci.cnsl_1_price  AS cnsl1Price,
            ci.cnsl_2_price  AS cnsl2Price,
            ci.cnsl_3_price  AS cnsl3Price,
            ci.cnsl_4_price  AS cnsl4Price,
            ci.cnsl_5_price  AS cnsl5Price,
            ci.cnsl_6_price  AS cnsl6Price
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join (
            select member_id,
                max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price,
                max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price,
                max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price,
                max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price,
                max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price,
                max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price
            from cnsl_info group by member_id
        ) ci on m.member_id = ci.member_id
        left join (
            select crg.cnsler_id,
                sum(case when crg.cnsl_cate = '1' then 1 else 0 end) cate_1_cnt,
                sum(case when crg.cnsl_cate = '2' then 1 else 0 end) cate_2_cnt,
                sum(case when crg.cnsl_cate = '3' then 1 else 0 end) cate_3_cnt,
                count(crg.cnsl_id) cnsl_cnt,
                count(crw.review_id) review_cnt,
                coalesce(avg(crw.eval_pt), 0) avg_eval_pt
            from cnsl_reg crg
            left join cnsl_review crw on crg.cnsl_id = crw.cnsl_id
            where coalesce(crg.del_yn,'N') = 'N'
            group by crg.cnsler_id
        ) a on a.cnsler_id = m.member_id
        where 1=1
        and ( :cnslCate is null or exists ( select 1 from cnsl_reg crg2 where crg2.cnsler_id = m.member_id and crg2.cnsl_cate in (:cnslCate) and coalesce(crg2.del_yn,'N') = 'N' ) )
        and ( :cnslTp is null or exists ( select 1 from cnsl_info ci2 where ci2.member_id = m.member_id and ci2.cnsl_tp in (:cnslTp) ) )
        and ( :minPrice is null or ( (ci.cnsl_1_price between :minPrice and :maxPrice) or (ci.cnsl_2_price between :minPrice and :maxPrice) or (ci.cnsl_3_price between :minPrice and :maxPrice) or (ci.cnsl_4_price between :minPrice and :maxPrice) or (ci.cnsl_5_price between :minPrice and :maxPrice) or (ci.cnsl_6_price between :minPrice and :maxPrice) ) )
        and ( m.hash_tags is not null and exists ( select 1 from jsonb_array_elements_text(m.hash_tags -> 'hashTag') as tag where tag = any(cast(:hashTags as text[])) ) )
        order by a.cnsl_cnt desc, a.avg_eval_pt desc, m.member_id
    """, countQuery = """
        select count(m.member_id)
        from member m
        join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1
        left join ( select member_id, max(case when cnsl_tp = '1' then cnsl_price else 0 end) cnsl_1_price, max(case when cnsl_tp = '2' then cnsl_price else 0 end) cnsl_2_price, max(case when cnsl_tp = '3' then cnsl_price else 0 end) cnsl_3_price, max(case when cnsl_tp = '4' then cnsl_price else 0 end) cnsl_4_price, max(case when cnsl_tp = '5' then cnsl_price else 0 end) cnsl_5_price, max(case when cnsl_tp = '6' then cnsl_price else 0 end) cnsl_6_price from cnsl_info group by member_id ) ci on m.member_id = ci.member_id
        left join ( select cnsler_id from cnsl_reg where coalesce(del_yn,'N') = 'N' group by cnsler_id ) a on a.cnsler_id = m.member_id
        where 1=1
        and ( :cnslCate is null or exists ( select 1 from cnsl_reg crg2 where crg2.cnsler_id = m.member_id and crg2.cnsl_cate in (:cnslCate) and coalesce(crg2.del_yn,'N') = 'N' ) )
        and ( :cnslTp is null or exists ( select 1 from cnsl_info ci2 where ci2.member_id = m.member_id and ci2.cnsl_tp in (:cnslTp) ) )
        and ( :minPrice is null or ( (coalesce(ci.cnsl_1_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_2_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_3_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_4_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_5_price,0) between :minPrice and :maxPrice) or (coalesce(ci.cnsl_6_price,0) between :minPrice and :maxPrice) ) )
        and ( m.hash_tags is not null and exists ( select 1 from jsonb_array_elements_text(m.hash_tags -> 'hashTag') as tag where tag = any(cast(:hashTags as text[])) ) )
    """, nativeQuery = true)
    Page<CounselorListDto> getCounselorListByHashTags(Pageable pageable, @Param("cnslCate") List<String> cnslCate, @Param("cnslTp") List<String> cnslTp, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice, @Param("hashTags") String[] hashTags);

    
    @Query(value="""
		select
		    m.member_id      AS memberId,
		    m.nickname       AS nickname,
		    m.profile        AS profile,
		    m.text           AS text,
			m.img_url 		 AS imgUrl,
		    a.cnsl_cnt       AS cnslCnt,
		    a.review_cnt     AS reviewCnt,
		    a.avg_eval_pt    AS avgEvalPt,
		    ci.cnsl_1_price  AS cnsl1Price,
		    ci.cnsl_2_price  AS cnsl2Price,
		    ci.cnsl_3_price  AS cnsl3Price,
		    ci.cnsl_4_price  AS cnsl4Price,
		    ci.cnsl_5_price  AS cnsl5Price,
		    ci.cnsl_6_price  AS cnsl6Price
		from member m -- member전제
		join member_role_list ml on m.member_id = ml.member_member_id and ml.member_role_list = 1  -- 상담사만 (1=SYSTEM). 역할 값은 member_role_list 컬럼에 저장됨
		left join (
		    select member_id,
		           max(case when cnsl_tp = '1' then cnsl_price else 0 end ) cnsl_1_price,
		           max(case when cnsl_tp = '2' then cnsl_price else 0 end ) cnsl_2_price,
		           max(case when cnsl_tp = '3' then cnsl_price else 0 end ) cnsl_3_price,
		           max(case when cnsl_tp = '4' then cnsl_price else 0 end ) cnsl_4_price,
		           max(case when cnsl_tp = '5' then cnsl_price else 0 end ) cnsl_5_price,
		           max(case when cnsl_tp = '6' then cnsl_price else 0 end ) cnsl_6_price
		    from cnsl_info
		    group by member_id
		) ci on m.member_id = ci.member_id
		left join (
		    select crg.cnsler_id,
		           count(crg.cnsl_id) cnsl_cnt,
		           count(crw.review_id) review_cnt,
		           ROUND(coalesce(avg(crw.eval_pt),0), 2) AS avg_eval_pt
		    from cnsl_reg crg
		    left join cnsl_review crw on crg.cnsl_id = crw.cnsl_id
		    where coalesce(crg.del_yn,'N') = 'N'
		    group by crg.cnsler_id
		) a on a.cnsler_id = m.member_id
		where m.member_id = :memberId
	""", nativeQuery = true)
    CounselorListDto getCounselor(@Param("memberId") String memberId);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Cnsl_Reg r
    SET r.memberId = :delMember
    WHERE r.memberId = :member
""")
    int updateMember(@Param("member") Member member,
                     @Param("delMember") Member deletedMember);
	
	// 마이페이지 상담 내역 리스트 (본인만, 정렬: 최신순)
	@Query(value = """
			select
			cr.cnsl_id,
			cr.cnsl_tp as cnslTp,
			get_code_nm('cnsl_tp', cr.cnsl_tp) as cnslType,
			cr.cnsl_title as cnslTitle,
			m.nickname,
			get_code_nm('cnsl_stat', cr.cnsl_stat) as cnslStat,
			cr.created_at as createdAt
			from cnsl_reg cr
			left join member m on m.member_id = cr.cnsler_id
			where cr.member_id = :memberId
			order by cr.created_at desc nulls last, cr.cnsl_id desc
			""", countQuery = "select count(*) from cnsl_reg cr where cr.member_id = :memberId", nativeQuery = true)
	Page<MyCnslListDto> findmycnsllist(@Param("memberId") String memberId, Pageable pageable);

	// 마이페이지 상담 내역 - AI 상담만 (cnsl_tp = '3')
	@Query(value = """
			select
			cr.cnsl_id,
			cr.cnsl_tp as cnslTp,
			get_code_nm('cnsl_tp', cr.cnsl_tp) as cnslType,
			cr.cnsl_title as cnslTitle,
			m.nickname,
			get_code_nm('cnsl_stat', cr.cnsl_stat) as cnslStat,
			cr.created_at as createdAt
			from cnsl_reg cr
			left join member m on m.member_id = cr.cnsler_id
			where cr.member_id = :memberId and cr.cnsl_tp = '3'
			order by cr.created_at desc nulls last, cr.cnsl_id desc
			""", countQuery = "select count(*) from cnsl_reg cr where cr.member_id = :memberId and cr.cnsl_tp = '3'", nativeQuery = true)
	Page<MyCnslListDto> findmycnsllistAi(@Param("memberId") String memberId, Pageable pageable);

	// 마이페이지 상담 내역 - 상담사 상담만 (cnsl_tp != '3')
	@Query(value = """
			select
			cr.cnsl_id,
			cr.cnsl_tp as cnslTp,
			get_code_nm('cnsl_tp', cr.cnsl_tp) as cnslType,
			cr.cnsl_title as cnslTitle,
			m.nickname,
			get_code_nm('cnsl_stat', cr.cnsl_stat) as cnslStat,
			cr.created_at as createdAt
			from cnsl_reg cr
			left join member m on m.member_id = cr.cnsler_id
			where cr.member_id = :memberId and (cr.cnsl_tp is null or cr.cnsl_tp <> '3')
			order by cr.created_at desc nulls last, cr.cnsl_id desc
			""", countQuery = "select count(*) from cnsl_reg cr where cr.member_id = :memberId and (cr.cnsl_tp is null or cr.cnsl_tp <> '3')", nativeQuery = true)
	Page<MyCnslListDto> findmycnsllistCounselor(@Param("memberId") String memberId, Pageable pageable);

	// 마이페이지 상담 내역 상세 페이지
	@Query(value = """
			select
			cr.cnsl_title,
			m1.nickname AS user_nickname,
			cr.cnsl_content,
			m2.nickname AS cnsler_name,
			m2.img_url AS cnsler_img_url,
			m2.text AS cnsler_text,
			m2.profile AS cnsler_profile,
			cr.cnsl_stat,
			cr.created_at
			from cnsl_reg cr
			left join member m1 on m1.member_id = cr.member_id
			left join member m2 on m2.member_id = cr.cnsler_id
			WHERE cr.cnsl_id = :cnslId AND cr.member_id = :memberId
			""", nativeQuery = true)
	Optional<CnslDetailDto> findcnslDetail(@Param("cnslId") Long cnslId, @Param("memberId") String memberId);

//	Chat_Msg save(Chat_Msg chatMsg);

	Optional<Cnsl_Reg> findByMemberId(Member member);

	/** 진행 중 AI 상담 1건 (마이페이지/재진입용) */
	Optional<Cnsl_Reg> findTopByMemberId_MemberIdAndCnslTpAndCnslStatOrderByCnslIdDesc(
			String memberId, String cnslTp, String cnslStat);
}
