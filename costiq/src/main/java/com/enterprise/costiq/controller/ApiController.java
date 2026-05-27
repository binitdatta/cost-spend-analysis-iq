package com.enterprise.costiq.controller;

import com.enterprise.costiq.entity.*;
import com.enterprise.costiq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * REST API controller for the CostIQ ChatBot.
 *
 * All queries use JOIN FETCH to eagerly load every LAZY association
 * within a single transaction, preventing LazyInitializationException
 * when Jackson serializes the response outside the Hibernate session.
 *
 * @JsonIgnore on Region.countries is also required — add it to Region.java.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/food-costs")
    public List<FoodCostEntry> foodCosts() {
        return em.createQuery("""
            SELECT e FROM FoodCostEntry e
            JOIN FETCH e.foodItem fi
            JOIN FETCH fi.category
            JOIN FETCH e.supplier s
            JOIN FETCH s.country sc
            JOIN FETCH sc.region
            JOIN FETCH e.costCenter
            JOIN FETCH e.fiscalPeriod
            JOIN FETCH e.country c
            JOIN FETCH c.region
            ORDER BY e.entryDate DESC
            """, FoodCostEntry.class).getResultList();
    }

    @GetMapping("/packaging-costs")
    public List<PackagingCostEntry> packagingCosts() {
        return em.createQuery("""
            SELECT e FROM PackagingCostEntry e
            JOIN FETCH e.packagingItem pi
            JOIN FETCH pi.packagingType
            JOIN FETCH e.supplier s
            JOIN FETCH s.country sc
            JOIN FETCH sc.region
            JOIN FETCH e.costCenter
            JOIN FETCH e.fiscalPeriod
            JOIN FETCH e.country c
            JOIN FETCH c.region
            ORDER BY e.entryDate DESC
            """, PackagingCostEntry.class).getResultList();
    }

    @GetMapping("/toy-allocations")
    public List<CampaignToyAllocation> toyAllocations() {
        return em.createQuery("""
            SELECT a FROM CampaignToyAllocation a
            JOIN FETCH a.campaign
            JOIN FETCH a.toyItem ti
            JOIN FETCH ti.toyCategory
            JOIN FETCH a.supplier s
            JOIN FETCH s.country sc
            JOIN FETCH sc.region
            JOIN FETCH a.fiscalPeriod
            JOIN FETCH a.country c
            JOIN FETCH c.region
            ORDER BY a.entryDate DESC
            """, CampaignToyAllocation.class).getResultList();
    }

    @GetMapping("/marketing-costs")
    public List<CampaignMarketingCost> marketingCosts() {
        return em.createQuery("""
            SELECT m FROM CampaignMarketingCost m
            JOIN FETCH m.campaign
            JOIN FETCH m.costCenter
            JOIN FETCH m.fiscalPeriod
            ORDER BY m.entryDate DESC
            """, CampaignMarketingCost.class).getResultList();
    }

    @GetMapping("/campaigns")
    public List<Campaign> campaigns() {
        return em.createQuery("""
            SELECT c FROM Campaign c
            ORDER BY c.startDate DESC
            """, Campaign.class).getResultList();
    }

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return em.createQuery("""
            SELECT s FROM Supplier s
            JOIN FETCH s.country c
            JOIN FETCH c.region
            ORDER BY s.name
            """, Supplier.class).getResultList();
    }

    @GetMapping("/food-items")
    public List<FoodItem> foodItems() {
        return em.createQuery("""
            SELECT f FROM FoodItem f
            JOIN FETCH f.category
            ORDER BY f.name
            """, FoodItem.class).getResultList();
    }

    @GetMapping("/packaging-items")
    public List<PackagingItem> packagingItems() {
        return em.createQuery("""
            SELECT p FROM PackagingItem p
            JOIN FETCH p.packagingType
            ORDER BY p.name
            """, PackagingItem.class).getResultList();
    }

    @GetMapping("/countries")
    public List<Country> countries() {
        return em.createQuery("""
            SELECT c FROM Country c
            JOIN FETCH c.region
            ORDER BY c.name
            """, Country.class).getResultList();
    }

    @GetMapping("/fiscal-periods")
    public List<FiscalPeriod> fiscalPeriods() {
        return em.createQuery("""
            SELECT fp FROM FiscalPeriod fp
            ORDER BY fp.fiscalYear DESC, fp.quarter DESC
            """, FiscalPeriod.class).getResultList();
    }

    @GetMapping("/cost-centers")
    public List<CostCenter> costCenters() {
        return em.createQuery("""
            SELECT cc FROM CostCenter cc
            ORDER BY cc.department, cc.name
            """, CostCenter.class).getResultList();
    }
}