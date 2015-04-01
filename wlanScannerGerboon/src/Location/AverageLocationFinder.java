package Location;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;
import javafx.geometry.Pos;

import java.util.HashMap;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class AverageLocationFinder implements LocationFinder{

    public static final int LOCATION_MEMORY = 10;
    private Position[] pastPositions;
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.

	public AverageLocationFinder(){
        if(pastPositions == null){
            pastPositions = new Position[LOCATION_MEMORY];
        }
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
                if (totalAbs == 0) {
                    factor = 1;
                } else {
                    factor = distanceFactor / (totalAbs + distanceFactor);
                }

                x =  x * (1-factor) + knownLocations.get(mac).getX() * factor;
                y = y * (1-factor) + knownLocations.get(mac).getY() * factor;

                totalAbs += distanceFactor;
            }
        }
        addLocation(new Position(x, y));
        return getAverageLocation();
	}

    private void addLocation(Position pos){
        for(int i = pastPositions.length - 2; i >= 0; i--){
            pastPositions[i+1] = pastPositions[i];
        }
        pastPositions[0] = pos;

    }

    private Position getAverageLocation(){
        double x = 0;
        double y = 0;
        double total = 0;
        double f = Math.pow(2, LOCATION_MEMORY);
        double factor;
        for(Position pos: pastPositions){
            if(pos != null) {
                factor = (f/(f + total));
                x = factor * pos.getX() + (1-factor) * x;
                y = factor * pos.getY() + (1-factor) * y;
                total += f;
                f /= 2;
            }
        }
        printArray(pastPositions);
        return new Position(x, y);
    }

    public static void printArray(Object[] array){
        String out = "[";
        for (Object o: array){
            out += " " + o;
        }
        out += "]";
        System.out.println(out);
    }


}
