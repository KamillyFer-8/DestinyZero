package io.github.destinyzero.dataset;

import io.github.destinyzero.domain.*;
import java.util.ArrayList;
import java.util.Random;

/** Fixed draw order: x, y, priority per delivery; uniform integer coordinates [0,1000]. */
public final class DatasetGenerator {
    public Scenario generate(int size, long seed) {
        if (size < 1) throw new IllegalArgumentException("Dataset size must be positive");
        var random = new Random(seed);
        var deliveries = new ArrayList<Delivery>(size);
        for (int i = 1; i <= size; i++) {
            int x = random.nextInt(1001);
            int y = random.nextInt(1001);
            Priority priority = switch (random.nextInt(3)) {
                case 0 -> Priority.LOW;
                case 1 -> Priority.NORMAL;
                default -> Priority.HIGH;
            };
            String id = "delivery-" + i;
            deliveries.add(new Delivery(id, new Location("location-" + i, "Destination " + i, x, y), priority));
        }
        return new Scenario("n" + size + "-seed" + seed, seed,
                new Location("depot", "Depot", 0, 0), deliveries);
    }
}
