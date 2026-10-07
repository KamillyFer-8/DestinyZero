package io.github.destinyzero.service;

import io.github.destinyzero.domain.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DistanceCalculatorTest {
    private final DistanceCalculator calculator = new DistanceCalculator();
    private final Location origin = new Location("origin", "Depot", 0, 0);

    @Test void calculatesEuclideanDistanceAndSymmetry() {
        var point = new Location("a", "A", 3, 4);
        assertEquals(5, calculator.between(origin, point), 1e-10);
        assertEquals(5, calculator.between(point, origin), 1e-10);
        assertEquals(0, calculator.between(point, point));
    }

    @Test void supportsNegativeCoordinates() {
        assertEquals(5, calculator.between(new Location("a", "A", -3, -4), origin), 1e-10);
    }

    @Test void emptyRouteHasZeroDistance() {
        assertEquals(0, calculator.totalDistance(new Route(origin, List.of())));
    }

    @Test void singleDeliveryDoesNotReturnToOrigin() {
        assertEquals(5, calculator.totalDistance(new Route(origin,
                List.of(delivery("a", 3, 4)))), 1e-10);
    }

    @Test void sumsConsecutiveLegsInGivenOrder() {
        var route = new Route(origin, List.of(delivery("a", 3, 4), delivery("b", 6, 8)));
        assertEquals(10, calculator.totalDistance(route), 1e-10);
    }

    @Test void differentDeliveriesMayShareCoordinates() {
        assertEquals(5, calculator.totalDistance(new Route(origin,
                List.of(delivery("a", 3, 4), delivery("b", 3, 4)))), 1e-10);
    }

    @Test void rejectsNullInputsAndNumericOverflow() {
        assertThrows(NullPointerException.class, () -> calculator.between(null, origin));
        assertThrows(NullPointerException.class, () -> calculator.between(origin, null));
        assertThrows(NullPointerException.class, () -> calculator.totalDistance(null));
        var left = new Location("a", "A", -Double.MAX_VALUE, 0);
        var right = new Location("b", "B", Double.MAX_VALUE, 0);
        assertThrows(IllegalArgumentException.class, () -> calculator.between(left, right));
    }

    private Delivery delivery(String id, double x, double y) {
        return new Delivery(id, new Location(id, id, x, y), Priority.NORMAL);
    }
}
