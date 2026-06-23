package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTION_NOT_FOUND;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionAdminRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionAdminService {
    private static final int MIN_PAGE = 1;

    private final AttractionMapper attractionMapper;

    public List<AttractionAdminRecord> findPlaces(boolean includeHidden) {
        return attractionMapper.findAllForAdmin(includeHidden);
    }

    public AdminPlacePage findPlacesPage(boolean includeHidden, int requestedPage, int pageSize) {
        int totalCount = attractionMapper.countForAdmin(includeHidden);
        int totalPages = totalPages(totalCount, pageSize);
        int currentPage = currentPage(requestedPage, totalPages);
        List<AttractionAdminRecord> places = totalCount == 0
                ? List.of()
                : attractionMapper.findAdminPage(
                        includeHidden,
                        pageSize,
                        (currentPage - 1) * pageSize
                );

        return new AdminPlacePage(
                places,
                currentPage,
                pageSize,
                totalCount,
                totalPages
        );
    }

    public AdminPlaceSummary summarizePlaces(boolean includeHidden) {
        return new AdminPlaceSummary(
                attractionMapper.countForAdmin(includeHidden),
                hiddenPlaceCount()
        );
    }

    @Transactional
    public void createPlace(Long id,
                            String title,
                            String addr1,
                            String addr2,
                            String firstImage,
                            String firstImage2,
                            Integer sidoCode,
                            Integer gugunCode,
                            Double latitude,
                            Double longitude,
                            String contentTypeId,
                            String overview) {
        attractionMapper.insertAdmin(new AttractionAdminRecord(
                id,
                title,
                addr1,
                addr2,
                firstImage,
                firstImage2,
                sidoCode,
                gugunCode,
                latitude,
                longitude,
                contentTypeId,
                overview,
                "ACTIVE",
                null,
                null,
                null,
                null,
                null
        ));
    }

    @Transactional
    public void updatePlace(Long id,
                            String title,
                            String addr1,
                            String addr2,
                            String firstImage,
                            String firstImage2,
                            Integer sidoCode,
                            Integer gugunCode,
                            Double latitude,
                            Double longitude,
                            String contentTypeId,
                            String overview,
                            String status,
                            Long duplicateOfAttractionId,
                            String duplicateReason) {
        int updated = attractionMapper.updateAdmin(new AttractionAdminRecord(
                id,
                title,
                addr1,
                addr2,
                firstImage,
                firstImage2,
                sidoCode,
                gugunCode,
                latitude,
                longitude,
                contentTypeId,
                overview,
                status,
                duplicateOfAttractionId,
                duplicateReason,
                null,
                null,
                null
        ));
        if (updated <= 0) {
            throw new CoreException(ATTRACTION_NOT_FOUND);
        }
    }

    @Transactional
    public void hidePlace(Long attractionId) {
        if (attractionMapper.hideForAdmin(attractionId) <= 0) {
            throw new CoreException(ATTRACTION_NOT_FOUND);
        }
    }

    @Transactional
    public void restorePlace(Long attractionId) {
        if (attractionMapper.restoreForAdmin(attractionId) <= 0) {
            throw new CoreException(ATTRACTION_NOT_FOUND);
        }
    }

    private int hiddenPlaceCount() {
        return attractionMapper.countForAdmin(true) - attractionMapper.countForAdmin(false);
    }

    private static int totalPages(int totalCount, int pageSize) {
        if (totalCount <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    private static int currentPage(int requestedPage, int totalPages) {
        return Math.min(Math.max(requestedPage, MIN_PAGE), totalPages);
    }

    public record AdminPlacePage(
            List<AttractionAdminRecord> places,
            int currentPage,
            int pageSize,
            int totalCount,
            int totalPages
    ) {
        public boolean hasPrevious() {
            return currentPage > MIN_PAGE;
        }

        public boolean hasNext() {
            return currentPage < totalPages;
        }

        public int previousPage() {
            return Math.max(MIN_PAGE, currentPage - 1);
        }

        public int nextPage() {
            return Math.min(totalPages, currentPage + 1);
        }
    }

    public record AdminPlaceSummary(
            int totalCount,
            long hiddenCount
    ) {
    }
}
