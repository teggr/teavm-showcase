# TeaVM Showcase: Skybound Platform Game

A small single-screen platform game written in Java and compiled to browser JavaScript with TeaVM. The game uses a Java update loop and physics model, with Canvas 2D for drawing; it has no image assets or game-engine dependency.

## Build and run

Use Java 17 or newer and Maven 3.9 or newer. From the repository root:

```sh
mvn -pl teavm-showcase-platform-game -am clean verify
mvn -f teavm-showcase-platform-game/pom.xml process-classes exec:java
```

Open <http://localhost:8001/> in a modern browser. The generated app is served over HTTP by the module's small Java static server. TeaVM writes its output to `teavm-showcase-platform-game/target/javascript/`.

## Play

Use the left/right arrow keys (or A/D) to move and Space, Up or W to jump. On touch screens, use the on-screen controls. Collect all five stars to win; press Play again or R to restart.

## Implementation notes

- The Java game model handles input state, frame-based movement, gravity, one-way platform landings, coin collection and restart.
- The Canvas renderer draws the scene and lightweight animation procedurally, so there are no external assets.
- Unit tests cover screen bounds, jumping, landing, and reset behavior.
