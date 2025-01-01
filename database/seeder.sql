-- Task 1
CREATE TABLE game_sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_no INT NOT NULL,
    game_name VARCHAR(20) NOT NULL,
    game_code VARCHAR(5) NOT NULL,
    type INT NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    date_of_sale TIMESTAMP NOT NULL
);

CREATE INDEX idx_date_of_sale ON game_sales (date_of_sale);
CREATE INDEX idx_sale_price ON game_sales (sale_price);

-- Task 2
CREATE TABLE csv_import_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    total_rows INT DEFAULT 0,
    imported_rows INT DEFAULT 0,
    error_message TEXT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Task 4 (suppose to use materialised view for faster performance but MYSQL does not support this type of view.)
CREATE TABLE game_sales_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_of_sale DATE NOT NULL,
    game_no INT,
    total_games_sold INT NOT NULL,
    total_sales DECIMAL(10, 2) NOT NULL,
    UNIQUE KEY unique_summary  (date_of_sale, game_no)
);

-- Task 4 (Stored Procedure to refresh the game_sales_summary table after every insertion done for the game_sales table)

DELIMITER //
CREATE PROCEDURE RefreshGameSalesSummary()
BEGIN
    -- Clear the existing data in the materialized view table
    DELETE FROM game_sales_summary;

    -- Insert the summarized data for all games
    INSERT INTO game_sales_summary (date_of_sale, game_no, total_games_sold, total_sales)
    SELECT
        DATE(date_of_sale) AS date_of_sale,
        NULL AS game_no,
        COUNT(*) AS total_games_sold,
        SUM(sale_price) AS total_sales
    FROM game_sales
    GROUP BY DATE(date_of_sale);

    -- Insert the summarized data for each game_no
    INSERT INTO game_sales_summary (date_of_sale, game_no, total_games_sold, total_sales)
    SELECT
        DATE(date_of_sale) AS date_of_sale,
        game_no,
        COUNT(*) AS total_games_sold,
        SUM(sale_price) AS total_sales
    FROM game_sales
    GROUP BY DATE(date_of_sale), game_no;
END //
DELIMITER ;

-- Task 4 (Create event scheduler to populate pre-aggregated table to run every 5 minute for testing purpose)
DELIMITER //
CREATE EVENT daily_task_event
ON SCHEDULE EVERY 5 MINUTE
STARTS '2024-01-01 00:00:00' -- Set the start time for the first execution
DO
BEGIN
    CALL RefreshGameSalesSummary();
END//
DELIMITER ;