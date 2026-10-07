package io.github.destinyzero.experiment;

import io.github.destinyzero.dataset.DatasetGenerator;
import io.github.destinyzero.domain.*;
import io.github.destinyzero.report.*;
import io.github.destinyzero.service.RouteComparisonService;
import io.github.destinyzero.strategy.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ExperimentTest {
    @TempDir Path temporary;

    private RouteComparisonService service() {
        return new RouteComparisonService(List.of(new NearestNeighborStrategy(), new PriorityStrategy()));
    }

    @Test void generationIsReproducibleAndUsesDocumentedDrawOrder() {
        var generator = new DatasetGenerator();
        var first = generator.generate(10, 1);
        assertEquals(first, generator.generate(10, 1));
        assertNotEquals(first.deliveries(), generator.generate(10, 2).deliveries());
        assertEquals(first.deliveries(), generator.generate(25, 1).deliveries().subList(0, 10));
        var random = new Random(1);
        assertEquals(random.nextInt(1001), first.deliveries().getFirst().destination().x());
        assertEquals(random.nextInt(1001), first.deliveries().getFirst().destination().y());
        Priority expected = switch (random.nextInt(3)) {
            case 0 -> Priority.LOW;
            case 1 -> Priority.NORMAL;
            default -> Priority.HIGH;
        };
        assertEquals(expected, first.deliveries().getFirst().priority());
        assertEquals(10, new HashSet<>(first.deliveries().stream().map(Delivery::id).toList()).size());
        assertThrows(UnsupportedOperationException.class, () -> first.deliveries().clear());
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0, 1));
    }

    @Test void configurationFixesFullExperimentBeforeResultsAreKnown() {
        var config = ExperimentConfig.full();
        assertEquals(List.of(10, 25, 50, 100, 250), config.sizes());
        assertEquals(20, config.seeds().size());
        assertEquals(1L, config.seeds().getFirst());
        assertEquals(20L, config.seeds().getLast());
        assertEquals(100, config.scenarioCount());
        assertThrows(IllegalArgumentException.class, () -> new ExperimentConfig(List.of(10, 10), List.of(1L), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExperimentConfig(List.of(10), List.of(1L, 1L), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExperimentConfig(List.of(), List.of(1L), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExperimentConfig(List.of(10), List.of(1L), 0, 0));
    }

    @Test void computesStatisticsForOddEvenAndUnsortedValues() {
        assertEquals(new Statistics(4, 3, 1, 9), Statistics.of(new double[]{9, 1, 4, 2}));
        assertEquals(new Statistics(2, 2, 1, 3), Statistics.of(new double[]{3, 1, 2}));
        assertThrows(IllegalArgumentException.class, () -> Statistics.of(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> Statistics.of(new double[]{Double.NaN}));
    }

    @Test void aggregatesPairedPercentagesAndExcludesMissingPriorityMetrics() {
        var route = new Route(new Location("o", "O", 0, 0), List.of());
        var absent = new PriorityMetrics(0, OptionalDouble.empty(), OptionalDouble.empty());
        var high = new PriorityMetrics(1, OptionalDouble.of(1), OptionalDouble.of(100));
        var rows = List.of(
                row("a", route, 10, -5, -100.0 / 3, 100, high),
                row("b", route, 20, 0, 0, 300, absent),
                row("c", route, 30, 5, 20, 200, high));
        var summary = new ResultAggregator().aggregate(rows).getFirst();
        assertEquals(new Statistics(20, 20, 10, 30), summary.distance());
        assertEquals(0, summary.meanDifference());
        assertEquals((-100.0 / 3 + 20) / 3, summary.meanPercentageDifference().orElseThrow(), 1e-10);
        assertEquals(1, summary.better());
        assertEquals(1, summary.equal());
        assertEquals(1, summary.worse());
        assertEquals(100.0 / 3, summary.winPercentage(), 1e-10);
        assertEquals(200, summary.executionTimeNanos().median());
        assertEquals(2, summary.priorityScenarioCount());
        assertEquals(1, summary.meanHighPriorityPosition().orElseThrow());
        assertEquals(100, summary.meanHighPriorityFirstThirdPercentage().orElseThrow());
    }

    private ExperimentRow row(String id, Route route, double distance, double difference,
                              double percentage, long time, PriorityMetrics priority) {
        return new ExperimentRow(id, 1, 10, new RouteResult("TEST", route, distance, time,
                difference, OptionalDouble.of(percentage), priority));
    }

    @Test void exportsAllSmallExperimentRowsAndReproducibleNonTimingMetrics() throws IOException {
        var config = new ExperimentConfig(List.of(3, 4), List.of(1L, 2L), 1, 3);
        var runner = new ExperimentRunner(service());
        var output = temporary.resolve("first");
        var run = runner.run(config, output);
        var second = runner.run(config, temporary.resolve("second"));
        assertEquals(12, run.rows().size());
        assertEquals(9, run.summary().size());
        for (int i = 0; i < run.rows().size(); i++) {
            var a = run.rows().get(i);
            var b = second.rows().get(i);
            assertEquals(a.scenarioId(), b.scenarioId());
            assertEquals(a.result().route(), b.result().route());
            assertEquals(a.result().totalDistance(), b.result().totalDistance());
            assertEquals(a.result().percentageDifference(), b.result().percentageDifference());
            assertEquals(a.result().priorityMetrics(), b.result().priorityMetrics());
        }
        var raw = Files.readAllLines(output.resolve("raw-results.csv"));
        assertEquals(13, raw.size());
        for (int i = 0; i < run.rows().size(); i++) {
            String[] fields = raw.get(i + 1).replace("\"", "").split(",", -1);
            assertEquals(11, fields.length);
            var row = run.rows().get(i);
            assertEquals(row.scenarioId(), fields[0]);
            assertEquals(row.result().strategyName(), fields[3]);
            assertEquals(row.result().totalDistance(), Double.parseDouble(fields[4]));
            assertEquals(row.result().executionTimeNanos(), Long.parseLong(fields[5]));
            assertEquals(row.result().differenceFromBaseline(), Double.parseDouble(fields[6]));
        }
        var csv = Files.readAllLines(output.resolve("summary.csv"));
        assertEquals(10, csv.size());
        for (int i = 0; i < run.summary().size(); i++) {
            String[] fields = csv.get(i + 1).replace("\"", "").split(",", -1);
            assertEquals(19, fields.length);
            var summary = run.summary().get(i);
            assertEquals(summary.distance().mean(), Double.parseDouble(fields[3]));
            assertEquals(summary.distance().median(), Double.parseDouble(fields[4]));
            assertEquals(summary.better() + summary.equal() + summary.worse(), Integer.parseInt(fields[2]));
        }
        var report = Files.readString(output.resolve("experiment-report.md"));
        assertEquals(new ResultsWriter().markdown(run), report);
        assertTrue(report.contains("Scenarios analyzed: 4"));
        assertTrue(report.contains("Route executions: 12"));
        assertTrue(report.contains("without global optimality guarantees"));
        assertThrows(IOException.class, () -> runner.run(config, output));
    }

    @Test void optionalValuesAreExportedAsEmptyFields() throws IOException {
        var route = new Route(new Location("o", "O", 0, 0), List.of());
        var result = service().compare(route.origin(), route.orderedDeliveries()).results().getFirst();
        var rows = List.of(new ExperimentRow("empty", 1, 0, result));
        var run = new ExperimentRun(new ExperimentConfig(List.of(1), List.of(1L), 0, 1), rows,
                new ResultAggregator().aggregate(rows));
        new ResultsWriter().write(run, temporary);
        String[] fields = Files.readAllLines(temporary.resolve("raw-results.csv")).get(1).replace("\"", "").split(",", -1);
        assertEquals("", fields[7]);
        assertEquals("", fields[9]);
        assertEquals("", fields[10]);
        assertTrue(run.summary().getFirst().meanPercentageDifference().isEmpty());
    }

    @Test void failuresAreRecordedAndNotSilentlyExcluded() throws IOException {
        var invalid = new RouteStrategy() {
            public String name() { return "BROKEN"; }
            public Route plan(Location origin, List<Delivery> deliveries) { throw new IllegalStateException("test failure"); }
        };
        var runner = new ExperimentRunner(new RouteComparisonService(List.of(invalid)));
        assertThrows(IllegalStateException.class, () -> runner.run(new ExperimentConfig(List.of(3), List.of(1L), 0, 1), temporary));
        assertTrue(Files.readString(temporary.resolve("failure.txt")).contains("n3-seed1"));
        assertFalse(Files.exists(temporary.resolve("raw-results.csv")));
    }

    @Test void repeatedPlanningCallsAreValidatedAndCounted() {
        var calls = new int[]{0};
        var strategy = new RouteStrategy() {
            public String name() { return "COUNTED"; }
            public Route plan(Location origin, List<Delivery> deliveries) {
                calls[0]++;
                return new Route(origin, deliveries);
            }
        };
        new RouteComparisonService(List.of(strategy)).compare(new Location("o", "O", 0, 0), List.of(), 2, 4);
        assertEquals(6, calls[0]);
        assertThrows(IllegalArgumentException.class, () -> service().compare(new Location("o", "O", 0, 0), List.of(), -1, 1));
        assertThrows(IllegalArgumentException.class, () -> service().compare(new Location("o", "O", 0, 0), List.of(), 0, 0));
    }
}
