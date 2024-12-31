package com.vanguard.vanguardapi.service;

import com.opencsv.CSVWriter;
import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.entity.GameSalesSummary;
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

    private final DataSource dataSource;

    // Optional: If you need the raw credentials for some reason (less common with DataSource)
    /*
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;
    */

    public GameService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final int NUM_ROWS = 1000000;
    private static final Random random = new Random();

    public void importCsv2(MultipartFile file) throws IOException {
        // Create a temporary file as MultipartFile does not have absoulute file path
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit(); // Ensure the temp file is deleted on exit

        // Transfer the MultipartFile contents to the temporary file
        file.transferTo(tempFile);

        String targetTable = GAMES_SALES_TABLE;
        String loadQuery = String.format(
                "LOAD DATA INFILE '%s' INTO TABLE %s " +
                        "FIELDS TERMINATED BY ',' " +
                        "ENCLOSED BY '\"' " +
                        "LINES TERMINATED BY '\\n' " +
                        "IGNORE 1 ROWS;",
                tempFile.getAbsoluteFile(), targetTable
        );

        try (Connection connection = dataSource.getConnection(); // Get connection from DataSource
             Statement statement = connection.createStatement()) {

            // Enable local file loading if necessary
            statement.execute("SET GLOBAL local_infile = 1");

            // Execute the LOAD DATA INFILE query
            int rowsAffected = statement.executeUpdate(loadQuery);
            System.out.printf("Successfully loaded %d rows into %s%n", rowsAffected, targetTable);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            throw new RuntimeException("Error loading data from CSV: " + e.getMessage(), e);
        }
    }

    public void importCsv(MultipartFile file) throws IOException {
        List<GameSales> gameSales = parseCsv(file);
        gameRepository.saveAll(gameSales);
    }

    public List<GameSales> parseCsv(MultipartFile file) throws IOException {
        List<GameSales> gameSalesList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 9){
                    GameSales gameSales = new GameSales();
                    gameSales.setGameNo(Integer.parseInt(nextLine[1]));
                    gameSales.setGameName(nextLine[2]);
                    gameSales.setGameCode(nextLine[3]);
                    gameSales.setType(Integer.parseInt(nextLine[4]));
                    gameSales.setCostPrice(new BigDecimal(nextLine[5]));

                    BigDecimal costPrice = gameSales.getCostPrice();
                    BigDecimal taxRate = new BigDecimal("0.09");
                    BigDecimal tax = costPrice.multiply(taxRate);
                    BigDecimal salePrice = costPrice.add(tax);

                    gameSales.setTax(tax.setScale(2, BigDecimal.ROUND_HALF_UP));
                    gameSales.setSalePrice(salePrice.setScale(2, BigDecimal.ROUND_HALF_UP));
                    gameSales.setDateOfSale(LocalDateTime.parse(nextLine[8]));
                    gameSalesList.add(gameSales);
                } else {
                    throw new CsvValidationException("Invalid CSV format: not enough columns");
                }

            }
        } catch (CsvValidationException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IOException("Error parsing CSV file: " + e.getMessage(), e);
        }
        return gameSalesList;
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