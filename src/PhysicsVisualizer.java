import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PhysicsVisualizer extends JPanel {
    private enum EngineMode { SEQUENTIAL, THREAD_POOL, FORK_JOIN }
    private EngineMode currentMode = EngineMode.SEQUENTIAL;

    private final PhysicsEngine sequentialEngine;
    private final ParallelPhysicsEngine parallelEngine;

    private final int width = 800;
    private final int height = 600;

    public PhysicsVisualizer(int numThreads) {
        sequentialEngine = new PhysicsEngine(width, height);
        parallelEngine = new ParallelPhysicsEngine(width, height, numThreads);

        generateInitialBodies(100);

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.WHITE);

        setupControlPanel();
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();

        JButton seqBtn = new JButton("Sequential");
        JButton tpBtn = new JButton("Thread Pool");
        JButton fjBtn = new JButton("ForkJoin");

        seqBtn.addActionListener(e -> currentMode = EngineMode.SEQUENTIAL);
        tpBtn.addActionListener(e -> currentMode = EngineMode.THREAD_POOL);
        fjBtn.addActionListener(e -> currentMode = EngineMode.FORK_JOIN);

        controlPanel.add(seqBtn);
        controlPanel.add(tpBtn);
        controlPanel.add(fjBtn);
    }

    private void generateInitialBodies(int count) {
        PolygonBody.resetIdCounter();
        for (int i = 0; i < count; i++) {
            double radius = 10 + Math.random() * 20;
            Vector2D pos = new Vector2D(
                    radius + Math.random() * (width - 2 * radius),
                    radius + Math.random() * (height - 2 * radius)
            );

            PolygonBody body = new PolygonBody(pos, radius);

            sequentialEngine.getBodies().add(new PolygonBody(body));
            parallelEngine.getBodies().add(new PolygonBody(body));
        }

        sequentialEngine.initializeGrid(3.0);
        parallelEngine.initializeGrid(3.0);
    }

    public void startSimulation() {
        long lastTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            if (deltaTime > 0.05) deltaTime = 0.05;

            switch (currentMode) {
                case SEQUENTIAL:
                    sequentialEngine.update(deltaTime);
                    break;
                case THREAD_POOL:
                    parallelEngine.update(deltaTime);
                    break;
                case FORK_JOIN:
                    parallelEngine.updateForkJoin(deltaTime);
                    break;
            }

            repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<PolygonBody> bodiesToDraw = (currentMode == EngineMode.SEQUENTIAL)
                ? sequentialEngine.getBodies()
                : parallelEngine.getBodies();

        for (PolygonBody body : bodiesToDraw) {
            drawPolygon(g2, body);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Mode: " + currentMode, 20, 30);
        g2.drawString("Objects: " + bodiesToDraw.size(), 20, 70);
    }

    private void drawPolygon(Graphics2D g2, PolygonBody body) {
        Path2D polygonPath = new Path2D.Double();
        Vector2D firstV = body.vertices.get(0);
        polygonPath.moveTo(body.position.x + firstV.x, body.position.y + firstV.y);

        for (int i = 1; i < body.vertices.size(); i++) {
            Vector2D v = body.vertices.get(i);
            polygonPath.lineTo(body.position.x + v.x, body.position.y + v.y);
        }
        polygonPath.closePath();

        g2.setColor(new Color(100, 150, 255, 150));
        g2.fill(polygonPath);
        g2.setColor(Color.BLUE);
        g2.draw(polygonPath);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Physics Engine Comparison");
        PhysicsVisualizer visualizer = new PhysicsVisualizer(8); // 8 потоків

        JPanel btnPanel = new JPanel();
        JButton b1 = new JButton("Sequential");
        JButton b2 = new JButton("Thread Pool");
        JButton b3 = new JButton("ForkJoin");

        b1.addActionListener(e -> visualizer.currentMode = EngineMode.SEQUENTIAL);
        b2.addActionListener(e -> visualizer.currentMode = EngineMode.THREAD_POOL);
        b3.addActionListener(e -> visualizer.currentMode = EngineMode.FORK_JOIN);

        btnPanel.add(b1); btnPanel.add(b2); btnPanel.add(b3);

        frame.setLayout(new BorderLayout());
        frame.add(visualizer, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(visualizer::startSimulation).start();
    }
}