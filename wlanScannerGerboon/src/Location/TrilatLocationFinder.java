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
public class TrilatLocationFinder implements LocationFinder {

    public static final int LOCATION_MEMORY = 10;
    private Position[] pastPositions;
    private HashMap<String, Position> knownLocations; //Contains the known locations of APs. The long is a MAC address.

    public TrilatLocationFinder() {
        if (pastPositions == null) {
            pastPositions = new Position[LOCATION_MEMORY];
        }
        knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
    }

    @Override
    public Position locate(MacRssiPair[] data) {
        double x = 0;
        double y = 0;
        printMacs(data);
        //Find the 3 closest points
        MacRssiPair[] highest = new MacRssiPair[3];
        for (MacRssiPair pair : data) {
            if (knownLocations.containsKey(pair.getMacAsString())) {
                for (int i = 0; i < highest.length; i++) {
                    if (highest[i] == null || highest[i].getRssi() < pair.getRssi()) {
                        highest[i] = pair;
                        break;
                    }
                }
            }
        }
        Position pos1 = knownLocations.get(highest[0].getMacAsString());
        Position pos2 = knownLocations.get(highest[1].getMacAsString());
        Position pos3 = knownLocations.get(highest[2].getMacAsString());
        Position[] poss = new Position[]{pos1, pos2, pos3};
        poss = sortOnSmallestX(poss);
        double r1 = sqr(Math.abs(highest[0].getRssi()) - 50) / 10;
        double r2 = sqr(Math.abs(highest[1].getRssi()) - 50) / 10;
        double r3 = sqr(Math.abs(highest[2].getRssi()) - 50) / 10;
        System.out.printf("r1: %s r2: %s r3: %s \n", r1, r2, r3);


        addLocation(getAveragedPosition(poss[0], poss[2], poss[1], r1, r2, r3, 10, 2));
        return getAverageLocation();
    }

    private void addLocation(Position pos) {
        for (int i = pastPositions.length - 2; i >= 0; i--) {
            pastPositions[i + 1] = pastPositions[i];
        }
        pastPositions[0] = pos;

    }

    private Position getAverageLocation() {
        double x = 0;
        double y = 0;
        double total = 0;
        double f = Math.pow(1.1, LOCATION_MEMORY);
        double factor;
        for (Position pos : pastPositions) {
            if (pos != null) {
                factor = (f / (f + total));
                x = factor * pos.getX() + (1 - factor) * x;
                y = factor * pos.getY() + (1 - factor) * y;
                total += f;
                f /= 2;
            }
        }
        printArray(pastPositions);
        return new Position(x, y);
    }

    public static void printArray(Object[] array) {
        String out = "[";
        for (Object o : array) {
            out += " " + o;
        }
        out += "]";
        System.out.println(out);
    }

    public static Position convertOverAngle(Position pos, double angle) {
        double x = pos.getY() * -Math.sin(angle) + pos.getX() * Math.cos(angle);
        double y = pos.getX() * Math.sin(angle) + pos.getY() * Math.cos(angle);
        return new Position(x, y);
    }

    public static double getAngle(Position pos1, Position pos2) {
        return Math.atan2(Math.abs(pos2.getY() - pos1.getY()), Math.abs(pos2.getX() - pos1.getX()));
    }

    public static Position[] sortOnSmallestX(Position[] pos) {
        Position[] posClone = pos.clone();
        Position[] out = new Position[3];
        int index = -1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++)
                if (posClone[j] != null && (out[i] == null || out[i].getX() > posClone[j].getX() || (out[i].getX() == posClone[j].getX() && out[i].getY() > posClone[j].getY()))) {
                    out[i] = posClone[j];
                    index = j;
                }
            posClone[index] = null;

        }
        return out;
    }

    public static Position getPosition(Position pos1, Position pos2, Position pos3, double r1, double r2, double r3) {
        double angle = -getAngle(pos1, pos2);
        System.out.println("Angle: " + angle);
        double d = convertOverAngle(pos2, angle).getX() - pos1.getX();
        double i = convertOverAngle(pos3, angle).getX() - pos1.getX();
        double j = convertOverAngle(pos3, angle).getY() - pos1.getY();
        double x = (sqr(r1) - sqr(r2) + sqr(d)) / (2 * d);
        double y = (sqr(r1) - sqr(r3) + sqr(i) + sqr(j)) / (2 * j) - (i / j) * x;
        return convertOverAngle(new Position(x, y), -angle);
    }

    public static Position getAveragedPosition(Position pos1, Position pos2, Position pos3, double r1, double r2, double r3, int accuracy, double deltaSize){
        double x = 0;
        double y = 0;
        Position p;
        for (int i = -accuracy; i < accuracy + 1; i++) {
            for (int j = -accuracy; j < accuracy + 1; j++) {
                for (int k = -accuracy; k < accuracy + 1; k++) {
                    p = getPosition(pos1, pos2, pos3, r1 + i*deltaSize, r2 + j*deltaSize, r3 + k*deltaSize);
                    x += p.getX();
                    y += p.getY();
                }
            }
        }
        x = x/(Math.pow(2*accuracy, 3));
        y = y/(Math.pow(2*accuracy, 3));
        return new Position(x, y);
    }

    public double toMeters(double rssi){
        return sqr(Math.abs(rssi) - 50) / 10;
    }


    public static double sqr(double x){
        return x*x;
    }

    private void printMacs(MacRssiPair[] data) {
        for (MacRssiPair pair : data) {
            System.out.println(pair);
        }
    }

}
