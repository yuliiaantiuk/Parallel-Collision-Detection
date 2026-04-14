import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        int testSize = 1000;
        double dt = 0.016;
        int iterations = 100;

        PhysicsEngine sequentialEngine = new PhysicsEngine(800, 600);
        ParallelPhysicsEngine parallelEngine = new ParallelPhysicsEngine(800, 600, 1);

        List<PolygonBody> prototypes = new ArrayList<>();
        PolygonBody.resetIdCounter();

        for (int i = 0; i < testSize; i++) {
            Vector2D pos = new Vector2D(Math.random() * 800, Math.random() * 600);
            double radius = 10 + Math.random() * 20;
            prototypes.add(new PolygonBody(pos, radius));
        }

        for (PolygonBody proto : prototypes) {
            sequentialEngine.getBodies().add(new PolygonBody(proto));
            parallelEngine.getBodies().add(new PolygonBody(proto));
        }

        System.out.println("Starting verification for " + iterations + " steps");

        for (int i = 0; i < iterations; i++) {
            sequentialEngine.update(dt);
            parallelEngine.update(dt);
        }

        double maxDiff = 0;
        for (int i = 0; i < testSize; i++) {
            PolygonBody b1 = sequentialEngine.getBodies().get(i);
            PolygonBody b2 = parallelEngine.getBodies().get(i);

            double diffX = Math.abs(b1.position.x - b2.position.x);
            double diffY = Math.abs(b1.position.y - b2.position.y);
            maxDiff = Math.max(maxDiff, Math.max(diffX, diffY));
        }

        System.out.println("Maximum coordinate difference: " + maxDiff);

        if (maxDiff < 1e-9) {
            System.out.println("SUCCESS: Parallel and Sequential algorithms are identical.");
        } else {
            System.out.println("FAILURE!");
        }

        parallelEngine.shutdown();
    }
}
