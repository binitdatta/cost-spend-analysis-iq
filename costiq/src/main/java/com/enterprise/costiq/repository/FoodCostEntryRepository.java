package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.FoodCostEntry;
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
public interface FoodCostEntryRepository extends JpaRepository<FoodCostEntry, Long> {

    @Query("""
        SELECT e FROM FoodCostEntry e
        JOIN FETCH e.foodItem fi
        JOIN FETCH fi.category
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        ORDER BY e.entryDate DESC
        """)
    List<FoodCostEntry> findAllWithDetails();

    @Query("""
        SELECT e FROM FoodCostEntry e
        JOIN FETCH e.foodItem fi
        JOIN FETCH fi.category
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        ORDER BY e.entryDate DESC
        """)
    Page<FoodCostEntry> findAllWithDetailsPaged(Pageable pageable);

    @Query("""
        SELECT e FROM FoodCostEntry e
        JOIN FETCH e.foodItem fi
        JOIN FETCH fi.category
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        WHERE e.id = :id
        """)
    Optional<FoodCostEntry> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT e FROM FoodCostEntry e
        JOIN FETCH e.foodItem fi
        JOIN FETCH fi.category
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        WHERE e.fiscalPeriod.id = :periodId
        ORDER BY e.entryDate DESC
        """)
    List<FoodCostEntry> findByFiscalPeriodWithDetails(@Param("periodId") Long periodId);

    @Query("""
        SELECT e FROM FoodCostEntry e
        JOIN FETCH e.foodItem fi
        JOIN FETCH fi.category
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        WHERE c.region.id = :regionId
        ORDER BY e.entryDate DESC
        """)
    List<FoodCostEntry> findByRegionWithDetails(@Param("regionId") Long regionId);

    @Query("SELECT COALESCE(SUM(e.quantity * e.unitCostUsd), 0) FROM FoodCostEntry e")
    BigDecimal sumTotalCost();

    @Query("SELECT COALESCE(SUM(e.quantity * e.unitCostUsd), 0) FROM FoodCostEntry e WHERE e.fiscalPeriod.id = :periodId")
    BigDecimal sumTotalCostByPeriod(@Param("periodId") Long periodId);

    @Query("""
        SELECT c.region.name, COALESCE(SUM(e.quantity * e.unitCostUsd), 0)
        FROM FoodCostEntry e JOIN e.country c
        GROUP BY c.region.name
        ORDER BY 2 DESC
        """)
    List<Object[]> totalCostByRegion();

    @Query("""
        SELECT fp.periodName, COALESCE(SUM(e.quantity * e.unitCostUsd), 0)
        FROM FoodCostEntry e JOIN e.fiscalPeriod fp
        GROUP BY fp.fiscalYear, fp.quarter, fp.periodName
        ORDER BY fp.fiscalYear, fp.quarter
        """)
    List<Object[]> totalCostByPeriod();
}
