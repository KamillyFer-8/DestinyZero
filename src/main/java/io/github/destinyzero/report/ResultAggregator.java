package io.github.destinyzero.report;

import io.github.destinyzero.experiment.ExperimentRow;
import java.util.*;
import java.util.function.ToDoubleFunction;

public final class ResultAggregator {
    public List<SummaryRow> aggregate(List<ExperimentRow> rows) {
        if (rows.isEmpty()) throw new IllegalArgumentException("Results must not be empty");
        var groups = new LinkedHashMap<String, List<ExperimentRow>>();
        for (var row : rows) {
            groups.computeIfAbsent(row.result().strategyName(), key -> new ArrayList<>()).add(row);
        }
        var summaries = new ArrayList<SummaryRow>();
        for (var group : groups.values()) {
            summaries.add(summarize(group, 0));
            group.stream().map(ExperimentRow::datasetSize).distinct().sorted().forEach(size ->
                    summaries.add(summarize(group.stream().filter(row -> row.datasetSize() == size).toList(), size)));
        }
        return List.copyOf(summaries);
    }

    private SummaryRow summarize(List<ExperimentRow> rows, int size) {
        int better = 0, equal = 0, worse = 0;
        for (var row : rows) {
            // Relative tolerance for floating-point summation differences only.
            double baseline = row.result().totalDistance() - row.result().differenceFromBaseline();
            double tolerance = 1e-9 * Math.max(1, Math.abs(baseline));
            double difference = row.result().differenceFromBaseline();
            if (Math.abs(difference) <= tolerance) equal++;
            else if (difference < 0) better++;
            else worse++;
        }
        double[] percentages = rows.stream().flatMapToDouble(row -> row.result().percentageDifference().stream()).toArray();
        double[] positions = rows.stream().flatMapToDouble(row -> row.result().priorityMetrics().averagePosition().stream()).toArray();
        double[] thirds = rows.stream().flatMapToDouble(row -> row.result().priorityMetrics().firstThirdPercentage().stream()).toArray();
        return new SummaryRow(rows.getFirst().result().strategyName(), size, rows.size(),
                statistics(rows, row -> row.result().totalDistance()),
                statistics(rows, row -> row.result().differenceFromBaseline()).mean(),
                average(percentages), percentages.length, better, equal, worse, 100.0 * better / rows.size(),
                statistics(rows, row -> row.result().executionTimeNanos()), positions.length,
                average(positions), average(thirds));
    }

    private Statistics statistics(List<ExperimentRow> rows, ToDoubleFunction<ExperimentRow> value) {
        return Statistics.of(rows.stream().mapToDouble(value).toArray());
    }

    private OptionalDouble average(double[] values) { return Arrays.stream(values).average(); }
}
