package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.external.minio.ProfileImageUploadUrlGenerator;
import com.ssafy.enjoytrip.external.minio.ProfileImageUploadResult;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberProfileImageServiceTest {
    private static final String PROFILE_IMAGE_OBJECT_KEY =
            "profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg";

    private ProfileImageUploadUrlGenerator uploadUrlGenerator;
    private MemberMapper memberMapper;
    private MemberProfileImageService service;

    @BeforeEach
    void setUp() {
        uploadUrlGenerator = mock(ProfileImageUploadUrlGenerator.class);
        memberMapper = mock(MemberMapper.class);
        service = new MemberProfileImageService(uploadUrlGenerator, memberMapper);
    }

    @DisplayName("프로필 이미지 presigned upload는 external 결과를 domain upload URL로 변환한다")
    @Test
    void createsPresignedUpload() {
        when(uploadUrlGenerator.generate("11", "image/jpeg", "jpg"))
                .thenReturn(new ProfileImageUploadResult(
                        PROFILE_IMAGE_OBJECT_KEY,
                        "http://localhost:9000/upload?signature=abc",
                        Instant.parse("2026-06-15T01:10:00Z"),
                        "http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY
                ));

        ProfileImageUploadUrl upload = service.createPresignedUpload(11L, "image/jpeg", "jpg");

        assertThat(upload.objectKey()).isEqualTo(PROFILE_IMAGE_OBJECT_KEY);
        assertThat(upload.uploadUrl()).contains("signature=abc");
        assertThat(upload.publicUrl()).endsWith(PROFILE_IMAGE_OBJECT_KEY);
    }

    @DisplayName("프로필 이미지 저장은 서버 생성 objectKey와 서버 계산 public URL만 저장한다")
    @Test
    void updatesOwnedProfileImageWithServerPublicUrl() {
        when(uploadUrlGenerator.publicUrl(PROFILE_IMAGE_OBJECT_KEY))
                .thenReturn("http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY);
        when(memberMapper.updateProfileImage(
                11L,
                PROFILE_IMAGE_OBJECT_KEY,
                "http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY
        )).thenReturn(1);

        service.updateProfileImage(11L, PROFILE_IMAGE_OBJECT_KEY);

        verify(memberMapper).updateProfileImage(
                11L,
                PROFILE_IMAGE_OBJECT_KEY,
                "http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY
        );
    }

    @DisplayName("프로필 이미지 저장은 갱신 대상 회원이 없으면 비즈니스 예외를 던진다")
    @Test
    void rejectsProfileImageUpdateForMissingMember() {
        when(uploadUrlGenerator.publicUrl(PROFILE_IMAGE_OBJECT_KEY))
                .thenReturn("http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY);
        when(memberMapper.updateProfileImage(
                11L,
                PROFILE_IMAGE_OBJECT_KEY,
                "http://localhost:9000/dongnepin-notes/" + PROFILE_IMAGE_OBJECT_KEY
        )).thenReturn(0);

        assertThatThrownBy(() -> service.updateProfileImage(11L, PROFILE_IMAGE_OBJECT_KEY))
                .isInstanceOf(CoreException.class);
    }
}
