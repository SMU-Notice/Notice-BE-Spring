package com.example.noticebespring.service.boardSubscription;

import com.example.noticebespring.dto.boardSubscription.postNotification.PostNotificationRequestDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class BoardSubscriptionNotificationServiceTest {

    private BoardSubscriptionNotificationService boardSubscriptionNotificationService;

    @BeforeEach
    void setUp() {
        boardSubscriptionNotificationService = new BoardSubscriptionNotificationService(
                null, null, null, null, null  // 일단 null로 두고 테스트
        );
    }

    @Test
    void 한_사용자가_3개_postType을_모두_구독한다() {
        // Given
        List<UserSubscriptionInfoDto> userSubscriptionInfos = Arrays.asList(
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "일반"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "글로벌")
        );

        Map<String, List<Integer>> postTypesMap = Map.of(
                "학사", Arrays.asList(101, 102),
                "일반", Arrays.asList(201, 202),
                "글로벌", Arrays.asList(301, 302)
        );
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);
        String boardName = "자유게시판";
        String timestamp = "202506261230";

        // When
        List<UserSubscriptionInfoGroupDto> result = boardSubscriptionNotificationService.groupUserSubscriptionInfos(
                userSubscriptionInfos, requestDto, boardName, timestamp);

        // Then
        assertThat(result).hasSize(1); // 1명의 사용자

        UserSubscriptionInfoGroupDto group = result.get(0);
        assertThat(group.userId()).isEqualTo(1);
        assertThat(group.email()).isEqualTo("user1@email.com");
        assertThat(group.boardId()).isEqualTo(100);
        assertThat(group.boardName()).isEqualTo("자유게시판");
        assertThat(group.timestamp()).isEqualTo("202506261230");

        // 3개 postType 모두 구독 확인
        Map<String, List<Integer>> resultPostTypes = group.postTypes();
        assertThat(resultPostTypes).hasSize(3);
        assertThat(resultPostTypes.get("학사")).containsExactly(101, 102);
        assertThat(resultPostTypes.get("일반")).containsExactly(201, 202);
        assertThat(resultPostTypes.get("글로벌")).containsExactly(301, 302);
    }

    @Test
    void 여러_사용자가_다양한_조합으로_구독한다() {
        // Given
        List<UserSubscriptionInfoDto> userSubscriptionInfos = Arrays.asList(
                // 사용자 1: 학사 + 일반 구독
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "일반"),
                // 사용자 2: 학사 + 글로벌 구독
                new UserSubscriptionInfoDto(2, "user2@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(2, "user2@email.com", 100, "글로벌"),
                // 사용자 3: 일반 + 글로벌 구독
                new UserSubscriptionInfoDto(3, "user3@email.com", 100, "일반"),
                new UserSubscriptionInfoDto(3, "user3@email.com", 100, "글로벌")
        );

        Map<String, List<Integer>> postTypesMap = Map.of(
                "학사", Arrays.asList(101, 102),
                "일반", Arrays.asList(201, 202),
                "글로벌", Arrays.asList(301, 302)
        );
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);
        String boardName = "자유게시판";
        String timestamp = "202506261230";

        // When
        List<UserSubscriptionInfoGroupDto> result = boardSubscriptionNotificationService.groupUserSubscriptionInfos(
                userSubscriptionInfos, requestDto, boardName, timestamp);

        // Then
        assertThat(result).hasSize(3); // 3명의 사용자

        // userId별로 정렬해서 검증하기 쉽게 만들기
        List<UserSubscriptionInfoGroupDto> sortedResult = new ArrayList<>(result);
        sortedResult.sort(Comparator.comparing(UserSubscriptionInfoGroupDto::userId));

        // 사용자 1 검증 (학사 + 일반)
        UserSubscriptionInfoGroupDto user1 = result.get(0);
        assertThat(user1.userId()).isEqualTo(1);
        assertThat(user1.postTypes()).hasSize(2);
        assertThat(user1.postTypes().get("학사")).containsExactly(101, 102);
        assertThat(user1.postTypes().get("일반")).containsExactly(201, 202);

        // 사용자 2 검증 (학사 + 글로벌)
        UserSubscriptionInfoGroupDto user2 = result.get(1);
        assertThat(user2.userId()).isEqualTo(2);
        assertThat(user2.postTypes()).hasSize(2);
        assertThat(user2.postTypes().get("학사")).containsExactly(101, 102);
        assertThat(user2.postTypes().get("글로벌")).containsExactly(301, 302);

        // 사용자 3 검증 (일반 + 글로벌)
        UserSubscriptionInfoGroupDto user3 = result.get(2);
        assertThat(user3.userId()).isEqualTo(3);
        assertThat(user3.postTypes()).hasSize(2);
        assertThat(user3.postTypes().get("일반")).containsExactly(201, 202);
        assertThat(user3.postTypes().get("글로벌")).containsExactly(301, 302);
    }

    @Test
    void 여러_조합의_구독자가_섞여있다() {
        // Given
        List<UserSubscriptionInfoDto> userSubscriptionInfos = Arrays.asList(
                // 사용자 1: 전체 구독자 (학사 + 일반 + 글로벌)
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "학사"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "일반"),
                new UserSubscriptionInfoDto(1, "user1@email.com", 100, "글로벌"),
                // 사용자 2: 부분 구독자 (학사만)
                new UserSubscriptionInfoDto(2, "user2@email.com", 100, "학사"),
                // 사용자 3: 부분 구독자 (글로벌만)
                new UserSubscriptionInfoDto(3, "user3@email.com", 100, "글로벌")
        );

        Map<String, List<Integer>> postTypesMap = Map.of(
                "학사", Arrays.asList(101, 102),
                "일반", Arrays.asList(201, 202),
                "글로벌", Arrays.asList(301, 302)
        );
        PostNotificationRequestDto requestDto = new PostNotificationRequestDto(100, postTypesMap);
        String boardName = "자유게시판";
        String timestamp = "202506261230";

        // When
        List<UserSubscriptionInfoGroupDto> result = boardSubscriptionNotificationService.groupUserSubscriptionInfos(
                userSubscriptionInfos, requestDto, boardName, timestamp);

        // Then
        assertThat(result).hasSize(3); // 3명의 사용자

        // userId별로 정렬
        List<UserSubscriptionInfoGroupDto> sortedResult = new ArrayList<>(result);
        sortedResult.sort(Comparator.comparing(UserSubscriptionInfoGroupDto::userId));

        // 사용자 1: 전체 구독자 검증
        UserSubscriptionInfoGroupDto user1 = result.get(0);
        assertThat(user1.userId()).isEqualTo(1);
        assertThat(user1.postTypes()).hasSize(3); // 모든 타입 구독
        assertThat(user1.postTypes().get("학사")).containsExactly(101, 102);
        assertThat(user1.postTypes().get("일반")).containsExactly(201, 202);
        assertThat(user1.postTypes().get("글로벌")).containsExactly(301, 302);

        // 사용자 2: 학사만 구독 검증
        UserSubscriptionInfoGroupDto user2 = result.get(1);
        assertThat(user2.userId()).isEqualTo(2);
        assertThat(user2.postTypes()).hasSize(1);
        assertThat(user2.postTypes().get("학사")).containsExactly(101, 102);

        // 사용자 3: 글로벌만 구독 검증
        UserSubscriptionInfoGroupDto user3 = result.get(2);
        assertThat(user3.userId()).isEqualTo(3);
        assertThat(user3.postTypes()).hasSize(1);
        assertThat(user3.postTypes().get("글로벌")).containsExactly(301, 302);
    }
}