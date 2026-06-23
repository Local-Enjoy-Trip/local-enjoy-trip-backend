package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRecord extends BaseRecord {
    private Long id;

    private String name;

    private String nickname;

    private String email;

    private String password;

    private String profileImageUrl;

    private String profileImageObjectKey;

    private String role;

    public MemberRecord(String name,
                        String nickname,
                        String email,
                        String password,
                        String profileImageUrl) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.role = "USER";
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
