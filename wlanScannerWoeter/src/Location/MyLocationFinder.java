package Location;

import java.util.*;

import Utils.*;

/**
 * Simple Location finder that returns the first known APs location from the list of received MAC addresses
 * @authors Gerben Meijer and Wouter Bos
 * @version 1.0
 */
public class MyLocationFinder implements LocationFinder{
	
	private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.
	private Position lastPosition; //Contains the last known position
	
	private static final double WEIGHT = 0.2; //weight of reference point
	private static final double APAMOUNT = 4; //amount of AP's needed for a calculation
	
	public MyLocationFinder(){
		knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
		lastPosition = new Position(0.0, 0.0);
	}

	@Override
	public Position locate(MacRssiPair[] data) {
		return getPoint(data); //return a calculated point. Calculated with 3 AP's
	}
	
	//TODO
	private Position getPoint(MacRssiPair[] data) {
		
		if (lastPosition.getX() == 0 && lastPosition.getY() == 0) {
			return getStrongestPoint(data);
		}
		
		Position referencepoint = getReferencePoint(data);
		Double x = lastPosition.getX() - WEIGHT * (lastPosition.getX() - referencepoint.getX());
		Double y = lastPosition.getY() - WEIGHT * (lastPosition.getY() - referencepoint.getY());
		lastPosition = new Position(x, y);
		return new Position(x, y);
	}
	
	//TODO
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
		lastPosition = ret;
		System.out.println(a.getMacAsString() + "  " + highestdBm);
		return ret;
	}
	
	//TODO
	private Position getReferencePoint(MacRssiPair[] data) {
		
		List<MacRssiPair> APs = new ArrayList<MacRssiPair>();
		
		//All AP's
		try {
			for(int i=0; i<data.length; i++){
				if(knownLocations.containsKey(data[i].getMacAsString())){
					APs.add(data[i]);
				}
			}
			if (APs.size() < 4) {
				throw new Exception("Less than 4 AP's available!");
			}
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		
		//Closest APs
		List<MacRssiPair> closestAPs = new ArrayList<MacRssiPair>();
		for (int i = 0; i < APAMOUNT; i++) {
			int highestRssi = -100;
			MacRssiPair closestAP = null;
			for (int j = 0; j < APs.size(); j++) {
				if (highestRssi < APs.get(j).getRssi() && !closestAPs.contains(APs.get(j))) {
					highestRssi = APs.get(j).getRssi();
					closestAP = APs.get(j);
				}
			}
			closestAPs.add(closestAP);
		}
		
		double generalRefWeight = (double)1 / (double)APAMOUNT;
		double x = 0;
		double y = 0;
		
		for (int i = 0; i < APAMOUNT; i++) {
			System.out.println(closestAPs.get(i).getMacAsString() + "  " + closestAPs.get(i).getRssi());
			switch (i) {
				case 0: x = x + (generalRefWeight * 3.0 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getX()));
						y = y + (generalRefWeight * 3.0 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getY()));
						break;
				case 1: x = x + (generalRefWeight * 0.5 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getX()));
						y = y + (generalRefWeight * 0.5 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getY()));
						break;
				case 2: x = x + (generalRefWeight * 0.25 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getX()));
						y = y + (generalRefWeight * 0.25 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getY()));
						break;
				case 3: x = x + (generalRefWeight * 0.25 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getX()));
						y = y + (generalRefWeight * 0.25 * (knownLocations.get(closestAPs.get(i).getMacAsString()).getY()));
						break;
			}
		}
		System.out.println(x + "  " + y);
		return new Position (x, y);
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
