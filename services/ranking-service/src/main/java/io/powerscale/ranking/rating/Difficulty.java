package io.powerscale.ranking.rating;

public enum Difficulty {
    LOW(1.0),
    MID(0.75),
    HIGH(0.5);

    private final double weight;

    Difficulty(double weight) {
        this.weight = weight;
    }

    public double weight() {
        return weight;
    }
}
