package com.example.noticebespring.repository;

import com.example.noticebespring.repository.Qrepository.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.noticebespring.entity.Post;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {
}
