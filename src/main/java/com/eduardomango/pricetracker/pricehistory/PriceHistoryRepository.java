package com.eduardomango.pricetracker.pricehistory;

import com.eduardomango.pricetracker.pricehistory.domain.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, Long> {
}
