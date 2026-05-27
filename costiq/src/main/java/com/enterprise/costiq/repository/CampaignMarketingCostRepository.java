package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.CampaignMarketingCost;
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
public interface CampaignMarketingCostRepository extends JpaRepository<CampaignMarketingCost, Long> {

    @Query("""
        SELECT m FROM CampaignMarketingCost m
        JOIN FETCH m.campaign
        JOIN FETCH m.costCenter
        JOIN FETCH m.fiscalPeriod
        ORDER BY m.entryDate DESC
        """)
    List<CampaignMarketingCost> findAllWithDetails();

    @Query("""
        SELECT m FROM CampaignMarketingCost m
        JOIN FETCH m.campaign
        JOIN FETCH m.costCenter
        JOIN FETCH m.fiscalPeriod
        ORDER BY m.entryDate DESC
        """)
    Page<CampaignMarketingCost> findAllWithDetailsPaged(Pageable pageable);

    @Query("""
        SELECT m FROM CampaignMarketingCost m
        JOIN FETCH m.campaign
        JOIN FETCH m.costCenter
        JOIN FETCH m.fiscalPeriod
        WHERE m.id = :id
        """)
    Optional<CampaignMarketingCost> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(m.amountUsd), 0) FROM CampaignMarketingCost m")
    BigDecimal sumTotalCost();

    @Query("""
        SELECT m.costType, COALESCE(SUM(m.amountUsd), 0)
        FROM CampaignMarketingCost m
        GROUP BY m.costType
        ORDER BY 2 DESC
        """)
    List<Object[]> totalCostByCostType();
}
