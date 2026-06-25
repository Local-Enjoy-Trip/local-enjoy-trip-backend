package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NoteServiceTest {
    private static final String NOTE_IMAGE_OBJECT_KEY =
            "notes/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg";
    private static final String NOTE_IMAGE_PUBLIC_URL =
            "http://localhost:9000/dongnepin-notes/" + NOTE_IMAGE_OBJECT_KEY;

    private NoteMapper noteMapper;
    private MinioNoteImageUploadUrlGenerator uploadUrlGenerator;
    private NoteService service;

    @BeforeEach
    void setUp() {
        noteMapper = mock(NoteMapper.class);
        uploadUrlGenerator = mock(MinioNoteImageUploadUrlGenerator.class);
        service = new NoteService(noteMapper, uploadUrlGenerator,
                mock(org.springframework.context.ApplicationEventPublisher.class));
    }

    @DisplayName("쪽지 생성은 인증 회원의 note image objectKey만 저장하고 public URL은 서버에서 계산한다")
    @Test
    void createNotePersistsValidatedOwnedImageObjectKeyAndServerPublicUrl() {
        when(uploadUrlGenerator.publicUrl(NOTE_IMAGE_OBJECT_KEY)).thenReturn(NOTE_IMAGE_PUBLIC_URL);
        when(noteMapper.insert(any(NoteRecord.class))).thenAnswer(invocation -> savedRecord(invocation.getArgument(0)));

        service.createNote(note(NOTE_IMAGE_OBJECT_KEY, "https://evil.example.com/image.jpg"));

        ArgumentCaptor<NoteRecord> recordCaptor = ArgumentCaptor.forClass(NoteRecord.class);
        verify(noteMapper).insert(recordCaptor.capture());
        NoteRecord saved = recordCaptor.getValue();
        assertThat(saved.getAuthorMemberId()).isEqualTo(11L);
        assertThat(saved.getImageObjectKey()).isEqualTo(NOTE_IMAGE_OBJECT_KEY);
        assertThat(saved.getImageUrl()).isEqualTo(NOTE_IMAGE_PUBLIC_URL);
    }

    @DisplayName("쪽지 생성은 다른 회원의 note image objectKey를 거부한다")
    @Test
    void createNoteRejectsForeignImageObjectKey() {
        assertThatThrownBy(() -> service.createNote(note(
                "notes/12/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
                "https://cdn.example.com/image.jpg"
        )))
                .isInstanceOf(ClientInputException.class);
    }

    private static Note note(String imageObjectKey, String imageUrl) {
        return new Note(
                null,
                11L,
                "제목",
                "내용",
                NoteCategory.BEST,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "서울",
                imageObjectKey,
                imageUrl,
                "image/jpeg",
                NoteStatus.ACTIVE,
                null,
                null,
                null
        );
    }

    private static NoteRecord savedRecord(NoteRecord source) {
        NoteRecord saved = new NoteRecord(
                1L,
                source.getAuthorMemberId(),
                source.getTitle(),
                source.getContent(),
                source.getCategory(),
                source.getVisibility(),
                source.getLatitude(),
                source.getLongitude(),
                source.getRegionName(),
                source.getImageObjectKey(),
                source.getImageUrl(),
                source.getImageContentType()
        );
        saved.setStatus(NoteStatus.ACTIVE.name());
        saved.setLatitude(BigDecimal.valueOf(37.5665));
        saved.setLongitude(BigDecimal.valueOf(126.9780));
        return saved;
    }
}
