package com.study.spring.wallet.repository;

import com.study.spring.Member.entity.Member;
import com.study.spring.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    @Query(value= """
        select *
        from wallet 
        where member_id = :email
    """, nativeQuery = true)
    Optional<Wallet> findByEmail(@Param("email")  String email);

	Optional<Wallet> findByMember(Member member);

    void deleteByMemberId(String memberId);
}
