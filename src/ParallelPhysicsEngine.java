import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelPhysicsEngine extends PhysicsEngine {
    private final ExecutorService executor;
    private final int threadCount;

    public ParallelPhysicsEngine(double width, double height, int threads) {
        super(width, height);
        this.threadCount = threads;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public void update(double dt) {
        parallelUpdatePositions(dt);

        grid.clear();
        for (PolygonBody b : bodies) {
            grid.insert(b);
        }

        parallelResolveCollisions();
    }

    private void parallelResolveCollisions() {
        CountDownLatch latch = new CountDownLatch(threadCount);
        int cols = grid.getCols();
        int chunkSize = (int) Math.ceil((double) cols / threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int startX = t * chunkSize;
            final int endX = Math.min(startX + chunkSize, cols);

            executor.submit(() -> {
                try {
                    for (int x = startX; x < endX; x++) {
                        for (int y = 0; y < grid.getRows(); y++) {
                            checkCollisionsInCell(x, y);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void parallelUpdatePositions(double dt) {
        int size = bodies.size();
        if (size == 0) return;

        CountDownLatch latch = new CountDownLatch(threadCount);

        int chunkSize = (int) Math.ceil((double) size / threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int startIdx = t * chunkSize;
            final int endIdx = Math.min(startIdx + chunkSize, size);

            if (startIdx >= size) {
                latch.countDown();
                continue;
            }

            executor.submit(() -> {
                try {
                    for (int i = startIdx; i < endIdx; i++) {
                        bodies.get(i).update(dt, width, height);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

//    private void parallelResolveCollision(PolygonBody a, PolygonBody b, CollisionInfo info) {
//        PolygonBody first = a.id < b.id ? a : b;
//        PolygonBody second = a.id < b.id ? b : a;
//
//        synchronized (first) {
//            synchronized (second) {
//                double totalMass = a.mass + b.mass;
//                double ratioA = b.mass / totalMass;
//                double ratioB = a.mass / totalMass;
//
//                double moveX = info.axis.x * info.overlap;
//                double moveY = info.axis.y * info.overlap;
//
//                Vector2D relativePos = new Vector2D(b.position.x - a.position.x, b.position.y - a.position.y);
//                if (relativePos.dot(info.axis) < 0) {
//                    moveX *= -1;
//                    moveY *= -1;
//                }
//
//                a.position.x -= moveX * ratioA;
//                a.position.y -= moveY * ratioA;
//                b.position.x += moveX * ratioB;
//                b.position.y += moveY * ratioB;
//
//                double relVelX = b.velocity.x - a.velocity.x;
//                double relVelY = b.velocity.y - a.velocity.y;
//
//                double velAlongNormal = relVelX * info.axis.x + relVelY * info.axis.y;
//
//                if (velAlongNormal > 0) return;
//
//                double restitution = 0.98;
//
//                double j = -(1 + restitution) * velAlongNormal;
//                j /= (1 / a.mass + 1 / b.mass);
//
//                double impulseX = j * info.axis.x;
//                double impulseY = j * info.axis.y;
//
//                a.velocity.x -= impulseX / a.mass;
//                a.velocity.y -= impulseY / a.mass;
//                b.velocity.x += impulseX / b.mass;
//                b.velocity.y += impulseY / b.mass;
//            }
//        }
//    }
}