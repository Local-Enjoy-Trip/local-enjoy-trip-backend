package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Hotplace;

import java.util.List;

public interface HotplaceRepository {
    List<Hotplace> findAll();

    List<Hotplace> findByUser(String userId);

    void insert(Hotplace hotplace);

    boolean delete(String id);
}
