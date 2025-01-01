package com.vanguard.vanguardapi.service;

import com.opencsv.CSVWriter;
import com.vanguard.vanguardapi.entity.CsvImportLog;
import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.entity.GameSalesSummary;
import com.vanguard.vanguardapi.repository.CsvImportLogRepository;
import com.vanguard.vanguardapi.repository.GameRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.vanguard.vanguardapi.config.Contants.*;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CsvImportLogRepository csvImportLogRepository;

    private final DataSource dataSource;

    public GameService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void importCsv(MultipartFile file) throws IOException {

        String targetTable = "game_sales";
        String loadQuery = String.format(
                "LOAD DATA LOCAL INFILE '%s' INTO TABLE %s " +
                        "FIELDS TERMINATED BY ',' " +
                        "ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\n' " +
                        "IGNORE 1 ROWS;",
                IMPORT_FILE_PATH + file.getOriginalFilename(), targetTable
        );

        // Keep track of the import status
        CsvImportLog importLog = new CsvImportLog();
        importLog.setFileName(file.getOriginalFilename());
        importLog.setStartTime(LocalDateTime.now());
        importLog.setStatus(CsvImportLog.ImportStatus.IN_PROGRESS);
        importLog.setCreatedBy("system");
        importLog.setCreatedAt(LocalDateTime.now());

        // Save the log entry before starting the import
        csvImportLogRepository.save(importLog);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Enable local file loading if necessary
            statement.execute("SET GLOBAL local_infile = 1");

            // Execute the LOAD DATA INFILE query
            int rowsAffected = statement.executeUpdate(loadQuery);

            // Update the log entry with the result
            importLog.setEndTime(LocalDateTime.now());
            importLog.setStatus(CsvImportLog.ImportStatus.COMPLETED);
            importLog.setTotalRows(rowsAffected);
            importLog.setImportedRows(rowsAffected);
            importLog.setUpdatedBy("system");
            importLog.setUpdatedAt(LocalDateTime.now());

            csvImportLogRepository.save(importLog);

            System.out.printf("Successfully loaded %d rows into %s%n", rowsAffected, targetTable);

        } catch (SQLException e) {
            // Update the log entry with the error
            importLog.setEndTime(LocalDateTime.now());
            importLog.setStatus(CsvImportLog.ImportStatus.FAILED);
            importLog.setErrorMessage(e.getMessage());
            importLog.setUpdatedBy("system");
            importLog.setUpdatedAt(LocalDateTime.now());

            csvImportLogRepository.save(importLog);

            System.err.println("Database error: " + e.getMessage());
            throw new RuntimeException("Error loading data from CSV: " + e.getMessage(), e);
        }
    }

    public Page<GameSales> getGameSales(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String salePriceCondition,
            BigDecimal salePrice,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        return gameRepository.findGameSales(fromDate, toDate, salePriceCondition, salePrice, pageable);
    }

    public List<GameSalesSummary> getTotalSales(
            LocalDate fromDate,
            LocalDate toDate,
            Integer gameNo) {

        return gameRepository.findGameSalesSummary(fromDate, toDate, gameNo);
    }

    private static final int NUM_ROWS = 1000000;
    private static final Random random = new Random();

    // For generating random CSV file for testing
    public String generateCsv() throws IOException {
        String fileName = GAMES_SALES_FILE_NAME;
        List<GameSales> gameSales = generateRandomGames(NUM_ROWS);

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            // Write header
            writer.writeNext(new String[]{"id", "game_no", "game_name", "game_code", "type", "cost_price", "tax", "sale_price", "date_of_sale"});

            // Write data
            for (GameSales game : gameSales) {
                writer.writeNext(new String[]{
                        game.getId().toString(),
                        game.getGameNo().toString(),
                        game.getGameName(),
                        game.getGameCode(),
                        game.getType().toString(),
                        game.getCostPrice().toString(),
                        game.getTax().toString(),
                        game.getSalePrice().toString(),
                        game.getDateOfSale().toString()
                });
            }
        }

        return fileName;
    }

    private List<GameSales> generateRandomGames(int numRows) {
        List<GameSales> games = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 4, 30, 23, 59);

        for (long id = 1; id <= numRows; id++) {
            GameSales gameSales = new GameSales();
            gameSales.setId(id);
            gameSales.setGameNo(random.nextInt(100) + 1);
            gameSales.setGameName("Game " + (random.nextInt(1000) + 1));
            gameSales.setGameCode("G" + (random.nextInt(1000) + 1));
            gameSales.setType(random.nextInt(2) + 1);
            gameSales.setCostPrice(BigDecimal.valueOf(random.nextDouble() * 100).setScale(2, RoundingMode.HALF_UP));
            gameSales.setTax(gameSales.getCostPrice().multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP));
            gameSales.setSalePrice(gameSales.getCostPrice().add(gameSales.getTax()).setScale(2, RoundingMode.HALF_UP));
            gameSales.setDateOfSale(generateRandomDate(startDate, endDate));
            games.add(gameSales);
        }

        return games;
    }

    private LocalDateTime generateRandomDate(LocalDateTime startDate, LocalDateTime endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return startDate.plusDays(random.nextInt((int) daysBetween + 1))
                .plusHours(random.nextInt(24))
                .plusMinutes(random.nextInt(60))
                .plusSeconds(random.nextInt(60));
    }
}