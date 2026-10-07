package io.github.destinyzero.domain;

import java.util.Objects;

public record Delivery(String id, Location destination, Priority priority) {
    public Delivery {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Delivery id must not be blank");
        }
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(priority, "Priority is required");
    }
}
