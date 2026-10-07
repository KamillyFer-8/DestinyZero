package io.github.destinyzero.service;

import io.github.destinyzero.domain.*;
import io.github.destinyzero.strategy.*;
import java.util.*;

/** Applies registered strategies to one validated immutable input snapshot. */
public final class RouteComparisonService {
    private final List<RouteStrategy> strategies;
    private final DistanceCalculator distances = new DistanceCalculator();
    private final PriorityMetricsCalculator priorities = new PriorityMetricsCalculator();

    /** Supply alternatives only: the original-order baseline is added automatically. */
    public RouteComparisonService(List<RouteStrategy> alternatives) {
        var registered = new ArrayList<RouteStrategy>();
        registered.add(new OriginalOrderStrategy());
        registered.addAll(List.copyOf(alternatives));
        var names = new HashSet<String>();
        for (var strategy : registered) {
            if (strategy.name() == null || strategy.name().isBlank() || !names.add(strategy.name())) {
                throw new IllegalArgumentException("Strategy names must be nonblank and unique");
            }
        }
        strategies = List.copyOf(registered);
    }

    public ComparisonReport compare(Location origin, List<Delivery> deliveries) {
        return compare(origin, deliveries, 0, 1);
    }

    /** Median time of repeated planning calls after unmeasured warm-up calls. */
    public ComparisonReport compare(Location origin, List<Delivery> deliveries, int warmups, int repetitions) {
        if (warmups < 0 || repetitions < 1) {
            throw new IllegalArgumentException("Warmups must be nonnegative and repetitions positive");
        }
        var input = new Route(origin, deliveries);
        var results = new ArrayList<RouteResult>();
        double baseline = 0;
        for (var strategy : strategies) {
            for (int i = 0; i < warmups; i++) {
                validateOutput(input, strategy.plan(input.origin(), input.orderedDeliveries()));
            }
            Route route = null;
            long[] times = new long[repetitions];
            for (int i = 0; i < repetitions; i++) {
                long start = System.nanoTime();
                Route planned = strategy.plan(input.origin(), input.orderedDeliveries());
                times[i] = System.nanoTime() - start;
                validateOutput(input, planned);
                if (route != null && !route.equals(planned)) {
                    throw new IllegalArgumentException("Strategy must be deterministic across repetitions");
                }
                route = planned;
            }
            Arrays.sort(times);
            int middle = times.length / 2;
            long elapsed = times.length % 2 == 1 ? times[middle]
                    : times[middle - 1] + (times[middle] - times[middle - 1]) / 2;
            double distance = distances.totalDistance(route);
            if (results.isEmpty()) baseline = distance;
            double difference = distance - baseline;
            OptionalDouble percentage = baseline == 0
                    ? OptionalDouble.empty() : OptionalDouble.of(100 * (difference / baseline));
            if (!percentage.isEmpty() && !Double.isFinite(percentage.getAsDouble())) {
                throw new IllegalArgumentException("Percentage exceeds the supported numeric range");
            }
            results.add(new RouteResult(strategy.name(), route, distance, elapsed, difference,
                    percentage, priorities.calculate(route)));
        }
        return new ComparisonReport(results);
    }

    private void validateOutput(Route input, Route output) {
        Objects.requireNonNull(output, "Strategy must return a route");
        if (!input.origin().equals(output.origin())
                || input.orderedDeliveries().size() != output.orderedDeliveries().size()
                || !new HashSet<>(input.orderedDeliveries()).equals(new HashSet<>(output.orderedDeliveries()))) {
            throw new IllegalArgumentException("Strategy must preserve origin and every delivery");
        }
    }
}
