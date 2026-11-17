package io.powerscale.events;

public record RankingFormula(
        String method,
        double baseRating,
        double kFactor,
        double lowWeight,
        double midWeight,
        double highWeight,
        double lowPoints,
        double midPoints,
        double highPoints
) {
    public static RankingFormula eloDefault() {
        return new RankingFormula("ELO", 1500.0, 32.0, 1.0, 0.75, 0.5, 3.0, 2.0, 1.0);
    }

    public static RankingFormula pointsDefault() {
        return new RankingFormula("POINTS", 1500.0, 32.0, 1.0, 0.75, 0.5, 3.0, 2.0, 1.0);
    }

    public boolean isPoints() {
        return "POINTS".equalsIgnoreCase(method);
    }

    public double weightFor(String difficulty) {
        return switch (difficulty) {
            case "LOW" -> lowWeight;
            case "MID" -> midWeight;
            case "HIGH" -> highWeight;
            default -> 1.0;
        };
    }

    public double pointsFor(String difficulty) {
        return switch (difficulty) {
            case "LOW" -> lowPoints;
            case "MID" -> midPoints;
            case "HIGH" -> highPoints;
            default -> 0.0;
        };
    }
}
