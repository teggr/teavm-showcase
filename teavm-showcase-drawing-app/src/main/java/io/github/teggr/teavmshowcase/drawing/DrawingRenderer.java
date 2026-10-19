package io.github.teggr.teavmshowcase.drawing;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

final class DrawingRenderer {
    private final HTMLCanvasElement canvas;
    private final CanvasRenderingContext2D context;

    DrawingRenderer(HTMLCanvasElement canvas, CanvasRenderingContext2D context) {
        this.canvas = canvas;
        this.context = context;
    }

    void render(DrawingController controller) {
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawingModel model = controller.getModel();
        for (DrawingShape shape : model.getShapes()) {
            drawShape(shape);
        }
        DrawingShape preview = controller.getPreview();
        if (preview != null) {
            context.setGlobalAlpha(0.7);
            drawShape(preview);
            context.setGlobalAlpha(1);
        }
        DrawingShape selected = model.getSelectedShape();
        if (selected != null) {
            drawSelection(selected);
        }
    }

    private void drawShape(DrawingShape shape) {
        context.setStrokeStyle(shape.getColor());
        context.setFillStyle(shape.getColor());
        context.setLineWidth(3);
        if (shape.getType() == ShapeType.RECTANGLE) {
            context.setGlobalAlpha(0.2);
            context.fillRect(shape.getLeft(), shape.getTop(), shape.getWidth(), shape.getHeight());
            context.setGlobalAlpha(1);
            context.strokeRect(shape.getLeft(), shape.getTop(), shape.getWidth(), shape.getHeight());
        } else if (shape.getType() == ShapeType.ELLIPSE) {
            context.beginPath();
            context.ellipse(shape.getLeft() + shape.getWidth() / 2,
                    shape.getTop() + shape.getHeight() / 2,
                    shape.getWidth() / 2, shape.getHeight() / 2, 0, 0, Math.PI * 2);
            context.setGlobalAlpha(0.2);
            context.fill();
            context.setGlobalAlpha(1);
            context.stroke();
        } else {
            drawArrow(shape);
        }
    }

    private void drawArrow(DrawingShape shape) {
        double angle = Math.atan2(shape.getEndY() - shape.getStartY(), shape.getEndX() - shape.getStartX());
        double headLength = 13;
        double wingAngle = Math.PI / 6;
        context.beginPath();
        context.moveTo(shape.getStartX(), shape.getStartY());
        context.lineTo(shape.getEndX(), shape.getEndY());
        context.moveTo(shape.getEndX(), shape.getEndY());
        context.lineTo(shape.getEndX() - headLength * Math.cos(angle - wingAngle),
                shape.getEndY() - headLength * Math.sin(angle - wingAngle));
        context.moveTo(shape.getEndX(), shape.getEndY());
        context.lineTo(shape.getEndX() - headLength * Math.cos(angle + wingAngle),
                shape.getEndY() - headLength * Math.sin(angle + wingAngle));
        context.stroke();
    }

    private void drawSelection(DrawingShape shape) {
        double padding = 7;
        double left = shape.getLeft() - padding;
        double top = shape.getTop() - padding;
        double width = shape.getWidth() + padding * 2;
        double height = shape.getHeight() + padding * 2;
        context.setStrokeStyle("#172033");
        context.setFillStyle("#ffffff");
        context.setLineWidth(1.5);
        context.strokeRect(left, top, width, height);
        double handle = 7;
        context.fillRect(left - handle / 2, top - handle / 2, handle, handle);
        context.fillRect(left + width - handle / 2, top - handle / 2, handle, handle);
        context.fillRect(left - handle / 2, top + height - handle / 2, handle, handle);
        context.fillRect(left + width - handle / 2, top + height - handle / 2, handle, handle);
        context.strokeRect(left - handle / 2, top - handle / 2, handle, handle);
        context.strokeRect(left + width - handle / 2, top - handle / 2, handle, handle);
        context.strokeRect(left - handle / 2, top + height - handle / 2, handle, handle);
        context.strokeRect(left + width - handle / 2, top + height - handle / 2, handle, handle);
    }
}
