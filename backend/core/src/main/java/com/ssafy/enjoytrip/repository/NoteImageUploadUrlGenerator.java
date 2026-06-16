package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.dto.command.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;

public interface NoteImageUploadUrlGenerator {
    NoteImageUploadUrl generate(NoteImageUploadCommand command);
}
