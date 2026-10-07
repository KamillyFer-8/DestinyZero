package io.github.destinyzero.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {
    private final Location origin = new Location("origin", "Depot", 0, 0);
    private final Delivery delivery = new Delivery("a", origin, Priority.HIGH);

    @Test void rejectsMissingLocationIdentity() {
        for (String invalid : Arrays.asList(null, "", " ")) {
            assertThrows(IllegalArgumentException.class, () -> new Location(invalid, "A", 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new Location("a", invalid, 0, 0));
        }
    }

    @Test void rejectsNonFiniteCoordinates() {
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Location("a", "A", invalid, 0));
            assertThrows(IllegalArgumentException.class, () -> new Location("a", "A", 0, invalid));
        }
    }

    @Test void rejectsInvalidDeliveries() {
        for (String invalid : Arrays.asList(null, "", " ")) {
            assertThrows(IllegalArgumentException.class, () -> new Delivery(invalid, origin, Priority.HIGH));
        }
        assertThrows(NullPointerException.class, () -> new Delivery("a", null, Priority.HIGH));
        assertThrows(NullPointerException.class, () -> new Delivery("a", origin, null));
    }

    @Test void routeCopiesInputAndPreventsMutation() {
        var input = new ArrayList<>(List.of(delivery));
        var route = new Route(origin, input);
        input.clear();
        assertEquals(List.of(delivery), route.orderedDeliveries());
        assertThrows(UnsupportedOperationException.class, () -> route.orderedDeliveries().clear());
    }

    @Test void rejectsInvalidRouteAndDuplicateDeliveryIds() {
        assertThrows(NullPointerException.class, () -> new Route(null, List.of()));
        assertThrows(NullPointerException.class, () -> new Route(origin, null));
        assertThrows(NullPointerException.class, () -> new Route(origin, Arrays.asList(delivery, null)));
        var duplicate = new Delivery("a", new Location("b", "B", 1, 2), Priority.LOW);
        assertThrows(IllegalArgumentException.class, () -> new Route(origin, List.of(delivery, duplicate)));
    }
}
