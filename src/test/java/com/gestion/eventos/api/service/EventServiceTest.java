package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Category;
import com.gestion.eventos.api.domain.Event;
import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.dto.EventRequestDto;
import com.gestion.eventos.api.dto.EventResponseDto;
import com.gestion.eventos.api.exception.ResourceNotFoundException;
import com.gestion.eventos.api.mapper.EventMapper;
import com.gestion.eventos.api.repository.EventRepository;
import com.gestion.eventos.api.util.EventSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private  EventRepository eventRepository;
    @Mock
    private  EventMapper eventMapper;
    @Mock
    private  ICategoryService categoryService;
    @Mock
    private  ISpeakerService speakerService;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private EventRequestDto eventRequestDto;
    private EventResponseDto eventResponseDto;
    private Category category;
    private Speaker speaker1;
    private Speaker speaker2;
    private Pageable pageable;

    @BeforeEach
    void setUp(){
        // Inicializar datos de prueba
        category = new Category(1L,"Conferencia","Descripcion de Conferencia" );
        speaker1 = new Speaker(10L,"John Doe","john@example.com","Bio de John",new HashSet<>());
        speaker2 = new Speaker(11L,"Jane Smith","jane@example.com","Bio de Jane",new HashSet<>());

        event = new Event();
        event.setId(1L);
        event.setName("Spring Boot Conf.");
        event.setDate(LocalDate.of(2025,03,29));
        event.setLocation("Online");
        event.setCategory(category);
        event.getSpeakers().add(speaker1); //Here estoy buscacando el set<Speaker> y agregando speaker1
        event.getSpeakers().add(speaker2);

        eventRequestDto = new EventRequestDto();
        eventRequestDto.setName("Spring Boot Conf.");
        eventRequestDto.setDate(LocalDate.of(2025,03,29));
        eventRequestDto.setLocation("Online");
        eventRequestDto.setCategoryId(1L);
        eventRequestDto.setSpeakersId(new HashSet<>(Set.of(10L,11L)));

        eventResponseDto = new EventResponseDto();
        eventResponseDto.setId(1L);
        eventResponseDto.setName("Spring Boot Conf.");
        eventResponseDto.setDate(LocalDate.of(2025,03,29));
        eventResponseDto.setLocation("Online");
        eventResponseDto.setCategoryId(1L);
        eventResponseDto.setCategoryName("Conferencia");

        pageable = PageRequest.of(0,10);

    }

    @Test
    @DisplayName("Debe retornar un Evento cuando el ID existe")
    void shouldRetornEventWhenIdExists(){
        //arrange- preparar
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        // Ejecutar
        Event foundEvent = eventService.findById(1L);

        // Assert - afirmar/ verificar
        assertNotNull(foundEvent);
        assertEquals(event.getId(), foundEvent.getId());
        verify(eventRepository,times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no exista")
    void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Here assertThrows espera que eventService lance la exception: ResourceNotFoundException
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class, ()->{
                 eventService.findById(99L); // verificacion y ejecucion combinadas
                });

        assertEquals("Evento no encontrado con id: 99", thrown.getMessage());
        verify(eventRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe guardar un evento exitosamente con categoria y oradores")
    void shouldSaveEventSuccessfullyWithCategoryAndSpeakers(){

        Event eventWithoutId = new Event();
        eventWithoutId.setName(eventRequestDto.getName());
        eventWithoutId.setDate(eventRequestDto.getDate());
        eventWithoutId.setLocation(eventRequestDto.getLocation());

        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(eventWithoutId);

        when(categoryService.findById(eventRequestDto.getCategoryId())).thenReturn(category);

        when(speakerService.findById(10L)).thenReturn(speaker1);
        when(speakerService.findById(11L)).thenReturn(speaker2);

        when(eventRepository.save(any(Event.class))).thenAnswer(
                invocation ->{
                    Event savedEvent = invocation.getArgument(0);
                    savedEvent.setId(1L);
                    return savedEvent;
                });

        // Ejecucion
        Event savedEvent = eventService.save(eventRequestDto);

        //verificacion
        assertNotNull(savedEvent);
        assertEquals(1L,savedEvent.getId());
        assertEquals(eventRequestDto.getName(),savedEvent.getName());
        assertEquals(category,savedEvent.getCategory());
        assertEquals(2,savedEvent.getSpeakers().size());

        assertTrue(savedEvent.getSpeakers().contains(speaker1));
        assertTrue(savedEvent.getSpeakers().contains(speaker2));

        verify(eventMapper, times(1)).toEntity(eventRequestDto);
        verify(categoryService, times(1)).findById(eventRequestDto.getCategoryId());
        verify(speakerService, times(1)).findById(10L);
        verify(speakerService, times(1)).findById(11L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe guardar un Evento Exitosamente pero sin operadores")
    void shouldSaveSuccessfullyWithoutSpeakers(){
        eventRequestDto.setSpeakersId(null);

        Event eventWithoutId = new Event();
        eventWithoutId.setName(eventRequestDto.getName());
        eventWithoutId.setDate(eventRequestDto.getDate());
        eventWithoutId.setLocation(eventRequestDto.getLocation());

        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(eventWithoutId);

        when(categoryService.findById(eventRequestDto.getCategoryId())).thenReturn(category);

        when(eventRepository.save(any(Event.class))).thenAnswer(
                invocation ->{
                    Event savedEvent = invocation.getArgument(0);
                    savedEvent.setId(1L);
                    return savedEvent;
                });

        // Ejecucion
        Event savedEvent = eventService.save(eventRequestDto);

        //verificacion
        assertNotNull(savedEvent);
        assertEquals(1L,savedEvent.getId());
        assertEquals(eventRequestDto.getName(),savedEvent.getName());
        assertEquals(category,savedEvent.getCategory());
        assertTrue(savedEvent.getSpeakers().isEmpty());// esta es la fundamental

        verify(eventMapper, times(1)).toEntity(eventRequestDto);
        verify(categoryService,times(1)).findById(eventRequestDto.getCategoryId());
        verify(speakerService, never()).findById(anyLong()); // esta es la fundamental
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la categoria no existe al guardar")
    void shouldThrowResourceNotFoundExceptionWhenCategoryNotFoundOnSave(){
        Event eventWithoutId = new Event();

        when(eventMapper.toEntity(any(EventRequestDto.class))).thenReturn(eventWithoutId);

        // Esto es una trampa que le ponemos la codigo
        when(categoryService.findById(anyLong())).thenThrow(
                new ResourceNotFoundException("Categoría no se encuentra con ese id: "
                        + eventRequestDto.getCategoryId())
        );

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, ()->{
            eventService.save(eventRequestDto);
        });

        assertEquals("Categoría no se encuentra con ese id: " + eventRequestDto.getSpeakersId(),
                thrown.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    // Este test lo he hecho con ayudo de Copilot
    @Test
    @DisplayName("Debe retornar una página de eventos filtrados y mapeados correctamente")
    void shouldReturnPagedEventsWithFilters() {
        // Arrange - preparar datos
        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("Spring Boot Conf.");
        event1.setDate(LocalDate.of(2025, 3, 29));
        event1.setLocation("Online");
        event1.setCategory(category);

        EventResponseDto dto1 = new EventResponseDto();
        dto1.setId(1L);
        dto1.setName("Spring Boot Conf.");
        dto1.setLocation("Online");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventsPage = new PageImpl<>(List.of(event1), pageable, 1);

        // Mockear repositorio y mapper
        when(eventRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(eventsPage);
        when(eventMapper.toResponseDto(event1)).thenReturn(dto1);

        // Act - ejecutar
        Page<EventResponseDto> result = eventService.findAll(
                "Spring Boot Conf.", "Online", LocalDate.of(2025, 3, 29),
                null, null, pageable);

        // Assert - verificar
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Conf.", result.getContent().get(0).getName());
        assertEquals("Online", result.getContent().get(0).getLocation());

        // Verify - confirmar interacciones
        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(eventMapper, times(1)).toResponseDto(event1);
    }

    @Test
    @DisplayName("Debe retornar una página vacía cuando no existan eventos")
    void shouldReturnEmptyPageWhenNoEventsExist() {
        // Arrange - preparar datos
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> emptyPage = Page.empty(pageable);

        // Mockear repositorio
        when(eventRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act - ejecutar
        Page<EventResponseDto> result = eventService.findAll(
                null, null, null, null, null, pageable);

        // Assert - verificar
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        // Verify - confirmar interacciones
        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        // No debería llamar al mapper porque no hay eventos
        verify(eventMapper, never()).toResponseDto(any(Event.class));
    }

    @Test
    @DisplayName("Debe retornar múltiples eventos filtrados por nombre y rango de fechas")
    void shouldReturnMultipleEventsFilteredByNameAndDateRange() {
        // Arrange - preparar datos
        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("Spring Boot Conf.");
        event1.setDate(LocalDate.of(2025, 3, 29));
        event1.setLocation("Online");
        event1.setCategory(category);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Spring Boot Workshop");
        event2.setDate(LocalDate.of(2025, 4, 10));
        event2.setLocation("New York");
        event2.setCategory(category);

        EventResponseDto dto1 = new EventResponseDto();
        dto1.setId(1L);
        dto1.setName("Spring Boot Conf.");
        dto1.setLocation("Online");

        EventResponseDto dto2 = new EventResponseDto();
        dto2.setId(2L);
        dto2.setName("Spring Boot Workshop");
        dto2.setLocation("New York");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventsPage = new PageImpl<>(List.of(event1, event2), pageable, 2);

        // Mockear repositorio y mapper
        when(eventRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(eventsPage);
        when(eventMapper.toResponseDto(event1)).thenReturn(dto1);
        when(eventMapper.toResponseDto(event2)).thenReturn(dto2);

        // Act - ejecutar con filtros
        Page<EventResponseDto> result = eventService.findAll(
                "Spring Boot", null, null,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 4, 30),
                pageable);

        // Assert - verificar
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Spring Boot Conf.", result.getContent().get(0).getName());
        assertEquals("Spring Boot Workshop", result.getContent().get(1).getName());

        // Verify - confirmar interacciones
        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(eventMapper, times(1)).toResponseDto(event1);
        verify(eventMapper, times(1)).toResponseDto(event2);
    }





















}