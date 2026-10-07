package io.github.destinyzero.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** An open route: starts at origin and ends at the last delivery. */
public record Route(Location origin, List<Delivery> orderedDeliveries) {
    public Route {
        Objects.requireNonNull(origin, "Origin is required");
        orderedDeliveries = List.copyOf(orderedDeliveries);
        var ids = new HashSet<String>();
        for (Delivery delivery : orderedDeliveries) {
            if (!ids.add(delivery.id())) {
                throw new IllegalArgumentException("Duplicate delivery id: " + delivery.id());
            }
        }
    }
}
