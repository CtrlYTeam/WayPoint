package waypoint;  

/**
 * Class to hold an arc.
 */
public class Arc extends NavPath {

    Point  center;      // Circle center
    double radius;      // Circle radius
    double startAngle;  // Starting angle of arc
    double endAngle;    // Ending angle of arc
    double length;      // circumferential length of arc
    boolean clockwise;  // true if direction from startAngle to endAngle decreases angle from center

    /**
     * Return an Arc, tangent to a given Navpoint, that also intersects a given Point
     */
    public static Arc calcArcNavPointToPoint(NavPoint npt, Point pt) {
        // Construct a line between the NavPoint and the Point
        // Then find the perpendicular bisector of the line at the midpoint between the Navpoint and Point
        StdLine line = StdLine.twoPointsToStdLine(npt.pt, pt);
        Point midpt = new Point((npt.pt.x+pt.x)/2.0, (npt.pt.y+pt.y)/2.0);
        StdLine perp = StdLine.perpLineAtPoint(line, midpt);
        // The intersection of the bisector and the perpendicular line to the NavPoint is the center of the Arc
        StdLine navline = npt.toStdLine();
        StdLine navperp = StdLine.perpLineAtPoint(navline, npt.pt);
        Point center = StdLine.intersectionPoint(perp, navperp);
        return new Arc(center, npt.pt, pt);
    }
    
    public static double tangentAngle(Point c, Point a, boolean clockwise) {
        double bearing = Point.radianAngle(c, a);
        if (clockwise) { bearing -= Math.PI/2.0; }
        else           { bearing += Math.PI/2.0; }
        return bearing;
    }
    
    /**
     * Create an arc of opposite direction and same magnitude of this arc.
     */
    public Arc reverse() {
        return new Arc(center, o.pt, i.pt, this.stop);
    }
    
    /**
     * Assuming arcs are always the smaller angle between two points on a circle,
     * return the direction of the smaller angle from start-to-end as clockwise(true) or not.
     */
    public static boolean calcClockwise(double sa, double ea) {
        double aa = ea - sa;
        if (aa < 0) { aa += 2*Math.PI; }
        return aa >= Math.PI;
    }
    
    /**
     * Return arc length, given arc direction, start and end angles, and radius.
     * @param clockwise - true if direction between starting angle and ending angle is in clockwise direction
     *                    false if direction is counter-clockwise
     * @param sa        - starting angle, in radians
     * @param ea        - ending angle, in radians
     * @param radius    - radius of arc, in inches
     * @return          - (double) length of arc, in inches
     */
    public static double calcLength(boolean clockwise, double sa, double ea, double radius) {
        double length = 0.0;
        if (clockwise) {
            length = sa - ea;
        }
        else {
            length = ea - sa;
        }
        if (length < 0) { length += 2.0*Math.PI; }
        return length*radius;
    }
    
    
    /**
     * Return a String with description of this Arc.
     */
    public String toString() {
        return this.getClass().getSimpleName()+" "+super.toString();
    }        
    public String toString(boolean flag) {
        if (flag) { return "Arc center: "+center.toString()+" radius: "+radius+" startAngle:"+startAngle+" endAngle:"+endAngle; }
        else      { return toString(); }
    }
     
    /**
     * Constructors
     */
    public Arc(Point c, Point p1, Point p2) {
        this(c, p1, p2, false);
    }        
    public Arc(Point c, Point p1, Point p2, boolean stop) {
        this.stop = stop;
        this.center = c;
        this.radius = Point.distance(c, p1); // ignores distance to p2
        this.startAngle = Point.radianAngle(c, p1);
        this.endAngle   = Point.radianAngle(c, p2);
        this.clockwise = Arc.calcClockwise(startAngle, endAngle);
        this.length = Arc.calcLength(this.clockwise, startAngle, endAngle, this.radius);
        double bearing;
        if (this.clockwise) { bearing = startAngle - Math.PI/2.0; }
        else                { bearing = startAngle + Math.PI/2.0; }
        this.i = new NavPoint(p1, bearing);
        if (this.clockwise) { bearing = endAngle - Math.PI/2.0; }
        else                { bearing = endAngle + Math.PI/2.0; }
        this.o = new NavPoint(p2, bearing);
    }
        
        
    
    public Arc(Point c, double r, double i, double o) {
        this(c.x, c.y, r, i, o);
    }
    
    public Arc(double cx, double cy, double r, double i, double o) {
        this.stop = false;
        this.center = new Point(cx, cy);
        this.radius = r;
        this.startAngle = i;
        this.endAngle = o;
        this.clockwise = Arc.calcClockwise(startAngle, endAngle);
        this.length = Arc.calcLength(this.clockwise, startAngle, endAngle, this.radius);
        double bearing;
        if (this.clockwise) { bearing = startAngle - Math.PI/2.0; }
        else                { bearing = startAngle + Math.PI/2.0; }
        this.i = new NavPoint(cx + r*Math.cos(i), cy + r*Math.sin(i), bearing);
        this.o = new NavPoint(cx + r*Math.cos(o), cy + r*Math.sin(o), bearing);
    }
}
