package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Tag;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.TagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.TagMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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

    public Tag create(String name) {
        TagRecord record = tagMapper.insert(name);
        return new Tag(record.id(), record.name());
    }

    public void updateOrThrow(Long id, String name) {
        try {
            if (tagMapper.update(id, name) <= 0) {
                throw new CoreException(TAG_NOT_FOUND);
            }
        } catch (DuplicateKeyException e) {
            throw new CoreException(TAG_ALREADY_EXISTS);
        }
    }

    public void deleteOrThrow(Long id) {
        if (tagMapper.delete(id) <= 0) {
            throw new CoreException(TAG_NOT_FOUND);
        }
    }

}
