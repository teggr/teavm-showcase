# Article support: TeaVM number guessing

## Architecture

`NumberGame.java` implements the target selection and game rules without browser types. Maven compiles it to JVM bytecode, then the TeaVM Maven plugin compiles that bytecode into `teavm-showcase-number-guessing/target/javascript/classes.js`. The browser loads that JavaScript from `index.html`; the one-line `bootstrap.js` calls TeaVM's exported `main` entry point. The Java entry point uses TeaVM's typed JSO browser wrappers to read and update DOM elements and register browser event handlers.

## Core Java logic

From `src/main/java/io/github/teggr/teavmshowcase/numberguessing/NumberGame.java`:

```java
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
```

The injectable `IntSupplier` constructor makes target selection deterministic in unit tests, while the browser uses the default constructor's random target generator.

## DOM access and event handling

From `src/main/java/io/github/teggr/teavmshowcase/numberguessing/NumberGuessingApp.java`:

```java
HTMLDocument document = HTMLDocument.current();
guessInput = requireInput(document, "guess");
feedback = requireElement(document, "feedback");

guessButton.addEventListener("click", event -> submitGuess());
guessInput.addEventListener("keydown", event -> {
    if ("Enter".equals(((KeyboardEvent) event).getKey())) {
        submitGuess();
    }
});
```

These types come from `org.teavm:teavm-jso-apis:0.16.0`; no handwritten JavaScript or `@JSBody` is used.

## Generated JavaScript extract

This extract is from the actual TeaVM output at `teavm-showcase-number-guessing/target/javascript/classes.js` after `mvn -pl teavm-showcase-number-guessing -am package`. It shows Java's range check and state reset in the generated code; TeaVM output is compiled code, not a line-for-line Java translation.

```javascript
igttn_NumberGame_startNewGame = $this => {
    let $nextTarget;
    $nextTarget = $this.$targetGenerator.$getAsInt();
    if ($nextTarget >= 1 && $nextTarget <= 10) {
        $this.$target = $nextTarget;
        $this.$guessCount = 0;
        $this.$complete = 0;
        return;
    }
    $rt_throw(jl_IllegalStateException__init_0($rt_s(10)));
},
```

## Maven configuration

The TeaVM browser APIs and compiler are pinned together so the plugin and runtime dependencies use the same release:

```xml
<dependency>
    <groupId>org.teavm</groupId>
    <artifactId>teavm-jso-apis</artifactId>
    <version>0.16.0</version>
</dependency>
<plugin>
    <groupId>org.teavm</groupId>
    <artifactId>teavm-maven-plugin</artifactId>
    <version>0.16.0</version>
    <configuration>
        <mainClass>io.github.teggr.teavmshowcase.numberguessing.NumberGuessingApp</mainClass>
        <targetType>JAVASCRIPT</targetType>
        <targetDirectory>${project.build.directory}/javascript</targetDirectory>
        <targetFileName>classes.js</targetFileName>
        <sourceMapsGenerated>true</sourceMapsGenerated>
        <sourceFilesCopied>true</sourceFilesCopied>
    </configuration>
</plugin>
```

The full module configuration, including the Java 17 compiler and test dependencies, is in `teavm-showcase-number-guessing/pom.xml`; shared versions are in the root `pom.xml`.

## Build and browser checks

The root `pom.xml` pins `org.teavm:teavm-maven-plugin` and `org.teavm:teavm-classlib` to **0.16.0**. Run `mvn -pl teavm-showcase-number-guessing -am test` for the deterministic model tests and `mvn -pl teavm-showcase-number-guessing -am package` to compile the browser app. Serve `teavm-showcase-number-guessing/target/javascript/` over HTTP, for example with `python -m http.server 8000 --directory teavm-showcase-number-guessing/target/javascript`.

The game module's `mvn test` passed with all six game-logic tests. Its `mvn package` completed successfully and Chromium verification exercised empty, decimal and out-of-range input, Enter submission, button submission, winning, and restart; no page errors were reported. The screenshot is `teavm-showcase-number-guessing/docs/number-guessing-game.png`.
