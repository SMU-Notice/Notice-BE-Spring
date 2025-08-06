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
import java.util.Set;
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
        long startTime = System.currentTimeMillis();

        String postTypeDetails = requestDto.postTypes().entrySet().stream()
                .map(entry -> String.format("%s(%d개): %s",
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().toString()))
                .collect(Collectors.joining(", "));

        log.info("새 게시물 알림 발송 시작 - boardId: {}, postTypes: [{}], 총 게시물 수: {}",
                requestDto.boardId(),
                postTypeDetails,
                requestDto.getAllPostIds().size());

        // 1. 알림 생성 시간 설정 (Redis 키와 알림 메시지에 사용)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        log.debug("알림 타임스탬프 생성: {}", timestamp);

        // 2. 게시판 정보 조회 및 검증
        Board board = boardRepository.findById(requestDto.boardId())
                .orElseThrow(() -> {
                    log.error("게시판을 찾을 수 없음 - boardId: {}", requestDto.boardId());
                    return new CustomException(ErrorCode.NOT_FOUND_BOARD);
                });
        log.info("게시판 조회 성공 - boardId: {}, boardName: {}", board.getId(), board.getName());

        // 3. 요청된 게시물들 조회
        List<PostSummaryDto> posts = postRepository.findPostSummariesByPostIds(requestDto.getAllPostIds());
        log.info("게시물 조회 완료 - 요청된 게시물 수: {}, 조회된 게시물 수: {}",
                requestDto.getAllPostIds().size(), posts.size());

        // 4. 요청한 게시물이 모두 존재하는지 검증
        if (posts.size() != requestDto.getAllPostIds().size()) {
            log.error("요청한 게시물 중 일부가 존재하지 않음 - 요청: {}, 조회: {}, 누락된 ID: {}",
                    requestDto.getAllPostIds().size(), posts.size(),
                    getMissingPostIds(requestDto.getAllPostIds(), posts));
            throw new CustomException(ErrorCode.INVALID_POST_ID_REQUEST);
        }

        // 4-1. 모든 게시물이 해당 게시판에 속하는지 검증
        List<Integer> wrongBoardPostIds = posts.stream()
                .filter(post -> !post.boardId().equals(requestDto.boardId()))
                .map(PostSummaryDto::postId)
                .toList();

        if (!wrongBoardPostIds.isEmpty()) {
            log.error("다른 게시판의 게시물이 포함됨 - 요청 boardId: {}, 잘못된 게시물 ID: {}",
                    requestDto.boardId(), wrongBoardPostIds);
            throw new CustomException(ErrorCode.INVALID_POST_ID_REQUEST);
        }
        log.info("게시판 소속 검증 완료 - 모든 게시물이 boardId {} 에 속함", requestDto.boardId());

        // 5. 해당 게시판의 구독자 정보 조회 (요청된 postType에 구독한 사용자들만)
        List<UserSubscriptionInfoDto> userSubscriptionInfos =
                boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(
                        requestDto.boardId(),
                        requestDto.getPostTypeNames()
                );
        log.info("구독자 조회 완료 - boardId: {}, postTypes: {}, 구독자 수: {}",
                requestDto.boardId(), requestDto.getPostTypeNames(), userSubscriptionInfos.size());

        // 6. 구독자가 없으면 알림 발송 중단
        if (userSubscriptionInfos.isEmpty()) {
            log.warn("구독자가 없어 알림 발송 중단 - boardId: {}, postTypes: {}",
                    requestDto.boardId(), requestDto.getPostTypeNames());
            return;
        }

        // 7. 사용자별 구독 정보 그룹화 (userId 기준으로 postType들을 묶음)
        List<UserSubscriptionInfoGroupDto> groupedResult =
                groupUserSubscriptionInfos(userSubscriptionInfos, requestDto, board.getName(), board.getKoreanStringCampus(), timestamp);
        log.info("사용자별 구독 정보 그룹화 완료 - 그룹 수: {}", groupedResult.size());

        // 8. Redis에 게시물 정보 캐싱 (이메일 템플릿에서 사용할 데이터)
        int cachedCount = 0;
        for (PostSummaryDto post : posts) {
            try {
                // 캐시 키 생성: timestamp_boardId_postId 형태
                String key = RedisKeyUtil.generatePostKey(timestamp, post.boardId(), post.postId());

                // 1시간(3600초) 동안 캐싱
                redisCacheHelper.cachePost(key, post, 3600L);
                cachedCount++;
            } catch (Exception e) {
                log.error("Redis 캐싱 실패 - postId: {}, error: {}", post.postId(), e.getMessage());
                throw new CustomException(ErrorCode.REDIS_CACHE_ERROR);
            }
        }
        log.info("Redis 캐싱 완료 - 성공: {}/{} 건", cachedCount, posts.size());

        // 9. RabbitMQ를 통해 이메일 발송 요청 (각 사용자별로 개별 메시지 전송)
        int sentCount = 0;
        int failedCount = 0;

        for (UserSubscriptionInfoGroupDto dto : groupedResult) {
            try {
                rabbitMqService.sendEmailMessage(dto);
                sentCount++;
                log.debug("이메일 메시지 전송 성공 - userId: {}, email: {}", dto.userId(), dto.email());
            } catch (Exception e) {
                failedCount++;
                log.error("이메일 메시지 전송 실패 - userId: {}, email: {}, error: {}",
                        dto.userId(), dto.email(), e.getMessage());
            }
        }

        log.info("이메일 발송 요청 완료 - boardId: {}, 성공: {} 건, 실패: {} 건, 총 처리 시간: {}ms",
                requestDto.boardId(), sentCount, failedCount,
                System.currentTimeMillis() - startTime);
    }

    /**
     * 누락된 게시물 ID를 찾는 헬퍼 메서드
     */
    private List<Integer> getMissingPostIds(List<Integer> requestedIds, List<PostSummaryDto> foundPosts) {
        Set<Integer> foundIds = foundPosts.stream()
                .map(PostSummaryDto::postId)
                .collect(Collectors.toSet());

        return requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 사용자 구독 정보를 userId 기준으로 그룹화하고 알림용 DTO로 변환
     *
     * @param userSubscriptionInfos 구독 정보 리스트 (같은 userId가 여러 개 있을 수 있음)
     * @param requestDto 알림 요청 정보 (postType별 postId 매핑 정보 포함)
     * @param boardName 게시판 이름
     * @param campus 캠퍼스 정보
     * @param timestamp 알림 생성 시간
     * @return userId별로 그룹화된 알림용 DTO 리스트
     */
    List<UserSubscriptionInfoGroupDto> groupUserSubscriptionInfos(
            List<UserSubscriptionInfoDto> userSubscriptionInfos,
            PostNotificationRequestDto requestDto,
            String boardName,
            String campus,
            String timestamp) {

        log.debug("사용자 구독 정보 그룹화 시작 - 전체 구독 정보 수: {}, 게시판: {}, 캠퍼스: {}",
                userSubscriptionInfos.size(), boardName, campus);

        // 사용자별 구독 정보 통계를 위한 Map
        Map<Integer, List<String>> userPostTypeMap = userSubscriptionInfos.stream()
                .collect(Collectors.groupingBy(
                        UserSubscriptionInfoDto::userId,
                        Collectors.mapping(UserSubscriptionInfoDto::postType, Collectors.toList())
                ));

        log.info("구독자별 postType 분석 - 총 구독자 수: {}", userPostTypeMap.size());
        userPostTypeMap.forEach((userId, postTypes) ->
                log.debug("구독자 ID: {}, 구독 postType: {}", userId, postTypes));

        return userSubscriptionInfos.stream()
                // 1. userId 기준으로 그룹화
                .collect(Collectors.groupingBy(
                        UserSubscriptionInfoDto::userId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    // 2. 그룹의 첫 번째 요소에서 공통 정보 추출 (userId, email, boardId는 동일)
                                    UserSubscriptionInfoDto first = list.get(0);
                                    log.debug("사용자 그룹 처리 중 - userId: {}, email: {}, 구독 항목 수: {}",
                                            first.userId(), first.email(), list.size());

                                    // 3. 해당 유저가 구독한 postType들을 수집하고 각 postType별 postId 매핑
                                    List<String> userPostTypes = list.stream()
                                            .map(UserSubscriptionInfoDto::postType)
                                            .distinct() // 중복 postType 제거
                                            .toList();

                                    log.debug("사용자 {} 의 고유 postType: {}", first.userId(), userPostTypes);

                                    Map<String, List<Integer>> postTypes = userPostTypes.stream()
                                            .collect(Collectors.toMap(
                                                    postType -> postType,
                                                    postType -> {
                                                        List<Integer> postIds = requestDto.getPostIdsByPostType(postType);
                                                        log.debug("postType '{}' 에 해당하는 게시물 ID: {}", postType, postIds);
                                                        return postIds;
                                                    }
                                            ));

                                    // 전체 게시물 수 계산
                                    int totalPosts = postTypes.values().stream()
                                            .mapToInt(List::size)
                                            .sum();

                                    log.debug("사용자 {} 알림 대상 - postType 수: {}, 총 게시물 수: {}",
                                            first.userId(), postTypes.size(), totalPosts);

                                    // 4. 최종 DTO 생성 (campus 정보 포함)
                                    UserSubscriptionInfoGroupDto result = new UserSubscriptionInfoGroupDto(
                                            first.userId(),
                                            first.email(),
                                            first.boardId(),
                                            boardName,
                                            campus,
                                            postTypes,
                                            timestamp
                                    );

                                    log.debug("UserSubscriptionInfoGroupDto 생성 완료 - userId: {}", first.userId());
                                    return result;
                                }
                        )
                ))
                .values() // Map의 값들만 추출
                .stream()
                .peek(dto -> log.debug("최종 그룹화 결과 - userId: {}, postType 수: {}, 총 게시물 수: {}",
                        dto.userId(), dto.postTypes().size(),
                        dto.postTypes().values().stream().mapToInt(List::size).sum()))
                .toList(); // List로 변환
    }
}
