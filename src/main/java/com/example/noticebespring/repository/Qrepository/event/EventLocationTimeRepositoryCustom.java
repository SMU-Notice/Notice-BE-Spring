package com.example.noticebespring.repository.Qrepository.event;

import com.example.noticebespring.dto.main.EventMapDto;

import java.time.LocalDate;
import java.util.List;

public interface EventLocationTimeRepositoryCustom {
    List<EventMapDto> findEventsVisibleOnMap(LocalDate date, Integer userId);
}
