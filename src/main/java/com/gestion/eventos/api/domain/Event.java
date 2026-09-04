package com.gestion.eventos.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "events" )
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date; // Usamos localDate para presentar solo la fecha

    @Column(nullable = false)
    private String location;

    // Con fetch = FetchType.LAZY solo se cargan los datos asociados al evento no los speakers
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "events_speakers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "speakers_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // las dos exclusiones siempre usarla cuando existan relaciones bidireccionales
    private Set<Speaker> speakers = new HashSet<>();

    /* Muchos events pueden tener una categoria */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany(mappedBy = "attendedEvents", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // las dos exclusiones siempre usarla cuando existan relaciones bidireccionales
    private Set<User> attendedUsers = new HashSet<>();

    public void addSpeaker(Speaker speaker){
        this.speakers.add(speaker);
        speaker.getEvents().add(this);
    }

    public void removeSpeaker(Speaker speaker){
        this.speakers.remove(speaker);
        speaker.getEvents().remove(this);
    }

}
/*
* Nota: Esta manera: joinColumns = @JoinColumn(name = "event_id"),
*  y esta joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
*  Son IGUALES
* */