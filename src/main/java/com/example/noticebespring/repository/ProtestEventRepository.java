package com.example.noticebespring.repository;

import com.example.noticebespring.entity.ProtestEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


//날짜 별 시위 일정 

public interface ProtestEventRepository extends JpaRepository<ProtestEvent, Integer> {
    List<ProtestEvent> findByProtestDateOrderByStartTimeAscIdAsc(LocalDate date);
}

//시작 시간을 기준으로 오름차순 정렬, 시간이 같은 경우 id 순서로 정렬