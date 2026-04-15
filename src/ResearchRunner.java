import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResearchRunner {
    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000, 1500, 2500, 5000, 10000, 20000, 50000, 100000};
        int[] threadConfigs = {2, 4, 6, 8, 10, 12};
        int runs = 5;
        double dt = 0.016;

        try (FileWriter fw = new FileWriter("research_results_updated.csv");
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("Objects;Threads;Time_TP;S_TP;E_TP;Time_FJ;S_FJ;E_FJ");

            System.out.println("N_Obj | Thr | Time TP (s) | S_TP | E_TP | Time FJ (s) | S_FJ | E_FJ");
            System.out.println("---------------------------------------------------------------------------------------");

            for (int size : sizes) {
                PhysicsEngine seqEngine = new PhysicsEngine(800, 600);
                generateForEngine(seqEngine, size);

                for(int i=0; i<3; i++) seqEngine.update(dt);

                long startSeq = System.nanoTime();
                for(int i=0; i<runs; i++) seqEngine.update(dt);
                long endSeq = System.nanoTime();
                double tSeq = (endSeq - startSeq) / (double)runs / 1_000_000_000.0;

                System.out.printf("%5d | %3d | %11.6f | %4.2f | %4.2f | %11.6f | %4.2f | %4.2f\n",
                        size, 1, tSeq, 1.0, 1.0, tSeq, 1.0, 1.0);
                pw.printf("%d;%d;%.6f;%.2f;%.2f;%.6f;%.2f;%.2f\n",
                        size, 1, tSeq, 1.0, 1.0, tSeq, 1.0, 1.0);

                for (int threads : threadConfigs) {
                    ParallelPhysicsEngine parEngine = new ParallelPhysicsEngine(800, 600, threads);
                    generateForEngine(parEngine, size);

                    for(int i=0; i<5; i++) parEngine.update(dt);

                    for(int i = 0; i < 5; i++) parEngine.update(dt); // Розігрів
                    long startTP = System.nanoTime();
                    for(int i = 0; i < runs; i++) parEngine.update(dt);
                    long endTP = System.nanoTime();
                    double tTP = (endTP - startTP) / (double)runs / 1_000_000_000.0;
                    double sTP = tSeq / tTP;
                    double eTP = sTP / threads;

                    for(int i = 0; i < 5; i++) parEngine.updateForkJoin(dt);

                    long startFJ = System.nanoTime();
                    for(int i = 0; i < runs; i++) parEngine.updateForkJoin(dt);
                    long endFJ = System.nanoTime();
                    double tFJ = (endFJ - startFJ) / (double)runs / 1_000_000_000.0;
                    double sFJ = tSeq / tFJ;
                    double eFJ = sFJ / threads;

                    System.out.printf("%5s | %3d | %11.6f | %4.2f | %4.2f | %11.6f | %4.2f | %4.2f\n",
                            "", threads, tTP, sTP, eTP, tFJ, sFJ, eFJ);
                    pw.printf("%d;%d;%.6f;%.2f;%.2f;%.6f;%.2f;%.2f\n",
                            size, threads, tTP, sTP, eTP, tFJ, sFJ, eFJ);

                    parEngine.shutdown();
                }
                System.out.println("---------------------------------------------------------------------------------------");
                pw.flush();
            }

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    private static void generateForEngine(PhysicsEngine engine, int count) {
        PolygonBody.resetIdCounter();
        for (int i = 0; i < count; i++) {
            engine.getBodies().add(new PolygonBody(new Vector2D(Math.random()*800, Math.random()*600), 20));
        }
    }
}
