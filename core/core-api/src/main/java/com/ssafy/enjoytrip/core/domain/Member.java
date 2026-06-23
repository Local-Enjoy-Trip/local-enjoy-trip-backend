package com.ssafy.enjoytrip.core.domain;

public record Member(
        Long memberId,
        String name,
        String nickname,
        String email,
        String password,
        String profileImageUrl
) {
    public Member(String name, String email, String password) {
        this(null, name, name, email, password, null);
    }

    public Member withPassword(String newPassword) {
        return new Member(
                memberId,
                name,
                nickname,
                email,
                newPassword,
                profileImageUrl
        );
    }

    public Member withMemberId(Long newMemberId) {
        return new Member(
                newMemberId,
                name,
                nickname,
                email,
                password,
                profileImageUrl
        );
    }

    public String displayName() {
        if (!isBlank(nickname)) {
            return nickname;
        }
        if (!isBlank(name)) {
            return name;
        }
        return email;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
