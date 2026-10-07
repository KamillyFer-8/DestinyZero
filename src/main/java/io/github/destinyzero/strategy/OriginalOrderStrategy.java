package io.github.destinyzero.strategy;

import io.github.destinyzero.domain.Delivery;
import io.github.destinyzero.domain.Location;
import io.github.destinyzero.domain.Route;
import java.util.List;

/** Baseline: preserves the supplied delivery order. */
public final class OriginalOrderStrategy implements RouteStrategy {
    @Override
    public String name() {
        return "ORIGINAL_ORDER";
    }

    @Override
    public Route plan(Location origin, List<Delivery> deliveries) {
        return new Route(origin, deliveries);
    }
}
