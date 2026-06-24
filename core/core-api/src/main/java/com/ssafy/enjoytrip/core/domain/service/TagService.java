package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Tag;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.TagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.TagMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagMapper tagMapper;

    public List<Tag> findAll() {
        return tagMapper.findAll().stream()
                .map(record -> new Tag(record.id(), record.name()))
                .toList();
    }

    public Tag createOrThrow(String name) {
        requireNameAvailable(null, name);
        TagRecord record = tagMapper.insert(name);
        return new Tag(record.id(), record.name());
    }

    public void updateOrThrow(Long id, String name) {
        requireNameAvailable(id, name);
        if (tagMapper.update(id, name) <= 0) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    public void deleteOrThrow(Long id) {
        if (tagMapper.delete(id) <= 0) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    public void validateTagIds(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<Long> distinct = tagIds.stream().distinct().toList();
        if (tagMapper.countByIds(distinct) != distinct.size()) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

    private void requireNameAvailable(Long currentId, String name) {
        boolean exists = findAll().stream()
                .anyMatch(tag -> tag.name().equals(name) && !tag.id().equals(currentId));
        if (exists) {
            throw new CoreException(TAG_ALREADY_EXISTS);
        }
    }
}
