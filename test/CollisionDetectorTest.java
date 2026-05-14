import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CollisionDetectorTest {
    @Test
    void testNormalCollision() {
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 20);
        PolygonBody b = new PolygonBody(new Vector2D(125, 100), 20);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);

        assertTrue(info.collided, "Collision has to be detected");
        assertNotNull(info.axis, "Normal vector can not be null");
        double axisLen = Math.sqrt(info.axis.x * info.axis.x + info.axis.y * info.axis.y);
        assertEquals(1.0, axisLen, 1e-9, "Normal vector must be normalized");
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
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 50);
        PolygonBody b = new PolygonBody(new Vector2D(105, 100), 50);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);
        assertTrue(info.collided);
        assertTrue(info.overlap > 40);
    }

    @Test
    void testIdenticalPositions() {
        PolygonBody a = new PolygonBody(new Vector2D(200, 200), 20);
        PolygonBody b = new PolygonBody(new Vector2D(200, 200), 20);

        CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);

        assertTrue(info.collided, "Bodies at same position must collide");
        assertTrue(info.overlap > 20.0, "Overlap should be significant");

        assertNotNull(info.axis, "Normal vector cannot be null");
        assertFalse(Double.isNaN(info.axis.x) || Double.isNaN(info.axis.y), "Axis should not contain NaN");
    }
}