import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PhysicsVisualizer extends JPanel {
    private final PhysicsEngine sequentialEngine;
    ParallelPhysicsEngine parallelEngine;
    private final int width = 800;
    private final int height = 600;

    public PhysicsVisualizer(int num_threads) {
        sequentialEngine = new PhysicsEngine(width, height);
        parallelEngine = new ParallelPhysicsEngine(width, height, num_threads);
        generateInitialBodies(30);

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.WHITE);
    }

    private void generateInitialBodies(int count) {
        for (int i = 0; i < count; i++) {
            double radius = 15 + Math.random() * 35;
            Vector2D pos = new Vector2D(
                    radius + Math.random() * (width - 2 * radius),
                    radius + Math.random() * (height - 2 * radius)
            );

            PolygonBody body = new PolygonBody(pos, radius);
            // sequentialEngine.getBodies().add(body);
            parallelEngine.getBodies().add(body);
            // sequentialEngine.initializeGrid(3.0);
            parallelEngine.initializeGrid(3.0);
        }
    }

    public void startSimulation() {
        long lastTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            if (deltaTime > 0.05) deltaTime = 0.05;

            long startCalc = System.nanoTime();
            // sequentialEngine.update(deltaTime);
            parallelEngine.update(deltaTime);
            long endCalc = System.nanoTime();

            if (System.currentTimeMillis() % 1000 < 15) {
                double ms = (endCalc - startCalc) / 1_000_000.0;
                System.out.printf("Objects: %d | Calc Time: %.3f ms\n", sequentialEngine.getBodies().size(), ms);
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

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));

//        for (PolygonBody body : sequentialEngine.getBodies()) {
//            drawPolygon(g2, body);
//        }
        for (PolygonBody body : parallelEngine.getBodies()) {
            drawPolygon(g2, body);
        }
    }

    private void drawPolygon(Graphics2D g2, PolygonBody body) {
        if (body.vertices.isEmpty()) return;

        Path2D polygonPath = new Path2D.Double();

        Vector2D firstV = body.vertices.get(0);
        polygonPath.moveTo(body.position.x + firstV.x, body.position.y + firstV.y);

        for (int i = 1; i < body.vertices.size(); i++) {
            Vector2D v = body.vertices.get(i);
            polygonPath.lineTo(body.position.x + v.x, body.position.y + v.y);
        }

        polygonPath.closePath();

        g2.setColor(new Color(100, 150, 255, 100));
        g2.fill(polygonPath);

        g2.setColor(Color.BLUE);
        g2.draw(polygonPath);
    }

    public static void main(String[] args) {
//        JFrame frame = new JFrame("Collision Detection Sequential 1");
//        PhysicsVisualizer visualizer = new PhysicsVisualizer();
//
//        frame.add(visualizer);
//        frame.pack();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//        new Thread(visualizer::startSimulation).start();

        JFrame frame = new JFrame("Collision Detection Parallel 1");
        PhysicsVisualizer visualizer = new PhysicsVisualizer(4);

        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new Thread(visualizer::startSimulation).start();


//        int[] threads = {2, 4, 6, 8, 10, 12};
//        int[] sizes = {100, 500, 1000, 1500, 2500, 5000, 10000, 20000, 50000, 100000};
//
//        for (int size : sizes) {
//            for (int thread : threads) {
//                PhysicsVisualizer visualizer = new PhysicsVisualizer(thread);
//                visualizer.runBenchmark(size);
//            }
//        }

    }

    public void runBenchmark(int objectCount) {
        sequentialEngine.getBodies().clear();
        generateInitialBodies(objectCount);

        double dt = 0.016;
        long totalTime = 0;
        int runs = 5;

        parallelEngine.update(dt);

        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            parallelEngine.update(dt);
            long end = System.nanoTime();

            totalTime += (end - start);
        }

        long averageTime = totalTime / runs;
        System.out.println("Average time for " + objectCount + " objects: " + (averageTime / 1_000_000_000.0) + " s");
    }
}