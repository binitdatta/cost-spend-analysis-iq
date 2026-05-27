package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s JOIN FETCH s.country c JOIN FETCH c.region WHERE s.active = true ORDER BY s.name")
    List<Supplier> findAllActiveWithCountry();

    List<Supplier> findByCategoryAndActiveTrue(Supplier.Category category);

    List<Supplier> findAllByOrderByNameAsc();
}
