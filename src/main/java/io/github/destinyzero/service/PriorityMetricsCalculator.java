package io.github.destinyzero.service;

import io.github.destinyzero.domain.*;
import java.util.OptionalDouble;

public final class PriorityMetricsCalculator {
    public PriorityMetrics calculate(Route route) {
        int count = 0;
        int inFirstThird = 0;
        long positionSum = 0;
        int size = route.orderedDeliveries().size();
        // Ceiling includes one position even for routes shorter than three deliveries.
        int firstThirdSize = size / 3 + (size % 3 == 0 ? 0 : 1);
        for (int i = 0; i < size; i++) {
            if (route.orderedDeliveries().get(i).priority() == Priority.HIGH) {
                count++;
                positionSum += i + 1;
                if (i < firstThirdSize) inFirstThird++;
            }
        }
        return count == 0
                ? new PriorityMetrics(0, OptionalDouble.empty(), OptionalDouble.empty())
                : new PriorityMetrics(count, OptionalDouble.of((double) positionSum / count),
                        OptionalDouble.of(100.0 * inFirstThird / count));
    }
}
