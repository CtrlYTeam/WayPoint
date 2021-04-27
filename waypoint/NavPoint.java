package waypoint;  

/**
 * Class to hold a 2D point and a direction.
 */
public class NavPoint {

    Point pt;           // point (x,y)
    double heading;     // direction of intended movement in radian angle CCW from +x-axis
    double orientation; // direction of robot orientation relative to its heading in radian angle CCW from heading
    boolean stop;       // true if this is a stop, false if a drive-thru waypoint
    
    private static final double defaultHeading = Math.PI/2.0;  // default heading = pi/2 radians, or 90 degrees
    
    /**
     *  Return a boolean value as true if this NavPoint's position is essentially
     *  the same as a given NavPoint, within tolerance of some roundoff calculation error.
     */
    public boolean isCoincident(NavPoint npt) {
        return (Point.distance(pt, npt.pt) < 0.000001);
    }
    
    /** 
     * Return a boolean value as true if this Navpoint's heading is essentially
     * the same a as a given Navpoint, within tolerance of some roundoff calculation error.
     */
     public boolean isParallel(NavPoint npt) {
        return (Math.abs(heading - npt.heading) < 0.000001);
     }
    
    /** 
     * Return a boolean value as true if this Navpoint's orientation is essentially
     * the same a as a given Navpoint, within tolerance of some roundoff calculation error.
     */
     public boolean isOriented(NavPoint npt) {
        // shouldn't this take into consideration the NavPoint headings,
        // as orienation is relative to heading ??
        return (Math.abs(orientation - npt.orientation) < 0.000001);
     }
    
    /**
     *  return line of standard form ax + by + c = 0 extrapolated from 
     *  parameters of this NavPoint.
     */
    public StdLine toStdLine() {
        
        StdLine stdLine = new StdLine();
        
        //System.out.println("  pt->line: "+pt.toString()+" heading:" +heading);
        
        double den = pt.y*Math.cos(heading) - pt.x*Math.sin(heading);

        // NavPoints parallel to x-axis need special handling.
        if ((Math.abs(heading - 0.0)     < 0.000001) ||
            (Math.abs(heading - Math.PI) < 0.000001)) {
            //System.out.println("    || x-axis");
            stdLine.a = 0.0;
            stdLine.b = 1.0;
            stdLine.c = 0.0 - pt.y;
        }
        // NavPoints parallel to y-axis need special handling.
        else if ((Math.abs(heading - 1.0*Math.PI/2.0) < 0.000001) ||
                 (Math.abs(heading - 3.0*Math.PI/2.0) < 0.000001)) {
            //System.out.println("    || y-axis");
            stdLine.a = 1.0;
            stdLine.b = 0.0;
            stdLine.c = 0.0 - pt.x;
        }
        // NavPoints at or aim through the origin need special handling.
        else if ((pt.y == 0.0 && pt.x == 0.0) || (Math.abs(den) < 0.0000001)) {
            //System.out.println("    . origin");
            // want the sin to be taken from [-PI/4,PI/4), [-90,90)
            double bearing = (heading + Math.PI/2) % (Math.PI) - Math.PI/2;
            stdLine.a = 0.0 - Math.sin(heading);
            // want the cos to be taken from [0,PI/2)  [0,180)
            bearing = heading % (Math.PI);
            //System.out.println("heading "+heading+ " bearing "+bearing);
            stdLine.b = 0.0 + Math.cos(bearing);
            stdLine.c = 0.0;
        }
        
        // Else if the point is not on or parallel to the x-axis, use solve-with-b
        else if (pt.y != 0.0) {
            //System.out.println("    solve-with-b");        
            stdLine.a = Math.sin(heading) / den;
            stdLine.b = (-1.0 - stdLine.a * pt.x)/pt.y;
            stdLine.c = 1.0;
        }
        // Else if the point is not on or parallel to the y-axis, use solve-with-a
        else if (pt.x != 0.0) {
            //System.out.println("    solve-with-a");        
            stdLine.b = Math.cos(heading) / (0.0 - den);
            stdLine.a = (-1.0 - stdLine.b * pt.y)/pt.x;
            stdLine.c = 1.0;
        }
        else {
            //System.out.println("    null");        
            return null;
        }
        //System.out.println(stdLine.toString());
        return stdLine;
    }
    
    
    /**
     *  Calculate the difference of position and heading of a NavPoint relative to another NavPoint.
     */
    public NavPoint relativeTo(NavPoint npt) {
        // We need to apply the rotation and translation to get the
        // given NavPoint as an argument to (0.0, 0.0) and 90 degrees 
        // and apply that to this current NavPoint.
        NavPoint newNpt = new NavPoint();
        // apply translation as if to argument NavPoint to get it to (0.0,0.0)
        // to the current NavPoint.
        double tx  =  pt.x + (0.0 - npt.pt.x);
        double ty  =  pt.y + (0.0 - npt.pt.y);
        // apply rotation as if to argument NavPoint to get it 
        // to 90 degrees to the current Navpoint.
        double bearing = Math.PI/2.0 - npt.heading;
        newNpt.heading = heading + bearing;
        // http://danceswithcode.net/engineeringnotes/rotations_in_2d/rotations_in_2d.html
        newNpt.pt.x = tx*Math.cos(bearing) - ty*Math.sin(bearing);
        newNpt.pt.y = tx*Math.sin(bearing) + ty*Math.cos(bearing);
        return newNpt;
    }
    
    /**
     *  Calculate the position and heading of a NavPoint as referenced to another NavPoint.
     */
    public NavPoint displacedBy(NavPoint npt) {
        // We need to apply rotation and translation of (0.0,0.0),90
        // to get to NavPoint given as argument to the current NavPoint
        NavPoint newNpt = new NavPoint();
        // apply rotation of origin,90degrees to get to argument Navpoint
        // to given NavPoint
        double bearing = npt.heading - Math.PI/2.0;
        newNpt.heading = heading + bearing;
        // http://danceswithcode.net/engineeringnotes/rotations_in_2d/rotations_in_2d.html
        double tx = pt.x*Math.cos(bearing) - pt.y*Math.sin(bearing);
        double ty = pt.x*Math.sin(bearing) + pt.y*Math.cos(bearing);
        // apply translation to go from origin to argument NavPoint
        // to given NavPoint
        newNpt.pt.x = tx + npt.pt.x;
        newNpt.pt.y = ty + npt.pt.y;
        return newNpt;        
    }
    
    /**
     * Create a NavPoint with heading in 180-degree from a source NavPoint.
     */
    public NavPoint reverse() {
        NavPoint newNpt = new NavPoint(pt, heading);
        newNpt.heading = (heading + Math.PI) % (2*Math.PI);
        return newNpt;
    }
    
    /**
     * Return a String with description of a given NavPoint.
     */
    public String toString() {
        return pt.toString()+" heading: "+heading+" orientation: "+orientation+" stop: "+stop;
    }
    
    /**
     * Constructors with alternate arguments than primary constructor.
     */
    public NavPoint()                                   { this(new Point(0.0, 0.0), defaultHeading, 0.0, false); }
    public NavPoint(double x, double y)                 { this(new Point(x,y), defaultHeading, 0.0, false); }
    public NavPoint(Point pt)                           { this(pt, defaultHeading, 0.0, false); }
    public NavPoint(double x, double y, double heading) { this(new Point(x,y), heading, 0.0, false); }
    public NavPoint(double x, double y, int degrees)    { this(new Point(x,y), Math.toRadians((double)degrees), 0.0, false); }
    public NavPoint(Point pt, double heading)           { this(pt, heading, 0.0, false);  }    
    public NavPoint(Point pt, int degrees)              { this(pt, Math.toRadians((double)degrees), 0.0, false); }
    public NavPoint(double x, double y, int degrees, boolean stop) { this(new Point(x,y), Math.toRadians((double)degrees), 0.0, stop); }
    public NavPoint(PathPoint pathPt)                   { this(pathPt.pt, pathPt.heading, 0.0, pathPt.stop); }
    public NavPoint(NavPoint npt)                       { this(npt.pt, npt.heading, 0.0, npt.stop); }
    
    /*
     * Primary class constructor.
     */
    public NavPoint(Point pt, double heading, double orientation, boolean stop) {
        this.pt = pt;
        this.heading = heading;
        this.orientation = orientation;
        this.stop = stop;
    }        
}
