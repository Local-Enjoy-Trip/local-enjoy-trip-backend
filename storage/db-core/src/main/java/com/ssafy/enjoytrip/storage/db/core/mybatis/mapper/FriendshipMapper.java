package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FriendshipMapper {
    FriendshipRecord findById(Long id);

    List<FriendshipRecord> findByParticipantAndStatus(@Param("userId") String userId,
                                                       @Param("status") FriendshipStatus status);

    List<FriendshipRecord> findReceivedRequests(@Param("addresseeUserId") String addresseeUserId,
                                                 @Param("status") FriendshipStatus status);

    List<FriendshipRecord> findSentRequests(@Param("requesterUserId") String requesterUserId,
                                             @Param("status") FriendshipStatus status);

    int existsActiveBetween(@Param("userId") String userId,
                            @Param("otherUserId") String otherUserId,
                            @Param("statuses") List<FriendshipStatus> statuses);

    int insert(FriendshipRecord record);

    int updateStatus(FriendshipRecord record);
}
