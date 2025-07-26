package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "board")
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "campus", nullable = false)
    @Enumerated(EnumType.STRING)
    private Campus campus;

    @Column(name = "site", nullable = false)
    private String site;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    public enum Campus {
        sangmyung, seoul
    }

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    /**
     * Campus enum을 한국어 문자열로 반환
     * @return campus의 한국어 표현 ("상명" 또는 "서울")
     */
    public String getKoreanStringCampus() {
        if (campus == null) {
            return null;
        }

        return switch (campus) {
            case sangmyung -> "상명";
            case seoul -> "서울";
        };
    }
}