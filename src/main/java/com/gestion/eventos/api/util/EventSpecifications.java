package com.gestion.eventos.api.util;

import com.gestion.eventos.api.domain.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/* Esta clase se usará para buscar en Event por varios campos*/
public class EventSpecifications {

    public static Specification<Event> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Event> hasLocation(String location) {
        return (root, query, cb) ->
                location == null ? null : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Event> hasDate(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null : cb.equal(root.get("date"), date);
    }

    public static Specification<Event> dateBetween(LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            if (dateFrom != null && dateTo != null) {
                return cb.between(root.get("date"), dateFrom, dateTo);
            } else if (dateFrom != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
            } else if (dateTo != null) {
                return cb.lessThanOrEqualTo(root.get("date"), dateTo);
            }
            return null;
        };
    }
}

