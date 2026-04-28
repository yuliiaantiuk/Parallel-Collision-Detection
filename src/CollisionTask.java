import java.util.concurrent.RecursiveAction;

public class CollisionTask extends RecursiveAction {
    private final int startCol, endCol;
    private final ParallelPhysicsEngine engine;
    private final double dt;
    public static int THRESHOLD = 2;

    public CollisionTask(int startCol, int endCol, ParallelPhysicsEngine engine, double dt) {
        this.startCol = startCol;
        this.endCol = endCol;
        this.engine = engine;
        this.dt = dt;
    }

    @Override
    protected void compute() {
        int width = endCol - startCol;
        if (width <= THRESHOLD) {
            for (int x = startCol; x < endCol; x++) {
                for (int y = 0; y < engine.grid.getRows(); y++) {
                    engine.checkCollisionsInCell(x, y);
                }
            }
        } else {
            int mid = startCol + width / 2;
            CollisionTask left = new CollisionTask(startCol, mid, engine, dt);
            CollisionTask right = new CollisionTask(mid, endCol, engine, dt);

            invokeAll(left, right);
        }
    }
}