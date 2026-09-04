package com.gestion.eventos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.eventos.api.domain.Category;
import com.gestion.eventos.api.domain.Event;
import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.dto.EventRequestDto;
import com.gestion.eventos.api.dto.EventResponseDto;
import com.gestion.eventos.api.dto.SpeakerResponseDto;
import com.gestion.eventos.api.exception.ResourceNotFoundException;
import com.gestion.eventos.api.mapper.EventMapper;
import com.gestion.eventos.api.security.jwt.JwtAuthEntryPoint;
import com.gestion.eventos.api.security.jwt.JwtAuthenticationFilter;
import com.gestion.eventos.api.security.jwt.JwtGenerator;
import com.gestion.eventos.api.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;



import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



// @WebMvcTest(EventController.class) esto es sin excluir nada
@WebMvcTest(
        controllers = EventController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class //Excluye la configuracion de seguridad principal y User DetailsSecurity
        },
        // Añadimos excludeFilters para eveitar q Spring escanee y cree tus beans de seguridad especificos
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JwtAuthenticationFilter.class,
                JwtGenerator.class,
                JwtAuthEntryPoint.class //Si, JwtAuthEntryPoint también es un Component y causa problemas
        }))
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc; // con esto podemos simular peticiones HTTP

    private EventService eventService;
    private EventMapper eventMapper;

    @Autowired
    private ObjectMapper objectMapper;//Para serializar y deserializar JSON, Esto es clave para estos tests

    private EventResponseDto eventResponseDto;
    private Event eventEntity;

    /* La finalidad de esta clase es actual como una fabrica de Bean para este test, recuerda que un Bean es un componente*/
    @TestConfiguration
    static class EventControllerTestConfig{
        @Bean
        @Primary
        EventService eventService(){
            return mock(EventService.class);
        }

        @Bean
        @Primary
        EventMapper eventMapper(){
            return mock(EventMapper.class);
        }
    }

    @BeforeEach
    void setUp(@Autowired EventService eventServiceMock, @Autowired EventMapper eventMapperMock){
        this.eventService = eventServiceMock;
        this.eventMapper = eventMapperMock;

        reset(eventService, eventMapper);

        Category category = new Category(10L, "Summit", "Descripcion Category");
        Speaker speaker1 = new Speaker(20L,"Francis Figuereo","francis@example.com","Bio de Francis",new HashSet<>());
        Speaker speaker2 = new Speaker(21L,"Ybis Segura","ybis@example.com","Bio de Ybis",new HashSet<>());

        eventEntity = new Event();
        eventEntity.setId(1L);
        eventEntity.setName("Spring Boot Summit");
        eventEntity.setDate(LocalDate.of(2026,03,29));
        eventEntity.setLocation("Online");
        eventEntity.setCategory(category);
        eventEntity.getSpeakers().add(speaker1);
        eventEntity.getSpeakers().add(speaker2);

        eventResponseDto = new EventResponseDto();
        eventResponseDto.setId(1L);
        eventResponseDto.setName("Spring Boot Summit");
        eventResponseDto.setDate(LocalDate.of(2026,03,29));
        eventResponseDto.setLocation("Online");
        eventResponseDto.setCategoryId(10L);
        eventResponseDto.setCategoryName("Summit");

        SpeakerResponseDto speakerResponseDto1 = new SpeakerResponseDto(20L,"Francis Figuereo","francis@example.com","Bio de Francis");
        SpeakerResponseDto speakerResponseDto2 = new SpeakerResponseDto(21L,"Ybis Segura","ybis@example.com","Bio de Ybis");

        Set<SpeakerResponseDto> speakerDto = new HashSet<>();
        speakerDto.add(speakerResponseDto1);
        speakerDto.add(speakerResponseDto2);
        eventResponseDto.setSpeakerDtos(speakerDto);

    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - Debe retornar un evento por ID cuando existe")
    @WithMockUser(username = "testUser", roles = "USER") //Esto se encarga de simular la Autenticacion
    void shouldReturnEventById() throws Exception{

        // Arrange - Preparación
        when(eventService.findById(anyLong())).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(any(Event.class))).thenReturn(eventResponseDto);

        //Action - Ejecucion
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/events/{id}",1L)

                .accept(MediaType.APPLICATION_JSON) // Esperamos un json

        )
                //Verificación
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Spring Boot Summit"))
                .andExpect(jsonPath("$.location").value("Online"))
                .andExpect(jsonPath("$.categoryName").value("Summit"))

                .andExpect(jsonPath("$.speakerDtos.length()").value(2)) // Verifica que hay exactamente 2 speakerDtos
                .andExpect(jsonPath("$.speakerDtos[2]").doesNotExist())
                // --- Verificación completa de Juan Pérez (sin asumir si es [0] o [1]) ---
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Francis Figuereo')].name").value("Francis Figuereo"))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Francis Figuereo')].email").value("francis@example.com"))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Francis Figuereo')].bio").value("Bio de Francis"))

                // --- Verificación completa de María García (sin asumir si es [0] o [1]) ---
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Ybis Segura')].name").value("Ybis Segura"))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Ybis Segura')].email").value("ybis@example.com"))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Ybis Segura')].bio").value("Bio de Ybis"));

        verify(eventService, times(1)).findById(1L);
        verify(eventMapper, times(1)).toResponseDto(eventEntity);
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - Debe retornar 404 Not found cuando el evento existe")
    @WithMockUser(username = "testUser", roles = "USER") //Esto se encarga de simular la Autenticacion
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception{
        when(eventService.findById(anyLong())).thenThrow(
                new ResourceNotFoundException("Evento no encontrado con id:  99")
        );

        //Action - Ejecucion
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/events/{id}",99L)
                .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Evento no encontrado con id:  99"));

        verify(eventService,times(1)).findById(99L);
        verify(eventMapper, never()).toResponseDto(any(Event.class));
    }

    @Test
    @DisplayName("GET /api/v1/events - Debe retornar todos los eventos paginados y filtrados")
    @WithMockUser(username = "testUser", roles = "USER") //Esto se encarga de simular la Autenticacion
    void shouldReturnAllEventsPagedAndFiltered() throws Exception{

        // Arrange - preparacion
        SpeakerResponseDto speakerResponseDtoA = new SpeakerResponseDto(20L,"Francis Figuereo","francis@example.com","Bio de Francis");
        Set<SpeakerResponseDto> speakersA = new HashSet<>(Set.of(speakerResponseDtoA));

        EventResponseDto eventResponse2 = new EventResponseDto();
        eventResponse2.setId(2L);
        eventResponse2.setName("Webinar de Spring Security");
        eventResponse2.setDate(LocalDate.of(2024,12,1));
        eventResponse2.setLocation("Virtual");
        eventResponse2.setCategoryName("Summit");
        eventResponse2.setCategoryId(10L);
        eventResponse2.setSpeakerDtos(speakersA); // Asigna los Speakers

        EventResponseDto eventResponse3 = new EventResponseDto();
        eventResponse3.setId(3L);
        eventResponse3.setName("Conferencia Cloud Native");
        eventResponse3.setDate(LocalDate.of(2025,1,20));
        eventResponse3.setLocation("Auditorio Principal");
        eventResponse3.setCategoryName("Summit");
        eventResponse3.setCategoryId(10L);
        eventResponse3.setSpeakerDtos(speakersA); // Asigna los mismos Speakers

        List<EventResponseDto> eventResponseDtoList = List.of(eventResponse2,eventResponse3);

        Pageable pageableMock = PageRequest.of(0,10);

        Page<EventResponseDto> eventResponseDtoPage = new PageImpl<>(eventResponseDtoList,pageableMock,
                                                            eventResponseDtoList.size());
        
        when(eventService.findAll(eq("Spring"),any(),any(),any(),any() ,any(Pageable.class))).thenReturn(eventResponseDtoPage);

        // Action - ejecucion, aquí toma la data de la URL
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/events")
                .param("page","0")
                .param("size","10")
                .param("name","Spring")
                .accept(MediaType.APPLICATION_JSON)
        )
        // Assert - Verificacion
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0]").exists())
                .andExpect(jsonPath("$.content[1]").exists())
                .andExpect(jsonPath("$.content[2]").doesNotExist())

                // Verificamos los datos del primer evento en la página
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Webinar de Spring Security"))
                .andExpect(jsonPath("$.content[0].date").value("2024-12-01"))
                .andExpect(jsonPath("$.content[0].location").value("Virtual"))
                .andExpect(jsonPath("$.content[0].categoryName").value("Summit"))
                .andExpect(jsonPath("$.content[0].speakerDtos.length()").value(1))
                .andExpect(jsonPath("$.content[0].speakerDtos[?(@.name == 'Francis Figuereo')].id").value(20))
                .andExpect(jsonPath("$.content[0].speakerDtos[?(@.name == 'Francis Figuereo')].name").value("Francis Figuereo"))
                .andExpect(jsonPath("$.content[0].speakerDtos[?(@.name == 'Francis Figuereo')].email").value("francis@example.com"))
                .andExpect(jsonPath("$.content[0].speakerDtos[?(@.name == 'Francis Figuereo')].bio").value("Bio de Francis"))

                // Verificamos los datos del segundo evento en la página
                .andExpect(jsonPath("$.content[1].id").value(3))
                .andExpect(jsonPath("$.content[1].name").value("Conferencia Cloud Native"))
                .andExpect(jsonPath("$.content[1].date").value("2025-01-20"))
                .andExpect(jsonPath("$.content[1].location").value("Auditorio Principal"))
                .andExpect(jsonPath("$.content[1].categoryName").value("Summit"))
                .andExpect(jsonPath("$.content[1].speakerDtos.length()").value(1))
                .andExpect(jsonPath("$.content[1].speakerDtos[?(@.name == 'Francis Figuereo')].id").value(20))
                .andExpect(jsonPath("$.content[1].speakerDtos[?(@.name == 'Francis Figuereo')].name").value("Francis Figuereo"))
                .andExpect(jsonPath("$.content[1].speakerDtos[?(@.name == 'Francis Figuereo')].email").value("francis@example.com"))
                .andExpect(jsonPath("$.content[1].speakerDtos[?(@.name == 'Francis Figuereo')].bio").value("Bio de Francis"))

                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        // Verify
        verify(eventService, times(1)).findAll(eq("Spring"), any(),any(),any(),any(), any(Pageable.class));
        //Test defensivo
        verify(eventService, never()).findById(anyLong()); // Explicito pero redundante
    }

    @Test
    @DisplayName("POST /api/v1/events - Debe crear un eventos y retornar 201 Created")
    @WithMockUser(username = "testUser", roles = "USER") //Esto se encarga de simular la Autenticacion
    void shouldCreateEventSuccessfully() throws Exception{

        // Arrange Preparacion

        // 1.- Crear del DTO de entrada para la peticion POST
        EventRequestDto eventRequestDto = new EventRequestDto();
        eventRequestDto.setName("Taller de Microservicios");
        eventRequestDto.setDate(LocalDate.of(2025,03,10));
        eventRequestDto.setLocation("Centro de Convenciones");
        eventRequestDto.setCategoryId(10L);
        eventRequestDto.setSpeakersId(Set.of(20L, 21L));

        //--- Datos para el Event que el servicio MOCK deberia devolver ---
        // Este evento es lo que eventService.save() retornaria *antes* de ser mapeado
        Event savedEventEntity = new Event();
        savedEventEntity.setId(5L); // El nuevo ID asignado
        savedEventEntity.setName("Taller de Microservicios");
        savedEventEntity.setDate(LocalDate.of(2025,03,10));
        savedEventEntity.setLocation("Centro de Convenciones");

        // Para el Event, necesitamos objetos Category y Speaker reales (o mocks si no tuvieras lo comunes)
        Category categoryForSavedEvent = new Category(10L, "Summit", "Descripcion Category");
        Speaker speaker1ForSavedEvent = new Speaker(20L,"Francis Figuereo","francis@example.com","Bio de Francis",new HashSet<>());
        Speaker speaker2ForSavedEvent = new Speaker(21L,"Ybis Segura","ybis@example.com","Bio de Ybis",new HashSet<>());
        savedEventEntity.setCategory(categoryForSavedEvent);
        savedEventEntity.addSpeaker(speaker1ForSavedEvent);
        savedEventEntity.addSpeaker(speaker2ForSavedEvent);

        //--- Datos para el EventResponseDto que el mapper MOCK debería devolver  ---
        // Este es el resultado final que el Controller enviará
        EventResponseDto createdEventResponseDto = new EventResponseDto();
        createdEventResponseDto.setId(5L);
        createdEventResponseDto.setName("Taller de Microservicios");
        createdEventResponseDto.setDate(LocalDate.of(2025,03,10));
        createdEventResponseDto.setLocation("Centro de Convenciones");
        createdEventResponseDto.setCategoryName("Summit");
        createdEventResponseDto.setCategoryId(10L);

        SpeakerResponseDto speakerResponseDto1 = new SpeakerResponseDto(20L,"Francis Figuereo","francis@example.com","Bio de Francis");
        SpeakerResponseDto speakerResponseDto2 = new SpeakerResponseDto(21L,"Ybis Segura","ybis@example.com","Bio de Ybis");
        Set<SpeakerResponseDto> speakersDto = new HashSet<>();
        speakersDto.add(speakerResponseDto1);
        speakersDto.add(speakerResponseDto2);
        createdEventResponseDto.setSpeakerDtos(speakersDto);

        // 3. Configurar el comportamiento de los MOCKS
        // Paso 1.- eventService.save() es llamado y devuelve un Event
        when(eventService.save(any(EventRequestDto.class))).thenReturn(savedEventEntity);

        // Paso 2.- eventMapper.toResponseDto() es llamado con el evento devuelto y devuelve un EventResponse
        when(eventMapper.toResponseDto(savedEventEntity)).thenReturn(createdEventResponseDto);
        
        // Action ejecucion, aquí toma la data del body
        mockMvc.perform(post("/api/v1/events") // Endpoint para crear
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequestDto)))

                // Assert - Verificacion:
                .andExpect(status().isCreated()) // Esperamos un 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Taller de Microservicios"))
                .andExpect(jsonPath("$.date").value("2025-03-10"))
                .andExpect(jsonPath("$.location").value("Centro de Convenciones"))
                .andExpect(jsonPath("$.categoryName").value("Summit"))
                .andExpect(jsonPath("$.categoryId").value(10))
                .andExpect(jsonPath("$.speakerDtos.length()").value(2))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Francis Figuereo')].id").value(20))
                .andExpect(jsonPath("$.speakerDtos[?(@.name == 'Ybis Segura')].id").value(21));

        // 4. Verificacion de integraciones con el Mock
        verify(eventService,times(1)).save(any(EventRequestDto.class));
        verify(eventMapper, times(1)).toResponseDto(savedEventEntity);

        // Verificaciones defensivas (que no se llamen otros metodos)
        verify(eventService, never()).findAll(anyString(), anyString(),any(LocalDate.class),any(LocalDate.class),any(LocalDate.class),any(Pageable.class));
        verify(eventService, never()).findById(anyLong());

        // Con esto confirmas que no hubo más interacciones con eventService. Esto puede sustituir los dos verify de arriba
        verifyNoMoreInteractions(eventService);
    }

    @Test
    @DisplayName("POST /api/v1/events - Debe actualizar un eventos existente y retornar 200 OK")
    @WithMockUser(username = "adminUser", roles = "ADMIN") //Esto se encarga de simular la Autenticacion
    void shouldUpdateEventSuccessfully() throws Exception{

        final Long eventIdToUpdate = 1l; // ID del evento que vamos a actualizar

        // Crear Dto de entrada para la peticion PUT (Nuevos datos para el evento)
        EventRequestDto updateEventRequestDto = new EventRequestDto();
        updateEventRequestDto.setName("Conferencia Tech v2.0 (Actualizada)");
        updateEventRequestDto.setDate(LocalDate.of(2025,01,15));
        updateEventRequestDto.setLocation("Centro de Convenciones Principal");
        updateEventRequestDto.setCategoryId(11L); // Nueva categoria (ej. Desarrollo de Software)
        updateEventRequestDto.setSpeakersId(Set.of(22L)); // Nuevo Speakers (ej. solo uno)

        // -- Datos del evento que el servicio MOCK deberia devolver despues de las actualizacion --
        // ESte Event simula el estado del evento despues de que eventService.update() lo haya procesado
        Event updatedEventEntity = new Event();
        updatedEventEntity.setId(eventIdToUpdate);
        updatedEventEntity.setName("Conferencia Tech v2.0 (Actualizada)");
        updatedEventEntity.setDate(LocalDate.of(2025,01,15));
        updatedEventEntity.setLocation("Centro de Convenciones Principal");

        // Necesitamos la Category y el Speaker reales (o Mocks) q correspondan a los nuevos ID
        Category newCategory = new Category(11L,"Desarrollo de Software", "Eventos de desarrollo");
        Speaker newSpeaker = new Speaker(22L,"Carlos Figuereo","carlos.figuereo@example.com","Experto en Fintech", new HashSet<>());
        updatedEventEntity.setCategory(newCategory);
        updatedEventEntity.addSpeaker(newSpeaker);

        // -- Datos para el EventResponseDto que el mapper MOCK deberia devolver --
        // Este es el resultado final que el controller deberia devolver como respuesta 200 Ok.
        EventResponseDto updatedEventResponseDto = new EventResponseDto();
        updatedEventResponseDto.setId(eventIdToUpdate);
        updatedEventResponseDto.setName("Conferencia Tech v2.0 (Actualizada)");
        updatedEventResponseDto.setDate(LocalDate.of(2025,01,15));
        updatedEventResponseDto.setLocation("Centro de Convenciones Principal");
        updatedEventResponseDto.setCategoryName("Desarrollo de Software");
        updatedEventResponseDto.setCategoryId(11L);
        updatedEventResponseDto.setSpeakerDtos(Set.of(new SpeakerResponseDto(22L,"Carlos Figuereo","carlos.figuereo@example.com","Experto en Fintech")));

        // 2. Configurar el comportamiento de los MOCKs
        // Paso 1: eventService.update() es llamado y devuelve el Evento actualizado. //Nota: al usar any en un argumento debo usar otro comparador especial en el otro
        when(eventService.update(eq(eventIdToUpdate),any(EventRequestDto.class) )).thenReturn(updatedEventEntity);

        // Paso 1: eventMapper.toResponseDto() es llamado con el Event actualizado y devuelve el EventResponseDto
        when(eventMapper.toResponseDto(updatedEventEntity)).thenReturn(updatedEventResponseDto);

        // Action ejecucion, aquí toma la data del body
        mockMvc.perform(put("/api/v1/events/{id}", eventIdToUpdate) // Endpoint para actualizar con el id
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEventRequestDto))) // El DTO con los nuevos datos

                // Assert Verificacion
                .andExpect(status().isOk()) // Esperamos un 200 para actualizar exitosamente
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventIdToUpdate)) // El ID sigue siendo el mismo
                .andExpect(jsonPath("$.name").value("Conferencia Tech v2.0 (Actualizada)"))
                .andExpect(jsonPath("$.date").value("2025-01-15"))
                .andExpect(jsonPath("$.location").value("Centro de Convenciones Principal"))
                .andExpect(jsonPath("$.categoryName").value("Desarrollo de Software"))
                .andExpect(jsonPath("$.categoryId").value(11))
                .andExpect(jsonPath("$.speakerDtos.length()").value(1))
                .andExpect(jsonPath("$.speakerDtos[0].id").value(22))
                .andExpect(jsonPath("$.speakerDtos[0].name").value("Carlos Figuereo"));

        // 4. Verificacion de interaciones con MOCKs:
        verify(eventService,times(1)).update(eq(eventIdToUpdate), any(EventRequestDto.class));
        verify(eventMapper, times(1)).toResponseDto(updatedEventEntity);

        // Verificacion defensiva
        verifyNoMoreInteractions(eventService);

    }

    @Test
    @DisplayName("DELETE /api/v1/events/{id} - Debe borrar un eventos existente y retornar 204 No Content")
    @WithMockUser(username = "adminUser", roles = "ADMIN") //Esto se encarga de simular la Autenticacion
    void shouldDeleteEventSuccessfully() throws Exception{
         Long eventIdToDelete = 1L;

        doNothing().when(eventService).deleteById(eventIdToDelete);

        mockMvc.perform(delete("/api/v1/events/{id}", eventIdToDelete)) // Endpoint para actualizar con el id
                .andExpect(status().isNoContent());

        verify(eventService,times(1)).deleteById(eventIdToDelete);

        verifyNoMoreInteractions(eventService);


    }
    @Test
    @DisplayName("DELETE /api/v1/events/{id} - Debe retornar Not Found 404  si el eventos a eliminar no existente")
    @WithMockUser(username = "adminUser", roles = "ADMIN") //Esto se encarga de simular la Autenticacion
    void shouldReturnNotFoundWhenDoesNotEvent() throws Exception{

        final Long nonExistenEventId = 99L;
        // Simulamos el error
        doThrow(new ResourceNotFoundException("Evento no encontrado con ID: " + nonExistenEventId))
                .when(eventService).deleteById(nonExistenEventId);

        // Lo esperado, definiendo la ruta y todo lo esperado
        mockMvc.perform(delete("/api/v1/events/{id}", nonExistenEventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Evento no encontrado con ID: " + nonExistenEventId));

        verify(eventService,times(1)).deleteById(nonExistenEventId);
        verifyNoMoreInteractions(eventService);
        verifyNoMoreInteractions(eventMapper);
    }

}