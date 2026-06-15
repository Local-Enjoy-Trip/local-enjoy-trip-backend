package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;

import com.ssafy.enjoytrip.domain.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.repository.NoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteImageUploadService {
    private final NoteImageUploadUrlGenerator uploadUrlGenerator;

    public NoteImageUploadUrl createPresignedUpload(NoteImageUploadCommand command) {
        return uploadUrlGenerator.generate(new NoteImageUploadCommand(
                requireUserId(command.userId()),
                requireImageContentType(command.contentType()),
                requireSafeExtension(command.fileExtension())
        ));
    }

    private static String requireUserId(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return value.strip();
    }

    private static String requireImageContentType(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(INVALID_REQUEST);
        }

        String normalized = value.strip().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("image/") || normalized.length() > 100) {
            throw new CoreException(INVALID_REQUEST);
        }
        return normalized;
    }

    private static String requireSafeExtension(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(INVALID_REQUEST);
        }

        String normalized = value.strip().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.matches("[a-z0-9]{1,10}")) {
            throw new CoreException(INVALID_REQUEST);
        }
        return normalized;
    }
}
