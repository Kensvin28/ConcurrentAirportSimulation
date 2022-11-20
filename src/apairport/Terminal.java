package apairport;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Terminal {
    public Terminal(){
    }

    public static CountDownLatch generatePassengers(Plane plane, int MAX_CAPACITY, CountDownLatch embarkLatch){
        int embarkingPassengers = new Random().nextInt(MAX_CAPACITY-10) + 10; //carrying passengers from range 10-50
        //count embarking passengers
        embarkLatch = new CountDownLatch(embarkingPassengers);
        //generate embarking passenger threads
        for (int i = 1; i <= embarkingPassengers; i++) {
            Passenger passenger = new Passenger(i, plane, Purpose.EMBARK, embarkLatch);
            passenger.start();
        }
        //set plane embark latch to the number of passengers created by terminal
        plane.setEmbarkLatch(embarkLatch);
        return embarkLatch;
    }
}
