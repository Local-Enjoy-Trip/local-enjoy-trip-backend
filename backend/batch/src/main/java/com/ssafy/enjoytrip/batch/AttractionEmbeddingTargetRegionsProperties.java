package com.ssafy.enjoytrip.batch;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "enjoytrip.ai.embedding.target-regions")
public class AttractionEmbeddingTargetRegionsProperties {
    private List<Region> regions = new ArrayList<>();

    public List<AttractionEmbeddingTargetRegion> toTargetRegions() {
        return regions.stream()
                .map(region -> new AttractionEmbeddingTargetRegion(
                        region.getSidoName(), region.getGugunName(), region.getSidoCode(), region.getGugunCode(), region.getProvenance()))
                .toList();
    }

    public List<Region> getRegions() { return regions; }
    public void setRegions(List<Region> regions) { this.regions = regions; }

    public static class Region {
        private String sidoName;
        private String gugunName;
        private int sidoCode;
        private int gugunCode;
        private String provenance;

        public String getSidoName() { return sidoName; }
        public void setSidoName(String sidoName) { this.sidoName = sidoName; }
        public String getGugunName() { return gugunName; }
        public void setGugunName(String gugunName) { this.gugunName = gugunName; }
        public int getSidoCode() { return sidoCode; }
        public void setSidoCode(int sidoCode) { this.sidoCode = sidoCode; }
        public int getGugunCode() { return gugunCode; }
        public void setGugunCode(int gugunCode) { this.gugunCode = gugunCode; }
        public String getProvenance() { return provenance; }
        public void setProvenance(String provenance) { this.provenance = provenance; }
    }
}
