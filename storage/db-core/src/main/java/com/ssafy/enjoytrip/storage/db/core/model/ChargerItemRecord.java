package com.ssafy.enjoytrip.storage.db.core.model;

public record ChargerItemRecord(
        String statId,
        String statNm,
        String chgerId,
        String chgerType,
        String addr,
        String location,
        Double lat,
        Double lng,
        String useTime,
        String busiNm,
        String busiCall,
        String stat
) {
}
