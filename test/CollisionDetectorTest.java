import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CollisionDetectorTest {
    @Test
    void testSquareCollision() {
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 20);
        PolygonBody b = new PolygonBody(new Vector2D(130, 100), 20);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);

        assertTrue(info.collided, "Collision has to be detected");
        assertNotNull(info.axis, "Normal vector can not be null");
        assertTrue(info.overlap > 0, "Overlap depth must be more than 0");
    }

    @Test
    void testNoCollision() {
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 10);
        PolygonBody b = new PolygonBody(new Vector2D(300, 100), 10);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);
        assertFalse(info.collided, "Collision can not be detected");
    }

    @Test
    void testDeepOverlap() {
        // Одне тіло майже в центрі іншого
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 50);
        PolygonBody b = new PolygonBody(new Vector2D(105, 100), 50);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);
        assertTrue(info.collided);
        // Очікуваний overlap має бути великим (сума радіусів - відстань)
        assertTrue(info.overlap > 40);
    }

    @Test
    void testIdenticalPositions() {
        PolygonBody a = new PolygonBody(new Vector2D(200, 200), 20);
        PolygonBody b = new PolygonBody(new Vector2D(200, 200), 20);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);
        assertTrue(info.collided, "Bodies at same position must collide");
    }
}