package Location;

import Utils.Position;

/**
 * Created by Gerryflap on 2015-04-01.
 */
public class TrilatTest {

    public static void main(String[] args){
        Position pos1 = new Position(5, 5);
        Position pos2 = new Position(2, 2);
        Position pos3 = new Position(1, 1);
        double angle = TrilatLocationFinder.getAngle(pos1, pos2);
        System.out.println(angle);
        Position pos2New = TrilatLocationFinder.convertOverAngle(pos2, -angle);
        System.out.println(pos2New);
        Position pos2NewNew = TrilatLocationFinder.convertOverAngle(pos2New, angle);
        System.out.println(pos2NewNew);
        Position[] poss = new Position[]{pos1, pos2, pos3};
        HackyAverageLocationFinder.printArray(poss);
        poss = TrilatLocationFinder.sortOnSmallestX(poss);
        HackyAverageLocationFinder.printArray(poss);
        Position out = TrilatLocationFinder.getPosition(poss[0], poss[2], poss[1], 1.5, 7 , 1.5);

    }
}
