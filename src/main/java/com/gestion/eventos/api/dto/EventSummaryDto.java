package com.gestion.eventos.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EventSummaryDto {
    private Long id;
    private String name;
    private LocalDate date;
    private String location;
}
