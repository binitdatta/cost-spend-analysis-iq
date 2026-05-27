package com.enterprise.costiq.repository;

import com.enterprise.costiq.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findAllByOrderByStartDateDesc();
    List<Campaign> findByStatusOrderByStartDateDesc(Campaign.Status status);
}
