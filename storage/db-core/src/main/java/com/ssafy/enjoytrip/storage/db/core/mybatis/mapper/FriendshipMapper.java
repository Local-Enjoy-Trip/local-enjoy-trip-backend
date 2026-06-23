package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FriendshipMapper {
    FriendshipRecord findById(Long id);

    List<FriendshipRecord> findByParticipantAndStatus(@Param("memberId") Long memberId,
                                                       @Param("status") FriendshipStatus status);

    List<FriendshipRecord> findReceivedRequests(@Param("addresseeMemberId") Long addresseeMemberId,
                                                 @Param("status") FriendshipStatus status);

    List<FriendshipRecord> findSentRequests(@Param("requesterMemberId") Long requesterMemberId,
                                             @Param("status") FriendshipStatus status);

    int existsActiveBetween(@Param("memberId") Long memberId,
                            @Param("otherMemberId") Long otherMemberId,
                            @Param("statuses") List<FriendshipStatus> statuses);

    int insert(FriendshipRecord record);

    int updateStatus(FriendshipRecord record);
}
