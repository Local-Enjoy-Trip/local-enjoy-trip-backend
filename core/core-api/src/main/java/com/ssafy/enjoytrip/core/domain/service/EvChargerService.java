package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.EvChargers.EV_CHARGERS;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.noCondition;

import com.ssafy.enjoytrip.core.domain.ChargerItem;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvChargerService {

    private static final Map<String, String> ZCODE_REGIONS = Map.ofEntries(
            Map.entry("11", "서울"),
            Map.entry("26", "부산"),
            Map.entry("27", "대구"),
            Map.entry("28", "인천"),
            Map.entry("29", "광주"),
            Map.entry("30", "대전"),
            Map.entry("31", "울산"),
            Map.entry("36", "세종"),
            Map.entry("41", "경기"),
            Map.entry("42", "강원"),
            Map.entry("43", "충북"),
            Map.entry("44", "충남"),
            Map.entry("45", "전북"),
            Map.entry("46", "전남"),
            Map.entry("47", "경북"),
            Map.entry("48", "경남"),
            Map.entry("50", "제주")
    );

    private final DSLContext dslContext;

    public List<ChargerItem> findChargers(String zcode, String keyword, int pageNo, int numOfRows) {
        int limit = Math.max(10, Math.min(500, numOfRows));
        int page = Math.max(1, pageNo);

        String region = ZCODE_REGIONS.get(trim(zcode));

        return dslContext.select(
                        EV_CHARGERS.STAT_ID.as("statId"),
                        EV_CHARGERS.STAT_NM.as("statNm"),
                        EV_CHARGERS.CHGER_ID.as("chgerId"),
                        EV_CHARGERS.CHGER_TYPE.as("chgerType"),
                        EV_CHARGERS.ADDR.as("addr"),
                        EV_CHARGERS.LOCATION_DESC.as("location"),
                        field("ST_Y({0})", Double.class, EV_CHARGERS.LOCATION).as("lat"),
                        field("ST_X({0})", Double.class, EV_CHARGERS.LOCATION).as("lng"),
                        EV_CHARGERS.USE_TIME.as("useTime"),
                        EV_CHARGERS.BUSI_NM.as("busiNm"),
                        EV_CHARGERS.BUSI_CALL.as("busiCall"),
                        EV_CHARGERS.STAT.as("stat")
                )
                .from(EV_CHARGERS)
                .where(regionCondition(region))
                .and(keywordCondition(keyword))
                .orderBy(EV_CHARGERS.STAT_NM.asc(), EV_CHARGERS.STAT_ID.asc(), EV_CHARGERS.CHGER_ID.asc())
                .limit(limit)
                .offset((page - 1) * limit)
                .fetchInto(ChargerItem.class);
    }

    private Condition regionCondition(String region) {
        if (region == null || region.isBlank()) {
            return noCondition();
        }
        return coalesce(EV_CHARGERS.ADDR, "").likeIgnoreCase("%" + region.trim() + "%");
    }

    private Condition keywordCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return noCondition();
        }
        String pattern = "%" + keyword.trim() + "%";
        return EV_CHARGERS.STAT_NM.likeIgnoreCase(pattern)
                .or(coalesce(EV_CHARGERS.ADDR, "").likeIgnoreCase(pattern))
                .or(coalesce(EV_CHARGERS.LOCATION_DESC, "").likeIgnoreCase(pattern));
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
