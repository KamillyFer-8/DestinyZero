package io.github.destinyzero.dataset;

import io.github.destinyzero.domain.*;
import java.util.List;

public record Scenario(String id, long seed, Location origin, List<Delivery> deliveries) {
    public Scenario {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Scenario id is required");
        var validated = new Route(origin, deliveries);
        deliveries = validated.orderedDeliveries();
    }
}
