package io.github.destinyzero.experiment;

import io.github.destinyzero.report.SummaryRow;
import java.util.List;

public record ExperimentRun(ExperimentConfig config, List<ExperimentRow> rows, List<SummaryRow> summary) {
    public ExperimentRun {
        rows = List.copyOf(rows);
        summary = List.copyOf(summary);
    }
}
