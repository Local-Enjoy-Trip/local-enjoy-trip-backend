package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.FriendshipApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.FriendshipRequestsResponse;
import com.ssafy.enjoytrip.core.domain.Friendship;
import com.ssafy.enjoytrip.core.domain.service.FriendshipService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController implements FriendshipApi {
    private final FriendshipService friendshipService;

    @PostMapping("/requests")
    @Override
    public ApiResponse<FriendshipMutationResponse> request(
            @RequestBody FriendRequestCreateRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        Friendship friendship = friendshipService.requestFriendship(
                memberId,
                request.targetEmail()
        );
        return success(FriendshipMutationResponse.from(friendship));
    }

    @PostMapping("/requests/{friendshipId}/accept")
    @Override
    public ApiResponse<FriendshipMutationResponse> accept(@PathVariable Long friendshipId,
                                                          @AuthenticatedMemberId Long memberId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.acceptRequest(friendshipId, memberId)
        ));
    }

    @PostMapping("/requests/{friendshipId}/reject")
    @Override
    public ApiResponse<FriendshipMutationResponse> reject(@PathVariable Long friendshipId,
                                                          @AuthenticatedMemberId Long memberId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.rejectRequest(friendshipId, memberId)
        ));
    }

    @DeleteMapping("/requests/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> cancel(@PathVariable Long friendshipId,
                                                          @AuthenticatedMemberId Long memberId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.cancelSentRequest(friendshipId, memberId)
        ));
    }

    @DeleteMapping("/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> delete(@PathVariable Long friendshipId,
                                                          @AuthenticatedMemberId Long memberId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.deleteFriendship(friendshipId, memberId)
        ));
    }

    @GetMapping
    @Override
    public ApiResponse<FriendsResponse> friends(@AuthenticatedMemberId Long memberId) {
        List<Friendship> friendships = friendshipService.findFriends(memberId);
        return success(FriendsResponse.from(friendships, memberId));
    }

    @GetMapping("/requests/received")
    @Override
    public ApiResponse<FriendshipRequestsResponse> received(@AuthenticatedMemberId Long memberId) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findReceivedPendingRequests(memberId)
        ));
    }

    @GetMapping("/requests/sent")
    @Override
    public ApiResponse<FriendshipRequestsResponse> sent(@AuthenticatedMemberId Long memberId) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findSentPendingRequests(memberId)
        ));
    }
}
