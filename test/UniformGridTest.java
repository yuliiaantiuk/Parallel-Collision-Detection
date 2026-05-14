import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class UniformGridTest {
    @Test
    void testGridInsertion() {
        UniformGrid grid = new UniformGrid(800, 600, 100);
        PolygonBody body1 = new PolygonBody(new Vector2D(250, 450), 10);
        PolygonBody body2 = new PolygonBody(new Vector2D(328, 41), 10);

        grid.insert(body1);
        grid.insert(body2);
        List<PolygonBody> cell1 = grid.getCells()[2][4];
        List<PolygonBody> cell2 = grid.getCells()[3][0];

        assertFalse(cell1.isEmpty() || cell2.isEmpty(), "The object 1 has to be in [2][4], the object 2 has to be in [3][0]");
        assertEquals(body1, cell1.get(0));
        assertEquals(body2, cell2.get(0));
    }

    @Test
    void testGridBoundaries() {
        UniformGrid grid = new UniformGrid(800, 600, 100);

        PolygonBody bodyOnEdge = new PolygonBody(new Vector2D(100.0, 100.0), 10);
        PolygonBody bodyNearEdge = new PolygonBody(new Vector2D(99.99, 99.99), 10);

        grid.insert(bodyOnEdge);
        grid.insert(bodyNearEdge);

        assertFalse(grid.getCells()[1][1].isEmpty(), "Exactly 100.0 should go to index 1");
        assertFalse(grid.getCells()[0][0].isEmpty(), "99.99 should stay in index 0");
    }

    @Test
    void testGridClear() {
        UniformGrid grid = new UniformGrid(800, 600, 100);
        grid.insert(new PolygonBody(new Vector2D(50, 50), 10));
        grid.clear();

        assertTrue(grid.getCells()[0][0].isEmpty(), "Grid must be empty after clear()");
    }

    @Test
    void testObjectOutOfBounds() {
        UniformGrid grid = new UniformGrid(800, 600, 100);
        PolygonBody bodyOutLeft = new PolygonBody(new Vector2D(-50, -50), 10);
        PolygonBody bodyOutRight = new PolygonBody(new Vector2D(900, 700), 10);

        assertDoesNotThrow(() -> grid.insert(bodyOutLeft),
                "Grid should handle out-of-bounds coordinates safely (left)");
        assertDoesNotThrow(() -> grid.insert(bodyOutRight),
                "Grid should handle out-of-bounds coordinates safely (right)");
    }

    @Test
    void testHighDensityCell() {
        UniformGrid grid = new UniformGrid(800, 600, 100);
        for (int i = 0; i < 100; i++) {
            grid.insert(new PolygonBody(new Vector2D(50, 50), 5));
        }
        assertEquals(100, grid.getCells()[0][0].size(),
                "Cell should correctly store multiple objects");
    }
}