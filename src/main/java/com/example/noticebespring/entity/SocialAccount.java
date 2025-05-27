package com.example.noticebespring.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "social_account")
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    public enum Provider {
        GOOGLE, KAKAO, NAVER
    }
}

