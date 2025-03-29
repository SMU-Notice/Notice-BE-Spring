package com.example.noticebespring.controller.auth;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.dto.sneaky.UserLoginForm;
import com.example.noticebespring.dto.sneaky.UserRegisterDto;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.auth.jwt.JwtService;
import com.example.noticebespring.common.response.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = " API", description = "OAuth 2.0 기반의 로그인 & 로그아웃 지원")
public class SneakyAuthController {
    private final JwtService jwtService;
    private final UserService userService;
    @PostMapping("/sneaky/register")
    public CommonResponse<String> register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto);

        return CommonResponse.ok("회원가입 성공");
    }


    @PostMapping("/sneaky/login")
    public CommonResponse<String> login(@RequestBody UserLoginForm loginForm, HttpServletResponse response){

        User user = userService.getUserByEmail(loginForm.email());

        // 4. JWT 토큰 발급
        String jwtToken = jwtService.generateToken(user.getId(), "nothing");
        if (jwtToken == null || jwtToken.isEmpty()) {
            return CommonResponse.fail(ErrorCode.JWT_GENERATION_FAILED);
        }
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);

        return CommonResponse.ok("로그인 성공");

    }
}

