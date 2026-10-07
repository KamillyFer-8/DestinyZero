package io.github.destinyzero.strategy;

import io.github.destinyzero.domain.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Advances high-priority deliveries; equal priorities retain input order. */
public final class PriorityStrategy implements RouteStrategy {
    public String name() { return "PRIORITY"; }

    public Route plan(Location origin, List<Delivery> deliveries) {
        var input = new Route(origin, deliveries);
        var ordered = new ArrayList<>(input.orderedDeliveries());
        ordered.sort(Comparator.comparingInt(delivery -> switch (delivery.priority()) {
            case HIGH -> 0;
            case NORMAL -> 1;
            case LOW -> 2;
        }));
        return new Route(origin, ordered);
    }
}
