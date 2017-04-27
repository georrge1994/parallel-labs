package multmatrix;

/**
 * Created by Георгий on 02.04.2017.
 */

class MultiplierThread extends Thread
{
    private final int[][] A;
    private final int[][] B;
    private final int[][] C;

    private final int firstIndex;
    private final int lastIndex;
    private final int sumLength;


    public MultiplierThread(final int[][] A, final int[][] B, final int[][] C, final int firstIndex,
                            final int lastIndex){
        this.A  = A;
        this.B = B;
        this.C = C;
        this.firstIndex   = firstIndex;
        this.lastIndex    = lastIndex;

        sumLength = B.length;
    }

    /**
     * calculation value in one cell
     * */
    private void calcValue(final int row, final int col)
    {
        int sum = 0;
        for (int i = 0; i < sumLength; ++i)
            sum += A[row][i] * B[i][col];
        C[row][col] = sum;
    }


    @Override
    public void run()
    {
        final int colCount = B[0].length;  // count row in result matrix
        for (int index = firstIndex; index < lastIndex; ++index)
            calcValue(index / colCount, index % colCount);
    }
}

public class multiplyMatrixMT
{
    public static int[][] multiplyMatrixMT(final int[][] A, final int[][] B, int threadCount)
    {
        assert threadCount > 0;

        final int rowCount = A.length;
        final int colCount = B[0].length;
        final int[][] result = new int[rowCount][colCount];

        final int cellsForThread = (rowCount * colCount) / threadCount;
        int firstIndex = 0;
        final MultiplierThread[] multiplierThreads = new MultiplierThread[threadCount];


        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;  // index of the last of the cell
            if (threadIndex == 0) {
                // остаток
                lastIndex = rowCount * colCount;
            }
            multiplierThreads[threadIndex] = new MultiplierThread(A, B, result, firstIndex, lastIndex);
            multiplierThreads[threadIndex].start();
            firstIndex = lastIndex;
        }

        // Ожидание завершения потоков.
        try {
            for (final MultiplierThread multiplierThread : multiplierThreads)
                multiplierThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}