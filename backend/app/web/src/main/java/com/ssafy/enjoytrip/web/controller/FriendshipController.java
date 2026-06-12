package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.service.FriendshipService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.FriendshipApi;
import com.ssafy.enjoytrip.web.dto.request.FriendRequestCreateRequest;
import com.ssafy.enjoytrip.web.dto.response.FriendsResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipMutationResponse;
import com.ssafy.enjoytrip.web.dto.response.FriendshipRequestsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
                                                           @AuthenticationPrincipal Jwt jwt) {
        Friendship friendship = friendshipService.requestFriendship(
                authenticatedUserId(jwt),
                request.normalizedTargetUserId()
        );
        return success(FriendshipMutationResponse.from(friendship));
    }

    @PostMapping("/requests/{friendshipId}/accept")
    @Override
    public ApiResponse<FriendshipMutationResponse> accept(@PathVariable Long friendshipId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipMutationResponse.from(
                friendshipService.acceptRequest(friendshipId, authenticatedUserId(jwt))
        ));
    }

    @PostMapping("/requests/{friendshipId}/reject")
    @Override
    public ApiResponse<FriendshipMutationResponse> reject(@PathVariable Long friendshipId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipMutationResponse.from(
                friendshipService.rejectRequest(friendshipId, authenticatedUserId(jwt))
        ));
    }

    @DeleteMapping("/requests/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> cancel(@PathVariable Long friendshipId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipMutationResponse.from(
                friendshipService.cancelSentRequest(friendshipId, authenticatedUserId(jwt))
        ));
    }

    @DeleteMapping("/{friendshipId}")
    @Override
    public ApiResponse<FriendshipMutationResponse> delete(@PathVariable Long friendshipId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipMutationResponse.from(
                friendshipService.deleteFriendship(friendshipId, authenticatedUserId(jwt))
        ));
    }

    @GetMapping
    @Override
    public ApiResponse<FriendsResponse> friends(@AuthenticationPrincipal Jwt jwt) {
        String actorUserId = authenticatedUserId(jwt);
        List<Friendship> friendships = friendshipService.findFriends(actorUserId);
        return success(FriendsResponse.from(friendships, actorUserId));
    }

    @GetMapping("/requests/received")
    @Override
    public ApiResponse<FriendshipRequestsResponse> received(@AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findReceivedPendingRequests(authenticatedUserId(jwt))
        ));
    }

    @GetMapping("/requests/sent")
    @Override
    public ApiResponse<FriendshipRequestsResponse> sent(@AuthenticationPrincipal Jwt jwt) {
        return success(FriendshipRequestsResponse.from(
                friendshipService.findSentPendingRequests(authenticatedUserId(jwt))
        ));
    }

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return jwt.getSubject().trim();
    }
}
