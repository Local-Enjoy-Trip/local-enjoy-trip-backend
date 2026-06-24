package com.ssafy.enjoytrip.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.external.minio.ProfileImageUploadUrlGenerator;
import com.ssafy.enjoytrip.external.minio.MinioProperties;
import com.ssafy.enjoytrip.external.minio.ProfileImageUploadResult;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileImageUploadUrlGeneratorTest {
    @DisplayName("프로필 이미지 presigned URL은 회원 prefix objectKey와 정규화된 public URL을 반환한다")
    @Test
    void generatesProfileImageUploadResult() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/dongnepin-notes/profiles/11/sample.jpg?signature=abc");
        MinioProperties properties = new MinioProperties();
        properties.setBucket("dongnepin-notes");
        properties.setPublicBaseUrl("http://localhost:9000/dongnepin-notes/");
        properties.setUploadExpiry(Duration.ofMinutes(10));
        ProfileImageUploadUrlGenerator generator = new ProfileImageUploadUrlGenerator(
                minioClient,
                properties
        );

        ProfileImageUploadResult result = generator.generate("11", "image/jpeg", "jpg");

        assertThat(result.objectKey()).startsWith("profiles/11/").endsWith(".jpg");
        assertThat(result.uploadUrl()).contains("signature=abc");
        assertThat(result.publicUrl()).isEqualTo(
                "http://localhost:9000/dongnepin-notes/" + result.objectKey()
        );
        assertThat(result.expiresAt()).isNotNull();
    }
}
