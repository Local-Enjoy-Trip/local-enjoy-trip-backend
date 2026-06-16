package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.application.dto.command.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.repository.NoteImageUploadUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteImageService {
    private final NoteImageUploadUrlGenerator uploadUrlGenerator;

    public NoteImageUploadUrl createUploadUrl(NoteImageUploadCommand command) {
        return uploadUrlGenerator.generate(command);
    }
}
