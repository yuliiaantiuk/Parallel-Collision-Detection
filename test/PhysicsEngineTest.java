import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PhysicsEngineTest {
    @Test
    void testCollisionResolution() {
        PhysicsEngine engine = new PhysicsEngine(800, 600);

        // Два тіла рухаються назустріч одне одному
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 20);
        a.velocity = new Vector2D(100, 0);

        PolygonBody b = new PolygonBody(new Vector2D(135, 100), 20);
        b.velocity = new Vector2D(-100, 0);

        engine.getBodies().add(a);
        engine.getBodies().add(b);

        // Один крок симуляції
        engine.update(0.016);

        // Після колізії швидкості мають змінитися (об'єкти мають розлетітися)
        assertTrue(a.velocity.x < 100, "Velocity of A should change direction or decrease");
        assertTrue(b.velocity.x > -100, "Velocity of B should change direction or increase");
    }

    @Test
    void testSlopStability() {
        PhysicsEngine engine = new PhysicsEngine(800, 600);

        // Дуже маленьке перекриття (менше за slop = 0.01)
        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 10);
        PolygonBody b = new PolygonBody(new Vector2D(119.995, 100), 10);

        a.velocity = new Vector2D(0, 0);
        b.velocity = new Vector2D(0, 0);

        engine.getBodies().add(a);
        engine.getBodies().add(b);

        engine.update(0.016);

        // Якщо slop працює, позиції не мають змінитися від мікро-перекриття
        assertEquals(100, a.position.x, 0.0001, "Should stay stable due to slop");
    }
}