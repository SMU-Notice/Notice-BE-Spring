package com.example.noticebespring.repository;

import com.example.noticebespring.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Integer> {
    Optional<SocialAccount> findByProviderAndProviderId(SocialAccount.Provider provider, String providerId);
    void deleteByUserId(Integer userId);
}
