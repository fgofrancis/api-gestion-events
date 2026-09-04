package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Event;
import com.gestion.eventos.api.dto.EventRequestDto;
import com.gestion.eventos.api.dto.EventResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface IEventService {
    Page<EventResponseDto> findAll(String name, String location, LocalDate date,
                                   LocalDate dateFrom, LocalDate dateTo,Pageable pageable);
    Event save(EventRequestDto event);
    Event findById(Long id);
    Event update(Long id, EventRequestDto requestDto);
    void deleteById(Long id);
    List<Event> getAllEventsAndTheirDetailsProblematic();
    List<Event> getAllEventsAndTheirDetailsOptimizedWithJoinFetch();
    List<Event> findAllEventsWithAllDetailsOptimized();
}
