import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ParallelPhysicsEngine extends PhysicsEngine {
    private final ExecutorService executor;
    private final int threadCount;
    private final ForkJoinPool forkJoinPool;

    public ParallelPhysicsEngine(double width, double height, int threads) {
        super(width, height);
        this.threadCount = threads;
        this.executor = Executors.newFixedThreadPool(threads);
        this.forkJoinPool = new ForkJoinPool(threads);
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

    public void updateForkJoin(double dt) {
        if (!bodies.isEmpty()) {
            forkJoinPool.invoke(new UpdatePositionTask(bodies, 0, bodies.size(), dt, width, height));
        }

        grid.clear();
        for (PolygonBody b : bodies) {
            grid.insert(b);
        }

        forkJoinPool.invoke(new CollisionTask(0, grid.getCols(), this, dt));
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
        forkJoinPool.shutdown();
    }
}