package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "original_post_id", nullable = false, unique = true)
    private Integer originalPostId;

    @Column(name = "type",length = 30, nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content_summary", nullable = false, columnDefinition = "TEXT")
    private String contentSummary;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "has_reference", nullable = false)
    private Boolean hasReference = false;

    @Column(name = "posted_date", nullable = false)
    private LocalDate postedDate;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

}
