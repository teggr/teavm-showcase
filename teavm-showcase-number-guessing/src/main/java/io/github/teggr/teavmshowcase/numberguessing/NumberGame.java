package io.github.teggr.teavmshowcase.numberguessing;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

public final class NumberGame {
    public enum GuessResult {
        TOO_LOW,
        TOO_HIGH,
        CORRECT,
        INVALID,
        GAME_OVER
    }

    private final IntSupplier targetGenerator;
    private int target;
    private int guessCount;
    private boolean complete;

    public NumberGame() {
        this(() -> ThreadLocalRandom.current().nextInt(1, 11));
    }

    public NumberGame(IntSupplier targetGenerator) {
        this.targetGenerator = targetGenerator;
        startNewGame();
    }

    public GuessResult submitGuess(int guess) {
        if (complete) {
            return GuessResult.GAME_OVER;
        }
        if (guess < 1 || guess > 10) {
            return GuessResult.INVALID;
        }

        guessCount++;
        if (guess < target) {
            return GuessResult.TOO_LOW;
        }
        if (guess > target) {
            return GuessResult.TOO_HIGH;
        }
        complete = true;
        return GuessResult.CORRECT;
    }

    public void startNewGame() {
        int nextTarget = targetGenerator.getAsInt();
        if (nextTarget < 1 || nextTarget > 10) {
            throw new IllegalStateException("Target generator must return a number from 1 to 10.");
        }
        target = nextTarget;
        guessCount = 0;
        complete = false;
    }

    public int getGuessCount() {
        return guessCount;
    }

    public boolean isComplete() {
        return complete;
    }
}
