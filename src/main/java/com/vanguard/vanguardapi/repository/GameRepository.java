package com.vanguard.vanguardapi.repository;

import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.entity.GameSalesSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameSales, Long> {

    @Query("SELECT gs FROM GameSales gs WHERE " +
            "(:fromDate IS NULL OR :toDate IS NULL OR gs.dateOfSale BETWEEN :fromDate AND :toDate) AND " +
            "(:salePriceCondition IS NULL OR " +
            "(:salePriceCondition = 'LESS_THAN' AND gs.salePrice < :salePrice) OR " +
            "(:salePriceCondition = 'GREATER_THAN' AND gs.salePrice > :salePrice))")
    Page<GameSales> findGameSales(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("salePriceCondition") String salePriceCondition,
            @Param("salePrice") BigDecimal salePrice,
            Pageable pageable);

    @Query("SELECT gss FROM GameSalesSummary gss WHERE " +
            "(:fromDate IS NULL OR gss.dateOfSale >= :fromDate) AND " +
            "(:toDate IS NULL OR gss.dateOfSale <= :toDate) AND " +
            "(:gameNo IS NULL OR gss.gameNo = :gameNo)" +
            "ORDER BY gss.dateOfSale ASC")
    List<GameSalesSummary> findGameSalesSummary(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("gameNo") Integer gameNo);
}