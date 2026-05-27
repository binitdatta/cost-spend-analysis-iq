package com.enterprise.costiq.controller;

import com.enterprise.costiq.dto.DashboardSummaryDto;
import com.enterprise.costiq.service.DashboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String dashboard(Model model) throws JsonProcessingException {
        DashboardSummaryDto summary = dashboardService.buildSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("pageTitle", "Dashboard");

        // Chart.js data as JSON strings
        model.addAttribute("regionLabelsJson",   toJson(summary.getFoodCostByRegion().keySet()));
        model.addAttribute("foodRegionJson",     toJson(summary.getFoodCostByRegion().values()));
        model.addAttribute("pkgRegionJson",      toJson(summary.getPackagingCostByRegion().values()));
        model.addAttribute("toyRegionJson",      toJson(summary.getToyCostByRegion().values()));

        model.addAttribute("periodLabelsJson",   toJson(summary.getFoodCostByPeriod().keySet()));
        model.addAttribute("foodPeriodJson",     toJson(summary.getFoodCostByPeriod().values()));
        model.addAttribute("pkgPeriodJson",      toJson(summary.getPackagingCostByPeriod().values()));

        model.addAttribute("campaignLabelsJson", toJson(summary.getToyCostByCampaign().keySet()));
        model.addAttribute("campaignToyJson",    toJson(summary.getToyCostByCampaign().values()));

        model.addAttribute("mktTypeLabelsJson",  toJson(summary.getMarketingCostByType().keySet()));
        model.addAttribute("mktTypeJson",        toJson(summary.getMarketingCostByType().values()));

        return "dashboard/dashboard";
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
