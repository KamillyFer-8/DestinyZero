package io.github.destinyzero.domain;

import java.util.OptionalDouble;

/** Positions start at 1; percentages refer to all HIGH deliveries in the route. */
public record PriorityMetrics(int highPriorityCount, OptionalDouble averagePosition,
                              OptionalDouble firstThirdPercentage) { }
