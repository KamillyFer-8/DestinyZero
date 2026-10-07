package io.github.destinyzero.domain;

import java.util.OptionalDouble;

/** Measured execution plus deterministic metrics; negative differences mean shorter routes. */
public record RouteResult(String strategyName, Route route, double totalDistance,
                          long executionTimeNanos, double differenceFromBaseline,
                          OptionalDouble percentageDifference, PriorityMetrics priorityMetrics) {
    public int deliveryCount() { return route.orderedDeliveries().size(); }
}
