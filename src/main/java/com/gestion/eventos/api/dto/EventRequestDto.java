package com.gestion.eventos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Schema(description = "Detalles de la solicitud para crear/actualizar un evento") // Esto es de Swagger en el Eventcontroller
public class EventRequestDto {
    @Schema(description = "Nombre del Evento", example = "Conferencia de Spring Boot") // Swagger. ahora vamos al OpenAPIConfig
    @NotBlank(message = "El nombre del evento no puede estar vacío.")
    private String name;

    @NotNull(message = "La fecha no puede ser nula.")
    private LocalDate date;

    @NotBlank(message = "La ubicación no puede estar vacía.")
    private String location;

    @NotNull(message = "La categoria es obligatoria")
    private Long categoryId;

    private Set<Long> speakersId;
}
