package apairport;

import java.util.Random;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author Kevin Matthew
 */
public class PlaneGenerator extends Thread {
    private Lock runwayLock;
    //set total planes in simulation
    private static final int TOTAL_PLANES = 6;

    public PlaneGenerator(Lock lock) {
        this.runwayLock = lock;
    }

    //create new plane threads
    private void generatePlane(int i){
        Plane plane = new Plane("PLANE " + i, runwayLock);
        Thread planeThread = new Thread(plane);
        planeThread.start();
    }


    public static int getTotalPlanes(){
        return TOTAL_PLANES;
    }

    public void run() {

        try {
            //generate plane every 0-3 seconds
            for(int i = 1; i <= TOTAL_PLANES; i++){
                generatePlane(i);
                Thread.sleep(new Random().nextInt(3000));
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
