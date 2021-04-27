package waypoint;  

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold a circle.
 */
public class Circle {

    Point  center;      // Circle center
    double radius;      // Circle radius
    
    /**
     * From two NavPoints, attempt to find a Circle that is tangent to the 
     * line of a starting NavPoint and is tangent to a target NavPoint.
     * @param startPt - staring NavPoint
     * @param targPt  - ending NavPoint
     * return         - Circle that is tangent to ending NavPoint and lines of both NavPoints
     */
    public static Circle findCircle(NavPoint startPt, NavPoint targPt) {
    
        // The starting point, with a heading, defines a line
        // and also a perpendicular line
        StdLine startLine = startPt.toStdLine();
        StdLine startNormalLine = StdLine.perpLineAtPoint(startLine, startPt);
                
        // The target point, with a heading, defines a line
        // and also a perpendicular line
        StdLine targLine = targPt.toStdLine();
        StdLine targNormalLine = StdLine.perpLineAtPoint(targLine, targPt);
        
        // The start line and target line intersect at a point
        // Check if they are parallel
        Point intPt = StdLine.intersectionPoint(startLine, targLine);
        if (intPt == null) {
            // debug            
            //System.out.println("No circles. Start and end Navpoints are parallel.");
            return null;
        }
        
        // The start line and target lines can be bisected by two more lines
        List<StdLine> bisectLines = StdLine.bisectionLine(startLine, targLine);

        // The target line intersects the two bisector lines (unless parallel)
        // These intersections would define the center of circle of curvature
        //System.out.println("  TN | b(0)");
        Point center0 = StdLine.intersectionPoint(targNormalLine, bisectLines.get(0));
        //System.out.println("  TN | b(1)");
        Point center1 = StdLine.intersectionPoint(targNormalLine, bisectLines.get(1));
        
        // A segment can be defined from start point to target point
        Segment startToTargetVector = new Segment(startPt, targPt);
        
        // Segments can be defined from the intersection of start/target lines to centers of circles of curvature
        Segment intToCenter0 = new Segment(intPt, center0);
        Segment intToCenter1 = new Segment(intPt, center1);
        
        boolean match0 = Segment.doIntersect(startToTargetVector, intToCenter0);
        boolean match1 = Segment.doIntersect(startToTargetVector, intToCenter1);
        
        if (match0 && !match1) {
            return new Circle(center0, Point.distance(targPt.pt, center0));
        }
        else if (!match0 && match1) {
            return new Circle(center1, Point.distance(targPt.pt, center1));
        }
        else if (match0 && match1) {
            // double match ?!? error!!
            System.out.println("Error: Two circles found.");
            Circle circle = new Circle(center0, Point.distance(targPt.pt, center0));
            System.out.println(circle.toString());
            circle = new Circle(center1, Point.distance(targPt.pt, center1));
            System.out.println(circle.toString());
            return null;
        }
        else {
            // no match ?!? error!!
            System.out.println("Error: No circles found.");
            return null;
        }
    }
    
    /**
     * String constructed from this Circle's variables
     */
    public String toString() {
        return "center: "+center.toString()+ " radius:"+radius;
    }
    
    /**
     * Constructor from a Point and a radius
     * @param c - center of circle, expressed as a Point
     * @param r - radius of circle
     */
    public Circle(Point c, double r) {
        this.center = c;
        this.radius = r;
    }    
    /**
     * Constructor from a NavPoint and a radius
     * @param c - center of circle, expressed as a NavPoint
     * @param r - radius of circle
     */
    public Circle(NavPoint c, double r) {
        this.center = c.pt;
        this.radius = r;
    }
    
}
