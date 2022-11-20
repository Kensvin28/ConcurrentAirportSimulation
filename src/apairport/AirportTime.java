package apairport;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Kevin Matthew
 */

public class AirportTime {
    private static String time;
    
    // Show current time
    public static String now(){
        time = "[" +
                ZonedDateTime.now(ZoneId.of( "Asia/Shanghai" ))
                        .format(DateTimeFormatter.ofPattern("d-MM-y kk:mm:ss"))
                + "] " ;
        return time;
    }
}
