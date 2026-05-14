import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PhysicsEngineTest {
    @Test
    void testCollisionResolution() {
        PhysicsEngine engine = new PhysicsEngine(800, 600);
        engine.initializeGrid(3.0);

        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 20);
        a.velocity = new Vector2D(100, 0);

        PolygonBody b = new PolygonBody(new Vector2D(130, 100), 20);
        b.velocity = new Vector2D(-100, 0);

        engine.getBodies().add(a);
        engine.getBodies().add(b);

        engine.update(0.016);

        assertTrue(a.velocity.x < 100, "Velocity of A should decrease or change direction");
        assertTrue(b.velocity.x > -100, "Velocity of B should increase or change direction");
    }

    @Test
    void testSlopStability() {
        PhysicsEngine engine = new PhysicsEngine(800, 600);

        PolygonBody a = new PolygonBody(new Vector2D(100, 100), 10);
        PolygonBody b = new PolygonBody(new Vector2D(119.995, 100), 10);

        a.velocity = new Vector2D(0, 0);
        b.velocity = new Vector2D(0, 0);

        engine.getBodies().add(a);
        engine.getBodies().add(b);

        engine.update(0.016);

        assertEquals(100, a.position.x, 0.0001, "Should stay stable due to slop");
    }

    @Test
    void testMassInfluence() {
        PhysicsEngine engine = new PhysicsEngine(800, 600);
        engine.initializeGrid(3.0);

        PolygonBody heavy = new PolygonBody(new Vector2D(100, 100), 50);
        heavy.velocity = new Vector2D(100, 0);

        PolygonBody light = new PolygonBody(new Vector2D(140, 100), 10);
        light.velocity = new Vector2D(0, 0);

        engine.getBodies().add(heavy);
        engine.getBodies().add(light);

        for(int i = 0; i < 5; i++) {
            engine.update(0.016);
        }

        double deltaVHeavy = Math.abs(100 - heavy.velocity.x);
        double deltaVLight = Math.abs(0 - light.velocity.x);

        assertTrue(deltaVLight > 0, "Light body should have moved");
        assertTrue(deltaVLight > deltaVHeavy, "Light body change should be much larger than heavy");
        assertTrue(heavy.velocity.x > 50, "Heavy body should maintain momentum");
    }
}