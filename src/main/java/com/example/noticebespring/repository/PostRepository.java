package com.example.noticebespring.repository;

import com.example.noticebespring.repository.Qrepository.post.AllNoticePostRepositoryCustom;
import com.example.noticebespring.repository.Qrepository.post.RecentNoticePostRepositoryCustom;
import com.example.noticebespring.repository.Qrepository.post.TopViewPostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.noticebespring.entity.Post;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, TopViewPostRepositoryCustom, AllNoticePostRepositoryCustom, RecentNoticePostRepositoryCustom {

    Optional<Post> findPreviousId(Integer id);
    Optional<Post> findNextId(Integer id);
}
