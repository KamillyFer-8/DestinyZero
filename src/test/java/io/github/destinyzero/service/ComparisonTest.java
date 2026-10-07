package io.github.destinyzero.service;

import io.github.destinyzero.domain.*;
import io.github.destinyzero.strategy.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComparisonTest {
    private final Location origin = new Location("origin", "Depot", 0, 0);
    private Delivery delivery(String id, double x, Priority priority) {
        return new Delivery(id, new Location(id, id, x, 0), priority);
    }
    private RouteComparisonService service() {
        return new RouteComparisonService(List.of(new NearestNeighborStrategy(), new PriorityStrategy()));
    }

    @Test void priorityOrderingIsExplicitAndStable() {
        var low = delivery("l", 1, Priority.LOW);
        var normal = delivery("n", 2, Priority.NORMAL);
        var high = delivery("h", 3, Priority.HIGH);
        var high2 = delivery("h2", 4, Priority.HIGH);
        var input = List.of(low, high, normal, high2);
        assertEquals(List.of(high, high2, normal, low), new PriorityStrategy().plan(origin, input).orderedDeliveries());
    }

    @Test void computesPriorityPositionsAndCeilingOfFirstThird() {
        var route = new Route(origin, List.of(delivery("a", 1, Priority.LOW),
                delivery("b", 2, Priority.HIGH), delivery("c", 3, Priority.NORMAL),
                delivery("d", 4, Priority.HIGH)));
        var metrics = new PriorityMetricsCalculator().calculate(route);
        assertEquals(2, metrics.highPriorityCount());
        assertEquals(3, metrics.averagePosition().orElseThrow());
        assertEquals(50, metrics.firstThirdPercentage().orElseThrow());
        var single = new Route(origin, List.of(delivery("a", 1, Priority.HIGH)));
        assertEquals(100, new PriorityMetricsCalculator().calculate(single).firstThirdPercentage().orElseThrow());
    }

    @Test void comparesDistanceAndPriorityWithSameInput() {
        var input = new ArrayList<>(List.of(delivery("far", 10, Priority.HIGH), delivery("near", 1, Priority.LOW)));
        var snapshot = List.copyOf(input);
        var report = service().compare(origin, input);
        var baseline = report.results().get(0);
        var nearest = report.results().get(1);
        var priority = report.results().get(2);
        assertEquals(19, baseline.totalDistance());
        assertEquals(10, nearest.totalDistance());
        assertEquals(-9, nearest.differenceFromBaseline());
        assertEquals(-900.0 / 19, nearest.percentageDifference().orElseThrow(), 1e-10);
        assertEquals(0, baseline.percentageDifference().orElseThrow());
        assertEquals(2, nearest.priorityMetrics().averagePosition().orElseThrow());
        assertEquals(1, priority.priorityMetrics().averagePosition().orElseThrow());
        assertEquals(100, priority.priorityMetrics().firstThirdPercentage().orElseThrow());
        assertEquals(snapshot, input);
        for (var result : report.results()) {
            assertEquals(2, result.deliveryCount());
            assertTrue(result.executionTimeNanos() >= 0);
        }
        assertThrows(UnsupportedOperationException.class, () -> report.results().clear());
    }

    @Test void undefinedMetricsAreAbsent() {
        for (var input : List.of(List.<Delivery>of(), List.of(delivery("a", 0, Priority.NORMAL)))) {
            for (var result : service().compare(origin, input).results()) {
                assertEquals(0, result.totalDistance());
                assertEquals(0, result.differenceFromBaseline());
                assertTrue(result.percentageDifference().isEmpty());
                assertTrue(result.priorityMetrics().averagePosition().isEmpty());
                assertTrue(result.priorityMetrics().firstThirdPercentage().isEmpty());
            }
        }
    }

    @Test void reportsPositiveDifferencesAsLongerRoutes() {
        var report = service().compare(origin, List.of(delivery("a", -2, Priority.NORMAL),
                delivery("b", 1, Priority.NORMAL), delivery("c", 3, Priority.NORMAL)));
        assertEquals(1, report.results().get(1).differenceFromBaseline());
        assertEquals(100.0 / 7, report.results().get(1).percentageDifference().orElseThrow(), 1e-10);
    }

    @Test void rejectsDuplicateStrategyNamesAndInvalidOutput() {
        assertThrows(IllegalArgumentException.class,
                () -> new RouteComparisonService(List.of(new OriginalOrderStrategy())));
        var invalid = new RouteStrategy() {
            public String name() { return "INVALID"; }
            public Route plan(Location start, List<Delivery> input) { return new Route(start, List.of()); }
        };
        assertThrows(IllegalArgumentException.class, () -> new RouteComparisonService(List.of(invalid))
                .compare(origin, List.of(delivery("a", 1, Priority.HIGH))));
    }

    @Test void givesEveryStrategyTheSameImmutableSnapshot() {
        var received = new ArrayList<List<Delivery>>();
        var alternatives = new ArrayList<RouteStrategy>();
        for (String name : List.of("ONE", "TWO")) {
            alternatives.add(new RouteStrategy() {
                public String name() { return name; }
                public Route plan(Location start, List<Delivery> input) {
                    received.add(input);
                    assertThrows(UnsupportedOperationException.class, () -> input.clear());
                    return new Route(start, input);
                }
            });
        }
        new RouteComparisonService(alternatives).compare(origin, List.of(delivery("a", 1, Priority.NORMAL)));
        assertSame(received.get(0), received.get(1));
    }
}
