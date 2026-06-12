package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipRequestsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Friendships", description = "동네핀 친구 요청/수락 API")
public interface FriendshipApi {
    @Operation(summary = "친구 요청 보내기", operationId = "requestFriendship")
    ApiResponse<FriendshipMutationResponse> request(FriendRequestCreateRequest request, Jwt jwt);

    @Operation(summary = "받은 친구 요청 수락", operationId = "acceptFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> accept(Long friendshipId, Jwt jwt);

    @Operation(summary = "받은 친구 요청 거절", operationId = "rejectFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> reject(Long friendshipId, Jwt jwt);

    @Operation(summary = "보낸 친구 요청 취소", operationId = "cancelFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> cancel(Long friendshipId, Jwt jwt);

    @Operation(summary = "친구 끊기", operationId = "deleteFriendship")
    ApiResponse<FriendshipMutationResponse> delete(Long friendshipId, Jwt jwt);

    @Operation(summary = "친구 목록 조회", operationId = "getFriends")
    ApiResponse<FriendsResponse> friends(Jwt jwt);

    @Operation(summary = "받은 대기 친구 요청 조회", operationId = "getReceivedFriendshipRequests")
    ApiResponse<FriendshipRequestsResponse> received(Jwt jwt);

    @Operation(summary = "보낸 대기 친구 요청 조회", operationId = "getSentFriendshipRequests")
    ApiResponse<FriendshipRequestsResponse> sent(Jwt jwt);
}
