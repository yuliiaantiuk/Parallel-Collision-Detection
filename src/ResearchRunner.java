import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ResearchRunner {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double DT = 0.016;
    private static final double RUNS = 20;

    public static void main(String[] args) {
        runGeneralResearch();
//        runScalerResearch();
//        runThresholdResearch();
//        runGeneralResearchScaler();
//        runCollisionThresholdOnly();
//        runUpdateThresholdOnly();
    }

    private static void runGeneralResearch() {
        int[] sizes = {100, 1000, 10000, 20000, 50000, 100000};
        int[] threadConfigs = {2, 4, 6, 8, 10, 12};
        double scaler = 3.0;

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_general_final.csv"))) {
            pw.println("Objects;Threads;Time_Seq;Time_TP;S_TP;Time_FJ;S_FJ");
            System.out.println("General Research Started");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);
                    double tSeq = measureSequential(prototypes, scaler);

                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engine = createEngine(prototypes, threads, scaler);

                        double tTP = measureParallel(engine, false);
                        double tFJ = measureParallel(engine, true);

                        pw.printf("%d;%d;%.6f;%.6f;%.2f;%.6f;%.2f\n", size, threads, tSeq, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                        engine.shutdown();
                    }
                pw.flush();
                System.out.println("Done for size: " + size);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runGeneralResearchScaler() {
        int[] sizes = {1000, 10000, 50000, 100000};
        int[] threadConfigs = {2, 4, 8, 12};
        double[] scalers = {2.0, 3.0, 5.0};

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_general.csv"))) {
            pw.println("Objects;Threads;Scaler;Time_Seq;Time_TP;S_TP;Time_FJ;S_FJ");
            System.out.println("General Research Started...");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);
                for (double sc : scalers) {
                    double tSeq = measureSequential(prototypes, sc);
                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engine = createEngine(prototypes, threads, sc);

                        double tTP = measureParallel(engine, false);
                        double tFJ = measureParallel(engine, true);

                        pw.printf("%d;%d;%.1f;%.6f;%.6f;%.2f;%.6f;%.2f\n",
                                size, threads, sc, tSeq, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                        engine.shutdown();
                    }
                    pw.flush();
                }
                System.out.println("Done for size: " + size);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runScalerResearch() {
        int[] sizes = {20000, 100000};
        int[] threadConfigs = {4, 8};
        double[] scalers = {2.0, 3.0, 5.0};

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_scaler_final.csv"))) {
            pw.println("Objects;Threads;Scaler;Time_Seq;Time_TP;S_TP;Time_FJ;S_FJ");
            System.out.println("Scaler Research Started");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);

                for (double scaler : scalers) {
                    double tSeq = measureSequential(prototypes, scaler);

                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engine = createEngine(prototypes, threads, scaler);

                        double tTP = measureParallel(engine, false);
                        double tFJ = measureParallel(engine, true);

                        pw.printf("%d;%d;%.1f;%.6f;%.6f;%.2f;%.6f;%.2f\n", size, threads, scaler, tSeq, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                        engine.shutdown();
                    }
                    pw.flush();
                }
                pw.flush();
                System.out.println("Done for size: " + size);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runThresholdResearch() {
        int[] sizes = {20000, 100000};
        int[] thUpdate = {1000, 10000};
        int[] thColl = {2, 10};
        int threads = 8;

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_threshold_combined.csv"))) {
            pw.println("Objects;Th_Update;Th_Coll;Time_FJ");
            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);
                for (int tu : thUpdate) {
                    for (int tc : thColl) {
                        UpdatePositionTask.THRESHOLD = tu;
                        CollisionTask.THRESHOLD = tc;
                        ParallelPhysicsEngine engine = createEngine(prototypes, threads, 3.0);
                        double tFJ = measureParallel(engine, true);
                        pw.printf("%d;%d;%d;%.6f\n", size, tu, tc, tFJ);
                        engine.shutdown();
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runUpdateThresholdOnly() {
        int size = 50000;
        int[] thresholds = {100, 500, 1000, 5000, 10000};

        try (PrintWriter pw = new PrintWriter(new FileWriter("threshold_update.csv"))) {
            pw.println("Threshold;Time_FJ");
            for (int th : thresholds) {
                UpdatePositionTask.THRESHOLD = th;
                ParallelPhysicsEngine engine = createEngine(generatePrototypes(size), 4, 3.0);
                double time = measureParallel(engine, true);
                pw.printf("%d;%.6f\n", th, time);
                engine.shutdown();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runCollisionThresholdOnly() {
        int size = 50000;
        int[] thresholds = {2, 4, 8, 16, 32};

        try (PrintWriter pw = new PrintWriter(new FileWriter("threshold_collision.csv"))) {
            pw.println("Threshold;Time_FJ");
            for (int th : thresholds) {
                CollisionTask.THRESHOLD = th;
                ParallelPhysicsEngine engine = createEngine(generatePrototypes(size), 4, 3.0);
                double time = measureParallel(engine, true);
                pw.printf("%d;%.6f\n", th, time);
                engine.shutdown();
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
        PhysicsEngine engine = new PhysicsEngine(800, 600);
        for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
        engine.initializeGrid(scaler);
        for (int i = 0; i < 3; i++) engine.update(DT);
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) engine.update(DT);
        return (System.nanoTime() - start) / (double) RUNS / 1_000_000_000.0;
    }

    private static double measureParallel(ParallelPhysicsEngine engine, boolean isFJ) {
        for (int i = 0; i < 5; i++) {
            if (isFJ) engine.updateForkJoin(DT); else engine.update(DT);
        }
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            if (isFJ) engine.updateForkJoin(DT); else engine.update(DT);
        }
        return (System.nanoTime() - start) / (double) RUNS / 1_000_000_000.0;
    }
}