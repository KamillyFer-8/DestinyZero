package io.github.destinyzero;

import io.github.destinyzero.experiment.*;
import io.github.destinyzero.report.ResultsWriter;
import io.github.destinyzero.service.RouteComparisonService;
import io.github.destinyzero.strategy.*;
import java.nio.file.Path;
import java.util.List;

public final class DestinyZero {
    private DestinyZero() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !(args[0].equals("--demo") || args[0].equals("--full"))) {
            throw new IllegalArgumentException("Usage: java ... DestinyZero --demo|--full <fresh-output-directory>");
        }
        var config = args[0].equals("--full") ? ExperimentConfig.full() : ExperimentConfig.demo();
        var service = new RouteComparisonService(List.of(new NearestNeighborStrategy(), new PriorityStrategy()));
        Path output = Path.of(args[1]);
        var run = new ExperimentRunner(service).run(config, output);
        System.out.println(new ResultsWriter().markdown(run));
        System.out.println("Generated files in: " + output.toAbsolutePath());
    }
}
