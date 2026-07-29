package io.dampen59.mineboxadditions.features.ahlerter;

import java.util.Map;

public class AhAlert {
    public String itemId;
    public Long maxPrice;
    public Map<String, StatFilter> statFilters;

    public AhAlert() {}

    public AhAlert(String itemId, Long maxPrice, Map<String, StatFilter> statFilters) {
        this.itemId = itemId;
        this.maxPrice = maxPrice;
        this.statFilters = statFilters;
    }

    public boolean matches(String auctionItemId, long pricePerUnit, Map<String, Integer> stats) {
        if (!itemId.equals(auctionItemId)) return false;
        if (maxPrice != null && pricePerUnit > maxPrice) return false;
        if (statFilters != null) {
            for (Map.Entry<String, StatFilter> e : statFilters.entrySet()) {
                StatFilter f = e.getValue();
                if (f == null) continue;
                Integer val = stats != null ? stats.get(e.getKey()) : null;
                if (val == null) return false;
                if (f.min != null && val < f.min) return false;
                if (f.max != null && val > f.max) return false;
            }
        }
        return true;
    }

    public static class StatFilter {
        public Long min;
        public Long max;

        public StatFilter() {}

        public StatFilter(Long min, Long max) {
            this.min = min;
            this.max = max;
        }
    }
}
