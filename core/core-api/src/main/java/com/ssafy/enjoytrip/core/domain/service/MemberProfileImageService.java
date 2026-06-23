package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.external.minio.ProfileImageUploadResult;
import com.ssafy.enjoytrip.external.minio.ProfileImageUploadUrlGenerator;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileImageService {
    private final ProfileImageUploadUrlGenerator uploadUrlGenerator;
    private final MemberMapper memberMapper;

    public ProfileImageUploadUrl createPresignedUpload(
            Long memberId,
            String contentType,
            String fileExtension
    ) {
        ProfileImageUploadResult generated = uploadUrlGenerator.generate(
                String.valueOf(memberId),
                contentType,
                fileExtension
        );

        return new ProfileImageUploadUrl(
                generated.objectKey(),
                generated.uploadUrl(),
                generated.expiresAt(),
                generated.publicUrl()
        );
    }

    @Transactional
    public void updateProfileImage(Long memberId, String objectKey) {
        int updated = memberMapper.updateProfileImage(
                memberId,
                objectKey,
                uploadUrlGenerator.publicUrl(objectKey)
        );
        if (updated <= 0) {
            throw new CoreException(ErrorType.USER_NOT_FOUND);
        }
    }
}
