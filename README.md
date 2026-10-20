# TeaVM Showcase

This Maven reactor contains standalone TeaVM examples. Each example lives in its own module, with its own source, tests, browser assets, and generated output.

## Modules

- [`teavm-showcase-number-guessing`](teavm-showcase-number-guessing/README.md) — a Java number-guessing game compiled to browser JavaScript.
- [`teavm-showcase-currency-converter`](teavm-showcase-currency-converter/README.md) — a TeaVM currency explorer with an asynchronous Spring Boot API and a Chart.js demo-rate history chart.
- [`teavm-showcase-drawing-app`](teavm-showcase-drawing-app/README.md) — a Java drawing editor using TeaVM and the browser Canvas 2D API.
- [`teavm-showcase-platform-game`](teavm-showcase-platform-game/README.md) — a single-screen Java platform game compiled to browser JavaScript with TeaVM.

## Build

Build and test every module from the repository root:

```sh
mvn test
mvn package
```

To work with the number guessing module alone, use `mvn -pl teavm-showcase-number-guessing test` or `mvn -pl teavm-showcase-number-guessing package`. Its generated browser application is in `teavm-showcase-number-guessing/target/javascript/`; see the [module README](teavm-showcase-number-guessing/README.md) for local run instructions.

Build and test all examples with `mvn clean verify`. To build just the currency explorer, run `mvn -pl teavm-showcase-currency-converter clean verify`. Start its same-origin browser app with `java -jar teavm-showcase-currency-converter/target/teavm-showcase-currency-converter-1.0-SNAPSHOT.jar` and open <http://localhost:8080/>. See the [module README](teavm-showcase-currency-converter/README.md) for API examples and implementation details.

Build and run the drawing editor with `mvn -f teavm-showcase-drawing-app/pom.xml clean process-classes exec:java`, then open <http://localhost:8000/>. The Java server serves the TeaVM output directly; see the [drawing app README](teavm-showcase-drawing-app/README.md) for details.

Build and run the platform game with `mvn -f teavm-showcase-platform-game/pom.xml clean process-classes exec:java`, then open <http://localhost:8001/>. See the [platform game README](teavm-showcase-platform-game/README.md) for controls and implementation notes.
