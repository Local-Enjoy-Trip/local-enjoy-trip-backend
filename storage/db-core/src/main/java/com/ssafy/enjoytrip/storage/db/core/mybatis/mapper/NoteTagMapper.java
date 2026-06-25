package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NoteTagRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TagFrequencyRecord;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface NoteTagMapper {
    List<NoteTagRecord> findByNoteId(@Param("noteId") Long noteId);

    int insertAll(@Param("noteId") Long noteId, @Param("tagIds") List<Long> tagIds);

    int deleteByNoteId(@Param("noteId") Long noteId);

    List<TagFrequencyRecord> findTagFrequencyByMemberId(@Param("memberId") Long memberId);
}
