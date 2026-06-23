package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MemberLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @JsonIgnore
    private final Map<String, Object> unknownFields = new LinkedHashMap<>();

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    @JsonAnySetter
    void putUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }

    @AssertTrue(message = "지원하지 않는 요청 필드입니다.")
    public boolean isUnknownFieldsEmpty() {
        return unknownFields.isEmpty();
    }
}
