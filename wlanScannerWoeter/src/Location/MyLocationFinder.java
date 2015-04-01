package Location;

import java.util.*;

import Utils.*;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @author Bernd
 *
 */
public class MyLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	private Position lastPosition; //Contains the last known position
	
	private static final double WEIGHT = 0.5; //weight of reference point
	
	public MyLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
		lastPosition = new Position(19.0, 27.0);
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		return getPoint(data); //return a calculated point. Calculated with 3 AP's
//		return getStrongestPoint(data); //return the strongest AP
	}
	
	private Position getPoint(MacRssiPair[] data) {
		Position referencepoint = getStrongestPoint(data);
		double x = lastPosition.getX() - WEIGHT * (lastPosition.getX() - referencepoint.getX());
		double y = lastPosition.getY() - WEIGHT * (lastPosition.getY() - referencepoint.getY());
		lastPosition = new Position(x, y);
		return new Position(x, y);
	}

	private Position getStrongestPoint(MacRssiPair[] data) {
		Position ret = lastPosition;
		MacRssiPair a = null;
		int highestdBm = -100; 
		for(int i=0; i<data.length; i++){
			if(knownLocations.containsKey(data[i].getMacAsString())){
				if (data[i].getRssi() > highestdBm) {
					a = data[i];
					ret = knownLocations.get(data[i].getMacAsString());
					highestdBm = data[i].getRssi();
				}
			}
		}
		System.out.println(a.getMacAsString() + "  " + highestdBm);
		return ret;
	}
	
	/**
	 * Outputs all the received MAC RSSI pairs to the standard out
	 * This method is provided so you can see the data you are getting
	 * @param data
	 */
	private void printMacs(MacRssiPair[] data) {
		for (MacRssiPair pair : data) {
			if (knownLocations.containsKey(pair.getMacAsString())) {
				System.out.println(pair);
			}
		}
	}

}
