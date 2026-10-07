package io.github.destinyzero.strategy;

import io.github.destinyzero.domain.Delivery;
import io.github.destinyzero.domain.Location;
import io.github.destinyzero.domain.Priority;
import io.github.destinyzero.domain.Route;
import io.github.destinyzero.service.DistanceCalculator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class RouteStrategyTest {
    private final Location origin = new Location("depot", "Depot", 0, 0);

    static Stream<RouteStrategy> strategies() {
        return Stream.of(new OriginalOrderStrategy(), new NearestNeighborStrategy(), new PriorityStrategy());
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void acceptsEmptyRoutesAndSingleDelivery(RouteStrategy strategy) {
        assertEquals(new Route(origin, List.of()), strategy.plan(origin, List.of()));
        var delivery = delivery("a", 3, 4);
        var route = strategy.plan(origin, List.of(delivery));
        assertEquals(List.of(delivery), route.orderedDeliveries());
        assertEquals(5, new DistanceCalculator().totalDistance(route), 1e-10);
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void preservesEveryDeliveryOriginAndInput(RouteStrategy strategy) {
        var input = new ArrayList<>(List.of(delivery("a", 9, 0), delivery("b", 1, 0), delivery("c", 4, 0)));
        var snapshot = List.copyOf(input);
        var route = strategy.plan(origin, input);
        assertEquals(origin, route.origin());
        assertEquals(snapshot, input);
        assertEquals(snapshot.size(), route.orderedDeliveries().size());
        for (Delivery delivery : snapshot) {
            assertEquals(1, route.orderedDeliveries().stream().filter(delivery::equals).count());
        }
        assertEquals(route, strategy.plan(origin, input));
        input.clear();
        assertEquals(snapshot.size(), route.orderedDeliveries().size());
        assertThrows(UnsupportedOperationException.class, () -> route.orderedDeliveries().clear());
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void rejectsInvalidInputs(RouteStrategy strategy) {
        assertThrows(NullPointerException.class, () -> strategy.plan(null, List.of()));
        assertThrows(NullPointerException.class, () -> strategy.plan(origin, null));
        assertThrows(NullPointerException.class, () -> strategy.plan(origin, Arrays.asList((Delivery) null)));
        assertThrows(IllegalArgumentException.class, () -> strategy.plan(origin,
                List.of(delivery("a", 1, 0), delivery("a", 2, 0))));
    }

    @Test void baselinePreservesOriginalOrder() {
        var input = List.of(delivery("far", 10, 0), delivery("near", 1, 0));
        assertEquals(input, new OriginalOrderStrategy().plan(origin, input).orderedDeliveries());
    }

    @Test void nearestNeighborUsesCurrentPositionAtEveryStep() {
        var a = delivery("a", 2, 0);
        var b = delivery("b", -3, 0);
        var c = delivery("c", 4, 0);
        var route = new NearestNeighborStrategy().plan(origin, List.of(b, c, a));
        assertEquals(List.of(a, c, b), route.orderedDeliveries());
        assertEquals(11, new DistanceCalculator().totalDistance(route), 1e-10);
    }

    @Test void tiesFollowInputOrderIncludingSharedDestinations() {
        var first = delivery("first", -1, 0);
        var second = delivery("second", 1, 0);
        var shared = delivery("shared", -1, 0);
        assertEquals(List.of(first, shared, second), new NearestNeighborStrategy()
                .plan(origin, List.of(first, second, shared)).orderedDeliveries());
        assertEquals(List.of(second, first, shared), new NearestNeighborStrategy()
                .plan(origin, List.of(second, first, shared)).orderedDeliveries());
    }

    @Test void greedyChoiceCanBeWorseThanOriginalOrder() {
        var left = delivery("left", -2, 0);
        var near = delivery("near", 1, 0);
        var far = delivery("far", 3, 0);
        var input = List.of(left, near, far);
        var calculator = new DistanceCalculator();
        assertEquals(7, calculator.totalDistance(new OriginalOrderStrategy().plan(origin, input)), 1e-10);
        assertEquals(8, calculator.totalDistance(new NearestNeighborStrategy().plan(origin, input)), 1e-10);
    }

    private Delivery delivery(String id, double x, double y) {
        return new Delivery(id, new Location(id, id, x, y), Priority.NORMAL);
    }
}

