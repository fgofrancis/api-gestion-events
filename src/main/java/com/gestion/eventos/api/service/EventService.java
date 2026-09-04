package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Category;
import com.gestion.eventos.api.domain.Event;
import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.domain.User;
import com.gestion.eventos.api.dto.EventRequestDto;
import com.gestion.eventos.api.dto.EventResponseDto;
import com.gestion.eventos.api.exception.ResourceNotFoundException;
import com.gestion.eventos.api.mapper.EventMapper;
import com.gestion.eventos.api.repository.EventRepository;
import com.gestion.eventos.api.util.EventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.identity;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService{

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ICategoryService categoryService;
    private final ISpeakerService speakerService;

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDto> findAll(String name, String location, LocalDate date,
                                          LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        Specification<Event> spec = EventSpecifications.hasName(name)
                .and(EventSpecifications.hasLocation(location))
                .and(EventSpecifications.hasDate(date))
                .and(EventSpecifications.dateBetween(dateFrom, dateTo));

       Page<Event> eventsPage = eventRepository.findAll(spec, pageable);

       List<EventResponseDto> dtos = eventsPage.getContent().stream()
               .map(eventMapper::toResponseDto)
               .toList();

        return new PageImpl<>(dtos, pageable, eventsPage.getTotalElements());
    }

    @Override
    @Transactional
    public Event save(EventRequestDto eventRequestDto) {
        Event event = eventMapper.toEntity(eventRequestDto);

        Category category = categoryService.findById(eventRequestDto.getCategoryId());
        event.setCategory(category);

        if(eventRequestDto.getSpeakersId()!=null && !eventRequestDto.getSpeakersId().isEmpty()){
            Set<Speaker> speakers = eventRequestDto.getSpeakersId().stream()
                    .map(speakerService::findById)
                    .collect(Collectors.toSet());
            speakers.forEach(event::addSpeaker);
        }

        return eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Evento no encontrado con id: " + id)
        );
    }

    @Override
    public Event update(Long id, EventRequestDto requestDto) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Evento no encontrado con ID: " + id)
                );
        eventMapper.updateEventFromDto(requestDto, existingEvent);
        
        if(!existingEvent.getCategory().getId().equals(requestDto.getCategoryId())){
            Category category = categoryService.findById(requestDto.getCategoryId());
            existingEvent.setCategory(category);
        }

        Set<Speaker> updatedSpeakers;
        if(requestDto.getSpeakersId() != null && !requestDto.getSpeakersId().isEmpty()){
            updatedSpeakers = requestDto.getSpeakersId().stream()
                    .map(speakerService::findById)
                    .collect(Collectors.toSet());
        } else {
            updatedSpeakers = new HashSet<>();
        }
        //Punto de partida existingEvent tiene [Luis, *Juan, *Maria] updatedSpeakers tiene [Luis, Fernando]
        new HashSet<>(existingEvent.getSpeakers())
                .forEach(currentSpeaker->{
                    if(!updatedSpeakers.contains(currentSpeaker)){
                        existingEvent.removeSpeaker(currentSpeaker);
                    }
                });

        updatedSpeakers.forEach(newSpeaker ->{
            if(!existingEvent.getSpeakers().contains(newSpeaker)){
                existingEvent.addSpeaker(newSpeaker);
            }
        });

        //existingEvent queda con [Luis, Fernando]
        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Event eventToDelete = this.findById(id);
        eventRepository.delete(eventToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEventsAndTheirDetailsProblematic(){
        List<Event> events = eventRepository.findAll();

        events.forEach(event -> {
            event.getSpeakers().size();
            event.getSpeakers().stream().map(Speaker::getName).collect(Collectors.toSet());
            event.getCategory().getName();
            event.getAttendedUsers().size();
        });
        return events;
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEventsAndTheirDetailsOptimizedWithJoinFetch(){
        List<Event> events = eventRepository.findAllWithCategoryAndSpeakers();

        events.forEach(event -> {
            System.out.println("Event: " + event.getName());
            System.out.println("Category: " + event.getCategory().getName());
            System.out.println("Event: " + event.getSpeakers()
                    .stream().map(Speaker::getName)
                    .collect(Collectors.joining(", "))
            );
        });
        return events;
    }

    @Transactional(readOnly = true)
    public List<Event> findAllEventsWithAllDetailsOptimized() {
        System.out.println("\n--- DEMO: findAllWithAllDetails(@EntityGraph con Category, speakers, AttendedUser) --");
        List<Event> events = eventRepository.findAllWithAllDetails();
        events.forEach(event -> {
            System.out.println("Event: " + event.getName());
            if(event.getCategory() != null){
                System.out.println("Category: " + event.getCategory().getName());
            }
            if(event.getSpeakers() != null && !event.getSpeakers().isEmpty()){
                System.out.println("Speakers: " + event.getSpeakers().stream().map(Speaker::getName).collect(Collectors.joining(", ")));
            }
            if(event.getAttendedUsers() != null && !event.getAttendedUsers().isEmpty()){
                System.out.println("Attended Users: " + event.getAttendedUsers().stream().map(User::getName).collect(Collectors.joining(", ")));
            }
        });
        return events;
    }


}
