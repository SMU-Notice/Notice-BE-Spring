package com.example.noticebespring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "email", length = 255, unique = true)
    private String email;


    @Column(name =  "major", length = 30)
    private String major;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String email, String major, LocalDateTime createdAt) {
        this.email = email;
        this.major = major;
        this.createdAt = createdAt;
    }

    public User(Integer id, String email, String major, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.major = major;
        this.createdAt = createdAt;
    }

}
