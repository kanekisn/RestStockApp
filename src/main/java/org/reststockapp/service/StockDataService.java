package org.reststockapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.reststockapp.exceptions.InvalidDateEntriesException;
import org.reststockapp.exceptions.UnknownTickerException;
import org.reststockapp.model.StockData;
import org.reststockapp.model.User;
import org.reststockapp.repository.StockDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockDataService {
    private final WebClient webClient;
    private final StockDataRepository stockDataRepository;
    private final ObjectMapper objectMapper;
    @Value("${polygon.api.key}")
    private String KEY;

    @Transactional
    public void processAndSaveStockData(String ticker, LocalDate start, LocalDate end, User user) {
        if (start.isAfter(end)) {
            throw new InvalidDateEntriesException("Start date must be before end date.");
        }

        fetchStockData(ticker, start, end)
                .flatMap(json -> parseJsonToStockData(json, ticker, user))
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new UnknownTickerException("No stock data found for ticker: " + ticker));
                    }
                    return checkForDuplicatesAndReturnNonDuplicates(list, user);
                })
                .doOnSuccess(this::saveStocks)
                .subscribe(
                        data -> System.out.println("Data processed successfully"),
                        error -> System.out.println("Error during data processing: " + error.getMessage()),
                        () -> System.out.println("Processing complete.")
                );
    }

    private Flux<String> fetchStockData(String ticker, LocalDate start, LocalDate end) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/aggs/ticker/{ticker}/range/1/day/{start}/{end}")
                        .queryParam("apiKey", KEY)
                        .build(ticker, start.toString(), end.toString()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new UnknownTickerException("Invalid ticker or request parameters: " + ticker)))
                .bodyToFlux(String.class);
    }

    private Flux<StockData> parseJsonToStockData(String json, String ticker, User user) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("results")) {
                return Flux.error(new UnknownTickerException("No results found in JSON for ticker: " + ticker));
            }

            List<StockData> stockDataList = new ArrayList<>();
            root.path("results").forEach(result -> {
                StockData stockData = new StockData();
                stockData.setUser(user);
                stockData.setTicker(ticker);
                stockData.setDate(Instant.ofEpochMilli(result.path("t").asLong()).atZone(ZoneId.systemDefault()).toLocalDate());
                stockData.setOpen(result.path("o").asDouble());
                stockData.setClose(result.path("c").asDouble());
                stockData.setHigh(result.path("h").asDouble());
                stockData.setLow(result.path("l").asDouble());
                stockDataList.add(stockData);
            });
            return Flux.fromIterable(stockDataList);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Error parsing JSON", e));
        }
    }

    private Mono<List<StockData>> checkForDuplicatesAndReturnNonDuplicates(List<StockData> stockDataList, User user) {
        Set<LocalDate> dates = stockDataList.stream().map(StockData::getDate).collect(Collectors.toSet());
        String ticker = stockDataList.isEmpty() ? "" : stockDataList.get(0).getTicker();
        return Mono.fromCallable(() -> stockDataRepository.findExistingDatesByUserAndTicker(user, ticker, dates))
                .subscribeOn(Schedulers.boundedElastic())
                .map(existingDates -> {
                    Set<LocalDate> existingDateSet = new HashSet<>(existingDates);
                    return stockDataList.stream()
                            .filter(stock -> !existingDateSet.contains(stock.getDate()))
                            .collect(Collectors.toList());
                });
    }

    private void saveStocks(List<StockData> stocks) {
        stockDataRepository.saveAll(stocks);
    }
}