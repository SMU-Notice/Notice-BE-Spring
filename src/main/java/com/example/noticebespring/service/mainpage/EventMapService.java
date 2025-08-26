package com.example.noticebespring.service.mainpage;

import com.example.noticebespring.dto.main.EventMapDto;
import com.example.noticebespring.repository.EventLocationTimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class EventMapService {
    private final EventLocationTimeRepository eventLocationTimeRepository;

    public EventMapService(EventLocationTimeRepository eventLocationTimeRepository) {
        this.eventLocationTimeRepository = eventLocationTimeRepository;
    }

    // 지도에 보여질 이벤트 조회
    @Transactional(readOnly = true)
    public List<EventMapDto> getEventsVisibleOnMap(Integer userId) {
        //현재 날짜 가져오기
        LocalDate date = LocalDate.now();
        log.debug("이벤트 조회 시작 - userId: {}, date: {}", userId, date);
        List<EventMapDto> events = eventLocationTimeRepository.findEventsVisibleOnMap(date, userId);
        log.info("이벤트 조회 성공 - {}개", events.size());

        return events;
    }
}
