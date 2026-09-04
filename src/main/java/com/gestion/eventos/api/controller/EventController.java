package com.gestion.eventos.api.controller;


import com.gestion.eventos.api.domain.Event;
import com.gestion.eventos.api.dto.EventRequestDto;
import com.gestion.eventos.api.dto.EventResponseDto;
import com.gestion.eventos.api.mapper.EventMapper;
import com.gestion.eventos.api.service.IEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name ="Eventos", description = "Operaciones relacionadas con al gestión de eventos")
public class EventController {

    private final IEventService eventService;

    // Vamos a usar logger para gravar log
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventMapper eventMapper;

    @GetMapping("/problematic")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Event>> getAllEventsProblematic(){
        List<Event> events = eventService.getAllEventsAndTheirDetailsProblematic();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/optimized-join-fetch")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Event>> getAllEventsOptimizedWithJoinFetch(){
        List<Event> events = eventService.getAllEventsAndTheirDetailsOptimizedWithJoinFetch();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/optimized/all-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Event>> getAllEventsWithAllDetails(){
        List<Event> events = eventService.findAllEventsWithAllDetailsOptimized();
        return ResponseEntity.ok(events);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(page = 0, size = 10, sort = "name")Pageable pageable
            ){

        logger.info("Recibida solicitud GET /events con nombre '{}' y paginación {}.", name, pageable);
        Page<EventResponseDto> events = eventService.findAll(name, location, date, dateFrom,
                                                        dateTo, pageable);

        logger.debug("Devolviendo {} eventos paginados.", events.getTotalElements());
        return ResponseEntity.ok(events);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto requestDto){
        logger.info("Recibida solicitud para crear evento: {}", requestDto.getName());

        Event eventSaved = eventService.save(requestDto);
        EventResponseDto responseDto = eventMapper.toResponseDto(eventSaved);

        logger.debug("Evento creado exitosamente...{}", eventSaved.getName());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    // Estos Swagger. Esto se conecta con el EventRequestDto
    @Operation(summary = "Obtener un evento por su ID", description = "Devuelve los detalles de un Evento especifico por su ID")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Evento encontrado exitosamente"),
                    @ApiResponse(responseCode = "204", description = "Evento no encontrado")
            }
    )
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id){
        Event event = eventService.findById(id);
        EventResponseDto responseDto = eventMapper.toResponseDto(event);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<EventResponseDto> updateEvent( @PathVariable Long id,
                                                        @Valid @RequestBody EventRequestDto requestDto
    ){
        Event updateEvent = eventService.update(id, requestDto);
        return ResponseEntity.ok(eventMapper.toResponseDto(updateEvent));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }










}
