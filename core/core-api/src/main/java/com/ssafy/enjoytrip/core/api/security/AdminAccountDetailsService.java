package com.ssafy.enjoytrip.core.api.security;

import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class AdminAccountDetailsService implements UserDetailsService {
    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String email) {
        MemberRecord member = memberMapper.findByEmail(email);
        if (member == null || !MemberRole.ADMIN.name().equals(member.getRole())) {
            throw new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다.");
        }
        return User.withUsername(member.getUserId())
                .password(member.getPassword())
                .roles(member.getRole())
                .build();
    }
}
