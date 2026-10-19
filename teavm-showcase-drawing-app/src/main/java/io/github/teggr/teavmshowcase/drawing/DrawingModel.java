package io.github.teggr.teavmshowcase.drawing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DrawingModel {
    private final List<DrawingShape> shapes = new ArrayList<>();
    private int selectedIndex = -1;

    public List<DrawingShape> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public DrawingShape getSelectedShape() {
        return selectedIndex >= 0 ? shapes.get(selectedIndex) : null;
    }

    public void addShape(DrawingShape shape) {
        shapes.add(shape);
        selectedIndex = shapes.size() - 1;
    }

    public boolean selectAt(double x, double y) {
        selectedIndex = -1;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).contains(x, y)) {
                selectedIndex = i;
                return true;
            }
        }
        return false;
    }

    public boolean moveSelectedBy(double deltaX, double deltaY) {
        DrawingShape selected = getSelectedShape();
        if (selected == null) {
            return false;
        }
        selected.moveBy(deltaX, deltaY);
        return true;
    }

    public boolean recolorSelected(String color) {
        DrawingShape selected = getSelectedShape();
        if (selected == null) {
            return false;
        }
        selected.setColor(color);
        return true;
    }

    public void clearSelection() {
        selectedIndex = -1;
    }
}
