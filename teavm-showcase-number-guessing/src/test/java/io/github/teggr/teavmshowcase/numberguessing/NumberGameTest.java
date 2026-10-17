package io.github.teggr.teavmshowcase.numberguessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class NumberGameTest {
    @Test
    void returnsFeedbackForGuessesBelowAndAboveTarget() {
        NumberGame game = new NumberGame(() -> 5);

        assertEquals(NumberGame.GuessResult.TOO_LOW, game.submitGuess(2));
        assertEquals(NumberGame.GuessResult.TOO_HIGH, game.submitGuess(8));
        assertEquals(2, game.getGuessCount());
    }

    @Test
    void countsCorrectGuessAndCompletesGame() {
        NumberGame game = new NumberGame(() -> 5);

        assertEquals(NumberGame.GuessResult.CORRECT, game.submitGuess(5));
        assertEquals(1, game.getGuessCount());
        assertTrue(game.isComplete());
    }

    @Test
    void restartResetsCountAndCompletionAndUsesNextTarget() {
        Iterator<Integer> targets = List.of(4, 9).iterator();
        NumberGame game = new NumberGame(targets::next);
        game.submitGuess(4);

        game.startNewGame();

        assertEquals(0, game.getGuessCount());
        assertFalse(game.isComplete());
        assertEquals(NumberGame.GuessResult.TOO_LOW, game.submitGuess(4));
    }

    @Test
    void rejectsGuessesAfterWinningWithoutChangingCount() {
        NumberGame game = new NumberGame(() -> 5);
        game.submitGuess(5);

        assertEquals(NumberGame.GuessResult.GAME_OVER, game.submitGuess(6));
        assertEquals(1, game.getGuessCount());
    }

    @Test
    void rejectsOutOfRangeGuessesWithoutCountingThem() {
        NumberGame game = new NumberGame(() -> 5);

        assertEquals(NumberGame.GuessResult.INVALID, game.submitGuess(0));
        assertEquals(NumberGame.GuessResult.INVALID, game.submitGuess(11));
        assertEquals(0, game.getGuessCount());
        assertEquals(NumberGame.GuessResult.CORRECT, game.submitGuess(5));
        assertEquals(1, game.getGuessCount());
    }

    @Test
    void acceptsBothRangeBoundaries() {
        NumberGame lowTarget = new NumberGame(() -> 1);
        NumberGame highTarget = new NumberGame(() -> 10);

        assertEquals(NumberGame.GuessResult.CORRECT, lowTarget.submitGuess(1));
        assertEquals(NumberGame.GuessResult.CORRECT, highTarget.submitGuess(10));
    }
}
