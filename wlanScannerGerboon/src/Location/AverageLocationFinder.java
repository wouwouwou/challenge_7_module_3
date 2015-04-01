package Location;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

import java.util.HashMap;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class AverageLocationFinder implements LocationFinder{

	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.

	public AverageLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		double x = 0;
        double y = 0;
        double totalAbs = 0;
        double factor;
        double distanceFactor;
        String mac = "";
        for (MacRssiPair pair: data){
            if(knownLocations.containsKey(pair.getMacAsString())) {
                mac = pair.getMacAsString();
                //distanceFactor = 100 + pair.getRssi();
                distanceFactor = pair.getRssi()!=0?Math.pow(2, pair.getRssi()):0;
                System.out.println("MAC: " + mac);
                if (totalAbs == 0) {
                    factor = 1;
                } else {
                    factor = distanceFactor / (totalAbs + distanceFactor);
                }
                System.out.println("Factor: " + factor);
                System.out.println("Total Abs: " + totalAbs);
                x =  x * (1-factor) + knownLocations.get(mac).getX() * factor;
                y = y * (1-factor) + knownLocations.get(mac).getY() * factor;
                System.out.printf("x: %s, y: %s \n", x, y);
                totalAbs += distanceFactor;
            }
        }
        return new Position(x, y);
	}


}
