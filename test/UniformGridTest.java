import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class UniformGridTest {
    @Test
    void testGridInsertion() {
        UniformGrid grid = new UniformGrid(800, 600, 100);
        PolygonBody body = new PolygonBody(new Vector2D(250, 450), 10);

        grid.insert(body);

        // x=250, S=100 => i = floor(2.5) = 2
        // y=450, S=100 => j = floor(4.5) = 4
        List<PolygonBody> cell = grid.getCells()[2][4];

        assertFalse(cell.isEmpty(), "The object has to be in [2][4]");
        assertEquals(body, cell.get(0));
    }

    @Test
    void testGridBoundaries() {
        UniformGrid grid = new UniformGrid(800, 600, 100);

        // Точно на межі клітинок (100.0 / 100 = 1.0, має бути індекс 1)
        PolygonBody bodyOnEdge = new PolygonBody(new Vector2D(100.0, 100.0), 10);
        // Майже на межі, але в попередній клітинці
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
}