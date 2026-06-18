package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.storage.db.core.entity.FriendshipEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FriendshipMapper {
    FriendshipEntity findById(Long id);

    List<FriendshipEntity> findByParticipantAndStatus(@Param("userId") String userId,
                                                       @Param("status") FriendshipStatus status);

    List<FriendshipEntity> findReceivedRequests(@Param("addresseeUserId") String addresseeUserId,
                                                 @Param("status") FriendshipStatus status);

    List<FriendshipEntity> findSentRequests(@Param("requesterUserId") String requesterUserId,
                                             @Param("status") FriendshipStatus status);

    int existsActiveBetween(@Param("userId") String userId,
                            @Param("otherUserId") String otherUserId,
                            @Param("statuses") List<FriendshipStatus> statuses);

    int insert(FriendshipEntity entity);

    int updateStatus(FriendshipEntity entity);
}
