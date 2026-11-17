package io.powerscale.ranking.rating;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingCalculatorTest {

    private final RatingCalculator calculator = new RatingCalculator();

    @Test
    void equalRatingsLowDiffAwardsHalfTheKFactor() {
        RatingCalculator.RatingUpdate update = calculator.calculate(1500.0, 1500.0, Difficulty.LOW);

        assertEquals(16.0, update.winnerDelta(), 0.0001);
        assertEquals(-16.0, update.loserDelta(), 0.0001);
    }

    @Test
    void difficultyScalesTheRatingAdjustment() {
        RatingCalculator.RatingUpdate mid = calculator.calculate(1500.0, 1500.0, Difficulty.MID);
        RatingCalculator.RatingUpdate high = calculator.calculate(1500.0, 1500.0, Difficulty.HIGH);

        assertEquals(12.0, mid.winnerDelta(), 0.0001);
        assertEquals(8.0, high.winnerDelta(), 0.0001);
    }

    @Test
    void higherRatedWinnerGainsLessForTheSameWin() {
        RatingCalculator.RatingUpdate update = calculator.calculate(1700.0, 1500.0, Difficulty.LOW);

        assertTrue(update.winnerDelta() > 0);
        assertTrue(update.winnerDelta() < 16.0);
    }

    @Test
    void upsetWinAwardsMoreThanASymmetricMatchup() {
        RatingCalculator.RatingUpdate update = calculator.calculate(1500.0, 1700.0, Difficulty.LOW);

        assertTrue(update.winnerDelta() > 16.0);
    }

    @Test
    void ratingChangesAreZeroSum() {
        RatingCalculator.RatingUpdate update = calculator.calculate(1620.5, 1480.0, Difficulty.MID);

        assertEquals(0.0, update.winnerDelta() + update.loserDelta(), 0.0001);
    }
}
