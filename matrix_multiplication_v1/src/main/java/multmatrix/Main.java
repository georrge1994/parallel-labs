package multmatrix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Main {
    private static final Random r =  new Random();

    // so simple
    public int[][] simple_multiplication_v1(int a[][], int b[][],int c[][]){
        for(int i = 0; i < a.length; ++i)
            for(int j = 0; j < a.length; ++j){
                c[i][j] = 0;
                for(int k = 0; k < a.length; ++k)
                    c[i][j] = a[i][k]*b[k][j];
            }
        return c;
    }

    // multiplication with transpose
    public int[][] simple_multiplication_v2(int a[][], int b[][],int c[][]){
        double bt[][] = new double[a.length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                bt[j][i] = b[i][j];
            }
        }

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                int summand = 0;
                for (int k = 0; k < a.length; k++) {
                    summand += a[i][k] * bt[j][k];
                }
                c[i][j] = summand;
            }
        }
        return c;
    }

    // combination cycles
    public int[][] simple_multiplication_v3(int a[][], int b[][],int c[][]){
        double thatColumn[] = new double[a.length];
        for (int j = 0; j < a.length; j++) {
            for (int k = 0; k < a.length; k++) {
                thatColumn[k] = B[k][j];
            }

            for (int i = 0; i < a.length; i++) {
                int[] thisRow = A[i];
                double summand = 0;
                for (int k = 0; k < a.length; k++) {
                    summand += thisRow[k] * thatColumn[k];
                }
                c[i][j] = (int) summand;
            }
        }
        return c;
    }

    // multiply Matrix
    public int[][] multiplyMatrixMT(int a[][], int b[][],int c[][]) {
        multiplyMatrixMT m = new multiplyMatrixMT();
        c = m.multiplyMatrixMT(a, b, Runtime.getRuntime().availableProcessors());
        return c;
    }


   // public int[][] getMPIResult(){}

    @Param({"1", "10", "100", "500", "1000", "2000"})
    private static int SIZE;

    int[][] A, B, C;

    public static void swap(int[] a, int i, int j) {
        int x = a[i];
        a[i] = a[j];
        a[j] = x;
    }

    @Setup
    public void setup() {
        A = new int[SIZE][SIZE];
        B = new int[SIZE][SIZE];
        C = new int[SIZE][SIZE];

        A = fillMatrix(A);
        B = fillMatrix(B);
    }

    @Benchmark
    public void checkIt(Blackhole bh) {
        bh.consume(simple_multiplication_v1(A,B,C));
    }

    /* Fill matrix*/
    private static int[][] fillMatrix(int A[][]){
        for (int i=0;i < A.length;i++){
            for (int j=0;j < A[i].length;j++){
                A[i][j]=(int)(Math.random()*10);
            }
        }
        return A;
    }

    /* Print matrix */
    private static void printMatrix(int A[][]){
        for (int i=0;i < A.length;i++,System.out.println()){
            for (int j=0;j < A[i].length;j++){
                System.out.print(A[i][j]+" ");
            }
        }
        System.out.println("");
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Main.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
