package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.PackagingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackagingItemRepository extends JpaRepository<PackagingItem, Long> {
    @Query("SELECT p FROM PackagingItem p JOIN FETCH p.packagingType WHERE p.active = true ORDER BY p.name")
    List<PackagingItem> findAllActiveWithType();
}
