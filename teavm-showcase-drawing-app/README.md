# TeaVM Showcase: Drawing App

A small drawing editor written in Java and compiled to browser JavaScript with TeaVM. The Java model owns shape geometry, ordering, selection and movement; TeaVM's typed JSO APIs connect it to the DOM and Canvas 2D context.

## Build and run

Use Java 17 or newer and Maven 3.9 or newer. The repository does not include a Maven wrapper. From the repository root, run:

```sh
mvn -pl teavm-showcase-drawing-app -am clean verify
mvn -f teavm-showcase-drawing-app/pom.xml process-classes exec:java
```

The second command compiles the TeaVM app and starts its Java static server. Open <http://localhost:8000/> in a modern browser; stop the server with Ctrl+C. Serve the generated output over HTTP; do not open the HTML file directly. The TeaVM compiler writes `classes.js`, its source map, `index.html`, `styles.css` and `bootstrap.js` to `teavm-showcase-drawing-app/target/javascript/`.

## Using the editor

Choose Select to pick and move the topmost shape. Choose Rectangle, Ellipse or Arrow, then drag on the canvas to draw. Dragging in either direction is supported. Select a shape and choose a palette swatch to recolour it. Clicking a swatch without a selection only displays a status message.

## Implementation notes

- Shapes are Java objects holding their start and end points and a fixed-palette colour. Rectangles and ellipses use normalized bounds; arrows retain their directed start and end points.
- The model redraws every shape on each interaction, so moving a shape does not leave stale pixels.
- Pointer capture keeps a drag active when the pointer leaves the canvas. Coordinates are scaled from the displayed canvas bounds to the canvas's internal size.
- The app is a learning example: it has no persistence, undo/redo, resize handles, export or advanced path hit testing.
- Unit tests cover model geometry, hit testing, ordering, recolouring and controller gestures. Browser verification evidence is recorded in [`docs/article-support.md`](docs/article-support.md).
