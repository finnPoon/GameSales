package com.vanguard.vanguardapi.service;

import com.vanguard.vanguardapi.entity.GameSalesAggregated;
import com.vanguard.vanguardapi.repository.DataAggregationRepository;
import com.vanguard.vanguardapi.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DataAggregationService {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DataAggregationRepository dataAggregationRepository;

    // Schedule the task to run daily at 12:00 AM
    @Scheduled(cron = "0 0 0 * * ?") // Cron expression for daily at midnight
    @Transactional
    public void aggregateDailySales() {
        LocalDate today = LocalDate.now();

        // Aggregate data from the game_sales table
        List<Object[]> aggregatedData = gameRepository.findDailyAggregatedSales(today);

        for (Object[] row : aggregatedData) {
            LocalDate dateOfSale = (LocalDate) row[0];
            Integer gameNo = (Integer) row[1];
            Long totalGamesSold = (Long) row[2];
            BigDecimal totalSales = (BigDecimal) row[3];

            // Save the aggregated data to the game_sales_aggregated table
            GameSalesAggregated aggregatedSale = new GameSalesAggregated();
            aggregatedSale.setDateOfSale(dateOfSale);
            aggregatedSale.setGameNo(gameNo);
            aggregatedSale.setTotalGamesSold(totalGamesSold.intValue());
            aggregatedSale.setTotalSales(totalSales);

            dataAggregationRepository.save(aggregatedSale);
        }
    }
}
