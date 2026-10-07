package io.github.destinyzero.report;

import io.github.destinyzero.experiment.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class ResultsWriter {
    public void write(ExperimentRun run, Path directory) throws IOException {
        Files.createDirectories(directory);
        var raw = new StringBuilder("scenario_id,seed,dataset_size,strategy,total_distance,execution_time_ns,difference_from_baseline,percentage_difference,high_priority_count,average_high_priority_position,high_priority_first_third_percentage\n");
        for (var row : run.rows()) {
            var r = row.result();
            var p = r.priorityMetrics();
            raw.append(csv(row.scenarioId(), row.seed(), row.datasetSize(), r.strategyName(), r.totalDistance(),
                    r.executionTimeNanos(), r.differenceFromBaseline(), optional(r.percentageDifference()),
                    p.highPriorityCount(), optional(p.averagePosition()), optional(p.firstThirdPercentage())));
        }
        var summary = new StringBuilder("strategy,dataset_size,scenario_count,mean_distance,median_distance,min_distance,max_distance,mean_difference,mean_percentage_difference,percentage_scenario_count,better,equal,worse,win_percentage,mean_execution_time_ns,median_execution_time_ns,priority_scenario_count,mean_high_priority_position,mean_high_priority_first_third_percentage\n");
        for (var s : run.summary()) {
            summary.append(csv(s.strategy(), s.datasetSize() == 0 ? "ALL" : s.datasetSize(), s.scenarioCount(),
                    s.distance().mean(), s.distance().median(), s.distance().min(), s.distance().max(),
                    s.meanDifference(), optional(s.meanPercentageDifference()), s.percentageScenarioCount(),
                    s.better(), s.equal(), s.worse(), s.winPercentage(), s.executionTimeNanos().mean(),
                    s.executionTimeNanos().median(), s.priorityScenarioCount(), optional(s.meanHighPriorityPosition()),
                    optional(s.meanHighPriorityFirstThirdPercentage())));
        }
        Files.writeString(directory.resolve("raw-results.csv"), raw, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        Files.writeString(directory.resolve("summary.csv"), summary, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        Files.writeString(directory.resolve("experiment-report.md"), markdown(run), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    public String markdown(ExperimentRun run) {
        var config = run.config();
        var text = new StringBuilder("# DestinyZero — Experiment Report\n\n");
        text.append("Scenarios analyzed: ").append(config.scenarioCount()).append("\n\nRoute executions: ")
                .append(run.rows().size()).append("\n\nDataset sizes: ").append(config.sizes())
                .append("\n\nSeeds: ").append(config.seeds()).append("\n\n")
                .append("Generated deterministically by Java from all configured scenarios.\n\n")
                .append("## Methodology\n\nUniform integer coordinates [0, 1000] and equally probable LOW/NORMAL/HIGH categories. ")
                .append("java.util.Random(seed); draw order per delivery: x, y, priority. Origin (0, 0), open route, Euclidean distance in simulation units. ")
                .append("Same input for every strategy. Warm-up calls per strategy/scenario: ").append(config.warmups())
                .append("; timed repetitions: ").append(config.repetitions())
                .append(". Each raw timing is the median of planning calls, including internal copies and validation. ")
                .append("Even-count timing medians are truncated to whole nanoseconds. Dataset generation, output validation, metric calculation and file I/O are excluded. ")
                .append("Strategies run in registered order. JVM, GC and order effects remain; this is not a scientific benchmark.\n\n")
                .append("Runtime: Java ").append(System.getProperty("java.version"))
                .append("; VM: ").append(System.getProperty("java.vm.name"))
                .append("; OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.arch"))
                .append("; processors available: ").append(Runtime.getRuntime().availableProcessors()).append(".\n\n")
                .append("Difference = strategy minus baseline; negative means shorter. Mean percentage is the mean of paired scenario percentages, not a percentage of pooled means. ")
                .append("Undefined percentages (zero baseline) are excluded, with valid counts in summary.csv. ")
                .append("Ties use abs(difference) <= 1e-9 * max(1, abs(baseline)).\n\n")
                .append("HIGH positions start at 1; first third uses ceil(n/3). Priority means give each scenario with HIGH deliveries equal weight; ")
                .append("scenarios without HIGH are excluded, with valid counts in summary.csv. ALL pools sizes equally by scenario and must be read alongside size-specific results.\n\n")
                .append("## Distance by strategy and dataset size\n\n")
                .append("| Strategy | Size | Scenarios | Mean | Median | Min | Max | Mean difference | Mean difference % | Better / Equal / Worse | Win % |\n")
                .append("|---|---:|---:|---:|---:|---:|---:|---:|---:|---|---:|\n");
        for (var s : run.summary()) {
            text.append("| ").append(s.strategy()).append(" | ").append(size(s)).append(" | ").append(s.scenarioCount())
                    .append(" | ").append(number(s.distance().mean())).append(" | ").append(number(s.distance().median()))
                    .append(" | ").append(number(s.distance().min())).append(" | ").append(number(s.distance().max()))
                    .append(" | ").append(number(s.meanDifference())).append(" | ").append(display(s.meanPercentageDifference()))
                    .append(" | ").append(s.better()).append(" / ").append(s.equal()).append(" / ").append(s.worse())
                    .append(" | ").append(number(s.winPercentage())).append(" |\n");
        }
        text.append("\n## Performance and priority\n\n")
                .append("| Strategy | Size | Mean time ns | Median time ns | Scenarios with HIGH | Mean HIGH position | Mean HIGH in first third % |\n")
                .append("|---|---:|---:|---:|---:|---:|---:|\n");
        for (var s : run.summary()) {
            text.append("| ").append(s.strategy()).append(" | ").append(size(s))
                    .append(" | ").append(number(s.executionTimeNanos().mean())).append(" | ").append(number(s.executionTimeNanos().median()))
                    .append(" | ").append(s.priorityScenarioCount()).append(" | ").append(display(s.meanHighPriorityPosition()))
                    .append(" | ").append(display(s.meanHighPriorityFirstThirdPercentage())).append(" |\n");
        }
        return text.append("\n## Limitations\n\nNearest Neighbor is a greedy heuristic without global optimality guarantees. Priority advances urgent deliveries; shortest distance is not its only goal. ")
                .append("Fictional coordinates omit streets, traffic and road restrictions. Results do not establish real logistics savings. ")
                .append("Routes and priority metrics reproduce with the same seeds/configuration; timings vary by hardware and environment.\n\n")
                .append("## Evidence\n\n[Individual executions](raw-results.csv) → [Java aggregates](summary.csv) → this deterministic report. ")
                .append("All metrics and tables are generated by the Java application.\n").toString();
    }

    private String size(SummaryRow row) { return row.datasetSize() == 0 ? "ALL" : Integer.toString(row.datasetSize()); }
    private String optional(OptionalDouble value) { return value.isPresent() ? Double.toString(value.getAsDouble()) : ""; }
    private String display(OptionalDouble value) { return value.isPresent() ? number(value.getAsDouble()) : "N/A"; }
    private String number(double value) { return String.format(Locale.ROOT, "%.4f", value); }
    private String csv(Object... fields) {
        var values = new ArrayList<String>();
        for (Object field : fields) {
            String value = field.toString();
            values.add("\"" + value.replace("\"", "\"\"") + "\"");
        }
        return String.join(",", values) + "\n";
    }
}
