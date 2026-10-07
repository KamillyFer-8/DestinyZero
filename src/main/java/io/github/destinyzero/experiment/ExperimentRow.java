package io.github.destinyzero.experiment;

import io.github.destinyzero.domain.RouteResult;

/** One logical strategy/scenario execution; timing repetitions are not extra CSV rows. */
public record ExperimentRow(String scenarioId, long seed, int datasetSize, RouteResult result) { }
