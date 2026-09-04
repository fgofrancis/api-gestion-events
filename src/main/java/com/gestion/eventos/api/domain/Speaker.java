package com.gestion.eventos.api.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "speakers")
public class Speaker {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Email(message = "El email debe ser un email válido")
    private String email;

    private String bio;

    @ManyToMany(mappedBy = "speakers") //así se llamará el field en la tabla events
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // las dos exclusiones siempre usarla cuando existan relaciones bidireccionales
    @JsonIgnore // Al serializar esta clase no incluya los events
    private Set<Event> events = new HashSet<>();
}
