import java.util.ArrayList;
import java.util.List;

public class CollisionDetector {
    public static CollisionInfo getCollisionInfo(PolygonBody a, PolygonBody b) {
        CollisionInfo info = new CollisionInfo();
        List<Vector2D> axes = getAxes(a, b);

        double minOverlap = Double.MAX_VALUE;
        Vector2D smallestAxis = null;

        for (Vector2D axis : axes) {
            Projection p1 = project(a, axis);
            Projection p2 = project(b, axis);

            if (!p1.overlaps(p2)) {
                return info;
            } else {
                double overlap = p1.getOverlap(p2);
                if (overlap < minOverlap) {
                    minOverlap = overlap;
                    smallestAxis = axis;
                }
            }
        }

        info.collided = true;
        info.axis = smallestAxis;
        info.overlap = minOverlap;
        return info;
    }

    private static List<Vector2D> getAxes(PolygonBody a, PolygonBody b) {
        List<Vector2D> axes = new ArrayList<>();
        axes.addAll(getNormals(a.vertices));
        axes.addAll(getNormals(b.vertices));
        return axes;
    }

    private static List<Vector2D> getNormals(List<Vector2D> vertices) {
        List<Vector2D> normals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            Vector2D p1 = vertices.get(i);
            Vector2D p2 = vertices.get((i + 1) % vertices.size());

            double edgeX = p2.x - p1.x;
            double edgeY = p2.y - p1.y;

            Vector2D normal = new Vector2D(-edgeY, edgeX);

            double length = Math.sqrt(normal.x * normal.x + normal.y * normal.y);
            normal.x /= length;
            normal.y /= length;

            normals.add(normal);
        }
        return normals;
    }

    public static Projection project(PolygonBody body, Vector2D axis) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (Vector2D vertex : body.vertices) {
            // координати відносно простору, а не центру об'єкта
            double worldVertexX = body.position.x + vertex.x;
            double worldVertexY = body.position.y + vertex.y;

            double dot = worldVertexX * axis.x + worldVertexY * axis.y;
            // найлівіша і найправіша точка
            if (dot < min) min = dot;
            if (dot > max) max = dot;
        }
        return new Projection(min, max);
    }
}