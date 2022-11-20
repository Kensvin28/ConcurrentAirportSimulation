package apairport;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author Kevin Matthew
 */
public class ATC {
    private static final String name = "ATC";
    private static Lock runwayLock;
    private static Semaphore availableGate = new Semaphore(2);
    private static AtomicReferenceArray<Plane> gates;
    private static int planeCounter;
    private static AtomicInteger passengersBoardedCounter = new AtomicInteger();
    private static long totalWaitingTime = 0;
    private static LinkedList<Long> waitingTimeList = new LinkedList<>();

    public ATC(AtomicReferenceArray<Plane> gates){
        ATC.gates = gates;
    }
    
    //confirm plane landing
    public static void confirmLand(Plane plane) {
            //if gate is not available, then plane should wait
            if(availableGate.availablePermits() == 0){
                System.out.println(AirportTime.now() + name + ": " + plane.getPlaneName() + 
                    ", landing request received. Please wait in circle queue");
            }

            try {
                //acquire gate
                availableGate.acquire();
                allocateGate(plane);
                //lock runway
                runwayLock = plane.getLock();
                runwayLock.lock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //permit landing
            System.out.println(AirportTime.now() + name + ": " + plane.getPlaneName() + 
                ", you can proceed for landing");
    }
    
    //confirm plane take off
    public static void confirmTakeOff(Plane plane) {
        try{
            //lock runway
            runwayLock = plane.getLock();
            runwayLock.lock();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        //allow take off
        System.out.println(AirportTime.now() + name + ": " + plane.getPlaneName() + 
            " permitted to take off");
    }

    //if false (not occupied), allocate gate
    public static void allocateGate(Plane plane) {
        int space = place();
        gates.getAndSet(space, plane);
        int gateNumber = space + 1;
        System.out.println(AirportTime.now() + name + ": " + plane.getPlaneName() + 
            ", please proceed to gate " + gateNumber + " after landing");
        plane.setGateNumber(gateNumber);
    }

    //if occupied, deallocate gate
    public static void deallocateGate(Plane plane) {
        gates.getAndSet(plane.getGateNumber() - 1, null);
        //Plane moving out of gate
        System.out.println(AirportTime.now() + name + ": " + "Gate " + plane.getGateNumber() + 
            " available");
        //release gate
        availableGate.release();
    }

    //find empty gate
    private static int place() {
        for (int i = 0; i < gates.length(); i++) {
            int space = i % gates.length();
            if(gates.get(space) == null) return space;
        }
        return 0;
    }

    /*
    STATISTICS
    */

    //increment boarded passenger counter
    public static void incrementPassenger(){
        passengersBoardedCounter.getAndIncrement();
    }

    //increment plane counter
    public static void incrementPlane(){
        planeCounter++;
        //generate sanity check and statistics when all planes have departed
        if (planeCounter == PlaneGenerator.getTotalPlanes()){
            sanityCheck();
            statistics();
            planeCounter++;
        }
    }

    //record waiting time
    public static void recordWaitingTime(long waitingTime){
        waitingTimeList.add(waitingTime);
        totalWaitingTime += waitingTime;
    }

    private static double minWaitingTime(){
        return (double) Collections.min(waitingTimeList) / 1000;
    }

    private static double maxWaitingTime(){
        return (double) Collections.max(waitingTimeList) / 1000;
    }

    private static double avgWaitingTime(){
        return (double) totalWaitingTime / planeCounter / 1000;
    }

    //display sanity check for gates
    private static void sanityCheck(){
        System.out.println("---------------------------");
        System.out.println("\tGATE STATUS");
        System.out.println("---------------------------");        
        for (int gateNumber = 1; gateNumber <= gates.length(); gateNumber++) {
            if (gates.get(gateNumber - 1) != null) {
                System.out.println("Gate " + gateNumber + " not empty");
            } else {
                System.out.println("Gate " + gateNumber + " empty");
            }
        }
    }

    //display statistics
    private static void statistics(){
        System.out.println();
        System.out.println("---------------------------");
        System.out.println("\tSTATISTICS");
        System.out.println("---------------------------");

        //Waiting time before plane is permitted to land
        DecimalFormat df = new DecimalFormat("#.###");
        System.out.println("Minimum waiting time\t\t: " + df.format(minWaitingTime()) + "s");
        System.out.println("Average waiting time\t\t: " + df.format(avgWaitingTime()) + "s");
        System.out.println("Maximum waiting time\t\t: " + df.format(maxWaitingTime()) + "s");

        //Number of planes served and number of passengers boarded
        System.out.println("Number of planes served\t\t: " + planeCounter);
        System.out.println("Number of passengers boarded\t: " + passengersBoardedCounter);
    }

    //get plane counter
    public static int getPlaneCounter() {
        return planeCounter;
    }
}
