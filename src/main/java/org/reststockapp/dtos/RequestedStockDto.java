package org.reststockapp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RequestedStockDto {
    private String ticker;
    private LocalDate start;
    private LocalDate end;
}