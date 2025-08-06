package com.example.noticebespring.repository;

import com.example.noticebespring.entity.EventLocationTime;
import com.example.noticebespring.repository.Qrepository.event.EventLocationTimeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLocationTimeRepository extends JpaRepository<EventLocationTime, Integer>, EventLocationTimeRepositoryCustom {
}
