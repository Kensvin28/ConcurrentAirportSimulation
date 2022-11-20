package apairport;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Kevin Matthew
 */
public class APAirport {
    public static void main(String[] args) {
        //create runway lock
        Lock runwayLock = new ReentrantLock(true);
        final int GATE_CAPACITY = 2;
        //create gate array
        AtomicReferenceArray<Plane> gates = new AtomicReferenceArray<>(GATE_CAPACITY);

        //instantiate objects and threads
        new ATC(gates);
        new Terminal();
        PlaneGenerator planeGenerator = new PlaneGenerator(runwayLock);
        RefuelTruck refuelTruck = new RefuelTruck();
        BaggageSupplyTruck baggageSupplyTruck1 = new BaggageSupplyTruck(gates, 1);
        BaggageSupplyTruck baggageSupplyTruck2 = new BaggageSupplyTruck(gates, 2);
        AircraftInspector aircraftInspector1 = new AircraftInspector(gates, 1);
        AircraftInspector aircraftInspector2 = new AircraftInspector(gates, 2);
        //start simulation output
        System.out.println("---------------------------");
        System.out.println("   AP AIRPORT SIMULATION");
        System.out.println("---------------------------");

        //start threads
        planeGenerator.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        refuelTruck.start();
        baggageSupplyTruck1.start();
        baggageSupplyTruck2.start();
        aircraftInspector1.start();
        aircraftInspector2.start();
    }
}

