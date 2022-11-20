package apairport;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Kevin Matthew
 */
public class Passenger extends Thread {
    private String name;
    int id;
    Plane plane;
    private CountDownLatch latch;

    Purpose purpose;
    
    public Passenger(int id, Plane plane, Purpose purpose, CountDownLatch latch){
        this.name = "Passenger";
        this.id = id;
        this.plane = plane;
        this.purpose = purpose;
        this.latch = latch;
    }

    //disembark from plane
    public void disembark(Plane plane){
        try {
            //get through plane door 1 by 1
            plane.getPlaneSemaphore().acquire();
            plane.decrementPassengerCount();
            System.out.println(AirportTime.now() + name + " " + id + ": " + 
                "Disembarked from " + plane.getPlaneName() + " [" + 
                plane.getPassengerCount() + "/" + plane.getTotalDisembarking() + "]");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            plane.getPlaneSemaphore().release();
        }
    }

    //embark from plane
    public void embark(Plane plane){
        try {
            //get through plane door 1 by 1
            plane.getPlaneSemaphore().acquire();
            plane.incrementPassengerCount();
            System.out.println(AirportTime.now() + name + " " + id + ": " + 
                "Boarding to " + plane.getPlaneName() + " [" + 
                plane.getPassengerCount() + "/" + plane.getTotalEmbarking() + "]");
            Thread.sleep(100);
            ATC.incrementPassenger();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            plane.getPlaneSemaphore().release();
        }
    }

    public void run() {
        while(true){
            //disembarking passengers
            if(purpose.equals(Purpose.DISEMBARK)){
                //if plane has docked
                if(!plane.isDocked()){
                    try {
                        //wait until plane is docked
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } 
                } else {
                    //disembark from plane
                    disembark(plane);
                    latch.countDown();
                    break;
                }
            }
            //embarking passengers
            else {
                //if plane has been cleaned by the cabin crew
                if(!plane.isCleaned()){
                    try {
                        //wait until plane is cleaned
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //embark to plane
                    embark(plane);
                    latch.countDown();
                    break;
                }
            }
        }
    }
}
