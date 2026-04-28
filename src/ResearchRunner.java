import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//public class ResearchRunner {
//    public static void main(String[] args) {
//        int[] sizes = {100, 500, 1000, 1500, 2500, 5000, 10000, 20000, 50000, 100000};
//        int[] threadConfigs = {2, 4, 6, 8, 10, 12};
//        double[] scalers = {2.0, 3.0, 5.0};
//        int runs = 3;
//        double dt = 0.016;
//
//        try (FileWriter fw = new FileWriter("research_results_grid_opt2.csv");
//             PrintWriter pw = new PrintWriter(fw)) {
//
//            pw.println("Objects;Threads;Scaler;Time_TP;S_TP;E_TP;Time_FJ;S_FJ;E_FJ");
//
//            System.out.println("N_Obj | Thr | Scal | Time TP (s) | S_TP | E_TP | Time FJ (s) | S_FJ | E_FJ");
//            System.out.println("---------------------------------------------------------------------------------------------");
//
//            for (int size : sizes) {
//                List<PolygonBody> prototypes = new ArrayList<>();
//                PolygonBody.resetIdCounter();
//                for (int i = 0; i < size; i++) {
//                    prototypes.add(new PolygonBody(new Vector2D(Math.random() * 800, Math.random() * 600), 20));
//                }
//
//                for (double scaler : scalers) {
//                    PhysicsEngine seqEngine = new PhysicsEngine(800, 600);
//                    for (PolygonBody p : prototypes) seqEngine.getBodies().add(new PolygonBody(p));
//                    seqEngine.initializeGrid(scaler);
//
//                    for (int i = 0; i < 3; i++) seqEngine.update(dt);
//                    long startSeq = System.nanoTime();
//                    for (int i = 0; i < runs; i++) seqEngine.update(dt);
//                    long endSeq = System.nanoTime();
//                    double tSeq = (endSeq - startSeq) / (double) runs / 1_000_000_000.0;
//
//                    System.out.printf("%5d | %3d | %4.1f | %11.6f | %4.2f | %4.2f | %11.6f | %4.2f | %4.2f\n",
//                            size, 1, scaler, tSeq, 1.0, 1.0, tSeq, 1.0, 1.0);
//                    pw.printf("%d;%d;%.1f;%.6f;%.2f;%.2f;%.6f;%.2f;%.2f\n",
//                            size, 1, scaler, tSeq, 1.0, 1.0, tSeq, 1.0, 1.0);
//
//                    for (int threads : threadConfigs) {
//                        ParallelPhysicsEngine parEngine = new ParallelPhysicsEngine(800, 600, threads);
//                        for (PolygonBody p : prototypes) parEngine.getBodies().add(new PolygonBody(p));
//                        parEngine.initializeGrid(scaler);
//
//                        for (int i = 0; i < 5; i++) parEngine.update(dt);
//                        long startTP = System.nanoTime();
//                        for (int i = 0; i < runs; i++) parEngine.update(dt);
//                        long endTP = System.nanoTime();
//                        double tTP = (endTP - startTP) / (double) runs / 1_000_000_000.0;
//                        double sTP = tSeq / tTP;
//                        double eTP = sTP / threads;
//
//                        for (int i = 0; i < 5; i++) parEngine.updateForkJoin(dt);
//                        long startFJ = System.nanoTime();
//                        for (int i = 0; i < runs; i++) parEngine.updateForkJoin(dt);
//                        long endFJ = System.nanoTime();
//                        double tFJ = (endFJ - startFJ) / (double) runs / 1_000_000_000.0;
//                        double sFJ = tSeq / tFJ;
//                        double eFJ = sFJ / threads;
//
//                        System.out.printf("%5s | %3d | %4.1f | %11.6f | %4.2f | %4.2f | %11.6f | %4.2f | %4.2f\n",
//                                "", threads, scaler, tTP, sTP, eTP, tFJ, sFJ, eFJ);
//                        pw.printf("%d;%d;%.1f;%.6f;%.2f;%.2f;%.6f;%.2f;%.2f\n",
//                                size, threads, scaler, tTP, sTP, eTP, tFJ, sFJ, eFJ);
//
//                        parEngine.shutdown();
//                    }
//                    System.out.println("---------------------------------------------------------------------------------------------");
//                }
//                pw.flush();
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error writing to CSV file: " + e.getMessage());
//        }
//    }
//}

public class ResearchRunner {
    public static void main(String[] args) {
        // runGeneralResearch();

        int[] thresholdSizes = {20000, 100000};
        for (int thresholdSize : thresholdSizes) {
            String UpdateThresholdFilename = "research_threshold_update_" +  thresholdSize + ".csv";
            String CollisionThresholdFilename = "research_threshold_collision_" +  thresholdSize + ".csv";
            runUpdateThresholdResearch(thresholdSize, UpdateThresholdFilename);
            runCollisionThresholdResearch(thresholdSize, CollisionThresholdFilename);
        }
    }

    private static void runGeneralResearch() {
        int[] sizes = {100, 1000, 5000, 10000, 20000, 50000, 100000};
        int[] threadConfigs = {4, 8, 12};
        double[] scalers = {2.0, 3.0, 5.0};
        int runs = 3;
        double dt = 0.016;

        try (PrintWriter pw = new PrintWriter(new FileWriter("research_general.csv"))) {
            pw.println("Objects;Threads;Scaler;Time_TP;S_TP;Time_FJ;S_FJ");
            System.out.println("General Research Started");

            for (int size : sizes) {
                List<PolygonBody> prototypes = generatePrototypes(size);

                for (double scaler : scalers) {
                    double tSeq = measureSequential(prototypes, scaler, dt, runs);

                    for (int threads : threadConfigs) {
                        ParallelPhysicsEngine engine = new ParallelPhysicsEngine(800, 600, threads);
                        for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
                        engine.initializeGrid(scaler);

                        // Warm up & Measure TP
                        double tTP = measureParallel(engine, false, dt, runs);
                        // Warm up & Measure FJ
                        double tFJ = measureParallel(engine, true, dt, runs);

                        pw.printf("%d;%d;%.1f;%.6f;%.2f;%.6f;%.2f\n", size, threads, scaler, tTP, tSeq/tTP, tFJ, tSeq/tFJ);
                        engine.shutdown();
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runUpdateThresholdResearch(int size, String filename) {
        int[] thresholds = {100, 1000, 10000, 50000};
        int threads = 4;
        List<PolygonBody> prototypes = generatePrototypes(size);

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Threshold;Time");
            System.out.println("\nUpdate Threshold Research (N=" + size + ")");

            for (int th : thresholds) {
                UpdatePositionTask.THRESHOLD = th;

                ParallelPhysicsEngine engine = new ParallelPhysicsEngine(800, 600, threads);
                for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
                engine.initializeGrid(3.0);

                double time = measureParallel(engine, true, 0.016, 5);
                pw.printf("%d;%.6f\n", th, time);
                System.out.printf("Update Threshold: %d | Time: %.6f s\n", th, time);
                engine.shutdown();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void runCollisionThresholdResearch(int size, String filename) {
        int[] thresholds = {2, 10, 50};
        int threads = 4;
        List<PolygonBody> prototypes = generatePrototypes(size);

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Threshold;Time");
            System.out.println("\nCollision Threshold Research (N=" + size + ")");

            for (int th : thresholds) {
                CollisionTask.THRESHOLD = th;

                ParallelPhysicsEngine engine = new ParallelPhysicsEngine(800, 600, threads);
                for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
                engine.initializeGrid(3.0);

                double time = measureParallel(engine, true, 0.016, 5);
                pw.printf("%d;%.6f\n", th, time);
                System.out.printf("Collision Threshold: %d | Time: %.6f s\n", th, time);
                engine.shutdown();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static List<PolygonBody> generatePrototypes(int count) {
        List<PolygonBody> list = new ArrayList<>();
        PolygonBody.resetIdCounter();
        for (int i = 0; i < count; i++) {
            list.add(new PolygonBody(new Vector2D(Math.random() * 800, Math.random() * 600), 20));
        }
        return list;
    }

    private static double measureSequential(List<PolygonBody> prototypes, double scaler, double dt, int runs) {
        PhysicsEngine engine = new PhysicsEngine(800, 600);
        for (PolygonBody p : prototypes) engine.getBodies().add(new PolygonBody(p));
        engine.initializeGrid(scaler);
        for (int i = 0; i < 3; i++) engine.update(dt);
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) engine.update(dt);
        return (System.nanoTime() - start) / (double) runs / 1_000_000_000.0;
    }

    private static double measureParallel(ParallelPhysicsEngine engine, boolean isFJ, double dt, int runs) {
        for (int i = 0; i < 5; i++) {
            if (isFJ) engine.updateForkJoin(dt); else engine.update(dt);
        }
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            if (isFJ) engine.updateForkJoin(dt); else engine.update(dt);
        }
        return (System.nanoTime() - start) / (double) runs / 1_000_000_000.0;
    }
}