package com.example.noticebespring.service.auth;

import com.example.noticebespring.domain.User;
import com.example.noticebespring.service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.noticebespring.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // JWT로 이미 검증된 사용자에 대한 정보를 User 엔티티로 반환
    public Optional<User> getUserFromValidatedToken(String token){
        Integer userId = jwtService.extractUserId(token);
        if(userId == null)
        {
            return Optional.empty();
        }
        return userRepository.findById(userId);

    }

}
