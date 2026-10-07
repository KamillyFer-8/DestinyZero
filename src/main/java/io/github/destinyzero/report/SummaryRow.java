package io.github.destinyzero.report;

import java.util.OptionalDouble;

/** datasetSize=0 indicates pooling all sizes; priority means give each valid scenario equal weight. */
public record SummaryRow(String strategy, int datasetSize, int scenarioCount,
                         Statistics distance, double meanDifference, OptionalDouble meanPercentageDifference,
                         int percentageScenarioCount, int better, int equal, int worse,
                         double winPercentage, Statistics executionTimeNanos,
                         int priorityScenarioCount, OptionalDouble meanHighPriorityPosition,
                         OptionalDouble meanHighPriorityFirstThirdPercentage) { }
