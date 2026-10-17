# TeaVM Showcase: Number Guessing

A small number-guessing game written in Java and compiled to browser JavaScript with TeaVM. The Java game model owns the random target, validation, guess count, completion state and restart behavior; typed TeaVM DOM APIs connect it to the page.

## Build and run

TeaVM **0.16.0** is pinned in the root `pom.xml`, matching the current official [Maven](https://teavm.org/docs/tooling/maven.html) and [JSO](https://teavm.org/docs/runtime/jso.html) documentation. The repository does not include a Maven wrapper, so use an installed Maven 3.9+. Run these commands from the repository root:

```sh
mvn -pl teavm-showcase-number-guessing -am test
mvn -pl teavm-showcase-number-guessing -am package
python -m http.server 8000 --directory teavm-showcase-number-guessing/target/javascript
```

Open <http://localhost:8000/> in a modern browser. Serve the generated directory over HTTP rather than opening the HTML file directly. The complete app is written to `teavm-showcase-number-guessing/target/javascript/`: `index.html`, `styles.css`, the generated `classes.js` and source map, and a one-line `bootstrap.js` that starts TeaVM's exported `main` entry point. The project uses Java 17 bytecode, `teavm-classlib`, and TeaVM's `teavm-jso-apis` browser wrappers.

## Project layout

- `src/main/java/.../NumberGame.java` — browser-independent game rules and state.
- `src/main/java/.../NumberGuessingApp.java` — DOM access, button and Enter handlers.
- `src/main/resources/` — accessible HTML and responsive CSS.
- `src/test/java/.../NumberGameTest.java` — deterministic unit tests for game behavior.
- `docs/article-support.md` — architecture and source/generated-code excerpts.
- `docs/number-guessing-game.png` — browser screenshot.

The article-supporting excerpts are concise entry points into the actual implementation; they are not a literal translation of the Java into JavaScript.
