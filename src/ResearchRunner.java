import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResearchRunner {
    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000, 1500, 2500, 5000, 10000, 20000, 50000, 100000};
        int[] threadConfigs = {2, 4, 6, 8, 10, 12};
        int runs = 5;
        double dt = 0.016;

        try (FileWriter fw = new FileWriter("research_results.csv");
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("Objects;Threads;AvgTime;Speedup;Efficiency");

            System.out.println("N_Objects | Threads | Avg Time (s) | Speedup (S) | Efficiency (E)");
            System.out.println("-------------------------------------------------------------------");

            for (int size : sizes) {
                PhysicsEngine seqEngine = new PhysicsEngine(800, 600);
                generateForEngine(seqEngine, size);

                for(int i=0; i<3; i++) seqEngine.update(dt);

                long startSeq = System.nanoTime();
                for(int i=0; i<runs; i++) seqEngine.update(dt);
                long endSeq = System.nanoTime();
                double tSeq = (endSeq - startSeq) / (double)runs / 1_000_000_000.0;

                System.out.printf("%9d | %7d | %12.6f | %11.2f | %14.2f\n", size, 1, tSeq, 1.0, 1.0);
                pw.printf("%d;%d;%.6f;%.2f;%.2f\n", size, 1, tSeq, 1.0, 1.0);

                for (int threads : threadConfigs) {
                    if (threads == 1) continue;

                    ParallelPhysicsEngine parEngine = new ParallelPhysicsEngine(800, 600, threads);
                    generateForEngine(parEngine, size);

                    for(int i=0; i<5; i++) parEngine.update(dt);

                    long startPar = System.nanoTime();
                    for(int i=0; i<runs; i++) parEngine.update(dt);
                    long endPar = System.nanoTime();

                    double tPar = (endPar - startPar) / (double)runs / 1_000_000_000.0;
                    double speedup = tSeq / tPar;
                    double efficiency = speedup / threads;

                    System.out.printf("%9s | %7d | %12.6f | %11.2f | %14.2f\n", "", threads, tPar, speedup, efficiency);
                    pw.printf("%d;%d;%.6f;%.2f;%.2f\n", size, threads, tPar, speedup, efficiency);

                    parEngine.shutdown();
                }
                System.out.println("-------------------------------------------------------------------");
                pw.flush();
            }

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }

//        System.out.println("N_Objects | Threads | Avg Time (s) | Speedup (S) | Efficiency (E)");
//        System.out.println("-------------------------------------------------------------------");
//
//        for (int size : sizes) {
//            PhysicsEngine seqEngine = new PhysicsEngine(800, 600);
//            generateForEngine(seqEngine, size);
//
//            for(int i=0; i<2; i++) seqEngine.update(dt);
//
//            long startSeq = System.nanoTime();
//            for(int i=0; i<runs; i++) seqEngine.update(dt);
//            long endSeq = System.nanoTime();
//            double tSeq = (endSeq - startSeq) / (double)runs / 1_000_000_000.0;
//
//            System.out.printf("%9d | %7d | %12.6f | %11.2f | %14.2f\n", size, 1, tSeq, 1.0, 1.0);
//
//            for (int threads : threadConfigs) {
//                ParallelPhysicsEngine parEngine = new ParallelPhysicsEngine(800, 600, threads);
//                generateForEngine(parEngine, size);
//
//                for(int i=0; i<5; i++) parEngine.update(dt);
//
//                long startPar = System.nanoTime();
//                for(int i=0; i<runs; i++) parEngine.update(dt);
//                long endPar = System.nanoTime();
//
//                double tPar = (endPar - startPar) / (double)runs / 1_000_000_000.0;
//                double speedup = tSeq / tPar;
//                double efficiency = speedup / threads;
//
//                System.out.printf("%9s | %7d | %12.6f | %11.2f | %14.2f\n", "", threads, tPar, speedup, efficiency);
//
//                parEngine.shutdown();
//            }
//            System.out.println("-------------------------------------------------------------------");
//        }
    }

    private static void generateForEngine(PhysicsEngine engine, int count) {
        PolygonBody.resetIdCounter();
        for (int i = 0; i < count; i++) {
            engine.getBodies().add(new PolygonBody(new Vector2D(Math.random()*800, Math.random()*600), 20));
        }
    }
}
