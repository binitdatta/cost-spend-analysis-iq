package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.CampaignToyAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignToyAllocationRepository extends JpaRepository<CampaignToyAllocation, Long> {

    @Query("""
        SELECT a FROM CampaignToyAllocation a
        JOIN FETCH a.campaign
        JOIN FETCH a.toyItem ti
        JOIN FETCH ti.toyCategory
        JOIN FETCH a.country c
        JOIN FETCH c.region
        JOIN FETCH a.supplier
        JOIN FETCH a.fiscalPeriod
        ORDER BY a.entryDate DESC
        """)
    List<CampaignToyAllocation> findAllWithDetails();

    @Query("""
        SELECT a FROM CampaignToyAllocation a
        JOIN FETCH a.campaign
        JOIN FETCH a.toyItem ti
        JOIN FETCH ti.toyCategory
        JOIN FETCH a.country c
        JOIN FETCH c.region
        JOIN FETCH a.supplier
        JOIN FETCH a.fiscalPeriod
        ORDER BY a.entryDate DESC
        """)
    Page<CampaignToyAllocation> findAllWithDetailsPaged(Pageable pageable);

    @Query("""
        SELECT a FROM CampaignToyAllocation a
        JOIN FETCH a.campaign
        JOIN FETCH a.toyItem ti
        JOIN FETCH ti.toyCategory
        JOIN FETCH a.country c
        JOIN FETCH c.region
        JOIN FETCH a.supplier
        JOIN FETCH a.fiscalPeriod
        WHERE a.id = :id
        """)
    Optional<CampaignToyAllocation> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT a FROM CampaignToyAllocation a
        JOIN FETCH a.campaign
        JOIN FETCH a.toyItem ti
        JOIN FETCH ti.toyCategory
        JOIN FETCH a.country c
        JOIN FETCH c.region
        JOIN FETCH a.supplier
        JOIN FETCH a.fiscalPeriod
        WHERE a.campaign.id = :campaignId
        ORDER BY a.entryDate DESC
        """)
    List<CampaignToyAllocation> findByCampaignWithDetails(@Param("campaignId") Long campaignId);

    @Query("SELECT COALESCE(SUM(a.quantity * a.unitCostUsd), 0) FROM CampaignToyAllocation a")
    BigDecimal sumTotalCost();

    @Query("""
        SELECT c.region.name, COALESCE(SUM(a.quantity * a.unitCostUsd), 0)
        FROM CampaignToyAllocation a JOIN a.country c
        GROUP BY c.region.name
        ORDER BY 2 DESC
        """)
    List<Object[]> totalCostByRegion();

    @Query("""
        SELECT camp.name, COALESCE(SUM(a.quantity * a.unitCostUsd), 0)
        FROM CampaignToyAllocation a JOIN a.campaign camp
        GROUP BY camp.id, camp.name
        ORDER BY 2 DESC
        """)
    List<Object[]> totalCostByCampaign();
}
