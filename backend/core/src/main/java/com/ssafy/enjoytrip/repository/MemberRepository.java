package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Member;

import java.util.List;

public interface MemberRepository {
    List<Member> findAll();

    Member findByUserId(String userId);

    Member findByEmail(String email);

    String findPassword(String userId, String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    void insert(Member member);

    boolean update(Member member);

    boolean delete(String userId);

    void insertAuthLog(String userId, String eventType);
}
