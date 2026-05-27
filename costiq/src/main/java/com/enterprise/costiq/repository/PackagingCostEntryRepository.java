package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.PackagingCostEntry;
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
public interface PackagingCostEntryRepository extends JpaRepository<PackagingCostEntry, Long> {

    @Query("""
        SELECT e FROM PackagingCostEntry e
        JOIN FETCH e.packagingItem pi
        JOIN FETCH pi.packagingType
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        ORDER BY e.entryDate DESC
        """)
    List<PackagingCostEntry> findAllWithDetails();

    @Query("""
        SELECT e FROM PackagingCostEntry e
        JOIN FETCH e.packagingItem pi
        JOIN FETCH pi.packagingType
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        ORDER BY e.entryDate DESC
        """)
    Page<PackagingCostEntry> findAllWithDetailsPaged(Pageable pageable);

    @Query("""
        SELECT e FROM PackagingCostEntry e
        JOIN FETCH e.packagingItem pi
        JOIN FETCH pi.packagingType
        JOIN FETCH e.supplier
        JOIN FETCH e.costCenter
        JOIN FETCH e.fiscalPeriod
        JOIN FETCH e.country c
        JOIN FETCH c.region
        WHERE e.id = :id
        """)
    Optional<PackagingCostEntry> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(e.quantity * e.unitCostUsd), 0) FROM PackagingCostEntry e")
    BigDecimal sumTotalCost();

    @Query("""
        SELECT c.region.name, COALESCE(SUM(e.quantity * e.unitCostUsd), 0)
        FROM PackagingCostEntry e JOIN e.country c
        GROUP BY c.region.name
        ORDER BY 2 DESC
        """)
    List<Object[]> totalCostByRegion();

    @Query("""
        SELECT fp.periodName, COALESCE(SUM(e.quantity * e.unitCostUsd), 0)
        FROM PackagingCostEntry e JOIN e.fiscalPeriod fp
        GROUP BY fp.fiscalYear, fp.quarter, fp.periodName
        ORDER BY fp.fiscalYear, fp.quarter
        """)
    List<Object[]> totalCostByPeriod();
}
