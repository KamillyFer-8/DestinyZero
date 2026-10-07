package io.github.destinyzero.domain;

/** A fictional point in a Cartesian plane; coordinates are not road distances. */
public record Location(String id, String name, double x, double y) {
    public Location {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Location id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be blank");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }
}
