package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "protest_event")
public class ProtestEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "location", length = 30, nullable = false)
    private String location;		//시위 장소

    @Column(name = "protest_date", nullable = false)
    private LocalDate protestDate;	//시위 날짜

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;	//시위 시작 시간

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;		//시위 종료 시간
}
