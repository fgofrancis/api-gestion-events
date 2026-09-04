package com.gestion.eventos.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String username;
    private String email;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany( fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_attended_events",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // las dos exclusiones siempre usarla cuando existan relaciones bidireccionales
    @JsonIgnore //
    private Set<Event> attendedEvents = new HashSet<>();

    public void addAttendedEvent(Event event){
        this.attendedEvents.add(event);
        event.getAttendedUsers().add(this);
    }

    public void removeAttendedEvent(Event event){
        this.attendedEvents.remove(event);
        event.getAttendedUsers().remove(this);
    }
}














