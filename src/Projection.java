public class Projection {
    private final double min;
    private final double max;

    public Projection(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean overlaps(Projection other) {
        return !(this.max < other.min || other.max < this.min);
    }

    public double getOverlap(Projection other) {
        if (!overlaps(other)) return 0;
        return Math.min(this.max, other.max) - Math.max(this.min, other.min);
    }
}