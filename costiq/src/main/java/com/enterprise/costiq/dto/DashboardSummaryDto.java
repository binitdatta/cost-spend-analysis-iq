package com.enterprise.costiq.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    private BigDecimal totalFoodCostUsd;
    private BigDecimal totalPackagingCostUsd;
    private BigDecimal totalToyCostUsd;
    private BigDecimal totalMarketingCostUsd;
    private BigDecimal grandTotalUsd;

    private long foodEntryCount;
    private long packagingEntryCount;
    private long toyAllocationCount;
    private long marketingCostCount;

    private long activeCampaigns;
    private long totalCampaigns;
    private long activeSuppliers;

    // Chart data: region → cost
    private Map<String, BigDecimal> foodCostByRegion;
    private Map<String, BigDecimal> packagingCostByRegion;
    private Map<String, BigDecimal> toyCostByRegion;

    // Trend: period → cost
    private Map<String, BigDecimal> foodCostByPeriod;
    private Map<String, BigDecimal> packagingCostByPeriod;

    // Campaign breakdown
    private Map<String, BigDecimal> toyCostByCampaign;
    private Map<String, BigDecimal> marketingCostByType;
}
