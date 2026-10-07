package io.github.destinyzero.service;

import io.github.destinyzero.domain.Location;
import io.github.destinyzero.domain.Route;
import java.util.Objects;

/** Euclidean distances in arbitrary simulation units, with no return to origin. */
public final class DistanceCalculator {
    public double between(Location from, Location to) {
        Objects.requireNonNull(from, "Starting location is required");
        Objects.requireNonNull(to, "Ending location is required");
        return requireFinite(Math.hypot(to.x() - from.x(), to.y() - from.y()));
    }

    public double totalDistance(Route route) {
        Objects.requireNonNull(route, "Route is required");
        double total = 0;
        Location current = route.origin();
        for (var delivery : route.orderedDeliveries()) {
            total = requireFinite(total + between(current, delivery.destination()));
            current = delivery.destination();
        }
        return total;
    }

    private double requireFinite(double distance) {
        if (!Double.isFinite(distance)) {
            throw new IllegalArgumentException("Distance exceeds the supported numeric range");
        }
        return distance;
    }
}
