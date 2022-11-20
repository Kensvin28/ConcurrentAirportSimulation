package apairport;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 *
 * @author Kevin Matthew
 */
public class AircraftInspector extends Thread {
    private final String name;
    private int gateNumber;
    private AtomicReferenceArray<Plane> gates;
    private Plane plane;
    
    public AircraftInspector(AtomicReferenceArray<Plane> gates, int gateNumber){
        this.name = "Aircraft Inspector " + gateNumber;
        this.gates = gates;
        this.gateNumber = gateNumber - 1;
    }

    public void inspectAircraft(Plane plane){
        String planeName = gates.get(gateNumber).getPlaneName();
        try {
            System.out.println(AirportTime.now() + name + ": " + "Checking " + planeName + "'s condition...");
            Thread.sleep(3000);
            System.out.println(AirportTime.now() + name + ": " + "All clear, " + planeName + " is good to go...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //loop until the simulation should end
        while(ATC.getPlaneCounter() < PlaneGenerator.getTotalPlanes() + 1){
            //get plane docked in the gate served by the inspector
            plane = gates.get(gateNumber);

            if(plane != null){
                //inspect aircraft when plane is docked and plane is not inspected yet
                if(plane.isDocked() && !plane.isInspected()){
                    inspectAircraft(plane);
                    plane.setInspected(true);
                    //notify plane that the inspection is done
                    synchronized(plane){
                        plane.notify();
                    }
                }
            }
        }
    }
}
