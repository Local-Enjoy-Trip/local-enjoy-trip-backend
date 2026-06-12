package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import com.ssafy.enjoytrip.repository.FriendshipRepository;
import com.ssafy.enjoytrip.storage.entity.FriendshipEntity;
import com.ssafy.enjoytrip.storage.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.jpa.FriendshipJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.MemberJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class FriendshipStorageRepository implements FriendshipRepository {
    private final FriendshipJpaRepository friendshipJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Friendship> findById(Long id) {
        return friendshipJpaRepository.findById(id).map(this::toModel);
    }

    @Override
    public boolean existsActiveBetween(String userId, String otherUserId) {
        return friendshipJpaRepository.existsActiveBetween(
                userId,
                otherUserId,
                List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED)
        );
    }

    @Override
    @Transactional
    public Friendship savePending(String requesterUserId, String addresseeUserId) {
        FriendshipEntity entity = friendshipJpaRepository.save(new FriendshipEntity(requesterUserId, addresseeUserId));
        return toModelWithoutMemberLookup(entity);
    }

    @Override
    @Transactional
    public Friendship updateStatus(Long id, FriendshipStatus status) {
        FriendshipEntity entity = friendshipJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Friendship not found: " + id));
        entity.transitionTo(status);
        return toModel(entity);
    }

    @Override
    public List<Friendship> findAcceptedByUser(String userId) {
        return friendshipJpaRepository.findByParticipantAndStatus(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Friendship> findPendingReceivedByUser(String userId) {
        return friendshipJpaRepository.findByAddresseeUserIdAndStatusOrderByRequestedAtDescIdDesc(
                        userId,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<Friendship> findPendingSentByUser(String userId) {
        return friendshipJpaRepository.findByRequesterUserIdAndStatusOrderByRequestedAtDescIdDesc(
                        userId,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(this::toModel)
                .toList();
    }

    private Friendship toModel(FriendshipEntity entity) {
        return toModel(
                entity,
                displayName(entity.getRequesterUserId()),
                displayName(entity.getAddresseeUserId())
        );
    }

    private static Friendship toModelWithoutMemberLookup(FriendshipEntity entity) {
        return toModel(entity, entity.getRequesterUserId(), entity.getAddresseeUserId());
    }

    private static Friendship toModel(FriendshipEntity entity,
                                      String requesterDisplayName,
                                      String addresseeDisplayName) {
        return new Friendship(
                entity.getId(),
                entity.getRequesterUserId(),
                requesterDisplayName,
                entity.getAddresseeUserId(),
                addresseeDisplayName,
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String displayName(String userId) {
        return memberJpaRepository.findByUserId(userId)
                .map(FriendshipStorageRepository::displayName)
                .orElse(userId);
    }

    private static String displayName(MemberEntity member) {
        if (member.getNickname() != null && !member.getNickname().isBlank()) {
            return member.getNickname();
        }
        if (member.getName() != null && !member.getName().isBlank()) {
            return member.getName();
        }
        return member.getUserId();
    }
}
