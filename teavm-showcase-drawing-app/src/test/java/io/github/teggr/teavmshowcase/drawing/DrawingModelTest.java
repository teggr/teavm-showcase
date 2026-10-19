package io.github.teggr.teavmshowcase.drawing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrawingModelTest {
    @Test
    void storesAllSupportedShapeTypes() {
        DrawingModel model = new DrawingModel();
        model.addShape(shape(ShapeType.RECTANGLE, 10, 20, 80, 60));
        model.addShape(shape(ShapeType.ELLIPSE, 100, 120, 180, 160));
        model.addShape(shape(ShapeType.ARROW, 200, 220, 280, 260));

        assertEquals(3, model.getShapes().size());
        assertEquals(ShapeType.RECTANGLE, model.getShapes().get(0).getType());
        assertEquals(ShapeType.ELLIPSE, model.getShapes().get(1).getType());
        assertEquals(ShapeType.ARROW, model.getShapes().get(2).getType());
    }

    @Test
    void normalizesBoundsForEitherDragDirection() {
        DrawingShape forward = shape(ShapeType.RECTANGLE, 10, 20, 80, 60);
        DrawingShape reverse = shape(ShapeType.RECTANGLE, 80, 60, 10, 20);

        assertEquals(10, forward.getLeft());
        assertEquals(20, forward.getTop());
        assertEquals(70, reverse.getWidth());
        assertEquals(40, reverse.getHeight());
        assertTrue(reverse.contains(20, 30));
    }

    @Test
    void movesAllShapeCoordinatesByDelta() {
        DrawingShape shape = shape(ShapeType.ARROW, 10, 20, 30, 40);

        shape.moveBy(-5, 12);

        assertEquals(5, shape.getStartX());
        assertEquals(32, shape.getStartY());
        assertEquals(25, shape.getEndX());
        assertEquals(52, shape.getEndY());
    }

    @Test
    void hitTestsRectanglesAndEllipses() {
        DrawingShape rectangle = shape(ShapeType.RECTANGLE, 10, 20, 80, 60);
        DrawingShape ellipse = shape(ShapeType.ELLIPSE, 100, 100, 200, 160);

        assertTrue(rectangle.contains(15, 25));
        assertFalse(rectangle.contains(90, 25));
        assertTrue(ellipse.contains(150, 130));
        assertFalse(ellipse.contains(101, 101));
    }

    @Test
    void hitTestsArrowLineAndRejectsDistantPoints() {
        DrawingShape arrow = shape(ShapeType.ARROW, 10, 10, 110, 60);

        assertTrue(arrow.contains(60, 35));
        assertFalse(arrow.contains(60, 60));
    }

    @Test
    void selectsTopmostShapeAndCanClearSelection() {
        DrawingModel model = new DrawingModel();
        DrawingShape lower = shape(ShapeType.RECTANGLE, 10, 10, 100, 100);
        DrawingShape upper = shape(ShapeType.RECTANGLE, 30, 30, 120, 120);
        model.addShape(lower);
        model.addShape(upper);

        assertTrue(model.selectAt(50, 50));
        assertSame(upper, model.getSelectedShape());
        assertFalse(model.selectAt(200, 200));
        assertNull(model.getSelectedShape());
    }

    @Test
    void movesAndRecolorsOnlyWhenASelectionExists() {
        DrawingModel model = new DrawingModel();
        assertFalse(model.moveSelectedBy(2, 3));
        assertFalse(model.recolorSelected("#ef4444"));

        DrawingShape shape = shape(ShapeType.RECTANGLE, 10, 10, 50, 50);
        model.addShape(shape);
        assertTrue(model.moveSelectedBy(2, 3));
        assertTrue(model.recolorSelected("#ef4444"));
        assertEquals(12, shape.getStartX());
        assertEquals("#ef4444", shape.getColor());
    }

    @Test
    void controllerCreatesPreviewThenCommitsShape() {
        DrawingModel model = new DrawingModel();
        DrawingController controller = new DrawingController(model);
        controller.setTool(DrawingTool.ELLIPSE);
        controller.pointerDown(90, 80);
        controller.pointerMove(20, 30);

        assertEquals(70, controller.getPreview().getWidth());
        assertEquals(0, model.getShapes().size());
        controller.pointerUp(20, 30);

        assertEquals(1, model.getShapes().size());
        assertEquals(ShapeType.ELLIPSE, model.getSelectedShape().getType());
    }

    @Test
    void pointerCancelDiscardsPreview() {
        DrawingController controller = new DrawingController(new DrawingModel());
        controller.setTool(DrawingTool.RECTANGLE);
        controller.pointerDown(10, 10);
        controller.pointerMove(50, 50);
        controller.pointerCancel();

        assertNull(controller.getPreview());
        assertTrue(controller.getModel().getShapes().isEmpty());
    }

    private DrawingShape shape(ShapeType type, double startX, double startY, double endX, double endY) {
        return new DrawingShape(type, startX, startY, endX, endY, "#3b82f6");
    }
}
