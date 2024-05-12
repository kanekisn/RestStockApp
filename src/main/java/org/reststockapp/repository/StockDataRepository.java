package org.reststockapp.repository;

import org.reststockapp.model.StockData;
import org.reststockapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findByUserAndTicker(User user, String ticker);
    @Query("SELECT sd.date FROM StockData sd WHERE sd.user = :user AND sd.ticker = :ticker AND sd.date IN :dates")
    List<LocalDate> findExistingDatesByUserAndTicker(@Param("user") User user, @Param("ticker") String ticker, @Param("dates") Set<LocalDate> dates);
}