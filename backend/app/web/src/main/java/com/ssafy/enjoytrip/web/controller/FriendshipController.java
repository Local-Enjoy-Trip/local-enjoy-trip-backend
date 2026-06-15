package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.service.FriendshipService;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.FriendshipApi;
import com.ssafy.enjoytrip.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipRequestsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.web.security.AuthenticatedUserId;
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
    public ApiResponse<FriendshipMutationResponse> request(@Valid @RequestBody FriendRequestCreateRequest request,
                                                           @AuthenticatedUserId String authenticatedUserId) {
        Friendship friendship = friendshipService.requestFriendship(
                authenticatedUserId,
                request.normalizedTargetUserId()
        );
        return success(FriendshipMutationResponse.from(friendship));
    }

    @PostMapping("/requests/{friendshipId}/accept")
    @Override
    public ApiResponse<FriendshipMutationResponse> accept(@PathVariable Long friendshipId,
                                                          @AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.acceptRequest(friendshipId, authenticatedUserId)
        ));
    }

    @PostMapping("/requests/{friendshipId}/reject")
    @Override
    public ApiResponse<FriendshipMutationResponse> reject(@PathVariable Long friendshipId,
                                                          @AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.rejectRequest(friendshipId, authenticatedUserId)
        ));
    }

    @DeleteMapping("/requests/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> cancel(@PathVariable Long friendshipId,
                                                          @AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.cancelSentRequest(friendshipId, authenticatedUserId)
        ));
    }

    @DeleteMapping("/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> delete(@PathVariable Long friendshipId,
                                                          @AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipMutationResponse.from(
                friendshipService.deleteFriendship(friendshipId, authenticatedUserId)
        ));
    }

    @GetMapping
    @Override
    public ApiResponse<FriendsResponse> friends(@AuthenticatedUserId String actorUserId) {
        List<Friendship> friendships = friendshipService.findFriends(actorUserId);
        return success(FriendsResponse.from(friendships, actorUserId));
    }

    @GetMapping("/requests/received")
    @Override
    public ApiResponse<FriendshipRequestsResponse> received(@AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findReceivedPendingRequests(authenticatedUserId)
        ));
    }

    @GetMapping("/requests/sent")
    @Override
    public ApiResponse<FriendshipRequestsResponse> sent(@AuthenticatedUserId String authenticatedUserId) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findSentPendingRequests(authenticatedUserId)
        ));
    }

}
