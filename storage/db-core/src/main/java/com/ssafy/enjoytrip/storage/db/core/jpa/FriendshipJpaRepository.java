package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.storage.db.core.entity.FriendshipEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, Long> {
    @Query("""
            select count(f) > 0
            from FriendshipEntity f
            where f.status in :activeStatuses
              and ((f.requesterUserId = :userId and f.addresseeUserId = :otherUserId)
                or (f.requesterUserId = :otherUserId and f.addresseeUserId = :userId))
            """)
    boolean existsActiveBetween(@Param("userId") String userId,
                                @Param("otherUserId") String otherUserId,
                                @Param("activeStatuses") List<FriendshipStatus> activeStatuses);

    @Query("""
            select f
            from FriendshipEntity f
            where f.status = :status
              and (f.requesterUserId = :userId or f.addresseeUserId = :userId)
            order by f.respondedAt desc, f.id desc
            """)
    List<FriendshipEntity> findByParticipantAndStatus(@Param("userId") String userId,
                                                       @Param("status") FriendshipStatus status);

    List<FriendshipEntity> findByAddresseeUserIdAndStatusOrderByRequestedAtDescIdDesc(String addresseeUserId,
                                                                                       FriendshipStatus status);

    List<FriendshipEntity> findByRequesterUserIdAndStatusOrderByRequestedAtDescIdDesc(String requesterUserId,
                                                                                       FriendshipStatus status);
}
