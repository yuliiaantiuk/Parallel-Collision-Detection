import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Test {
    static final double WIDTH = 800;
    static final double HEIGHT = 600;
    static final double DT = 0.016;
    static final double SLOP = 5;

    public static void main(String[] args) throws Exception {
        int[] threads = {4, 8};
        int iters = 100;
        for (int thread : threads) {
            System.out.println("=== TEST 1: Physical invariants ( " + thread + " threads, " + iters + " iterations) ===");
            testPhysicalInvariants(thread, iters);

            System.out.println("\n=== TEST 2: Race condition / NaN detection ( " + thread + " threads, " + iters + " iterations) ===");
            testRaceCondition(thread, iters);

            System.out.println("\n=== TEST 3: Deadlock detection ( " + thread + " threads) ===");
            testDeadlock(thread);
        }
    }

    static void testPhysicalInvariants(int threads, int iterations) {
        for (String mode : new String[]{"ThreadPool", "ForkJoin"}) {
            ParallelPhysicsEngine engine = createEngine(200, threads);
            boolean passed = true;

            int warmupIterations = 20;
            for (int i = 0; i < warmupIterations; i++) {
                if (mode.equals("ThreadPool")) engine.update(DT);
                else engine.updateForkJoin(DT);
            }

            for (int iter = 0; iter < iterations; iter++) {
                double[] pBefore = totalMomentum(engine);

                if (mode.equals("ThreadPool")) engine.update(DT);
                else engine.updateForkJoin(DT);

                for (PolygonBody b : engine.getBodies()) {
                    if (b.position.x < b.radius - SLOP || b.position.x > WIDTH - b.radius + SLOP ||
                            b.position.y < b.radius - SLOP || b.position.y > HEIGHT - b.radius + SLOP) {
                        System.out.println("  FAIL [" + mode + "] iter=" + iter
                                + ": body id=" + b.id + " out of bounds");
                        passed = false;
                    }
                }

                List<PolygonBody> bodies = engine.getBodies();
                for (int i = 0; i < bodies.size(); i++) {
                    for (int j = i + 1; j < bodies.size(); j++) {
                        PolygonBody a = bodies.get(i);
                        PolygonBody bBody = bodies.get(j);
                        double minRadius = Math.min(a.radius, bBody.radius);
                        CollisionInfo info = CollisionDetector.getCollisionInfo(a, bBody);
                        if (info.collided && info.overlap > minRadius * 0.5) {
                            System.out.println("  FAIL [" + mode + "] iter=" + iter
                                    + ": critical overlap between id=" + a.id
                                    + " and id=" + bBody.id
                                    + ", overlap=" + info.overlap
                                    + " (minRadius=" + minRadius + ")");
                            passed = false;
                        }
                    }
                }

                boolean wallContactAnywhere = false;
                for (PolygonBody b : engine.getBodies()) {
                    if (b.hitWallThisFrame) {
                        wallContactAnywhere = true;
                        break;
                    }
                }

                if (!wallContactAnywhere) {
                    double[] pAfter = totalMomentum(engine);
                    double diffPx = Math.abs(pAfter[0] - pBefore[0]);
                    double diffPy = Math.abs(pAfter[1] - pBefore[1]);

                    double tolerance = 1e-7;

                    if (diffPx > tolerance || diffPy > tolerance) {
                        System.out.println("  FAIL [" + mode + "] iter=" + iter
                                + ": momentum changed without wall contact: dPx=" + diffPx
                                + ", dPy=" + diffPy);
                        passed = false;
                    }
                }
            }

            if (passed) System.out.println("  PASS [" + mode + "]");
            engine.shutdown();
        }
    }

    static void testRaceCondition(int threads, int iterations) {
        for (String mode : new String[]{"ThreadPool", "ForkJoin"}) {
            ParallelPhysicsEngine engine = createEngine(10000, threads);
            boolean passed = true;

            outer:
            for (int iter = 0; iter < iterations; iter++) {
                if (mode.equals("ThreadPool")) engine.update(DT);
                else engine.updateForkJoin(DT);

                for (PolygonBody b : engine.getBodies()) {
                    if (Double.isNaN(b.position.x) || Double.isNaN(b.position.y) ||
                            Double.isNaN(b.velocity.x) || Double.isNaN(b.velocity.y) ||
                            Double.isInfinite(b.position.x) || Double.isInfinite(b.position.y) ||
                            Double.isInfinite(b.velocity.x) || Double.isInfinite(b.velocity.y)) {
                        System.out.println("  FAIL [" + mode + "] iter=" + iter
                                + ": NaN or Infinity in body id=" + b.id
                                + " pos=(" + b.position.x + "," + b.position.y + ")"
                                + " vel=(" + b.velocity.x + "," + b.velocity.y + ")");
                        passed = false;
                        break outer;
                    }
                }
            }

            if (passed) System.out.println("  PASS [" + mode + "]");
            engine.shutdown();
        }
    }

    static void testDeadlock(int threads) throws Exception {
        for (String mode : new String[]{"ThreadPool", "ForkJoin"}) {
            ParallelPhysicsEngine engine = createEngine(5000, threads);

            ExecutorService watchdog = Executors.newSingleThreadExecutor();
            Future<?> future = watchdog.submit(() -> {
                for (int i = 0; i < 20; i++) {
                    if (mode.equals("ThreadPool")) engine.update(DT);
                    else engine.updateForkJoin(DT);
                }
            });

            try {
                future.get(10, TimeUnit.SECONDS);
                System.out.println("  PASS [" + mode + "]: completed within timeout");
            } catch (java.util.concurrent.TimeoutException e) {
                future.cancel(true);
                System.out.println("  FAIL [" + mode + "]: DEADLOCK detected (timeout exceeded)");
            } finally {
                watchdog.shutdownNow();
                engine.shutdown();
            }
        }
    }

    static ParallelPhysicsEngine createEngine(int objectCount, int threads) {
        ParallelPhysicsEngine engine = new ParallelPhysicsEngine(WIDTH, HEIGHT, threads);
        PolygonBody.resetIdCounter();

        for (int i = 0; i < objectCount; i++) {
            Vector2D pos = new Vector2D(
                    50 + Math.random() * (WIDTH - 100),
                    50 + Math.random() * (HEIGHT - 100)
            );
            double radius = 8 + Math.random() * 12;
            engine.getBodies().add(new PolygonBody(pos, radius));
        }

        engine.initializeGrid(3.0);
        return engine;
    }

    static double[] totalMomentum(PhysicsEngine engine) {
        double px = 0, py = 0;
        for (PolygonBody b : engine.getBodies()) {
            px += b.mass * b.velocity.x;
            py += b.mass * b.velocity.y;
        }
        return new double[]{px, py};
    }
}