package com.example.noticebespring.service.email;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.common.util.RedisUtil;
import com.example.noticebespring.dto.email.EmailDto;
import com.example.noticebespring.dto.email.EmailVerificationDto;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.service.auth.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private static final String senderEmail = "jj@naver.com";

    /**
     * 무작위 6자리 코드 생성
     * @return
     */
    private static String createCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 57; // number '9'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)  // 숫자 0부터 9까지
                .limit(targetStringLength)  // 6자리 길이로 제한
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * 이메일 내용 초기화
     * @param code
     * @return
     */
    // 이메일 내용 초기화
    private String setVerificationEmailContext(String code) {
        Context context = new Context();
        context.setVariable("code", code); // 코드 값 템플릿에 전달

        // Thymeleaf 템플릿 파일을 사용하여 HTML 생성
        return templateEngine.process("verification-email", context);
    }

    /**
     * 이메일 폼 생성
     * @param emailDto
     * @return
     * @throws MessagingException */

    private MimeMessage createEmailForm(EmailDto emailDto, String authCode) throws MessagingException {

        String email = emailDto.email();

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 인증번호입니다.");
        message.setFrom(senderEmail);
        message.setText(setVerificationEmailContext(authCode), "utf-8", "html");


        return message;
    }

    /**
     * 인증코드가 담긴 이메일 송신
     * @param emailDto
     * @throws MessagingException */

    public void sendEmail(EmailDto emailDto) throws MessagingException {
        String toEmail = emailDto.email();
        String purpose = "verify";
        String redisKey = purpose + ":" + toEmail;

        // Redis 설정
        if (redisUtil.existData(redisKey)) {
            redisUtil.deleteData(redisKey);
        }

        String authCode = createCode();

        // Redis 에 해당 인증코드 인증 시간 설정
        redisUtil.setDataExpire(redisKey, authCode, 60 * 30L);

        // 이메일 폼 생성
        MimeMessage emailForm = createEmailForm(emailDto, authCode);

        // 이메일 송신
        javaMailSender.send(emailForm);
    }

    /**
     * 인증 코드 일치 여부 검증
     * @param emailDto
     * @return
     */
    @Transactional
    public Boolean verifyEmailCode(EmailVerificationDto emailDto) {
        String purpose = "verify";
        String email = emailDto.email();
        String code = emailDto.verificationCode();

        String redisKey = purpose + ":" + email;

        // Redis에서 코드 가져오기
        String codeFoundByEmail = redisUtil.getData(redisKey);
        log.info("code found by email: " + codeFoundByEmail);

        // 코드가 없거나 다르면 실패 처리
        if (codeFoundByEmail == null || !codeFoundByEmail.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_VERIFY_CODE); // 유효하지 않은 코드
        }

        // 인증된 유저 조회
        User user = userService.getAuthenticatedUser();
        // 이메일 업데이트
        user.setEmail(email);  // 기존 유저의 이메일 수정
        userRepository.save(user);  // 유저 정보 업데이트

        return true;
    }

}
