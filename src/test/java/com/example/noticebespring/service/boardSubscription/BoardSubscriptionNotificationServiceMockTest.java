package com.example.noticebespring.service.boardSubscription;

import com.example.noticebespring.common.helper.RedisCacheHelper;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostNotificationRequestDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import com.example.noticebespring.entity.Board;
import com.example.noticebespring.repository.BoardRepository;
import com.example.noticebespring.repository.BoardSubscriptionRepository;
import com.example.noticebespring.repository.PostRepository;
import com.example.noticebespring.service.RabbitMqService;
import com.example.noticebespring.common.util.RedisKeyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BoardSubscriptionNotificationServiceMockTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private BoardSubscriptionRepository boardSubscriptionRepository;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Mock
    private RabbitMqService rabbitMqService;

    @InjectMocks
    private BoardSubscriptionNotificationService notificationService;

    @Test
    void 정상적으로_알림_발송_프로세스가_동작한다() {
        // Given
        // 1. 요청 데이터 준비
        Map<String, List<Integer>> postTypesMap = Map.of(
                "학사", Arrays.asList(101, 102),
                "일반", Arrays.asList(201)
        );
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);

        // 2. Mock 게시판 데이터 (Builder 패턴 또는 정적 팩토리 메서드 사용)
        Board mockBoard = Board.builder()
                .id(100)
                .name("자유게시판")
                .build();
        // 또는 정적 팩토리 메서드가 있다면: Board.of(100, "자유게시판");
        when(boardRepository.findById(100)).thenReturn(Optional.of(mockBoard));

        // 3. Mock 게시물 데이터 (hasReference, url 필드 추가)
        List<PostSummaryDto> mockPosts = Arrays.asList(
                new PostSummaryDto(100, 101, "학사", "학사 공지 1", "내용 요약1", false, "https://example.com/101", LocalDate.now()),
                new PostSummaryDto(100, 102, "학사", "학사 공지 2", "내용 요약2", true, "https://example.com/102", LocalDate.now()),
                new PostSummaryDto(100, 201, "일반", "일반 게시글 1", "내용 요약3", false, "https://example.com/201", LocalDate.now())
        );
        when(postRepository.findPostSummariesByPostIds(anyList()))
                .thenReturn(mockPosts);

        // 4. Mock 구독자 데이터
        List<UserSubscriptionInfoDto> mockSubscriptionInfos = Arrays.asList(
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "일반"),
                new UserSubscriptionInfoDto(2, "user2@email.com", 100, "학사")
        );
        when(boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(
                100, Arrays.asList("학사", "일반")))
                .thenReturn(mockSubscriptionInfos);

        // 5. RedisKeyUtil Mock 설정
        try (MockedStatic<RedisKeyUtil> mockedRedisKeyUtil = mockStatic(RedisKeyUtil.class)) {
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey(anyString(), eq(100), eq(101)))
                    .thenReturn("202506261230_100_101");
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey(anyString(), eq(100), eq(102)))
                    .thenReturn("202506261230_100_102");
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey(anyString(), eq(100), eq(201)))
                    .thenReturn("202506261230_100_201");

            // When
            notificationService.sendNewPostNotification(requestDto);

            // Then
            // 1. 게시판 조회가 호출되었는지 확인
            verify(boardRepository, times(1)).findById(100);

            // 2. 게시물 조회가 호출되었는지 확인
            verify(postRepository, times(1)).findPostSummariesByPostIds(Arrays.asList(101, 102, 201));

            // 3. 구독자 조회가 호출되었는지 확인
            verify(boardSubscriptionRepository, times(1))
                    .findUserSubscriptionInfoByBoardIdAndPostTypes(100, Arrays.asList("학사", "일반"));

            // 4. Redis에 게시물 수만큼 저장되었는지 확인 (3개 게시물)
            verify(redisCacheHelper, times(3)).cachePost(anyString(), any(PostSummaryDto.class), eq(3600L));

            // 5. 각 게시물이 올바른 키로 저장되었는지 확인
            verify(redisCacheHelper).cachePost(eq("202506261230_100_101"), any(PostSummaryDto.class), eq(3600L));
            verify(redisCacheHelper).cachePost(eq("202506261230_100_102"), any(PostSummaryDto.class), eq(3600L));
            verify(redisCacheHelper).cachePost(eq("202506261230_100_201"), any(PostSummaryDto.class), eq(3600L));

            // 6. RabbitMQ로 사용자 수만큼 메시지가 전송되었는지 확인 (2명의 사용자)
            verify(rabbitMqService, times(2)).sendEmailMessage(any(UserSubscriptionInfoGroupDto.class));
        }
    }

    @Test
    void 한_명의_사용자가_여러_postType을_구독한_경우_정상_처리된다() {
        // Given
        Map<String, List<Integer>> postTypesMap = Map.of(
                "학사", Arrays.asList(101),
                "글로벌", Arrays.asList(301)
        );
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);

        Board mockBoard = Board.builder()
                .id(100)
                .name("자유게시판")
                .build();
        when(boardRepository.findById(100)).thenReturn(Optional.of(mockBoard));

        // hasReference, url 필드 추가
        List<PostSummaryDto> mockPosts = Arrays.asList(
                new PostSummaryDto(100, 101, "학사", "학사 공지", "내용 요약", true, "https://example.com/101", LocalDate.now()),
                new PostSummaryDto(100, 301, "글로벌", "글로벌 공지", "내용 요약", false, "https://example.com/301", LocalDate.now())
        );
        when(postRepository.findPostSummariesByPostIds(anyList()))
                .thenReturn(mockPosts);

        // 한 사용자가 두 postType 모두 구독
        List<UserSubscriptionInfoDto> mockSubscriptionInfos = Arrays.asList(
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "글로벌")
        );
        when(boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(
                100, Arrays.asList("학사", "글로벌")))
                .thenReturn(mockSubscriptionInfos);

        try (MockedStatic<RedisKeyUtil> mockedRedisKeyUtil = mockStatic(RedisKeyUtil.class)) {
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey(anyString(), eq(100), anyInt()))
                    .thenReturn("test_key");

            // When
            notificationService.sendNewPostNotification(requestDto);

            // Then
            // 1. Redis에 2개 게시물 저장 확인
            verify(redisCacheHelper, times(2)).cachePost(anyString(), any(PostSummaryDto.class), eq(3600L));

            // 2. RabbitMQ로 1개 메시지 전송 확인 (1명의 사용자)
            verify(rabbitMqService, times(1)).sendEmailMessage(any(UserSubscriptionInfoGroupDto.class));
        }
    }

    @Test
    void 구독자가_없으면_Redis_저장과_RabbitMQ_발송을_하지_않는다() {
        // Given
        Map<String, List<Integer>> postTypesMap = Map.of("학사", Arrays.asList(101));
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);

        Board mockBoard = Board.builder()
                .id(100)
                .name("자유게시판")
                .build();
        when(boardRepository.findById(100)).thenReturn(Optional.of(mockBoard));

        // hasReference, url 필드 추가
        List<PostSummaryDto> mockPosts = Arrays.asList(
                new PostSummaryDto(100, 101, "학사", "학사 공지", "내용 요약", false, "https://example.com/101", LocalDate.now())
        );
        when(postRepository.findPostSummariesByPostIds(anyList()))
                .thenReturn(mockPosts);

        // 구독자가 없음
        when(boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(100, Arrays.asList("학사")))
                .thenReturn(Collections.emptyList());

        // When
        notificationService.sendNewPostNotification(requestDto);

        // Then
        // 1. Redis 저장이 호출되지 않았는지 확인
        verify(redisCacheHelper, never()).cachePost(anyString(), any(PostSummaryDto.class), anyLong());

        // 2. RabbitMQ 발송이 호출되지 않았는지 확인
        verify(rabbitMqService, never()).sendEmailMessage(any(UserSubscriptionInfoGroupDto.class));

        // 3. 하지만 게시판과 게시물 조회는 여전히 호출되었는지 확인
        verify(boardRepository, times(1)).findById(100);
        verify(postRepository, times(1)).findPostSummariesByPostIds(Arrays.asList(101));
    }

    @Test
    void Redis_저장_후_RabbitMQ_발송_순서가_올바른지_확인한다() {
        // Given
        Map<String, List<Integer>> postTypesMap = Map.of("학사", Arrays.asList(101));
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);

        Board mockBoard = Board.builder()
                .id(100)
                .name("자유게시판")
                .build();
        when(boardRepository.findById(100)).thenReturn(Optional.of(mockBoard));

        // hasReference, url 필드 추가
        List<PostSummaryDto> mockPosts = Arrays.asList(
                new PostSummaryDto(100, 101, "학사", "학사 공지", "내용 요약", true, "https://example.com/101", LocalDate.now())
        );
        when(postRepository.findPostSummariesByPostIds(anyList()))
                .thenReturn(mockPosts);

        List<UserSubscriptionInfoDto> mockSubscriptionInfos = Arrays.asList(
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사")
        );
        when(boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(100, Arrays.asList("학사")))
                .thenReturn(mockSubscriptionInfos);

        try (MockedStatic<RedisKeyUtil> mockedRedisKeyUtil = mockStatic(RedisKeyUtil.class)) {
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey(anyString(), eq(100), eq(101)))
                    .thenReturn("test_key");

            // When
            notificationService.sendNewPostNotification(requestDto);

            // Then - 실행 순서 확인
            InOrder inOrder = inOrder(redisCacheHelper, rabbitMqService);

            // Redis 저장이 먼저 호출되고
            inOrder.verify(redisCacheHelper).cachePost(anyString(), any(PostSummaryDto.class), eq(3600L));

            // 그 다음 RabbitMQ 발송이 호출되어야 함
            inOrder.verify(rabbitMqService).sendEmailMessage(any(UserSubscriptionInfoGroupDto.class));
        }
    }
}