package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipRequestsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Friendships", description = "동네핀 친구 요청/수락 API")
public interface FriendshipApi {
    @Operation(summary = "친구 요청 보내기", operationId = "requestFriendship")
    ApiResponse<FriendshipMutationResponse> request(FriendRequestCreateRequest request, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "받은 친구 요청 수락", operationId = "acceptFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> accept(Long friendshipId, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "받은 친구 요청 거절", operationId = "rejectFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> reject(Long friendshipId, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "보낸 친구 요청 취소", operationId = "cancelFriendshipRequest")
    ApiResponse<FriendshipMutationResponse> cancel(Long friendshipId, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "친구 끊기", operationId = "deleteFriendship")
    ApiResponse<FriendshipMutationResponse> delete(Long friendshipId, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "친구 목록 조회", operationId = "getFriends")
    ApiResponse<FriendsResponse> friends(@Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "받은 대기 친구 요청 조회", operationId = "getReceivedFriendshipRequests")
    ApiResponse<FriendshipRequestsResponse> received(@Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "보낸 대기 친구 요청 조회", operationId = "getSentFriendshipRequests")
    ApiResponse<FriendshipRequestsResponse> sent(@Parameter(hidden = true) String authenticatedUserId);
}
