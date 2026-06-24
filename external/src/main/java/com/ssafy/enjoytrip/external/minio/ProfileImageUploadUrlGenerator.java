package com.ssafy.enjoytrip.external.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileImageUploadUrlGenerator {
    private final MinioClient minioClient;
    private final MinioProperties properties;

    public ProfileImageUploadResult generate(String memberId, String contentType, String fileExtension) {
        String objectKey = objectKey(memberId, fileExtension);
        Instant expiresAt = Instant.now().plus(properties.getUploadExpiry());

        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry((int) properties.getUploadExpiry().toSeconds())
                    .extraHeaders(Map.of("Content-Type", contentType))
                    .build());

            return new ProfileImageUploadResult(objectKey, uploadUrl, expiresAt, publicUrl(objectKey));
        } catch (Exception exception) {
            throw new IllegalStateException("프로필 이미지 업로드 URL 생성에 실패했습니다.", exception);
        }
    }

    private static String objectKey(String memberId, String extension) {
        return "profiles/" + memberId + "/" + UUID.randomUUID() + "." + extension;
    }

    public String publicUrl(String objectKey) {
        return properties.getPublicBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
    }
}
