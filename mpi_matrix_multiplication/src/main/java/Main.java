import mpi.*;           // for mpiJava
import java.net.*;      // for InetAddress
import java.util.*;     // for Date

public class Main {

    private int myrank = 0;
    private int nprocs = 0;

    // matrices
    private double A[], B[], C[];

    // messages
    int averows;               // average #rows allocated to each rank
    int extra;                 // extra #rows allocated to some ranks
    int offset[] = new int[1]; // offset in row
    int rows[] = new int[1];   // the actual # rows allocated to each rank
    int mtype;                 // message type (tagFromMaster or tagFromWorker )

    final static int tagFromMaster = 1;
    final static int tagFromWorker = 2;
    final static int master = 0;

    // print option
    boolean isPrint = false;


    /**
     *  Matrix initialization
     */
    private void init( int size ) {
        // Initialize matrices

        for ( int i = 0; i < size; i++ )
            for ( int j = 0; j < size; j++ )
                A[i * size + j] = (int)(Math.random()*10);

        for ( int i = 0; i < size; i++ )
            for ( int j = 0; j < size; j++ )
                B[i * size + j] = (int)(Math.random()*10);
    }

    /**
     * Computes a multiplication for rows
     */
    private void compute( int size ) {

        for ( int k = 0; k < size; k++ )
            for ( int i = 0; i <= rows[0]; i++ )
                for ( int j = 0; j < size; j++ )
                    C[i * size + k] += A[i * size + j] * B[j *size + k];

    }

    /**
     * Printing matrices
     */
    private void print( double array[] ) {
        if ( myrank == 0 && isPrint == true ) {
            int size = ( int )Math.sqrt( ( double )array.length );
            for ( int i = 0; i < size; i++ ){
                for ( int j = 0; j < size; j++ ) {
                    System.out.print( array[i * size + j] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }


    public Main( int size, boolean option ) throws MPIException {
        myrank = MPI.COMM_WORLD.Rank( );
        nprocs = MPI.COMM_WORLD.Size( );

        A = new double[size * size];
        B = new double[size * size];
        C = new double[size * size];

        isPrint = option;

        if ( myrank == 0 ) {
            // Initialize matrices.
            init( size );
            System.out.println( "array a:" );
            print( A );
            System.out.println( "array b:" );
            print( B );

            // Construct message components.
            averows = size / nprocs;
            extra = size % nprocs;
            offset[0] = 0;
            mtype = tagFromMaster;

            // Start timer.
            Date startTime = new Date( );

            // Send matrices to each worker.
            for ( int rank = 0; rank < nprocs; rank++ ) {
                if(rank < extra){
                    rows[0] = averows + 1;
                }else{
                    rows[0] = averows;
                }

                System.out.println( "sending " + rows[0] + " rows to rank " +
                        rank );
                if ( rank != 0 ) {
                    MPI.COMM_WORLD.Send( offset, 0, 1, MPI.INT, rank, mtype );
                    MPI.COMM_WORLD.Send( rows, 0, 1, MPI.INT, rank, mtype );
                    MPI.COMM_WORLD.Send( A, offset[0] * size, rows[0] * size,
                            MPI.DOUBLE, rank, mtype );
                    MPI.COMM_WORLD.Send( B, 0, size * size, MPI.DOUBLE, rank,
                            mtype );
                }
                offset[0] += rows[0];
            }

            // Perform matrix multiplication.
            compute( size );

            // Collect results from each worker.
            int mytpe = tagFromWorker;
            for ( int source = 1; source < nprocs; source++ ) {
                MPI.COMM_WORLD.Recv( offset, 0, 1, MPI.INT, source, mtype );
                MPI.COMM_WORLD.Recv( rows, 0, 1, MPI.INT, source, mtype );
                MPI.COMM_WORLD.Recv( C, offset[0] * size, rows[0] * size,
                        MPI.DOUBLE, source, mtype );
            }

            // Stop timer.
            Date endTime = new Date( );

            // Print out results
            System.out.println( "result c:" );
            print( C );

            System.out.println( "time elapsed = " +
                    ( endTime.getTime( ) - startTime.getTime( ) ) +
                    " msec" );
        }
        else {
            // I'm a worker.

            // Receive matrices.
            int mtype = tagFromMaster;
            MPI.COMM_WORLD.Recv( offset, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Recv( rows, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Recv( A, 0, rows[0] * size, MPI.DOUBLE, master,
                    mtype );
            MPI.COMM_WORLD.Recv( B, 0, size * size, MPI.DOUBLE, master,
                    mtype );

            // Perform matrix multiplication.
            compute( size );

            // Send results to the master.
            MPI.COMM_WORLD.Send( offset, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Send( rows, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Send( C, 0, rows[0] * size, MPI.DOUBLE, master,
                    mtype );
        }

        try {
            // Print out a complication message.
            InetAddress inetaddr = InetAddress.getLocalHost( );
            String ipname = inetaddr.getHostName( );
            System.out.println( "rank[" + myrank + "] at " + ipname +
                    ": multiplication completed" );
        } catch ( UnknownHostException e ) {
            System.err.println( e );
        }
    }

    /**
     *
     * @param args Receive the matrix size and the print option in args[0] and
     *             args[1]
     */
    public static void main( String[] args ) {
        // Check # args.

        // Start the MPI library.
        MPI.Init( args );

        // Will initialize size[0] with args[1] and option with args[2] (y | n)
        int size[] = new int[1];
        size[0] = 11;
        boolean option[] = new boolean[1];
        option[0] =  true;

        // Broadcast size and option to all workers.
        MPI.COMM_WORLD.Bcast( size, 0, 1, MPI.INT, master );
        MPI.COMM_WORLD.Bcast( option, 0, 1, MPI.BOOLEAN, master );

        // Compute matrix multiplication in both master and workers.
        new Main( size[0], option[0] );

        // Terminate the MPI library.
        MPI.Finalize( );
    }
}