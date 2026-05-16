import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ResearchRunner {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final double DT = 0.016;
    private static final double RUNS = 20;

    public static void main(String[] args) {
        runThresholdResearch();
        runScalerResearch();
        runGeneralResearch();
    }

    private static void runGeneralResearch() {
        int[] sizes = {1000, 10000, 20000, 50000, 100000};
        int[] threadConfigs = {2, 4, 6, 8, 10, 12};
        double scaler = 2.0;

        System.out.println("=== GENERAL RESEARCH ===");
        try (PrintWriter pw = new PrintWriter(new FileWriter("research_general_final.csv"))) {
            pw.println("Objects;Threads;Time_Seq;Time_TP;S_TP;Time_FJ;S_FJ");
            System.out.println("General Research Started");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);
                    double tSeq = measureSequential(prototypes, scaler);

                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engineTP = createEngine(prototypes, threads, scaler);
                        double tTP = measureParallel(engineTP, false);
                        engineTP.shutdown();

                        ParallelPhysicsEngine engineFJ = createEngine(prototypes, threads, scaler);
                        double tFJ = measureParallel(engineFJ, true);
                        engineFJ.shutdown();

                        pw.printf("%d;%d;%.6f;%.6f;%.2f;%.6f;%.2f\n", size, threads, tSeq, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                    }
                pw.flush();
                System.out.println("Done for size: " + size);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runScalerResearch() {
        int[] sizes = {20000, 50000, 100000};
        int[] threadConfigs = {2, 4, 6, 8, 10};
        double[] scalers = {2.0, 3.0, 5.0};

        System.out.println("=== SCALER RESEARCH ===");

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_scaler_final.csv"))) {
            pw.println("Objects;Threads;Scaler;Time_Seq;Time_TP;S_TP;Time_FJ;S_FJ");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);

                for (double scaler : scalers) {
                    double tSeq = measureSequential(prototypes, scaler);

                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engineTP = createEngine(prototypes, threads, scaler);
                        double tTP = measureParallel(engineTP, false);
                        engineTP.shutdown();

                        ParallelPhysicsEngine engineFJ = createEngine(prototypes, threads, scaler);
                        double tFJ = measureParallel(engineFJ, true);
                        engineFJ.shutdown();

                        pw.printf("%d;%d;%.1f;%.6f;%.6f;%.2f;%.6f;%.2f\n", size, threads, scaler, tSeq, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                    }
                    pw.flush();
                }
                pw.flush();
                System.out.println("Done for size: " + size);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runThresholdResearch() {
        int[] sizes = {1000, 20000, 50000, 100000};
        int[] thUpdate = {100, 1000, 10000};
        int[] thColl = {2, 5, 10};
        int[] threads = {4, 8, 12};
        double scaler = 2.0;

        System.out.println("=== THRESHOLD RESEARCH ===");

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_threshold_combined.csv"))) {
            pw.println("Objects;Threads;Th_Update;Th_Coll;Time_Seq;Time_FJ;S_FJ");
            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);
                double tSeq = measureSequential(prototypes, scaler);
                for(int thread : threads) {
                    for (int tu : thUpdate) {
                        for (int tc : thColl) {
                            UpdatePositionTask.THRESHOLD = tu;
                            CollisionTask.THRESHOLD = tc;
                            ParallelPhysicsEngine engine = createEngine(prototypes, thread, scaler);
                            double tFJ = measureParallel(engine, true);
                            pw.printf("%d;%d;%d;%d;%.6f;%.6f;%.2f\n", size, thread, tu, tc, tSeq, tFJ, tSeq/tFJ);
                        }
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static ParallelPhysicsEngine createEngine(List<PolygonBody> prototypes, int threads, double sc) {
        ParallelPhysicsEngine engine = new ParallelPhysicsEngine(WIDTH, HEIGHT, threads);
        for (PolygonBody p : prototypes) {
            engine.getBodies().add(new PolygonBody(p));
        }
        engine.initializeGrid(sc);
        return engine;
    }

    private static List<PolygonBody> generatePrototypes(int count) {
        List<PolygonBody> list = new ArrayList<>();
        PolygonBody.resetIdCounter();
        for (int i = 0; i < count; i++) {
            list.add(new PolygonBody(new Vector2D(Math.random() * 800, Math.random() * 600), 20));
        }
        return list;
    }

    private static double measureSequential(List<PolygonBody> prototypes, double scaler) {
        PhysicsEngine engine = new PhysicsEngine(WIDTH, HEIGHT);
        for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
        engine.initializeGrid(scaler);
        for (int i = 0; i < 2; i++) engine.update(DT);

        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) engine.update(DT);
        return (System.nanoTime() - start) / (double) RUNS / 1_000_000_000.0;
    }

    private static double measureParallel(ParallelPhysicsEngine engine, boolean isFJ) {
        for (int i = 0; i < 2; i++) {
            if (isFJ) engine.updateForkJoin(DT); else engine.update(DT);
        }

        long start, end;
        if (isFJ) {
            start = System.nanoTime();
            for (int i = 0; i < RUNS; i++) {
                engine.updateForkJoin(DT);
            }
            end = System.nanoTime();
        } else {
            start = System.nanoTime();
            for (int i = 0; i < RUNS; i++) {
                engine.update(DT);
            }
            end = System.nanoTime();
        }
        return (end - start) / (double) RUNS / 1_000_000_000.0;
    }
}