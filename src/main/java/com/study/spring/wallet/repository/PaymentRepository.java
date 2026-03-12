package com.study.spring.wallet.repository;

import java.util.Optional;

import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.study.spring.wallet.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{

	@Query(value="""
		select * from payment where imp_uid = :orderId
	""", nativeQuery = true)
	Optional<Payment> findByOrderId(@Param("orderId") String orderId);

	void deleteByMemberId(Member member);
}
