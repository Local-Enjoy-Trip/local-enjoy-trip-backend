package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.storage.entity.AuthLogEntity;
import com.ssafy.enjoytrip.storage.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.jpa.AuthLogJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberStorageRepository implements MemberRepository {
    private final MemberJpaRepository memberRepository;
    private final AuthLogJpaRepository authLogRepository;
    
    // (Note: manual constructor removed)

    @Override
    public List<Member> findAll() {
        return memberRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toModel).toList();
    }

    @Override
    public Member findByUserId(String userId) {
        return memberRepository.findByUserId(userId).map(this::toModel).orElse(null);
    }

    @Override
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).map(this::toModel).orElse(null);
    }

    @Override
    public String findPassword(String userId, String email) {
        return memberRepository.findByUserIdAndEmail(userId, email)
                .map(MemberEntity::getPassword)
                .orElse(null);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void insert(Member member) {
        memberRepository.save(new MemberEntity(member.userId(), member.name(), member.email(), member.password()));
    }

    @Override
    @Transactional
    public boolean update(Member member) {
        return memberRepository.findByUserId(member.userId())
                .map(entity -> {
                    entity.update(member.name(), member.email(), member.password());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(String userId) {
        if (!memberRepository.existsByUserId(userId)) {
            return false;
        }
        memberRepository.deleteByUserId(userId);
        return true;
    }

    @Override
    @Transactional
    public void insertAuthLog(String userId, String eventType) {
        authLogRepository.save(new AuthLogEntity(userId, eventType));
    }

    private Member toModel(MemberEntity entity) {
        return new Member(
                entity.getUserId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                stringValue(entity.getCreatedAt())
        );
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
