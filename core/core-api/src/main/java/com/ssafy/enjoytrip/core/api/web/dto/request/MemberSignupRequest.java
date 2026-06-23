package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MemberSignupRequest {
    @NotBlank
    @Size(min = 2, max = 30)
    private String name;

    @Size(min = 2, max = 30)
    private String nickname;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$")
    private String password;

    @Size(max = 512)
    private String profileImageUrl;

    @JsonIgnore
    private final Map<String, Object> unknownFields = new LinkedHashMap<>();

    public String name() {
        return name;
    }

    public String nicknameOrName() {
        String normalizedNickname = trimToNull(nickname);
        if (normalizedNickname == null) {
            return name.trim();
        }
        return normalizedNickname;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String normalizedProfileImageUrl() {
        return trimToNull(profileImageUrl);
    }

    @JsonAnySetter
    void putUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }

    @AssertTrue(message = "지원하지 않는 요청 필드입니다.")
    public boolean isUnknownFieldsEmpty() {
        return unknownFields.isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
