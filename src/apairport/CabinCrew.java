package apairport;

/**
 *
 * @author Kevin Matthew
 */
public class CabinCrew {
    private Plane plane;
    private String name;
    
    public CabinCrew(Plane plane) {
        this.plane = plane;
        this.name = plane.getPlaneName() + " Cabin Crew";
    }

    //clean plane
    public void clean() {
        System.out.println(AirportTime.now() + name + ": " + "Cleaning " + plane.getPlaneName() + "...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(AirportTime.now() + name + ": " + "Finished cleaning " + plane.getPlaneName() + "...");
        plane.setCleaned(true);
    }
}
