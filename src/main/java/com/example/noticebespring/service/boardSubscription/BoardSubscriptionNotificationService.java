package com.example.noticebespring.service.boardSubscription;

import com.example.noticebespring.common.helper.RedisCacheHelper;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.common.util.RedisKeyUtil;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostNotificationRequestDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import com.example.noticebespring.entity.Board;
import com.example.noticebespring.repository.BoardRepository;
import com.example.noticebespring.repository.BoardSubscriptionRepository;
import com.example.noticebespring.repository.PostRepository;
import com.example.noticebespring.service.RabbitMqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class BoardSubscriptionNotificationService {
    private final BoardSubscriptionRepository boardSubscriptionRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final RabbitMqService rabbitMqService;
    private final RedisCacheHelper redisCacheHelper;



    /**
     * 새 게시물 등록 시 구독자들에게 이메일 알림을 발송하는 메서드
     *
     * @param requestDto 알림 요청 정보 (boardId, postType별 postId 매핑)
     * @throws CustomException 게시판을 찾을 수 없거나 요청한 게시물이 존재하지 않는 경우
     */
    public void sendNewPostNotification(PostNotificationRequestDto requestDto) {
        // 1. 알림 생성 시간 설정 (Redis 키와 알림 메시지에 사용)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // 2. 게시판 정보 조회 및 검증
        Board board = boardRepository.findById(requestDto.boardId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BOARD));

        // 3. 요청된 게시물들 조회
        List<PostSummaryDto> posts = postRepository.findPostSummariesByPostIds(requestDto.getAllPostIds());

        // 4. 요청한 게시물이 모두 존재하는지 검증
        if (posts.size() != requestDto.getAllPostIds().size()) {
            throw new CustomException(ErrorCode.INVALID_POST_ID_REQUEST);
        }

        // 5. 해당 게시판의 구독자 정보 조회 (요청된 postType에 구독한 사용자들만)
        List<UserSubscriptionInfoDto> userSubscriptionInfos =
                boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(
                        requestDto.boardId(),
                        requestDto.getPostTypeNames()
                );

        // 6. 구독자가 없으면 알림 발송 중단
        if (userSubscriptionInfos.isEmpty()) {
            return;
        }

        // 7. 사용자별 구독 정보 그룹화 (userId 기준으로 postType들을 묶음)
        List<UserSubscriptionInfoGroupDto> groupedResult =
                groupUserSubscriptionInfos(userSubscriptionInfos, requestDto, board.getName(), timestamp);

        // 8. Redis에 게시물 정보 캐싱 (이메일 템플릿에서 사용할 데이터)
        for (PostSummaryDto post : posts) {
            // 캐시 키 생성: timestamp_boardId_postId 형태
            String key = RedisKeyUtil.generatePostKey(timestamp, post.boardId(), post.postId());

            // 1시간(3600초) 동안 캐싱
            redisCacheHelper.cachePost(key, post, 3600L);
        }

        // 9. RabbitMQ를 통해 이메일 발송 요청 (각 사용자별로 개별 메시지 전송)
        for (UserSubscriptionInfoGroupDto dto : groupedResult) {
            rabbitMqService.sendEmailMessage(dto);
        }
    }

    /**
     * 사용자 구독 정보를 userId 기준으로 그룹화하고 알림용 DTO로 변환
     *
     * @param userSubscriptionInfos 구독 정보 리스트 (같은 userId가 여러 개 있을 수 있음)
     * @param requestDto 알림 요청 정보 (postType별 postId 매핑 정보 포함)
     * @param boardName 게시판 이름
     * @param timestamp 알림 생성 시간
     * @return userId별로 그룹화된 알림용 DTO 리스트
     */
    List<UserSubscriptionInfoGroupDto> groupUserSubscriptionInfos(
            List<UserSubscriptionInfoDto> userSubscriptionInfos,
            PostNotificationRequestDto requestDto,
            String boardName,
            String timestamp) {

        return userSubscriptionInfos.stream()
                // 1. userId 기준으로 그룹화
                .collect(Collectors.groupingBy(
                        UserSubscriptionInfoDto::userId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    // 2. 그룹의 첫 번째 요소에서 공통 정보 추출 (userId, email, boardId는 동일)
                                    UserSubscriptionInfoDto first = list.get(0);

                                    // 3. 해당 유저가 구독한 postType들을 수집하고 각 postType별 postId 매핑
                                    Map<String, List<Integer>> postTypes = list.stream()
                                            .map(UserSubscriptionInfoDto::postType)
                                            .distinct() // 중복 postType 제거
                                            .collect(Collectors.toMap(
                                                    postType -> postType,
                                                    requestDto::getPostIdsByPostType // postType에 해당하는 postId 리스트 가져오기
                                            ));

                                    // 4. 최종 DTO 생성
                                    return new UserSubscriptionInfoGroupDto(
                                            first.userId(),
                                            first.email(),
                                            first.boardId(),
                                            boardName,
                                            postTypes,
                                            timestamp
                                    );
                                }
                        )
                ))
                .values() // Map의 값들만 추출
                .stream()
                .toList(); // List로 변환
    }


}
