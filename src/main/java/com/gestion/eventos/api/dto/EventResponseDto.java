package com.gestion.eventos.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class EventResponseDto {
    private Long id;
    private String name;
    private LocalDate date;
    private String location;

    // Cambie el campo tipo Category por este. Por motivo de simplisidad, si Category crece aqui solo necesito id y name
    private Long categoryId;
    private String categoryName;

    private Set<SpeakerResponseDto> speakerDtos;
}
