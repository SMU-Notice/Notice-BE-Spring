package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_picture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "post_id", nullable = false) //사진이 있던 게시글
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false) //사진 URL
    private String url;

    @Column(name = "picture_summary", columnDefinition = "TEXT") //사진 요약
    private String pictureSummary;
}
