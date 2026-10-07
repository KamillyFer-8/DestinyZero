package io.github.destinyzero.domain;

import java.util.List;

/** Baseline is always the first result. */
public record ComparisonReport(List<RouteResult> results) {
    public ComparisonReport { results = List.copyOf(results); }
}
