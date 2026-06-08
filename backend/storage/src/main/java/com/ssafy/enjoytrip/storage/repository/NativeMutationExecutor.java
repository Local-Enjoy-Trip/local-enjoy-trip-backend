package com.ssafy.enjoytrip.storage.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NativeMutationExecutor {
    private final DSLContext dslContext;

    public int update(String sql, Object... args) {
        return dslContext.query(sql, args).execute();
    }
}
