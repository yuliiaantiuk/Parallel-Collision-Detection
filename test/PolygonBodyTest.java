import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PolygonBodyTest {
    @Test
    void testBounceRightWall() {
        double width = 800;
        double radius = 10;
        // Позиція майже біля правої стіни, швидкість спрямована вправо
        PolygonBody body = new PolygonBody(new Vector2D(795, 300), radius);
        body.velocity = new Vector2D(100, 0);

        // dt = 0.1 секунди, переміщення мало б бути на 10 пікселів (795 + 10 = 805)
        // Але стіна на 800 - 10 = 790 (центр об'єкта)
        body.update(0.1, width, 600);

        assertTrue(body.velocity.x < 0, "Velocity should change its direction");
        assertTrue(body.position.x <= width - radius, "Object out of bounds");
    }

    @Test
    void testBounceAllWalls() {
        double w = 800, h = 600, r = 10;

        // Верхня ліва межа (Верхній лівий кут)
        PolygonBody cornerBody = new PolygonBody(new Vector2D(5, 5), r);
        cornerBody.velocity = new Vector2D(-100, -100);
        cornerBody.update(0.1, w, h);

        assertTrue(cornerBody.velocity.x > 0 && cornerBody.velocity.y > 0, "Should bounce from corner");
        assertEquals(r, cornerBody.position.x, 1e-9);
        assertEquals(r, cornerBody.position.y, 1e-9);
    }

    @Test
    void testStationaryBody() {
        PolygonBody staticBody = new PolygonBody(new Vector2D(100, 100), 10);
        staticBody.velocity = new Vector2D(0, 0);
        staticBody.update(1.0, 800, 600);

        assertEquals(100, staticBody.position.x, "Stationary body should not move");
    }
}