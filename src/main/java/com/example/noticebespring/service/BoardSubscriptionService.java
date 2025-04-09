package com.example.noticebespring.service;

import com.example.noticebespring.common.helper.RedisCacheHelper;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.common.util.RedisKeyUtil;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostNotificationRequestDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionRequestDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionResponseDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionItemDto;

import com.example.noticebespring.entity.Board;
import com.example.noticebespring.entity.BoardSubscription;
import com.example.noticebespring.entity.BoardSubscriptionId;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.repository.BoardRepository;
import com.example.noticebespring.repository.BoardSubscriptionRepository;
import com.example.noticebespring.repository.PostRepository;
import com.example.noticebespring.service.auth.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardSubscriptionService {

    private final BoardSubscriptionRepository boardSubscriptionRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserService userService;
    private final RabbitMqService rabbitMqService;
    private final RedisCacheHelper redisCacheHelper;

    public SubscriptionResponseDto getSubscriptions() {
        // 인증된 사용자 가져오기 (인증된 사용자 정보를 가져오는 메서드가 있다고 가정)
        User user = userService.getAuthenticatedUser();

        // 인증된 사용자의 구독 정보 조회
        log.info("Fetching subscriptions for user ID: {}", user.getId());
        List<BoardSubscription> subscriptions = boardSubscriptionRepository.findByUserId(user.getId());

        // 응답 형식 준비하기
        List<SubscriptionItemDto> subscriptionItems = subscriptions.stream()
                .collect(Collectors.groupingBy(subscription -> subscription.getId().getBoardId())) // boardId 기준으로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    // boardId 추출
                    Integer boardId = entry.getKey();
                    // 해당 boardId에 대한 postTypes 추출
                    List<String> postTypes = entry.getValue().stream()
                            .map(subscription -> subscription.getId().getPostType())  // BoardSubscriptionId의 postType을 추출
                            .collect(Collectors.toList());

                    // SubscriptionItemDto 반환 (boardId와 postTypes)
                    return new SubscriptionItemDto(boardId, postTypes);
                })
                .collect(Collectors.toList());

        log.info("Fetching subscriptions for user ID: {}", user.getId());

        return new SubscriptionResponseDto(subscriptionItems);
    }

    @Transactional
    public SubscriptionResponseDto manageSubscriptions(SubscriptionRequestDto subscriptionRequestDto) {

        // 인증된 유저 조회
        User user = userService.getAuthenticatedUser();

        // 이메일이 존재하지 않으면 오류 반환
        if (user.getEmail() == null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }

        // 요청이 비어 있으면 오류 반환
        if (subscriptionRequestDto.subscriptions().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST);
        }


        // 1. B: DB에서 user_id로 모든 구독 정보 조회
        log.info("Fetching existing subscriptions for user ID: {}", user.getId());
        List<BoardSubscription> existingSubscriptions = boardSubscriptionRepository.findByUserId(user.getId());

        /// A : 기존 구독 정보 (DB에서 가져온 값)
        Set<String> existingSubscriptionsSet = existingSubscriptions.stream()
                .map(subscription -> subscription.getId().getBoardId() + "-" + subscription.getId().getPostType())
                .collect(Collectors.toSet());

        // B : 요청에 포함된 구독 정보 (요청 받은 값)
        Set<String> requestSubscriptionsSet = subscriptionRequestDto.subscriptions().stream()
                .flatMap(requestSubscription -> requestSubscription.postTypes().stream()
                        .map(postType -> requestSubscription.boardId() + "-" + postType))
                .collect(Collectors.toSet());


        // DB에만 있는 데이터 (A - B) -> toDelete 리스트에 추가
        Set<String> toDeleteSet = new HashSet<>(existingSubscriptionsSet); // A 복사
        toDeleteSet.removeAll(requestSubscriptionsSet);
        log.info("Subscriptions to delete (A - B): {}", toDeleteSet.size());

        // 요청에만 있는 데이터 (B - A) -> toCreate 리스트에 추가
        Set<String> toCreateSet = new HashSet<>(requestSubscriptionsSet); // B 복사
        toCreateSet.removeAll(existingSubscriptionsSet); // B - A
        log.info("Subscriptions to create (B - A): {}", toCreateSet.size());

        // 교집합: 기존 구독 정보와 요청된 구독 정보의 교집합
        Set<String> intersectionSet = new HashSet<>(existingSubscriptionsSet);
        intersectionSet.retainAll(requestSubscriptionsSet); // A ∩ B
        log.info("Intersection subscriptions (A ∩ B): {}", intersectionSet.size());


        // DB에만 있는 데이터 -> toDelete 리스트에 추가
        List<BoardSubscriptionId> toDeleteIds = new ArrayList<>();
        for (String key : toDeleteSet) {
            String[] parts = key.split("-"); // boardId와 postType 분리
            Integer boardId = Integer.valueOf(parts[0]);
            String postType = parts[1];

            // BoardSubscriptionId 생성
            BoardSubscriptionId id = new BoardSubscriptionId(user, boardId, postType);

            toDeleteIds.add(id);
        }

        if (!toDeleteIds.isEmpty()) {
            boardSubscriptionRepository.deleteAllById(toDeleteIds);
        }

        // 요청에만 있는 데이터 -> toCreate 리스트에 추가
        List<BoardSubscription> toCreate = new ArrayList<>();
        for (String key : toCreateSet) {
            String[] parts = key.split("-"); // boardId와 postType 분리
            Integer boardId = Integer.valueOf(parts[0]);
            String postType = parts[1];

            // BoardSubscription 객체 생성
            BoardSubscriptionId id = new BoardSubscriptionId(user, boardId, postType);
            BoardSubscription newSubscription = BoardSubscription.builder()
                    .id(id)
                    .build();
            toCreate.add(newSubscription);
        }

        if (!toCreate.isEmpty()) {
            boardSubscriptionRepository.saveAll(toCreate);
        }

        // 결과 합치기 (교집합 + 요청에만 있는 데이터)
        Set<String> combinedSet = new HashSet<>(toCreateSet);
        combinedSet.addAll(intersectionSet);

        // BoardSubscription 객체로 변환하여 반환할 DTO 생성
        List<SubscriptionItemDto> subscriptionItemDtos = combinedSet.stream()
                .collect(Collectors.groupingBy(key -> key.split("-")[0])) // boardId 기준으로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    Integer boardId = Integer.valueOf(entry.getKey());
                    List<String> postTypes = entry.getValue().stream()
                            .map(key -> key.split("-")[1])
                            .collect(Collectors.toList());
                    return new SubscriptionItemDto(boardId, postTypes);
                })
                .collect(Collectors.toList());

        // 결과를 SubscriptionResponseDto로 래핑
        return new SubscriptionResponseDto(subscriptionItemDtos);
    }

    // 모든 구독 삭제
    public String cancelAllSubscription () {

        // 인증된 유저 조회
        User user = userService.getAuthenticatedUser();

        // 해당 유저의 모든 구독 조회
        List<BoardSubscription> subscriptions = boardSubscriptionRepository.findByUserId(user.getId());

        // 구독 정보 없음
        if (subscriptions.isEmpty()) {
            return "구독이 모두 취소되었습니다.";
        }

        // 모든 구독 삭제
        boardSubscriptionRepository.deleteAll(subscriptions);

        return "구독이 모두 취소되었습니다.";

    }


    public void sendNewPostNotification(PostNotificationRequestDto requestDto)  {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // 게시판 조회
        Board board = boardRepository.findById(requestDto.boardId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BOARD));


        // 게시물 조회
        List<PostSummaryDto> posts = postRepository.findPostSummariesByPostIds(requestDto.getAllPostIds());

        // 요청한 postId 중 누락된 ID가 있는지 확인
        if (posts.size() != requestDto.getAllPostIds().size()) {
            throw new CustomException(ErrorCode.INVALID_POST_ID_REQUEST);
        }


        // 구독한 사용자 조회
        List<UserSubscriptionInfoDto> userSubscriptionInfos = boardSubscriptionRepository.findUserSubscriptionInfoByBoardIdAndPostTypes(requestDto.boardId(), requestDto.getPostTypeNames());

        // 구독자가 없는 경우 메서드 종료
        if (userSubscriptionInfos.isEmpty()) {
            return;
        }


        // userId 기준으로 구독 정보를 유저 기준으로 그룹화 + timestamp 포함
        List<UserSubscriptionInfoGroupDto> groupedResult = userSubscriptionInfos.stream()
                .collect(Collectors.groupingBy(
                        UserSubscriptionInfoDto::userId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    UserSubscriptionInfoDto first = list.get(0);
                                    Map<String, List<Integer>> postTypes = list.stream()
                                            .map(UserSubscriptionInfoDto::postType)
                                            .distinct()
                                            .collect(Collectors.toMap(
                                                    postType -> postType,
                                                    requestDto::getPostIdsByPostType
                                            ));

                                    return new UserSubscriptionInfoGroupDto(
                                            first.userId(),
                                            first.email(),
                                            first.boardId(),
                                            board.getName(),
                                            postTypes,
                                            timestamp // 이 부분은 그대로
                                    );
                                }
                        )
                ))
                .values()
                .stream()
                .toList();

        // Redis에 게시물 정보 저장

        for (PostSummaryDto post : posts) {
            // redis에 저장할 데이터 key 설정
            String key = RedisKeyUtil.generatePostKey(timestamp, post.boardId(), post.postId());

            // redis에 post 캐시
            redisCacheHelper.cachePost(key, post, 3600L);

        }


        // rabbitMQ producer 요청
        for (UserSubscriptionInfoGroupDto dto : groupedResult) {
            rabbitMqService.sendEmailMessage(dto);
        }


    }

}
