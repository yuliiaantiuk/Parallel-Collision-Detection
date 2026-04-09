import java.util.ArrayList;
import java.util.List;

public class PhysicsEngine {
    private List<PolygonBody> bodies;
    private UniformGrid grid;
    private double width, height;

    public PhysicsEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.bodies = new ArrayList<>();
        this.grid = new UniformGrid(width, height, 100.0); // розмір клітинки потім
    }

    public List<PolygonBody> getBodies() {
        return bodies;
    }

    public void update(double dt) {
        for (PolygonBody b : bodies) {
            b.update(dt, width, height);
        }

        grid.clear();
        for (PolygonBody b : bodies) {
            grid.insert(b);
        }

        for (int x = 0; x < grid.getCols(); x++) {
            for (int y = 0; y < grid.getRows(); y++) {
                checkCollisionsInCell(x, y);
            }
        }
    }

    private void checkCollisionsInCell(int x, int y) {
        List<PolygonBody> currentCell = grid.getCells()[x][y];
        if (currentCell.isEmpty()) return;
        // в межах цієї клітинки
        for (int i = 0; i < currentCell.size(); i++) {
            for (int j = i + 1; j < currentCell.size(); j++) {
                checkAndResolve(currentCell.get(i), currentCell.get(j));
            }
        }

        // сусідні клітинки
        // ліво, низ, ліва нижня діагональ
        int[][] offsets = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};

        for (int[] offset : offsets) {
            int nx = x + offset[0];
            int ny = y + offset[1];

            if (nx >= 0 && nx < grid.getCols() && ny >= 0 && ny < grid.getRows()) {
                List<PolygonBody> nextCell = grid.getCells()[nx][ny];
                for (PolygonBody a : currentCell) {
                    for (PolygonBody b : nextCell) {
                        checkAndResolve(a, b);
                    }
                }
            }
        }
    }

    private void checkAndResolve(PolygonBody a, PolygonBody b) {
        double distSq = a.position.distSq(b.position);
        double minSize = a.radius + b.radius;

        if (distSq < minSize * minSize) {
            CollisionInfo info = CollisionDetector.getCollisionInfo(a, b);
            if (info.collided) {
                resolveCollision(a, b, info);
            }
        }
    }

    private void resolveCollision(PolygonBody a, PolygonBody b, CollisionInfo info) {
        double totalMass = a.mass + b.mass;
        double ratioA = b.mass / totalMass;
        double ratioB = a.mass / totalMass;

        double moveX = info.axis.x * info.overlap;
        double moveY = info.axis.y * info.overlap;

        Vector2D relativePos = new Vector2D(b.position.x - a.position.x, b.position.y - a.position.y);
        if (relativePos.dot(info.axis) < 0) {
            moveX *= -1;
            moveY *= -1;
        }

        a.position.x -= moveX * ratioA;
        a.position.y -= moveY * ratioA;
        b.position.x += moveX * ratioB;
        b.position.y += moveY * ratioB;

        double relVelX = b.velocity.x - a.velocity.x;
        double relVelY = b.velocity.y - a.velocity.y;

        double velAlongNormal = relVelX * info.axis.x + relVelY * info.axis.y;

        if (velAlongNormal > 0) return;

        double restitution = 0.8;

        double j = -(1 + restitution) * velAlongNormal;
        j /= (1 / a.mass + 1 / b.mass);

        double impulseX = j * info.axis.x;
        double impulseY = j * info.axis.y;

        a.velocity.x -= impulseX / a.mass;
        a.velocity.y -= impulseY / a.mass;
        b.velocity.x += impulseX / b.mass;
        b.velocity.y += impulseY / b.mass;
    }
}