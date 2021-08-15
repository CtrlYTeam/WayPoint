package waypoint;  

import java.util.Locale;

/**
 * Class to hold a 2D point.
 */
public class Point {

    double x;          // x-coordinate
    double y;          // y-coordinate

    /**
     * Return the radian angle from Point p1 to Point p2.
     */
    public static double radianAngle(Point p1, Point p2) {
        return Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }
    
    /**
     * Return the distance between two Points.
     */
    public static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }
    
    /**
     * Return true if the given Points are within some tolerance (like math roundoff error)
     * of distance between each other.
     */
    public static boolean isCoincident(Point p1, Point p2) {
        return (Point.distance(p1, p2) < 0.000001);
    }
    
    /**
     * String of this Point's parameters
     */
    public String toString() {
        return String.format(Locale.US, "(%6.2f,%6.2f)",x,y);
        //return "("+x+","+y+")";
    }
    
    /**
     * Constructors
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Point() {
        this.x = 0.0;
        this.y = 0.0;
    }    
    public Point(NavPoint navpt) {
        this.x = navpt.pt.x;
        this.y = navpt.pt.y;
    }
}
