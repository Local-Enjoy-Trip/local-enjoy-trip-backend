package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.ChargerItem;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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

    private final EvChargerMapper evChargerMapper;

    public List<ChargerItem> findChargers(String zcode, String keyword, int pageNo, int numOfRows) {
        int limit = Math.max(10, Math.min(500, numOfRows));
        int page = Math.max(1, pageNo);
        String region = ZCODE_REGIONS.get(trim(zcode));

        return evChargerMapper.findChargers(region, blankToNull(keyword), limit, (page - 1) * limit)
                .stream()
                .map(record -> new ChargerItem(
                        record.statId(),
                        record.statNm(),
                        record.chgerId(),
                        record.chgerType(),
                        record.addr(),
                        record.location(),
                        record.lat(),
                        record.lng(),
                        record.useTime(),
                        record.busiNm(),
                        record.busiCall(),
                        record.stat()
                ))
                .toList();
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
