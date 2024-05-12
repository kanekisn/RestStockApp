package org.reststockapp.controller;

import lombok.RequiredArgsConstructor;
import org.reststockapp.dtos.RequestedStockDto;
import org.reststockapp.model.StockData;
import org.reststockapp.model.User;
import org.reststockapp.repository.StockDataRepository;
import org.reststockapp.service.StockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockDataController {
    private final StockDataService stockDataService;
    private final StockDataRepository stockDataRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveStockData(@RequestBody RequestedStockDto requestedStockDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        stockDataService.processAndSaveStockData(requestedStockDto.getTicker(), requestedStockDto.getStart(), requestedStockDto.getEnd(), user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedStockData(@RequestParam String ticker) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<StockData> data = stockDataRepository.findByUserAndTicker(user, ticker);
        return ResponseEntity.ok(data);
    }
}