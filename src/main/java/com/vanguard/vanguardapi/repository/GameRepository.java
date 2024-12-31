package com.vanguard.vanguardapi.repository;

import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.entity.GameSalesAggregated;
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
            "(:fromDate IS NULL OR gs.dateOfSale >= :fromDate) AND " +
            "(:toDate IS NULL OR gs.dateOfSale <= :toDate) AND " +
            "(:salePriceCondition IS NULL OR " +
            "(:salePriceCondition = 'LESS_THAN' AND gs.salePrice < :salePrice) OR " +
            "(:salePriceCondition = 'GREATER_THAN' AND gs.salePrice > :salePrice))")
    Page<GameSales> findGameSales(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("salePriceCondition") String salePriceCondition,
            @Param("salePrice") BigDecimal salePrice,
            Pageable pageable);

    @Query("SELECT gsa FROM GameSalesAggregated gsa WHERE " +
            "(:fromDate IS NULL OR gsa.dateOfSale >= :fromDate) AND " +
            "(:toDate IS NULL OR gsa.dateOfSale <= :toDate) AND " +
            "(:gameNo IS NULL OR gsa.gameNo = :gameNo)")
    List<GameSalesAggregated> findAggregatedSales(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("gameNo") Integer gameNo);

    @Query("SELECT " +
            "DATE(gs.dateOfSale) AS dateOfSale, " +
            "gs.gameNo AS gameNo, " +
            "COUNT(gs) AS totalGamesSold, " +
            "SUM(gs.salePrice) AS totalSales " +
            "FROM GameSales gs " +
            "WHERE DATE(gs.dateOfSale) = :today " +
            "GROUP BY DATE(gs.dateOfSale), gs.gameNo")
    List<Object[]> findDailyAggregatedSales(@Param("today") LocalDate today);
}