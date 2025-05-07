package com.example.noticebespring.service.auth;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.sneaky.UserRegisterDto;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.noticebespring.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // JWT로 이미 검증된 사용자에 대한 정보를 User 엔티티로 반환
    public User getUserFromValidatedToken(String token) {
        Integer userId = Optional.ofNullable(jwtService.extractUserId(token))
                .orElseThrow(() -> new CustomException(ErrorCode.JWT_TOKEN_ERROR));

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    /**
     * 인증된 사용자 정보를 SecurityContext에서 가져옵니다.
     *
     * @return 인증된 사용자 정보
     * @throws CustomException JWT 토큰이 없거나 인증되지 않은 경우, 또는 사용자가 데이터베이스에 존재하지 않는 경우
     */
    public User getAuthenticatedUser() {
        // SecurityContext에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 인증되지 않은 경우 예외 처리
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.JWT_TOKEN_ERROR);
        }

        Integer userId;
        try {
            // Principal에서 사용자 ID 추출
            userId = Integer.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            // ID 추출 실패 시 예외 처리
            throw new CustomException(ErrorCode.JWT_TOKEN_ERROR);
        }

        // 데이터베이스에서 사용자 조회
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    /**
     * 사용자 회원가입
     * @param userRegisterDto
     * @return
     */
    @Transactional
    public User createUser(UserRegisterDto userRegisterDto) {

        User user = User.builder()
                .email(userRegisterDto.email())
                .build();

        return userRepository.save(user);
    }

    // 이메일로 유저 찾기
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
    }



}
