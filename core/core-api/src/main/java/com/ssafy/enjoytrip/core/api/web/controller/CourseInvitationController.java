package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.CourseInvitationApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseInviteRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseInvitationResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseInvitationsResponse;
import com.ssafy.enjoytrip.core.domain.CourseInvitation;
import com.ssafy.enjoytrip.core.domain.service.CourseInvitationService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/invitations")
@RequiredArgsConstructor
public class CourseInvitationController implements CourseInvitationApi {
    private final CourseInvitationService courseInvitationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<CourseInvitationResponse> invite(
            @PathVariable String courseId,
            @RequestBody @Valid CourseInviteRequest request,
            @AuthenticatedMemberId Long authenticatedMemberId
    ) {
        CourseInvitation invitation = courseInvitationService.inviteFriend(
                authenticatedMemberId,
                courseId.strip(),
                request.inviteeEmail().strip()
        );
        return success(CourseInvitationResponse.from(invitation));
    }

    @PostMapping("/{invitationId}/accept")
    @Override
    public ApiResponse<CourseInvitationResponse> accept(
            @PathVariable String courseId,
            @PathVariable Long invitationId,
            @AuthenticatedMemberId Long authenticatedMemberId
    ) {
        CourseInvitation invitation = courseInvitationService.acceptInvitation(
                authenticatedMemberId,
                invitationId
        );
        return success(CourseInvitationResponse.from(invitation));
    }

    @PostMapping("/{invitationId}/reject")
    @Override
    public ApiResponse<CourseInvitationResponse> reject(
            @PathVariable String courseId,
            @PathVariable Long invitationId,
            @AuthenticatedMemberId Long authenticatedMemberId
    ) {
        CourseInvitation invitation = courseInvitationService.rejectInvitation(
                authenticatedMemberId,
                invitationId
        );
        return success(CourseInvitationResponse.from(invitation));
    }

    @GetMapping
    @Override
    public ApiResponse<CourseInvitationsResponse> list(
            @PathVariable String courseId,
            @AuthenticatedMemberId Long authenticatedMemberId
    ) {
        List<CourseInvitationResponse> invitations = courseInvitationService
                .findByCourse(authenticatedMemberId, courseId.strip())
                .stream()
                .map(CourseInvitationResponse::from)
                .toList();
        return success(new CourseInvitationsResponse(invitations));
    }
}
