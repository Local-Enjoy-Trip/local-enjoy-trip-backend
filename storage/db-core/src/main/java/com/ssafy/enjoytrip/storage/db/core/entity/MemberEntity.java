package com.ssafy.enjoytrip.storage.db.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_members_user_id", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_members_email", columnNames = "email")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "representative_latitude", precision = 10, scale = 7)
    private BigDecimal representativeLatitude;

    @Column(name = "representative_longitude", precision = 10, scale = 7)
    private BigDecimal representativeLongitude;

    @Column(name = "representative_region_name", length = 100)
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
