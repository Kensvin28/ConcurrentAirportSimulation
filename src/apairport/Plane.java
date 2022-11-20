package apairport;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author Kevin Matthew
 */
public class Plane extends Thread {
    private final String name;
    private Lock runwayLock;
    private Semaphore planeSemaphore;
    //set maximum passengers to 50
    private final int MAX_CAPACITY = 50;
    private long requestTime;
    private long landingTime;
    private long waitingTime;
    private AtomicBoolean docked, inspected, supplied, cleaned, refuelled;
    private CabinCrew cabinCrew;
    private int passengerCount;
    private int totalDisembarking;
    private int totalEmbarking;
    private CountDownLatch disembarkLatch;
    private CountDownLatch embarkLatch;
    private int gateNumber;

    public Plane(String name, Lock runwayLock) {
        this.docked = new AtomicBoolean(false);
        this.inspected = new AtomicBoolean(false);
        this.supplied = new AtomicBoolean(false);
        this.cleaned = new AtomicBoolean(false);
        this.refuelled = new AtomicBoolean(false);
        this.name = name;
        this.runwayLock = runwayLock;
        planeSemaphore = new Semaphore(1);
        passengerCount = new Random().nextInt(MAX_CAPACITY-10) + 10; //carrying passengers ranging 10-50
        totalDisembarking = passengerCount;
        cabinCrew = new CabinCrew(this);
        //count number of passengers
        disembarkLatch = new CountDownLatch(passengerCount);
        embarkLatch = Terminal.generatePassengers(this, MAX_CAPACITY, embarkLatch);
        totalEmbarking = (int) embarkLatch.getCount();
        for(int i = 1; i <= passengerCount; i++){
            new Passenger(i, this, Purpose.DISEMBARK, disembarkLatch).start();
        }
    }
    
    //request landing permission from ATC
    private void requestForLanding() {
        System.out.println(AirportTime.now() + this.name + ": " + "Requesting for landing");
        requestTime = System.currentTimeMillis();
        ATC.confirmLand(this);
    }

    //land on runway
    public void land(){
        System.out.println(AirportTime.now() + name + ": " + name + " LANDING ON RUNWAY...");
        landingTime = System.currentTimeMillis();
        waitingTime = landingTime - requestTime;
        ATC.recordWaitingTime(waitingTime);
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + name + " has successfully touched down on AP Airport");
    }
    
    //request take off permission to ATC
    private void requestForTakeOff() {
        System.out.println(AirportTime.now() + name + ": " + "Requesting for take off");
        ATC.confirmTakeOff(this);
    }

    //take off from runway
    public void takeOff(){
        try {
            System.out.println(AirportTime.now() + name + ": " + "Undocking from gate...");
            Thread.sleep(1000);
            System.out.println(AirportTime.now() + name + ": " + "Taxiing to runway...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + "TAKING OFF FROM RUNWAY...");
        System.out.println(AirportTime.now() + name + ": " + name + " has successfully departed from AP Airport");
        runwayLock.unlock();
        ATC.deallocateGate(this);
        ATC.incrementPlane();
    }

    //request refuel from refuel truck
    private void requestRefuel(){
        try {
            //enqueue to refuel blocking queue
            RefuelTruck.getRefuelQueue().put(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //dock to gate
    private void dock()  {
        System.out.println(AirportTime.now() + name + ": " + "Taxiing to gate " + gateNumber + "...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
        System.out.println(AirportTime.now() + name + ": " + "SUCCESSFULLY DOCKED TO GATE " + gateNumber);
        runwayLock.unlock();
        System.out.println(AirportTime.now() + name + ": " + "Dispatching " + passengerCount + " passengers");
        docked.set(true);
    }

    public void run() {
        //request to land on runway
        requestForLanding();
        //land on runway
        land();
        try {
            //dock to gate
            dock();

            //request refuel truck to refuel the plane
            requestRefuel();
            
            //wait until all passengers have disembarked
            disembarkLatch.await();
            
            //clean plane
            cabinCrew.clean();
            
            //take passengers
            System.out.println(AirportTime.now() + name + ": Taking " + totalEmbarking + " passengers");
            
            //wait until all passengers have embarked
            embarkLatch.await();

            //wait until inspection by AircraftInspector and resupply by BaggageSupplyTruck is completed
            while(!inspected.get() && !supplied.get() && !refuelled.get()) wait();
            
            //request for take off to ATC
            requestForTakeOff();
            //take off from runway
            takeOff();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void incrementPassengerCount(){
        this.passengerCount++;
    }

    public void decrementPassengerCount(){
        this.passengerCount--;
    }


    public String getPlaneName() {
        return this.name;
    }

    public int getPassengerCount() {
        return this.passengerCount;
    }
    
    public Lock getLock() {
        return runwayLock;
    }
    
    public long getWaitingTime() {
        return waitingTime;
    }

    public void setGateNumber(int gateNumber) {
		this.gateNumber = gateNumber;
	}
    
    public int getGateNumber(){
        return this.gateNumber;
    }

    public void setEmbarkLatch(CountDownLatch embarkLatch) {
        this.embarkLatch = embarkLatch;
    }

    public Semaphore getPlaneSemaphore() {
        return this.planeSemaphore;
    }

    public int getTotalDisembarking(){
        return this.totalDisembarking;
    }

    public int getTotalEmbarking(){
        return this.totalEmbarking;
    }

    public boolean isDocked() {
        return docked.get();
    }

    public boolean isInspected() {
        return inspected.get();
    }

    public void setInspected(boolean inspected){
        this.inspected.set(inspected);
    }

    public boolean isSupplied(){
        return supplied.get();
    }

    public void setSupplied(boolean supplied){
        this.supplied.set(supplied);
    }

    public boolean isCleaned() {
        return cleaned.get();
    }

    public void setCleaned(boolean cleaned){
        this.cleaned.set(cleaned);
    }

    public boolean isRefuelled(){
        return refuelled.get();
    }

    public void setRefuelled(boolean refuelled){
        this.refuelled.set(refuelled);
    }

}