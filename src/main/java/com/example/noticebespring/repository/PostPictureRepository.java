package com.example.noticebespring.repository;

import com.example.noticebespring.entity.PostPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostPictureRepository extends JpaRepository<PostPicture, Integer> {
    List<PostPicture> findByPost_Id(Integer postId);
}
