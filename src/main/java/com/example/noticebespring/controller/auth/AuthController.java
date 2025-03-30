package com.example.noticebespring.controller.auth;

import com.example.noticebespring.entity.User;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.auth.jwt.JwtService;
import com.example.noticebespring.service.auth.social.SocialTokenServiceFactory;
import com.example.noticebespring.service.auth.social.SocialUserInfoServiceFactory;
import com.example.noticebespring.service.auth.social.token.SocialTokenService;
import com.example.noticebespring.service.auth.social.user.SocialUserInfoService;
import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "소셜로그인 API", description = "OAuth 2.0 기반의 로그인 & 로그아웃 지원")
public class AuthController {
    private final SocialTokenServiceFactory socialTokenServiceFactory;
    private final SocialUserInfoServiceFactory socialUserInfoServiceFactory;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;


    @Operation(
            summary = "소셜 로그인",
            description = "인증 프로바이더(구글, 카카오, 네이버)를 통해 로그인 후 자체적으로 JWT 토큰 발급",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    }),
                    @ApiResponse(responseCode = "400", description = "해당 프로바이더에 대한 서비스가 존재하지 않음"),
                    @ApiResponse(responseCode = "401", description = "인증에 실패함"),
                    @ApiResponse(responseCode = "404", description = "사용자 정보 등록 실패"),
                    @ApiResponse(responseCode = "500", description = "JWT 토큰 발급에 실패함")
            },
            parameters = {
                    @Parameter(name = "provider", description = "소셜 제공자 (ex: google, kakao, naver)",in = ParameterIn.PATH, required = true),
                    @Parameter(name = "code", description = "인가 코드",in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "state", description = "state(네이버에만 적용)", in = ParameterIn.QUERY, required = false)
            }
            )
    @PostMapping("/login/{provider}")
    public CommonResponse<String> socialLogin(@PathVariable String provider, @RequestParam String code, @RequestParam(required = false) String state){
            log.info("소셜 로그인 요청 - provider: {}, code: {}, state: {}", provider, code, state);

            //1. 프로바이더에 따른 서비스 불러오기
            SocialTokenService socialTokenService = socialTokenServiceFactory.getService(provider);
            SocialUserInfoService socialUserInfoService = socialUserInfoServiceFactory.getService(provider);

            //2. 액세스 토큰 발급
            String accessToken = socialTokenService.getToken(code, state);
            if (accessToken == null || accessToken.isEmpty()){
                log.warn("액세스 토큰 발급 실패 - provider: {}", provider);
                return CommonResponse.fail(ErrorCode.UNAUTHORIZED);
            }
            log.debug("액세스 토큰 발급 성공 - provider: {}, token: {}", provider, accessToken);


            //3. 사용자 정보 처리
            User user = socialUserInfoService.processUser(accessToken);
            if(user == null){
                log.error("사용자 정보 처리 실패 - provider: {}", provider);
                return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            log.info("사용자 정보 처리 성공 - provider: {}, userId: {}", provider, user.getId());

            //4. JWT 토큰 발급
            String jwtToken = jwtService.generateToken(user.getId(), user.getEmail());
            if (jwtToken == null || jwtToken.isEmpty()){
                log.error("JWT 토큰 발급 실패 - userId: {}", user.getId());
                return CommonResponse.fail(ErrorCode.JWT_GENERATION_FAILED);
            }
            log.info("소셜 로그인 완료 - provider: {}, userId: {}, jwtToken: {}", provider, user.getId(), jwtToken);

            return CommonResponse.ok(jwtToken);

    }

    @Operation(
            summary = "로그아웃",
            description = "JWT 토큰으로 로그아웃. 클라이언트에서 토큰을 삭제해줘야 함.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공",content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    }),
                    @ApiResponse(responseCode = "401", description = "JWT 토큰이 유효하지 않음")

            },
            parameters = {
                    @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
            }

    )
    @PostMapping("/logout")
    public CommonResponse<Void> logout() {
        // 클라이언트가 알아서 토큰을 삭제하도록
        SecurityContextHolder.clearContext();
        log.info("로그아웃 성공");
        return CommonResponse.ok(null);
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 JWT 토큰으로 계정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공",content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    }),
                    @ApiResponse(responseCode = "401", description = "JWT 토큰이 유효하지 않음"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾는데 실패함"),
                    @ApiResponse(responseCode = "500", description = "회원 탈퇴에 실패함")

            },
            parameters = {
            @Parameter(name = "Authorization", description = "Authorization 헤더에 JWT 토큰 추가", in = ParameterIn.HEADER, required = true)
    }
    )
    @PostMapping("/withdraw")
    @Transactional // 트랜잭션 관리
    public CommonResponse<Void> withdraw() {
        log.info("회원 탈퇴 요청 수신");
        Integer userId = userService.getAuthenticatedUser().getId();

        try {
            // 관련 소셜 계정 삭제
            socialAccountRepository.deleteByUserId(userId);
            // 사용자 삭제
            userRepository.deleteById(userId);
            SecurityContextHolder.clearContext();
            log.info("회원 탈퇴 성공 - userId: {}", userId);
            return CommonResponse.ok(null);
        } catch (Exception e) {
            log.error("회원 탈퇴 실패 - userId: {}", userId, e);
            throw new RuntimeException("회원 탈퇴에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
