import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PolygonBodyTest {
    @Test
    void testBounceRightWall() {
        double width = 800;
        double radius = 10;
        PolygonBody body = new PolygonBody(new Vector2D(795, 300), radius);
        body.velocity = new Vector2D(100, 0);

        body.update(0.1, width, 600);

        assertTrue(body.velocity.x < 0, "Velocity X should be negative (bounce right)");
        assertTrue(body.position.x <= width - radius, "Body should be pushed back from right wall");
    }

    @Test
    void testBounceLeftWall() {
        double radius = 10;
        PolygonBody body = new PolygonBody(new Vector2D(5, 300), radius);
        body.velocity = new Vector2D(-100, 0);

        body.update(0.1, 800, 600);

        assertTrue(body.velocity.x > 0, "Velocity X should be positive (bounce left)");
        assertTrue(body.position.x >= radius, "Body should be pushed back from left wall");
    }

    @Test
    void testBounceTopWall() {
        double radius = 10;
        PolygonBody body = new PolygonBody(new Vector2D(400, 5), radius);
        body.velocity = new Vector2D(0, -100);

        body.update(0.1, 800, 600);

        assertTrue(body.velocity.y > 0, "Velocity Y should be positive (bounce top)");
        assertTrue(body.position.y >= radius, "Body should be pushed back from top wall");
    }

    @Test
    void testBounceBottomWall() {
        double height = 600;
        double radius = 10;
        PolygonBody body = new PolygonBody(new Vector2D(400, 595), radius);
        body.velocity = new Vector2D(0, 100);

        body.update(0.1, 800, height);

        assertTrue(body.velocity.y < 0, "Velocity Y should be negative (bounce bottom)");
        assertTrue(body.position.y <= height - radius, "Body should be pushed back from bottom wall");
    }

    @Test
    void testBounceAllWalls() {
        double w = 800, h = 600, r = 10;

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

    @Test
    void testHighVelocity() {
        double width = 800;
        PolygonBody fastBody = new PolygonBody(new Vector2D(700, 300), 10);
        fastBody.velocity = new Vector2D(5000, 0);

        fastBody.update(0.1, width, 600);

        assertTrue(fastBody.position.x <= width - 10,
                "Body should be clamped to world bounds even at extreme speeds");
        assertTrue(fastBody.velocity.x < 0, "Body should reflect velocity");
    }
}