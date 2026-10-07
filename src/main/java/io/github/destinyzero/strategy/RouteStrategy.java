package io.github.destinyzero.strategy;

import io.github.destinyzero.domain.Delivery;
import io.github.destinyzero.domain.Location;
import io.github.destinyzero.domain.Route;
import java.util.List;

/** Interchangeable ordering algorithms for open delivery routes. */
public interface RouteStrategy {
    /** Stable identifier used by future reports. */
    String name();

    /**
     * Preserves origin and every delivery exactly once without modifying input.
     * Empty inputs are valid. Nulls and duplicate delivery IDs are rejected
     * according to the Route domain contract.
     */
    Route plan(Location origin, List<Delivery> deliveries);
}
