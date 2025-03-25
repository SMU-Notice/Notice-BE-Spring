package com.example.noticebespring.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
public class BoardSubscriptionId implements Serializable {

    private Integer userId;
    private Integer boardId;
    private String postType;

    public BoardSubscriptionId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardSubscriptionId that = (BoardSubscriptionId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(boardId, that.boardId) &&
                Objects.equals(postType, that.postType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, boardId, postType);
    }
}
