package com.gestion.eventos.api.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpeakerRequestDto {

    @NotBlank(message = "El nombre del speaker no puede estar vacio")
    @Size(max = 100, message = "El nombre del speaker no puede exceder de 100 caracteres..")
    private String name;

    @NotBlank(message = "El email del speaker no puede estar vacio")
    @Email(message = "El email debe ser un email válido")
    @Size(max = 100, message ="El email del speaker no puede exceder de 100 caracteres..." )
    private String email;

    @Size(max = 500, message = "La biografia de puede exceder de 500 caracteres...")
    private String bio;
}
