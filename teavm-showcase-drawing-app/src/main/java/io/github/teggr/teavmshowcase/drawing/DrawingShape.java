package io.github.teggr.teavmshowcase.drawing;

public final class DrawingShape {
    private final ShapeType type;
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String color;

    public DrawingShape(ShapeType type, double startX, double startY, double endX, double endY, String color) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
    }

    public ShapeType getType() {
        return type;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public String getColor() {
        return color;
    }

    public void setEnd(double x, double y) {
        endX = x;
        endY = y;
    }

    public void moveBy(double deltaX, double deltaY) {
        startX += deltaX;
        endX += deltaX;
        startY += deltaY;
        endY += deltaY;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getLeft() {
        return Math.min(startX, endX);
    }

    public double getTop() {
        return Math.min(startY, endY);
    }

    public double getWidth() {
        return Math.abs(endX - startX);
    }

    public double getHeight() {
        return Math.abs(endY - startY);
    }

    public boolean contains(double x, double y) {
        if (type == ShapeType.ARROW) {
            return distanceToSegment(x, y) <= 8;
        }
        if (type == ShapeType.ELLIPSE) {
            double radiusX = getWidth() / 2;
            double radiusY = getHeight() / 2;
            if (radiusX == 0 || radiusY == 0) {
                return false;
            }
            double normalizedX = (x - (getLeft() + radiusX)) / radiusX;
            double normalizedY = (y - (getTop() + radiusY)) / radiusY;
            return normalizedX * normalizedX + normalizedY * normalizedY <= 1;
        }
        return x >= getLeft() && x <= getLeft() + getWidth()
                && y >= getTop() && y <= getTop() + getHeight();
    }

    private double distanceToSegment(double x, double y) {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared == 0) {
            return Math.hypot(x - startX, y - startY);
        }
        double projection = ((x - startX) * deltaX + (y - startY) * deltaY) / lengthSquared;
        projection = Math.max(0, Math.min(1, projection));
        double closestX = startX + projection * deltaX;
        double closestY = startY + projection * deltaY;
        return Math.hypot(x - closestX, y - closestY);
    }
}
