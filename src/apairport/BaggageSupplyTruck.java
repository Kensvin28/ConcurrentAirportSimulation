package apairport;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 *
 * @author Kevin Matthew
 */
public class BaggageSupplyTruck extends Thread {
    private String name;
    private int gateNumber;
    private AtomicReferenceArray<Plane> gates;
    private Plane plane;
    
    public BaggageSupplyTruck(AtomicReferenceArray<Plane> gates, int gateNumber){
        this.name = "Baggage Supply Truck " + gateNumber;
        this.gates = gates;
        this.gateNumber = gateNumber - 1;
    }

    //unload bagage from plane
    private void unloadBaggage(Plane plane){
        System.out.println(AirportTime.now() + name + ": " + "Unloading baggage from " 
            + plane.getPlaneName() + "...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + "Delivering baggage from " 
            + plane.getPlaneName() + " to terminal...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //load baggage to plane
    private void loadBaggageAndSupplies(Plane plane){
        System.out.println(AirportTime.now() + name + ": " + "Delivering baggage and supplies from terminal to " 
            + plane.getPlaneName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + "Loading baggage and supplies to " + plane.getPlaneName() 
            + "...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + "Finished loading baggage and supplies to " 
            + plane.getPlaneName() + "...");
    }

    public void run() {
        //loop until the simulation should end
        while(ATC.getPlaneCounter() < PlaneGenerator.getTotalPlanes() + 1){
            //get plane docked in the gate served by the baggage supply truck
            plane = gates.get(gateNumber);

            if(plane != null){
                //unload baggage when plane is docked and not supplied yet
                if(plane.isDocked() && !plane.isSupplied()){
                    unloadBaggage(plane);
                    loadBaggageAndSupplies(plane);
                    plane.setSupplied(true);
                    //notify plane that the supply is loaded
                    synchronized(plane){
                        plane.notify();
                    }
                }
            }
        }
        
    }
}
