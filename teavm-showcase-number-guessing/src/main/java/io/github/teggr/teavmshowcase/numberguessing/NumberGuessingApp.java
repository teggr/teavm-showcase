package io.github.teggr.teavmshowcase.numberguessing;

import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

public final class NumberGuessingApp {
    private final NumberGame game = new NumberGame();
    private final HTMLInputElement guessInput;
    private final HTMLElement feedback;
    private final HTMLElement guessCount;
    private final HTMLElement guessButton;
    private final HTMLElement newGameButton;

    private NumberGuessingApp() {
        HTMLDocument document = HTMLDocument.current();
        guessInput = requireInput(document, "guess");
        feedback = requireElement(document, "feedback");
        guessCount = requireElement(document, "guess-count");
        guessButton = requireElement(document, "guess-button");
        newGameButton = requireElement(document, "new-game-button");

        guessButton.addEventListener("click", event -> submitGuess());
        guessInput.addEventListener("keydown", event -> {
            if ("Enter".equals(((KeyboardEvent) event).getKey())) {
                submitGuess();
            }
        });
        newGameButton.addEventListener("click", event -> startNewGame());
        renderInitialState();
    }

    public static void main(String[] args) {
        new NumberGuessingApp();
    }

    private static HTMLElement requireElement(HTMLDocument document, String id) {
        HTMLElement element = document.getElementById(id);
        if (element == null) {
            throw new IllegalStateException("Required page element is missing: " + id);
        }
        return element;
    }

    private static HTMLInputElement requireInput(HTMLDocument document, String id) {
        HTMLElement element = requireElement(document, id);
        if (!(element instanceof HTMLInputElement)) {
            throw new IllegalStateException("Page element must be an input: " + id);
        }
        return (HTMLInputElement) element;
    }

    private void renderInitialState() {
        feedback.setTextContent("Make your first guess.");
        updateGuessCount();
        newGameButton.setHidden(true);
    }

    private void submitGuess() {
        if (game.isComplete()) {
            return;
        }

        String value = guessInput.getValue().trim();
        if (value.isEmpty()) {
            showValidationMessage("Enter a whole number from 1 to 10.");
            return;
        }

        final int guess;
        try {
            guess = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            showValidationMessage("Enter a whole number from 1 to 10.");
            return;
        }

        NumberGame.GuessResult result = game.submitGuess(guess);
        switch (result) {
            case INVALID:
                showValidationMessage("Your guess must be between 1 and 10.");
                return;
            case TOO_LOW:
                feedback.setTextContent("Too low. Try a higher number.");
                break;
            case TOO_HIGH:
                feedback.setTextContent("Too high. Try a lower number.");
                break;
            case CORRECT:
                feedback.setTextContent("Correct! You guessed it in " + game.getGuessCount() + " "
                        + (game.getGuessCount() == 1 ? "guess." : "guesses."));
                guessInput.setDisabled(true);
                guessButton.setAttribute("disabled", "");
                newGameButton.setHidden(false);
                break;
            case GAME_OVER:
                return;
            default:
                throw new IllegalStateException("Unexpected guess result: " + result);
        }
        updateGuessCount();
    }

    private void showValidationMessage(String message) {
        feedback.setTextContent(message);
    }

    private void startNewGame() {
        game.startNewGame();
        guessInput.setValue("");
        guessInput.setDisabled(false);
        guessButton.removeAttribute("disabled");
        newGameButton.setHidden(true);
        feedback.setTextContent("New game started. Make a guess from 1 to 10.");
        updateGuessCount();
        guessInput.focus();
    }

    private void updateGuessCount() {
        guessCount.setTextContent(Integer.toString(game.getGuessCount()));
    }
}
