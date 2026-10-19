package io.github.teggr.teavmshowcase.drawing;

public final class DrawingController {
    private final DrawingModel model;
    private DrawingTool tool = DrawingTool.SELECT;
    private DrawingShape preview;
    private double lastX;
    private double lastY;
    private boolean moving;

    public DrawingController(DrawingModel model) {
        this.model = model;
    }

    public DrawingModel getModel() {
        return model;
    }

    public DrawingTool getTool() {
        return tool;
    }

    public DrawingShape getPreview() {
        return preview;
    }

    public void setTool(DrawingTool tool) {
        this.tool = tool;
        preview = null;
        moving = false;
        model.clearSelection();
    }

    public void pointerDown(double x, double y) {
        lastX = x;
        lastY = y;
        moving = false;
        if (tool == DrawingTool.SELECT) {
            moving = model.selectAt(x, y);
            return;
        }
        model.clearSelection();
        preview = new DrawingShape(tool.getShapeType(), x, y, x, y, "#3b82f6");
    }

    public void pointerMove(double x, double y) {
        if (preview != null) {
            preview.setEnd(x, y);
        } else if (moving) {
            model.moveSelectedBy(x - lastX, y - lastY);
        }
        lastX = x;
        lastY = y;
    }

    public void pointerUp(double x, double y) {
        pointerMove(x, y);
        if (preview != null) {
            if (isVisible(preview)) {
                model.addShape(preview);
            }
            preview = null;
        }
        moving = false;
    }

    public void pointerCancel() {
        preview = null;
        moving = false;
    }

    public boolean recolorSelected(String color) {
        return model.recolorSelected(color);
    }

    private boolean isVisible(DrawingShape shape) {
        if (shape.getType() == ShapeType.ARROW) {
            return Math.hypot(shape.getEndX() - shape.getStartX(), shape.getEndY() - shape.getStartY()) >= 8;
        }
        return shape.getWidth() >= 3 && shape.getHeight() >= 3;
    }
}
