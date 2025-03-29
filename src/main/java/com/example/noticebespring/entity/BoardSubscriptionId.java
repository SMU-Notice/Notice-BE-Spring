package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@AllArgsConstructor
public class BoardSubscriptionId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "board_id")
    private Integer boardId;

    @Column(name = "post_type", length = 30, nullable = false)
    private String postType;

    public BoardSubscriptionId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardSubscriptionId that = (BoardSubscriptionId) o;
        return Objects.equals(user.getId(), that.user.getId()) &&
                Objects.equals(boardId, that.boardId) &&
                Objects.equals(postType, that.postType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId(), boardId, postType);
    }
}
