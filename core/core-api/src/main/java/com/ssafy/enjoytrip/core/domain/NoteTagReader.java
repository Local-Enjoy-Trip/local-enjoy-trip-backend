package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.storage.db.core.model.TagFrequencyRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteTagMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteTagReader {
    private final NoteTagMapper noteTagMapper;

    public Map<Long, Long> findMemberTagFrequency(Long memberId) {
        List<TagFrequencyRecord> records =
                noteTagMapper.findTagFrequencyByMemberId(memberId);

        Map<Long, Long> frequency = new HashMap<>();
        for (TagFrequencyRecord record : records) {
            frequency.put(record.getTagId(), record.getCount());
        }
        return frequency;
    }
}
