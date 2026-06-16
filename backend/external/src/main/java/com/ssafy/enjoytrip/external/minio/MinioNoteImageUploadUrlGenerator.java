package com.ssafy.enjoytrip.external.minio;

import com.ssafy.enjoytrip.dto.command.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.repository.NoteImageUploadUrlGenerator;
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
public class MinioNoteImageUploadUrlGenerator implements NoteImageUploadUrlGenerator {
    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public NoteImageUploadUrl generate(NoteImageUploadCommand command) {
        String objectKey = objectKey(command.userId(), command.fileExtension());
        Instant expiresAt = Instant.now().plus(properties.getUploadExpiry());

        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry((int) properties.getUploadExpiry().toSeconds())
                    .extraHeaders(Map.of("Content-Type", command.contentType()))
                    .build());

            return new NoteImageUploadUrl(objectKey, uploadUrl, expiresAt, publicUrl(objectKey));
        } catch (Exception exception) {
            throw new IllegalStateException("쪽지 이미지 업로드 URL 생성에 실패했습니다.", exception);
        }
    }

    private static String objectKey(String userId, String extension) {
        return "notes/" + userId + "/" + UUID.randomUUID() + "." + extension;
    }

    private String publicUrl(String objectKey) {
        return properties.getPublicBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
    }
}
