package com.vanguard.vanguardapi.controller;

import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.entity.GameSalesSummary;
import com.vanguard.vanguardapi.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a CSV file.");
        }

        try {
            gameService.importCsv(file);
            return ResponseEntity.ok("CSV file imported successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/getGameSales")
    public Page<GameSales> getGameSales(
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(required = false) String salePriceCondition,
            @RequestParam(required = false) BigDecimal salePrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        return gameService.getGameSales(fromDate, toDate, salePriceCondition, salePrice, page, size);
    }

    @GetMapping("/getTotalSales")
    public List<GameSalesSummary> getTotalSales(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Integer gameNo) {

        return gameService.getTotalSales(fromDate, toDate, gameNo);
    }

    //I use this to generate CSV file for testing
    @GetMapping("/generateCSV")
    public String generateCsv() throws IOException {
        String fileName = gameService.generateCsv();
        return "CSV file generated successfully: " + fileName;
    }
}