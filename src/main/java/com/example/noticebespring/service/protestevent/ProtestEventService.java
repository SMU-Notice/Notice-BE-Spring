package com.example.noticebespring.service.protestevent;

import com.example.noticebespring.dto.protestevent.*;
import com.example.noticebespring.entity.ProtestEvent;
import com.example.noticebespring.repository.ProtestEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class ProtestEventService {

    private final ProtestEventRepository repository;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");	

    public LocalDate todaySeoul() {
        return LocalDate.now(SEOUL);	//시간을 서울 기준으로 고정
    }
    /**
     * 조회 시 기준일(오늘)과 다음날(내일)을 함께 반환하도록 설정
     * 기준일이 오늘인 경우 내일 블록이 비고 현재시각<19:00이면
     * 내일의 시위정보 항목은 "아직 시위정보가 없습니다. (19:00 이후 업데이트)"로 안내문 이용
     */
    @Transactional(readOnly = true)
    public ProtestEventAllResponseDto getAllDays(LocalDate baseDate) {
        boolean baseIsToday = baseDate.equals(todaySeoul());

        ProtestEventDailyDto todayEvent    = buildDaily(baseDate, false, baseIsToday);
        ProtestEventDailyDto tomorrowEvent = buildDaily(baseDate.plusDays(1), true,  baseIsToday);

        return new ProtestEventAllResponseDto(todayEvent, tomorrowEvent);
    }
    /* 하루치 시위정보 구성 */
    private ProtestEventDailyDto buildDaily(LocalDate date, boolean isTomorrow, boolean baseIsToday) {
        List<ProtestEventItemDto> items = repository
                .findByProtestDateOrderByStartTimeAscIdAsc(date) // 정렬시 시간 순 정렬 후 같을 시 id 순 정렬
                .stream()
                .map(this::toItem)
                .toList();
        
        
        String message = null;
        
        if (items.isEmpty()) {
            if (isTomorrow && baseIsToday && LocalTime.now(SEOUL).isBefore(LocalTime.of(19, 0))) {
                message = "아직 시위 일정이 없습니다. (19:00 이후 업데이트됩니다.)";	//19시 이전에 조회 시 내일 정보에 해당 안내문
            } else {
                message = "시위 일정이 존재하지 않습니다.";	//오늘 시위정보가 없거나 19시 이후에도 내일 시위정보가 없을 시
            }
        }
        
        return new ProtestEventDailyDto(date, items.size(), items, message);
    }
        
        private ProtestEventItemDto toItem(ProtestEvent e) {
            return new ProtestEventItemDto(
                    e.getLocation(),
                    e.getProtestDate(),
                    e.getStartTime(),
                    e.getEndTime()
            );
        }
    }

