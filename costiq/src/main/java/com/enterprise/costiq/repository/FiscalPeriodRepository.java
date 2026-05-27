package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    List<FiscalPeriod> findAllByOrderByFiscalYearDescQuarterDesc();
    List<FiscalPeriod> findByFiscalYearOrderByQuarter(int year);
}
