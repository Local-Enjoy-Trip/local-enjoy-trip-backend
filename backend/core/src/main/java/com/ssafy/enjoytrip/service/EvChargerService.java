package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvChargerService {
    private final ChargerRepository repository;

    public List<ChargerItem> findChargers(String zcode, String keyword, int pageNo, int numOfRows) {
        return repository.findChargers(zcode, keyword, pageNo, numOfRows);
    }
}
