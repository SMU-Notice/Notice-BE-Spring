package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.ApiResponse;
import com.example.noticebespring.dto.TopViewDto;
import com.example.noticebespring.service.MainService;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MainController {
    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/main")
    public ApiResponse<List<TopViewDto>> getTop7PostsByBoardName(){
        List<TopViewDto> topViewDtoList = mainService.getTop7PostsByBoardName("통합공지");
        return ApiResponse.ok(topViewDtoList);
    }

}

