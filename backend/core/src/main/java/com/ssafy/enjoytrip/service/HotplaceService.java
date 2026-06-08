package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.repository.HotplaceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotplaceService {
    private final HotplaceRepository repository;

    public List<Hotplace> findAllHotplaces() {
        return repository.findAll();
    }

    public List<Hotplace> findHotplacesByUser(String userId) {
        return repository.findByUser(userId);
    }

    public void insertHotplace(Hotplace hotplace) {
        repository.insert(hotplace);
    }

    public boolean deleteHotplace(String id) {
        return repository.delete(id);
    }
}
