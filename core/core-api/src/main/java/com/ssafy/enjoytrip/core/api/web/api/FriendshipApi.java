package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendshipRequestsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Friendships", description = "동네핀 친구 요청/수락 API")
public interface FriendshipApi {
    @Operation(
            summary = "친구 요청 보내기",
            operationId = "requestFriendship",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendRequestCreateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.FRIEND_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipMutationResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_MUTATION_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipMutationResponse> request(
            FriendRequestCreateRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "받은 친구 요청 수락", operationId = "acceptFriendshipRequest")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 요청 수락 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipMutationResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_MUTATION_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipMutationResponse> accept(
            @Parameter(description = "친구 관계 ID", example = "10", required = true) Long friendshipId,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "받은 친구 요청 거절", operationId = "rejectFriendshipRequest")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 요청 거절 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipMutationResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_MUTATION_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipMutationResponse> reject(
            @Parameter(description = "친구 관계 ID", example = "10", required = true) Long friendshipId,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "보낸 친구 요청 취소", operationId = "cancelFriendshipRequest")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 요청 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipMutationResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_MUTATION_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipMutationResponse> cancel(
            @Parameter(description = "친구 관계 ID", example = "10", required = true) Long friendshipId,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "친구 끊기", operationId = "deleteFriendship")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 끊기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipMutationResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_MUTATION_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipMutationResponse> delete(
            @Parameter(description = "친구 관계 ID", example = "10", required = true) Long friendshipId,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(summary = "친구 목록 조회", operationId = "getFriends")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "친구 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDS_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendsResponse> friends(@Parameter(hidden = true) Long memberId);

    @Operation(summary = "받은 대기 친구 요청 조회", operationId = "getReceivedFriendshipRequests")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "받은 대기 친구 요청 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipRequestsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_REQUESTS_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipRequestsResponse> received(@Parameter(hidden = true) Long memberId);

    @Operation(summary = "보낸 대기 친구 요청 조회", operationId = "getSentFriendshipRequests")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "보낸 대기 친구 요청 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipRequestsResponse.class),
                            examples = @ExampleObject(value = ApiExamples.FRIENDSHIP_REQUESTS_RESPONSE)
                    )
            )
    })
    ApiResponse<FriendshipRequestsResponse> sent(@Parameter(hidden = true) Long memberId);
}
