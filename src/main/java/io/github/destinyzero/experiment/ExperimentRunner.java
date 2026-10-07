package io.github.destinyzero.experiment;

import io.github.destinyzero.dataset.DatasetGenerator;
import io.github.destinyzero.report.*;
import io.github.destinyzero.service.RouteComparisonService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;

public final class ExperimentRunner {
    private final RouteComparisonService comparison;

    public ExperimentRunner(RouteComparisonService comparison) {
        this.comparison = Objects.requireNonNull(comparison);
    }

    public ExperimentRun run(ExperimentConfig config, Path output) throws IOException {
        Files.createDirectories(output);
        // Reject old artifacts to prevent confusing failed runs with previous evidence.
        for (String file : new String[]{"raw-results.csv", "summary.csv", "experiment-report.md", "failure.txt"}) {
            if (Files.exists(output.resolve(file))) throw new IOException("Output already contains " + file + "; choose a fresh directory");
        }
        var rows = new ArrayList<ExperimentRow>();
        var generator = new DatasetGenerator();
        String current = "initialization";
        try {
            for (int size : config.sizes()) {
                for (long seed : config.seeds()) {
                    current = "n" + size + "-seed" + seed;
                    var scenario = generator.generate(size, seed);
                    var report = comparison.compare(scenario.origin(), scenario.deliveries(), config.warmups(), config.repetitions());
                    for (var result : report.results()) {
                        rows.add(new ExperimentRow(scenario.id(), seed, size, result));
                    }
                }
            }
            var run = new ExperimentRun(config, rows, new ResultAggregator().aggregate(rows));
            new ResultsWriter().write(run, output);
            return run;
        } catch (RuntimeException | IOException failure) {
            Files.writeString(output.resolve("failure.txt"), "Experiment failed at " + current
                    + "\nCompleted logical executions: " + rows.size() + "\n"
                    + failure.getClass().getSimpleName() + ": " + failure.getMessage() + "\n",
                    StandardCharsets.UTF_8);
            throw failure;
        }
    }
}
