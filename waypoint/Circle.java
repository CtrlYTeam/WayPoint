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
     * From two NavPoints, attempt to find Circles that are tangent to the 
     * line of a starting NavPoint and is tangent to a target NavPoint.
     *          / .  -- circle center with arc
     *          |    -- vector from starting NavPoint
     *          *
     * @param startPt - staring NavPoint
     * @param targPt  - ending NavPoint
     * return         - Circle that is tangent to ending NavPoint and lines of both NavPoints
     */
    public static List<Circle> findCircles(NavPoint startPt, NavPoint targPt) {
    
        List<Circle> circlesList = new ArrayList<>();
    
        // The starting point, with a heading, defines a line
        // and also a perpendicular line
        StdLine startLine = startPt.toStdLine();
        StdLine startNormalLine = StdLine.perpLineAtPoint(startLine, startPt);
                
        // The target point, with a heading, defines a line
        // and also a perpendicular line
        StdLine targLine = targPt.toStdLine();
        StdLine targNormalLine = StdLine.perpLineAtPoint(targLine, targPt);
                
        // Check if they are start line and target lines are parallel
        Point intPt = StdLine.intersectionPoint(startLine, targLine);
        if (intPt == null) {
        
            // A semicircle solution exists
            // Find intersection of starting line and line perpendicular to target line
            Point pt = StdLine.intersectionPoint(startLine, targNormalLine);
            // The semicircle center is the midpoint of the segment between the
            // above intersection and the target point
            Point semiCenter = new Point((targPt.pt.x + pt.x)/2.0, (targPt.pt.y + pt.y)/2.0);
            Circle semiCircle = new Circle(semiCenter, Point.distance(semiCenter, targPt.pt));
            circlesList.add(semiCircle);
            return circlesList;
        }
        
        // If the start line and target line are not parallel then a solution
        // may be found by using the bisections of these lines.
        // This will return two circles.
        
        // The start line and target line intersect at a point
        // The start line and target lines can be bisected by two more lines
        List<StdLine> bisectLines = StdLine.bisectionLine(startLine, targLine);
            
        // The target line intersects the two bisector lines (unless paralel)
        // These intersections would define the center of circle of curvature
        //System.out.println("  TN | b(0)");
        Point center0 = StdLine.intersectionPoint(targNormalLine, bisectLines.get(0));
        //System.out.println("  TN | b(1)");
        Point center1 = StdLine.intersectionPoint(targNormalLine, bisectLines.get(1));
        
        Circle circle0 = new Circle(center0, Point.distance(center0, targPt.pt));
        Circle circle1 = new Circle(center1, Point.distance(center1, targPt.pt));
        
        circlesList.add(circle0);
        circlesList.add(circle1);
        return circlesList;
/*        
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
*/        
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
