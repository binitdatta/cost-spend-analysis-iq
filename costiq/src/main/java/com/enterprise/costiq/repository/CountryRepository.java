package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c JOIN FETCH c.region ORDER BY c.name")
    List<Country> findAllWithRegion();

    List<Country> findByRegionIdOrderByName(Long regionId);
}
