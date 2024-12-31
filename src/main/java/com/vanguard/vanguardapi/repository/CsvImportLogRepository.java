package com.vanguard.vanguardapi.repository;

import com.vanguard.vanguardapi.entity.CsvImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsvImportLogRepository extends JpaRepository<CsvImportLog, Long> {
}
