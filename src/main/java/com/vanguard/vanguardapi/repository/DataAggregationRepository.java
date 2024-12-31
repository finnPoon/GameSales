package com.vanguard.vanguardapi.repository;

import com.vanguard.vanguardapi.entity.GameSalesAggregated;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DataAggregationRepository extends JpaRepository<GameSalesAggregated, Long> {
    Optional<GameSalesAggregated> findByDateOfSaleAndGameNo(LocalDate dateOfSale, Integer gameNo);
}
