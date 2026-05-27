package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {
    List<CostCenter> findAllByOrderByDepartmentAscNameAsc();
    List<CostCenter> findByDepartmentOrderByName(String department);
}
