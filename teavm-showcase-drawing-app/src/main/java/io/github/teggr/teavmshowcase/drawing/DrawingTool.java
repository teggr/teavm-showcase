package io.github.teggr.teavmshowcase.drawing;

public enum DrawingTool {
    SELECT,
    RECTANGLE,
    ELLIPSE,
    ARROW;

    public ShapeType getShapeType() {
        return switch (this) {
            case RECTANGLE -> ShapeType.RECTANGLE;
            case ELLIPSE -> ShapeType.ELLIPSE;
            case ARROW -> ShapeType.ARROW;
            case SELECT -> null;
        };
    }
}
