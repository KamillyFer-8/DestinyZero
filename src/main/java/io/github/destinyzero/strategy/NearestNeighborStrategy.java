package io.github.destinyzero.strategy;

import io.github.destinyzero.domain.Delivery;
import io.github.destinyzero.domain.Location;
import io.github.destinyzero.domain.Route;
import io.github.destinyzero.service.DistanceCalculator;
import java.util.ArrayList;
import java.util.List;

/**
 * Greedy nearest-neighbor heuristic; does not guarantee a globally optimal route.
 * Equal distances are resolved by original input order. Time complexity: O(n²).
 */
public final class NearestNeighborStrategy implements RouteStrategy {
    private final DistanceCalculator distances = new DistanceCalculator();

    @Override
    public String name() {
        return "NEAREST_NEIGHBOR";
    }

    @Override
    public Route plan(Location origin, List<Delivery> deliveries) {
        // Validate and snapshot input using the same contract as the baseline.
        var input = new Route(origin, deliveries);
        var remaining = new ArrayList<>(input.orderedDeliveries());
        var ordered = new ArrayList<Delivery>(remaining.size());
        Location current = origin;

        while (!remaining.isEmpty()) {
            int nearestIndex = 0;
            double nearestDistance = distances.between(current, remaining.getFirst().destination());
            for (int i = 1; i < remaining.size(); i++) {
                double distance = distances.between(current, remaining.get(i).destination());
                if (distance < nearestDistance) {
                    nearestIndex = i;
                    nearestDistance = distance;
                }
            }
            Delivery next = remaining.remove(nearestIndex);
            ordered.add(next);
            current = next.destination();
        }
        return new Route(origin, ordered);
    }
}
