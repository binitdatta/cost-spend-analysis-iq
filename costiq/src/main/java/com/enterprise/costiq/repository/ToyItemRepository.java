package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.ToyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ToyItemRepository extends JpaRepository<ToyItem, Long> {
    @Query("SELECT t FROM ToyItem t JOIN FETCH t.toyCategory WHERE t.active = true ORDER BY t.name")
    List<ToyItem> findAllActiveWithCategory();
}
