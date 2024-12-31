package com.vanguard.vanguardapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class GameSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer gameNo;
    private String gameName;
    private String gameCode;
    private Integer type;
    private BigDecimal costPrice;
    private BigDecimal tax;
    private BigDecimal salePrice;
    private LocalDateTime dateOfSale;
}