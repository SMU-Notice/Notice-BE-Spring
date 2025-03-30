package com.example.noticebespring.service;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionRequestDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionResponseDto;
import com.example.noticebespring.dto.boardSubscription.register.SubscriptionItemDto;

import com.example.noticebespring.entity.BoardSubscription;
import com.example.noticebespring.entity.BoardSubscriptionId;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.repository.BoardSubscriptionRepository;
import com.example.noticebespring.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardSubscriptionService {

    private final BoardSubscriptionRepository boardSubscriptionRepository;
    private final UserService userService;

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
            throw new CustomException(ErrorCode.NOT_FOUND_SUBSCRIPTION);
        }

        // 모든 구독 삭제
        boardSubscriptionRepository.deleteAll(subscriptions);

        return "구독이 모두 취소되었습니다.";

    }



    // board_id, post_type 에 id로 조회

    // 조회 햇는데 id 가 없다면 에러 반환

    // board_id, post_type  에 id 이메일로 전송

    //

}
