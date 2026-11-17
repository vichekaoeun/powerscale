package io.powerscale.ranking.rating;

import io.powerscale.events.RankingFormula;
import org.springframework.stereotype.Component;

@Component
public class RatingCalculator {

    public static final double BASE_RATING = 1500.0;
    public static final double K_FACTOR = 32.0;

    public RatingUpdate calculate(double winnerRating, double loserRating, Difficulty difficulty) {
        return calculate(RankingFormula.eloDefault(), winnerRating, loserRating, difficulty);
    }

    public RatingUpdate calculate(
            RankingFormula formula,
            double winnerRating,
            double loserRating,
            Difficulty difficulty
    ) {
        if (formula.isPoints()) {
            return new RatingUpdate(formula.pointsFor(difficulty.name()), 0.0);
        }

        double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (loserRating - winnerRating) / 400.0));
        double delta = formula.kFactor() * formula.weightFor(difficulty.name()) * (1.0 - expectedWinner);
        return new RatingUpdate(delta, -delta);
    }

    public record RatingUpdate(double winnerDelta, double loserDelta) {
    }
}
