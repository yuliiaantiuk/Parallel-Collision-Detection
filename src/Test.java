import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        int testSize = 1000;
        double dt = 0.016;
        int iterations = 100;

        PhysicsEngine sequentialEngine = new PhysicsEngine(800, 600);
        ParallelPhysicsEngine poolEngine = new ParallelPhysicsEngine(800, 600, 1);
        ParallelPhysicsEngine fjEngine = new ParallelPhysicsEngine(800, 600, 1);

        List<PolygonBody> prototypes = new ArrayList<>();
        PolygonBody.resetIdCounter();

        for (int i = 0; i < testSize; i++) {
            Vector2D pos = new Vector2D(Math.random() * 800, Math.random() * 600);
            double radius = 10 + Math.random() * 20;
            prototypes.add(new PolygonBody(pos, radius));
        }

        for (PolygonBody proto : prototypes) {
            sequentialEngine.getBodies().add(new PolygonBody(proto));
            poolEngine.getBodies().add(new PolygonBody(proto));
            fjEngine.getBodies().add(new PolygonBody(proto));
        }

        sequentialEngine.initializeGrid(3.0);
        poolEngine.initializeGrid(3.0);
        fjEngine.initializeGrid(3.0);

        System.out.println("Starting verification for " + iterations + " steps");

        for (int i = 0; i < iterations; i++) {
            sequentialEngine.update(dt);
            poolEngine.update(dt);
            fjEngine.updateForkJoin(dt);
        }

        double maxDiffPool = 0;
        double maxDiffFJ = 0;

        for (int i = 0; i < testSize; i++) {
            PolygonBody bSeq = sequentialEngine.getBodies().get(i);
            PolygonBody bPool = poolEngine.getBodies().get(i);
            PolygonBody bFJ = fjEngine.getBodies().get(i);

            // Порівняння Sequential vs Fixed Pool
            double dPoolX = Math.abs(bSeq.position.x - bPool.position.x);
            double dPoolY = Math.abs(bSeq.position.y - bPool.position.y);
            maxDiffPool = Math.max(maxDiffPool, Math.max(dPoolX, dPoolY));

            // Порівняння Sequential vs ForkJoin
            double dFJX = Math.abs(bSeq.position.x - bFJ.position.x);
            double dFJY = Math.abs(bSeq.position.y - bFJ.position.y);
            maxDiffFJ = Math.max(maxDiffFJ, Math.max(dFJX, dFJY));
        }

        System.out.println("Max diff (Sequential vs Fixed Pool): " + maxDiffPool);
        System.out.println("Max diff (Sequential vs ForkJoin):   " + maxDiffFJ);

        boolean poolOk = maxDiffPool < 1e-9;
        boolean fjOk = maxDiffFJ < 1e-9;

        if (poolOk && fjOk) {
            System.out.println("SUCCESS!");
        } else {
            if (!poolOk) System.out.println("FAILURE: Fixed Pool!");
            if (!fjOk) System.out.println("FAILURE: ForkJoin!");
        }

        poolEngine.shutdown();
        fjEngine.shutdown();
    }
}
