package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRecord extends BaseRecord {
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

    public MemberRecord(String userId,
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

    public void update(String nickname,
                       String profileImageUrl,
                       Double representativeLatitude,
                       Double representativeLongitude,
                       String representativeRegionName) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.representativeLatitude = decimalValue(representativeLatitude);
        this.representativeLongitude = decimalValue(representativeLongitude);
        this.representativeRegionName = representativeRegionName;
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
