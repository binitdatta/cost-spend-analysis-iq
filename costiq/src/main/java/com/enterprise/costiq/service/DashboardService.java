package com.enterprise.costiq.service;

import com.enterprise.costiq.dto.DashboardSummaryDto;
import com.enterprise.costiq.entity.Campaign;
import com.enterprise.costiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final FoodCostEntryRepository foodRepo;
    private final PackagingCostEntryRepository packagingRepo;
    private final CampaignToyAllocationRepository toyRepo;
    private final CampaignMarketingCostRepository marketingRepo;
    private final CampaignRepository campaignRepo;
    private final SupplierRepository supplierRepo;

    public DashboardSummaryDto buildSummary() {
        BigDecimal foodTotal      = foodRepo.sumTotalCost();
        BigDecimal packagingTotal = packagingRepo.sumTotalCost();
        BigDecimal toyTotal       = toyRepo.sumTotalCost();
        BigDecimal marketingTotal = marketingRepo.sumTotalCost();
        BigDecimal grand = foodTotal.add(packagingTotal).add(toyTotal).add(marketingTotal);

        long activeCampaigns = campaignRepo.findByStatusOrderByStartDateDesc(Campaign.Status.ACTIVE).size();
        long totalCampaigns  = campaignRepo.count();
        long activeSuppliers = supplierRepo.findAllActiveWithCountry().size();

        return DashboardSummaryDto.builder()
            .totalFoodCostUsd(foodTotal)
            .totalPackagingCostUsd(packagingTotal)
            .totalToyCostUsd(toyTotal)
            .totalMarketingCostUsd(marketingTotal)
            .grandTotalUsd(grand)
            .foodEntryCount(foodRepo.count())
            .packagingEntryCount(packagingRepo.count())
            .toyAllocationCount(toyRepo.count())
            .marketingCostCount(marketingRepo.count())
            .activeCampaigns(activeCampaigns)
            .totalCampaigns(totalCampaigns)
            .activeSuppliers(activeSuppliers)
            .foodCostByRegion(toMap(foodRepo.totalCostByRegion()))
            .packagingCostByRegion(toMap(packagingRepo.totalCostByRegion()))
            .toyCostByRegion(toMap(toyRepo.totalCostByRegion()))
            .foodCostByPeriod(toMap(foodRepo.totalCostByPeriod()))
            .packagingCostByPeriod(toMap(packagingRepo.totalCostByPeriod()))
            .toyCostByCampaign(toMap(toyRepo.totalCostByCampaign()))
            .marketingCostByType(toMapStr(marketingRepo.totalCostByCostType()))
            .build();
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }
        return map;
    }

    private Map<String, BigDecimal> toMapStr(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), (BigDecimal) row[1]);
        }
        return map;
    }
}
