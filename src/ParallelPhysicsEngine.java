import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

public class ParallelPhysicsEngine extends PhysicsEngine {
    private ExecutorService executor;
    private final int threadCount;
    private ForkJoinPool forkJoinPool;

    public ParallelPhysicsEngine(double width, double height, int threads) {
        super(width, height);
        this.threadCount = threads;
    }

    private ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(threadCount);
        }
        return executor;
    }

    private ForkJoinPool getForkJoinPool() {
        if (forkJoinPool == null || forkJoinPool.isShutdown()) {
            forkJoinPool = new ForkJoinPool(threadCount);
        }
        return forkJoinPool;
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
        ForkJoinPool fjPool = getForkJoinPool();

        if (!bodies.isEmpty()) {
            fjPool.invoke(new UpdatePositionTask(bodies, 0, bodies.size(), dt, width, height));
        }

        grid.clear();
        for (PolygonBody b : bodies) {
            grid.insert(b);
        }

        fjPool.invoke(new CollisionTask(0, grid.getCols(), this, dt));
    }

    private void parallelResolveCollisions() {
        ExecutorService exec = getExecutor();
        int cols = grid.getCols();
        int evenColsCount = (int) Math.ceil((double) cols / 2);
        CountDownLatch evenLatch = new CountDownLatch(evenColsCount);

        for (int x = 0; x < cols; x += 2) {
            final int currentX = x;
            exec.submit(() -> {
                try {
                    for (int y = 0; y < grid.getRows(); y++) {
                        checkCollisionsInCell(currentX, y);
                    }
                } finally {
                    evenLatch.countDown();
                }
            });
        }

        try {
            evenLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int oddColsCount = cols / 2;
        CountDownLatch oddLatch = new CountDownLatch(oddColsCount);

        for (int x = 1; x < cols; x += 2) {
            final int currentX = x;
            exec.submit(() -> {
                try {
                    for (int y = 0; y < grid.getRows(); y++) {
                        checkCollisionsInCell(currentX, y);
                    }
                } finally {
                    oddLatch.countDown();
                }
            });
        }

        try {
            oddLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void parallelUpdatePositions(double dt) {
        int size = bodies.size();
        if (size == 0) return;

        ExecutorService exec = getExecutor();

        int chunkSize = (int) Math.ceil((double) size / (threadCount * 2));
        int actualTasks = (int) Math.ceil((double) size / chunkSize);
        CountDownLatch latch = new CountDownLatch(actualTasks);

        for (int t = 0; t < actualTasks; t++) {
            final int startIdx = t * chunkSize;
            final int endIdx = Math.min(startIdx + chunkSize, size);

            exec.submit(() -> {
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
        if (executor != null) {
            shutdownPool(executor);
            executor = null;
        }
        if (forkJoinPool != null) {
            shutdownPool(forkJoinPool);
            forkJoinPool = null;
        }
    }

    private void shutdownPool(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}