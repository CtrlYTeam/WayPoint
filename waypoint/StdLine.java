package waypoint;   

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold an equation of a line in standard form: ax + by + c = 0.
 */
public class StdLine {

    double a;    // x-multiplier
    double b;    // y-multiplier
    double c;    // distance from origin, scaled by a*a+b*b
    
    /**
     * return a line of standard from ax+by+c=0 from two given points.
     */
    public static StdLine twoPointsToStdLine(Point p1, Point p2) {
        double theta = Point.radianAngle(p1, p2);
        NavPoint npt = new NavPoint(p1, theta);
        return npt.toStdLine();
    }
    
    
    /**
     * return a line of standard form ax+by+c=0 that bisects two given lines.
     * https://www.math-only-math.com/equations-of-the-bisectors-of-the-angles-between-two-straight-lines.html
     */    
    public static List<StdLine> bisectionLine(StdLine line1, StdLine line2) {
        List<StdLine> list = new ArrayList<>();
        double d1 = Math.sqrt(line1.a*line1.a + line1.b*line1.b);
        double d2 = Math.sqrt(line2.a*line2.a + line2.b*line2.b);
        StdLine line = new StdLine();
        line.a = d2*line1.a - d1*line2.a;
        line.b = d2*line1.b - d1*line2.b;
        line.c = d2*line1.c - d1*line2.c;
        list.add(line);
        line = new StdLine();
        line.a = d2*line1.a + d1*line2.a;
        line.b = d2*line1.b + d1*line2.b;
        line.c = d2*line1.c + d1*line2.c;
        list.add(line);
        return list;
    }
    
    /**
     * return a point that is the intersection of two lines.
     */
    public static Point intersectionPoint(StdLine line1, StdLine line2) {
        // Need to check of div by 0?
        // Need to check if lines are parallel?
        double den = (line1.a*line2.b) - (line2.a*line1.b);
        //System.out.println("den = "+den+" TA:"+line1.a+" BB:"+line2.b+" BA:"+line2.a+" TB:"+line1.b);
        if (den == 0.0) {
            return null;
        }
        double x = ((line1.b*line2.c) - (line2.b*line1.c)) / den;
        double y = ((line2.a*line1.c) - (line1.a*line2.c)) / den;
        return new Point(x,y);        
    }    
    
    /**
     * return a line of standard form ax+by+c=0 perpendicular to given line and contains given point.
     */
    public static StdLine perpLineAtPoint(StdLine baseLine, Point pt) {
        StdLine line = new StdLine();
        line.a = baseLine.b;
        line.b = 0.0 - baseLine.a;
        line.c = 0.0 - (line.a*pt.x + line.b*pt.y);
        //System.out.println(" baseLine.a:"+baseLine.a+" baseLine.b:"+baseLine.b+" pt.x:"+pt.x+" pt.y:"+pt.y);
        //System.out.println(" line.a:"+line.a+" line.b"+line.b+" line.c"+line.c);
        return line;
    }
    public static StdLine perpLineAtPoint(StdLine baseLine, NavPoint navpt) {
        return perpLineAtPoint(baseLine, navpt.pt);        
    }
    

    /** 
     * return polar coordinate theta of line.
     */
    public static double getTheta(StdLine line) {
        return Math.atan2(line.b, 0.0 - line.a);
    }
    
    /**
     * return the distance between this line and a given point.
     * https://brilliant.org/wiki/dot-product-distance-between-point-and-a-line/
     */
    public double distanceTo(Point pt) {
        return Math.abs(a*pt.x + b*pt.y + c)/Math.sqrt(a*a+b*b);
    }
    
    public String toString() {
        return a+"x + "+b+"y + "+c+" = 0";
    }
    
    
    public StdLine() {
        this.a = 0.0;
        this.b = 0.0;
        this.c = 0.0;
    }
    
    public StdLine(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
