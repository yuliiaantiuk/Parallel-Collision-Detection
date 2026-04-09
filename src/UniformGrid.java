import java.util.ArrayList;
import java.util.List;

public class UniformGrid {
    private final int cols, rows;
    private final double cellSize;
    private final List<PolygonBody>[][] cells;

    @SuppressWarnings("unchecked")
    public UniformGrid(double width, double height, double cellSize) {
        this.cellSize = cellSize;
        this.cols = (int) Math.ceil(width / cellSize);
        this.rows = (int) Math.ceil(height / cellSize);

        this.cells = new ArrayList[cols][rows];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                cells[i][j] = new ArrayList<>();
            }
        }
    }

    public void clear() {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                cells[i][j].clear();
            }
        }
    }

    public void insert(PolygonBody body) {
        int x = (int) (body.position.x / cellSize);
        int y = (int) (body.position.y / cellSize);

        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            cells[x][y].add(body);
        }
    }

    public List<PolygonBody>[][] getCells() { return cells; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
}