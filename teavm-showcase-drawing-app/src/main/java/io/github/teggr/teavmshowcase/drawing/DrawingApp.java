package io.github.teggr.teavmshowcase.drawing;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class DrawingApp {
    private static final String[] COLORS = { "#1689ef", "#ff4b26", "#168364", "#ffd92f", "#f45bc7" };

    private final HTMLCanvasElement canvas;
    private final DrawingController controller = new DrawingController(new DrawingModel());
    private final DrawingRenderer renderer;
    private final HTMLElement status;
    private final HTMLElement[] toolButtons = new HTMLElement[DrawingTool.values().length];

    private DrawingApp() {
        HTMLDocument document = HTMLDocument.current();
        canvas = requireCanvas(document, "drawing-canvas");
        CanvasRenderingContext2D context = (CanvasRenderingContext2D) canvas.getContext("2d");
        if (context == null) {
            throw new IllegalStateException("The browser could not create a 2D canvas context.");
        }
        renderer = new DrawingRenderer(canvas, context);
        status = requireElement(document, "status");

        registerTool(document, DrawingTool.SELECT, "tool-select");
        registerTool(document, DrawingTool.RECTANGLE, "tool-rectangle");
        registerTool(document, DrawingTool.ELLIPSE, "tool-ellipse");
        registerTool(document, DrawingTool.ARROW, "tool-arrow");
        registerColor(document, "color-blue", COLORS[0]);
        registerColor(document, "color-red", COLORS[1]);
        registerColor(document, "color-green", COLORS[2]);
        registerColor(document, "color-amber", COLORS[3]);
        registerColor(document, "color-purple", COLORS[4]);
        registerPointerEvents();
        updateToolButtons();
        renderer.render(controller);
    }

    public static void main(String[] args) {
        new DrawingApp();
    }

    private static HTMLElement requireElement(HTMLDocument document, String id) {
        HTMLElement element = document.getElementById(id);
        if (element == null) {
            throw new IllegalStateException("Required page element is missing: " + id);
        }
        return element;
    }

    private static HTMLCanvasElement requireCanvas(HTMLDocument document, String id) {
        HTMLElement element = requireElement(document, id);
        if (!(element instanceof HTMLCanvasElement)) {
            throw new IllegalStateException("Page element must be a canvas: " + id);
        }
        return (HTMLCanvasElement) element;
    }

    private void registerTool(HTMLDocument document, DrawingTool tool, String id) {
        HTMLElement button = requireElement(document, id);
        toolButtons[tool.ordinal()] = button;
        button.addEventListener("click", event -> {
            controller.setTool(tool);
            status.setTextContent(tool == DrawingTool.SELECT
                    ? "Select a shape to move it, or choose a drawing tool."
                    : tool.name().charAt(0) + tool.name().substring(1).toLowerCase() + " tool selected.");
            updateToolButtons();
            renderer.render(controller);
        });
    }

    private void registerColor(HTMLDocument document, String id, String color) {
        requireElement(document, id).addEventListener("click", event -> {
            if (controller.recolorSelected(color)) {
                status.setTextContent("Selected shape recoloured.");
                renderer.render(controller);
            } else {
                status.setTextContent("Select a shape before choosing a colour.");
            }
        });
    }

    private void registerPointerEvents() {
        EventListener<Event> down = event -> {
            event.preventDefault();
            CanvasInterop.capturePointer(canvas, event);
            controller.pointerDown(CanvasInterop.canvasX(canvas, event), CanvasInterop.canvasY(canvas, event));
            renderer.render(controller);
        };
        EventListener<Event> move = event -> {
            controller.pointerMove(CanvasInterop.canvasX(canvas, event), CanvasInterop.canvasY(canvas, event));
            renderer.render(controller);
        };
        EventListener<Event> up = event -> {
            controller.pointerUp(CanvasInterop.canvasX(canvas, event), CanvasInterop.canvasY(canvas, event));
            renderer.render(controller);
        };
        EventListener<Event> cancel = event -> {
            controller.pointerCancel();
            renderer.render(controller);
        };
        canvas.addEventListener("pointerdown", down);
        canvas.addEventListener("pointermove", move);
        canvas.addEventListener("pointerup", up);
        canvas.addEventListener("pointercancel", cancel);
    }

    private void updateToolButtons() {
        for (DrawingTool tool : DrawingTool.values()) {
            HTMLElement button = toolButtons[tool.ordinal()];
            button.setAttribute("aria-pressed", Boolean.toString(controller.getTool() == tool));
        }
    }
}
