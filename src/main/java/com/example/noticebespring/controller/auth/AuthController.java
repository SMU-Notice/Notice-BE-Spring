package com.example.noticebespring.controller.auth;

import com.example.noticebespring.entity.User;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.jwt.JwtService;
import com.example.noticebespring.service.auth.social.SocialTokenServiceFactory;
import com.example.noticebespring.service.auth.social.SocialUserInfoServiceFactory;
import com.example.noticebespring.service.auth.social.token.SocialTokenService;
import com.example.noticebespring.service.auth.social.user.SocialUserInfoService;
import com.example.noticebespring.common.response.ApiResponse;
import com.example.noticebespring.common.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "소셜로그인 API", description = "OAuth 2.0 기반의 로그인 & 로그아웃 지원")
public class AuthController {
    private final SocialTokenServiceFactory socialTokenServiceFactory;
    private final SocialUserInfoServiceFactory socialUserInfoServiceFactory;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Operation(summary = "소셜 로그인", description = "인증 프로바이더(구글, 카카오, 네이버)를 통해 로그인 후 자체적으로 JWT 토큰 발급")
    @Parameters({
            @Parameter(name = "provider", description = "소셜 제공자 (ex: google, kakao, naver)", required = true),
            @Parameter(name = "code", description = "인가 코드", required = true)
    })
    @PostMapping("/login/{provider}")
    public ApiResponse<String> socialLogin(@PathVariable String provider, @RequestParam String code, @RequestParam(required = false) String state ){

            //1. 프로바이더에 따른 서비스 불러오기
            SocialTokenService socialTokenService = socialTokenServiceFactory.getService(provider);
            SocialUserInfoService socialUserInfoService = socialUserInfoServiceFactory.getService(provider);

            //2. 액세스 토큰 발급
            String accessToken = socialTokenService.getToken(code, state);
            if (accessToken == null || accessToken.isEmpty()){

                return ApiResponse.fail(ErrorCode.UNAUTHORIZED);
            }

            //3. 사용자 정보 처리
            User user = socialUserInfoService.processUser(accessToken);
            if(user == null){
                return ApiResponse.fail(ErrorCode.NOT_FOUND_USER);
            }

            //4. JWT 토큰 발급
            String jwtToken = jwtService.generateToken(user.getId(), user.getEmail());
            if (jwtToken == null || jwtToken.isEmpty()){
                return ApiResponse.fail(ErrorCode.JWT_GENERATION_FAILED);
            }

            return ApiResponse.ok(jwtToken);

    }

    @Operation(summary = "로그아웃", description = "현재 JWT 토큰으로 로그아웃합니다. 클라이언트에서 토큰을 삭제해줘야 합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = jwtService.extractToken(request); // JWT 토큰 추출 로직
        if (token == null || !jwtService.isTokenValid(token)) {
            return ApiResponse.fail(ErrorCode.JWT_TOKEN_ERROR);
        }
        // 클라이언트가 알아서 토큰을 삭제하도록
        SecurityContextHolder.clearContext();
        return ApiResponse.ok(null);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 JWT 토큰으로 계정을 삭제합니다.")
    @PostMapping("/withdraw")
    @Transactional // 트랜잭션 관리
    public ApiResponse<Void> withdraw(HttpServletRequest request) {
        String token = jwtService.extractToken(request);
        if (token == null || !jwtService.isTokenValid(token)) {
            return ApiResponse.fail(ErrorCode.JWT_TOKEN_ERROR);
        }

        Integer userId = jwtService.extractUserId(token);
        if (userId == null) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND_USER);
        }

        try {
            // 관련 소셜 계정 삭제
            socialAccountRepository.deleteByUserId(userId);
            // 사용자 삭제
            userRepository.deleteById(userId);
            SecurityContextHolder.clearContext();
            return ApiResponse.ok(null);
        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
