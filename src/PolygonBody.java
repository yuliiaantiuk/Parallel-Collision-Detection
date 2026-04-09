import java.util.ArrayList;
import java.util.List;

public class PolygonBody {
    public Vector2D position;
    public Vector2D velocity;
    public List<Vector2D> vertices;
    public double radius;
    public double mass;

    public PolygonBody(Vector2D pos, double radius) {
        this.position = pos;
        this.velocity = new Vector2D(Math.random() * 300 - 100, Math.random() * 300 - 100);
        this.radius = radius;
        this.vertices = generateRandomPolygon(radius);
        this.mass = radius * radius;
    }

    private List<Vector2D> generateRandomPolygon(double radius) {
        List<Vector2D> v = new ArrayList<>();
        int verticesCount = 3 + (int)(Math.random() * 8);

        for (int i = 0; i < verticesCount; i++) {
            double angle = (2 * Math.PI * i) / verticesCount;
            double distortedRadius = radius * (0.7 + Math.random() * 0.3);

            v.add(new Vector2D(Math.cos(angle) * distortedRadius, Math.sin(angle) * distortedRadius));
        }
        return v;
    }

    public void update(double dt, double width, double height) {
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        if (position.x < radius) {
            position.x = radius;
            velocity.x = Math.abs(velocity.x);
        } else if (position.x > width - radius) {
            position.x = width - radius;
            velocity.x = -Math.abs(velocity.x);
        }

        if (position.y < radius) {
            position.y = radius;
            velocity.y = Math.abs(velocity.y);
        } else if (position.y > height - radius) {
            position.y = height - radius;
            velocity.y = -Math.abs(velocity.y);
        }
    }
}