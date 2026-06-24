package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.TagRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagMapper {
    List<TagRecord> findAll();

    TagRecord insert(String name);

    int update(@Param("id") Long id, @Param("name") String name);

    int delete(Long id);

    int countByIds(@Param("ids") List<Long> ids);
}
