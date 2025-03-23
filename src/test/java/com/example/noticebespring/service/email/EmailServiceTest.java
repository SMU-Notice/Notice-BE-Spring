//package com.example.noticebespring.service.email;
//
//import com.example.noticebespring.common.util.RedisUtil;
//import com.example.noticebespring.dto.email.EmailDto;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mail.javamail.JavaMailSender;
//
//@ExtendWith(MockitoExtension.class)
//public class EmailServiceTest {
//
//    @Mock
//    private JavaMailSender javaMailSender;  // 실제 이메일 서버 대신 모킹 객체 사용
//
//    @Mock
//    private RedisUtil redisUtil;  // 실제 Redis 대신 모킹 객체 사용
//
//    @InjectMocks
//    private EmailService emailService;  // 테스트 대상 클래스
//
//    @Test
//    void testSendEmail() throws MessagingException {
//        // Given
//        EmailDto emailDto = new EmailDto("test@example.com");
//
//        // When
//        emailService.sendEmail(emailDto);  // 메일 발송 메소드 호출
//
//        // Then
//        // 메일이 실제로 보내지지 않고, JavaMailSender의 send 메소드가 한 번 호출되었는지 확인
//        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
//
//        // Redis에 인증 코드가 설정되었는지 확인
//        verify(redisUtil, times(1)).setDataExpire(anyString(), anyString(), anyLong());
//    }
//}
