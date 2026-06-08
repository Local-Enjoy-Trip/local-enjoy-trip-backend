package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.ChargerItem;

import java.util.List;

public interface ChargerRepository {
    List<ChargerItem> findChargers(String zcode, String keyword, int pageNo, int numOfRows);
}
