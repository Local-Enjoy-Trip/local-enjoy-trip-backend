package com.ssafy.enjoytrip.storage.db.core.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity extends BaseEntity {
    private Long id;

    private String userId;

    private String name;

    private String nickname;

    private String email;

    private String password;

    private String profileImageUrl;

    private BigDecimal representativeLatitude;

    private BigDecimal representativeLongitude;

    private String representativeRegionName;

    public MemberEntity(String userId,
                        String name,
                        String nickname,
                        String email,
                        String password,
                        String profileImageUrl,
                        Double representativeLatitude,
                        Double representativeLongitude,
                        String representativeRegionName) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.representativeLatitude = decimalValue(representativeLatitude);
        this.representativeLongitude = decimalValue(representativeLongitude);
        this.representativeRegionName = representativeRegionName;
    }

    public void update(String name,
                       String nickname,
                       String email,
                       String password,
                       String profileImageUrl,
                       Double representativeLatitude,
                       Double representativeLongitude,
                       String representativeRegionName) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
        if (representativeLatitude != null && representativeLongitude != null) {
            this.representativeLatitude = decimalValue(representativeLatitude);
            this.representativeLongitude = decimalValue(representativeLongitude);
        }
        if (representativeRegionName != null && !representativeRegionName.isBlank()) {
            this.representativeRegionName = representativeRegionName;
        }
    }

    public Double getRepresentativeLatitude() {
        return doubleValue(representativeLatitude);
    }

    public Double getRepresentativeLongitude() {
        return doubleValue(representativeLongitude);
    }

    private static BigDecimal decimalValue(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private static Double doubleValue(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }
}
