package io.github.destinyzero.experiment;

import java.util.HashSet;
import java.util.List;
import java.util.stream.LongStream;

public record ExperimentConfig(List<Integer> sizes, List<Long> seeds, int warmups, int repetitions) {
    public ExperimentConfig {
        sizes = List.copyOf(sizes);
        seeds = List.copyOf(seeds);
        if (sizes.isEmpty() || seeds.isEmpty() || sizes.stream().anyMatch(n -> n < 1)
                || new HashSet<>(sizes).size() != sizes.size() || new HashSet<>(seeds).size() != seeds.size()
                || warmups < 0 || repetitions < 1) {
            throw new IllegalArgumentException("Nonempty unique sizes/seeds and valid timing settings required");
        }
    }

    public static ExperimentConfig full() {
        return new ExperimentConfig(List.of(10, 25, 50, 100, 250),
                LongStream.rangeClosed(1, 20).boxed().toList(), 5, 11);
    }

    public static ExperimentConfig demo() {
        return new ExperimentConfig(List.of(10, 25), List.of(1L, 2L), 2, 3);
    }

    public int scenarioCount() { return Math.multiplyExact(sizes.size(), seeds.size()); }
}
