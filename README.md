# TeaVM Showcase

This Maven reactor contains standalone TeaVM examples. Each example lives in its own module, with its own source, tests, browser assets, and generated output.

## Modules

- [`teavm-showcase-number-guessing`](teavm-showcase-number-guessing/README.md) — a Java number-guessing game compiled to browser JavaScript.

## Build

Build and test every module from the repository root:

```sh
mvn test
mvn package
```

To work with the number guessing module alone, use `mvn -pl teavm-showcase-number-guessing test` or `mvn -pl teavm-showcase-number-guessing package`. Its generated browser application is in `teavm-showcase-number-guessing/target/javascript/`; see the [module README](teavm-showcase-number-guessing/README.md) for local run instructions.
