package com.ssafy.enjoytrip.core.domain;

public record ChargerItem(
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
