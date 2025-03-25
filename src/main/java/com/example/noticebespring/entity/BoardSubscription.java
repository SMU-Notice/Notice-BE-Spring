package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardSubscription {

    @EmbeddedId
    private BoardSubscriptionId id;  // 복합키

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "board_id")
    private Integer boardId;

    @Column(name = "post_type", length = 30, nullable = false)
    private String postType;
}
