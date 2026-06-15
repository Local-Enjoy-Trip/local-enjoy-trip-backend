package com.ssafy.enjoytrip.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.enjoytrip.domain.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.repository.NoteImageUploadUrlGenerator;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteImageUploadServiceTest {

    @DisplayName("쪽지 이미지 업로드 URL은 정규화된 명령을 생성기에 그대로 전달한다")
    @Test
    void delegatesNormalizedCommandToGenerator() {
        FakeUploadUrlGenerator generator = new FakeUploadUrlGenerator();
        NoteImageUploadService service = new NoteImageUploadService(generator);

        NoteImageUploadUrl upload = service.createPresignedUpload(
                new NoteImageUploadCommand("ssafy", "image/jpeg", "jpg")
        );

        assertEquals("ssafy", generator.command.userId());
        assertEquals("image/jpeg", generator.command.contentType());
        assertEquals("jpg", generator.command.fileExtension());
        assertTrue(upload.objectKey().startsWith("notes/ssafy/"));
        assertTrue(upload.objectKey().endsWith(".jpg"));
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
