package com.ssafy.enjoytrip.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.enjoytrip.domain.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.repository.NoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteImageUploadServiceTest {

    @DisplayName("쪽지 이미지 업로드 URL은 사용자와 정리된 이미지 메타데이터를 생성기에 전달한다")
    @Test
    void createsUserScopedPresignedUpload() {
        FakeUploadUrlGenerator generator = new FakeUploadUrlGenerator();
        NoteImageUploadService service = new NoteImageUploadService(generator);

        NoteImageUploadUrl upload = service.createPresignedUpload(
                new NoteImageUploadCommand("ssafy", " IMAGE/JPEG ", ".JPG")
        );

        assertEquals("ssafy", generator.command.userId());
        assertEquals("image/jpeg", generator.command.contentType());
        assertEquals("jpg", generator.command.fileExtension());
        assertTrue(upload.objectKey().startsWith("notes/ssafy/"));
        assertTrue(upload.objectKey().endsWith(".jpg"));
    }

    @DisplayName("이미지가 아닌 contentType은 presigned upload 요청을 거부한다")
    @Test
    void rejectsNonImageContentType() {
        NoteImageUploadService service = new NoteImageUploadService(new FakeUploadUrlGenerator());

        assertThrows(
                CoreException.class,
                () -> service.createPresignedUpload(
                        new NoteImageUploadCommand("ssafy", "text/plain", "txt")
                )
        );
    }

    private static class FakeUploadUrlGenerator implements NoteImageUploadUrlGenerator {
        private NoteImageUploadCommand command;

        @Override
        public NoteImageUploadUrl generate(NoteImageUploadCommand command) {
            this.command = command;
            String objectKey = "notes/%s/generated.%s".formatted(command.userId(), command.fileExtension());
            return new NoteImageUploadUrl(
                    objectKey,
                    "http://localhost:9000/upload",
                    Instant.parse("2026-06-15T00:10:00Z"),
                    "http://localhost:9000/dongnepin-notes/" + objectKey
            );
        }
    }
}
