package apairport;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kevin Matthew
 */
public class RefuelTruck extends Thread {
    private static BlockingQueue<Plane> refuelQueue = new ArrayBlockingQueue<>(2);
    private static String name;
    private boolean isFull;

    public static BlockingQueue<Plane> getRefuelQueue() {
        return RefuelTruck.refuelQueue;
    }

    public RefuelTruck() {
        name = "Refuel Truck";
        isFull = false;
    }

    //refuel plane
    public static void refuel(Plane plane){
        try {
            System.out.println(AirportTime.now() + RefuelTruck.name + ": " + "Refuelling " 
                + plane.getPlaneName() + "...");
            Thread.sleep(new Random().nextInt(2500) + 500);
            System.out.println(AirportTime.now() + RefuelTruck.name + ": " + "Finished refuelling "
                + plane.getPlaneName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //take fuel from depot
    private void takeFuel(){
        System.out.println(AirportTime.now() + RefuelTruck.name + ": " + "Taking fuel from fuel depot...");
        try {
            Thread.sleep(new Random().nextInt(1500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isFull = true;        
        System.out.println(AirportTime.now() + RefuelTruck.name + ": " + "Ready to refuel plane");
    }

    public void run() {
        try {
            //loop until the simulation should end
            while(ATC.getPlaneCounter() < PlaneGenerator.getTotalPlanes()){
                //if the refuel truck is empty
                if(!isFull){
                    takeFuel();
                }

                //get plane from blocking queue to be refuelled
                Plane plane = refuelQueue.poll(10, TimeUnit.SECONDS);
                //refuel plane
                if(plane != null){
                    refuel(plane);
                    isFull = false;
                    plane.setRefuelled(true);
                    synchronized(plane){
                        plane.notify();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
