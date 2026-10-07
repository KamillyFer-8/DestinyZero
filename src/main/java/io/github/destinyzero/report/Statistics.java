package io.github.destinyzero.report;

import java.util.Arrays;

/** Descriptive statistics; no external analysis library is needed. */
public record Statistics(double mean, double median, double min, double max) {
    public static Statistics of(double[] values) {
        if (values.length == 0 || Arrays.stream(values).anyMatch(v -> !Double.isFinite(v))) {
            throw new IllegalArgumentException("Statistics require nonempty finite values");
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int middle = sorted.length / 2;
        double median = sorted.length % 2 == 1 ? sorted[middle]
                : sorted[middle - 1] / 2 + sorted[middle] / 2;
        return new Statistics(Arrays.stream(sorted).average().orElseThrow(), median,
                sorted[0], sorted[sorted.length - 1]);
    }
}
