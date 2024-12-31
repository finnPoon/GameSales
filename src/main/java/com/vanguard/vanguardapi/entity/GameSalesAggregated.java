package com.vanguard.vanguardapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "game_sales_aggregated")
public class GameSalesAggregated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_sale", nullable = false)
    private LocalDate dateOfSale;

    @Column(name = "game_no", nullable = false)
    private Integer gameNo;

    @Column(name = "total_games_sold", nullable = false)
    private Integer totalGamesSold;

    @Column(name = "total_sales", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSales;
}