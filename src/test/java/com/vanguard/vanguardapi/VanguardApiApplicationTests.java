package com.vanguard.vanguardapi;

import com.vanguard.vanguardapi.entity.GameSales;
import com.vanguard.vanguardapi.repository.GameRepository;
import com.vanguard.vanguardapi.service.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VanguardApiApplicationTests {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void testImportCsv_Successful() throws IOException {
        // Given
        String csvContent = "id,game_no,game_name,game_code,type,cost_price,tax,sale_price,date_of_sale\n" +
                "1,10,Test Game,TG001,1,50.00,4.50,54.50,2023-11-20 10:00:00\n" +
                "2,25,Another Game,AG002,2,75.00,6.75,81.75,2023-11-21 15:30:00";
        MultipartFile multipartFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        // When
        gameService.importCsv(multipartFile);

        // Then
        verify(gameRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportCsv_InvalidCsvFormat() {
        // Given
        String csvContent = "id,game_no,game_name\n1,10,Test Game"; // Missing columns
        MultipartFile multipartFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        // When & Then
        assertThrows(IOException.class, () -> gameService.importCsv(multipartFile));
        verify(gameRepository, never()).saveAll(anyList());
    }
    @Test
    void testParseCsv_Successful() throws IOException {
        // Given
        String csvContent = "id,game_no,game_name,game_code,type,cost_price,tax,sale_price,date_of_sale\n" +
                "1,10,Test Game,TG001,1,50.00,4.50,54.50,2023-11-20 10:00:00";
        MultipartFile multipartFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        // When
        List<GameSales> gameSalesList = gameService.parseCsv(multipartFile);

        // Then
        assertEquals(1, gameSalesList.size());
        GameSales gameSales = gameSalesList.get(0);
        assertEquals(10, gameSales.getGameNo());
        assertEquals("Test Game", gameSales.getGameName());
        assertEquals("TG001", gameSales.getGameCode());
        assertEquals(1, gameSales.getType());
        assertEquals(new BigDecimal("50.00"), gameSales.getCostPrice());
        assertEquals(new BigDecimal("4.50"), gameSales.getTax());
        assertEquals(new BigDecimal("54.50"), gameSales.getSalePrice());
        assertEquals(LocalDateTime.parse("2023-11-20 10:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), gameSales.getDateOfSale());
    }

}
