import java.util.concurrent.RecursiveAction;
import java.util.List;

public class UpdatePositionTask extends RecursiveAction {
    private final List<PolygonBody> bodies;
    private final int start, end;
    private final double dt, width, height;
    private static final int THRESHOLD = 1000;

    public UpdatePositionTask(List<PolygonBody> bodies, int start, int end, double dt, double width, double height) {
        this.bodies = bodies;
        this.start = start;
        this.end = end;
        this.dt = dt;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void compute() {
        if (end - start <= THRESHOLD) {
            for (int i = start; i < end; i++) {
                bodies.get(i).update(dt, width, height);
            }
        } else {
            int mid = (start + end) / 2;
            invokeAll(
                    new UpdatePositionTask(bodies, start, mid, dt, width, height),
                    new UpdatePositionTask(bodies, mid, end, dt, width, height)
            );
        }
    }
}