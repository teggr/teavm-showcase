package io.github.teggr.teavmshowcase.drawing;

import org.teavm.jso.JSBody;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLCanvasElement;

final class CanvasInterop {
    private CanvasInterop() {
    }

    @JSBody(params = { "canvas", "event" }, script = """
            const bounds = canvas.getBoundingClientRect();
            return (event.clientX - bounds.left) * canvas.width / bounds.width;
            """)
    static native double canvasX(HTMLCanvasElement canvas, Event event);

    @JSBody(params = { "canvas", "event" }, script = """
            const bounds = canvas.getBoundingClientRect();
            return (event.clientY - bounds.top) * canvas.height / bounds.height;
            """)
    static native double canvasY(HTMLCanvasElement canvas, Event event);

    @JSBody(params = { "canvas", "event" }, script = """
            canvas.setPointerCapture(event.pointerId);
            """)
    static native void capturePointer(HTMLCanvasElement canvas, Event event);
}
