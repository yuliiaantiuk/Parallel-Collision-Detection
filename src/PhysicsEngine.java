import java.util.ArrayList;
import java.util.List;

public class PhysicsEngine {
    public List<PolygonBody> bodies;
    public UniformGrid grid;
    public double width, height;

    public PhysicsEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.bodies = new ArrayList<>();
        this.grid = new UniformGrid(width, height, 70.0); // розмір клітинки потім
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

    public void checkCollisionsInCell(int x, int y) {
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

//    public void resolveCollision(PolygonBody a, PolygonBody b, CollisionInfo info) {
//        PolygonBody first = (a.id < b.id) ? a : b;
//        PolygonBody second = (a.id < b.id) ? b : a;
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

    private void resolveCollision(PolygonBody a, PolygonBody b, CollisionInfo info) {
        PolygonBody first = (a.id < b.id) ? a : b;
        PolygonBody second = (a.id < b.id) ? b : a;

        synchronized (first) {
            synchronized (second) {
                Vector2D normal = info.axis;
                Vector2D relativePos = new Vector2D(b.position.x - a.position.x, b.position.y - a.position.y);
                if (relativePos.dot(normal) < 0) {
                    normal = new Vector2D(-normal.x, -normal.y);
                }

                double relVelX = b.velocity.x - a.velocity.x;
                double relVelY = b.velocity.y - a.velocity.y;
                double velAlongNormal = relVelX * normal.x + relVelY * normal.y;

                double percent = 1.0;
                double slop = 0.01;
                double penetration = Math.max(info.overlap - slop, 0.0);
                double inverseMassSum = 1.0 / a.mass + 1.0 / b.mass;

                double correctionMagnitude = penetration / inverseMassSum * percent;
                Vector2D correction = new Vector2D(normal.x * correctionMagnitude, normal.y * correctionMagnitude);

                a.position.x -= correction.x / a.mass;
                a.position.y -= correction.y / a.mass;
                b.position.x += correction.x / b.mass;
                b.position.y += correction.y / b.mass;

                if (velAlongNormal < 0) {
                    double restitution = 0.95;
                    double j = -(1 + restitution) * velAlongNormal;
                    j /= inverseMassSum;

                    Vector2D impulse = new Vector2D(normal.x * j, normal.y * j);

                    a.velocity.x -= impulse.x / a.mass;
                    a.velocity.y -= impulse.y / a.mass;
                    b.velocity.x += impulse.x / b.mass;
                    b.velocity.y += impulse.y / b.mass;
                }
            }
        }
    }
}