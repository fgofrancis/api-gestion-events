package com.gestion.eventos.api.repository;

import com.gestion.eventos.api.domain.Event;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/* Tu EventRepository debe extender JpaSpecificationExecutor además de JpaRepository:
 Esto es para poder consultar por varios campos. Alteré este repository, creé una class en util y
 modifiqué el servicio (En tu EventService combinas las especificaciones según los parámetros recibidos:)
 y Ajusté el controller
 */
public interface EventRepository extends JpaRepository<Event, Long>,
                                    JpaSpecificationExecutor<Event> {
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.category LEFT JOIN FETCH e.speakers")
    List<Event> findAllWithCategoryAndSpeakers();

    @Query("SELECT e FROM Event e JOIN FETCH e.category LEFT JOIN FETCH e.speakers WHERE e.id = :id")
    Optional<Event> findByIdWithCategoryAndSpeakers(Long id);

    @Override
    @NotNull
    @EntityGraph(attributePaths = {"category", "speakers"})
    List<Event> findAll();

    @Override
    @NotNull
    @EntityGraph(attributePaths = {"category", "speakers"})
    Optional<Event> findById(Long id);

    /* En esta SQL EntityGraph hace los join y los left join segun correspondan */
    @EntityGraph(attributePaths = {"category", "speakers", "attendedUsers"})
    @Query("SELECT e FROM Event e")
    List<Event> findAllWithAllDetails();
}

