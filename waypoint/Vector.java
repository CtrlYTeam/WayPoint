package waypoint;  

/**
 * Class to hold a 2D vector.
 */
public class Vector extends NavPath {

    double magnitude; // length of vector
    double heading;   // direction of vector
    
    /**
     * Create a Vector from two NavPoints: one the startpoint of the Vector, the other the endpoint.
     */
    public static Vector genVector(NavPoint npt1, NavPoint npt2) {
        // If the orientations and headings are not the same then no Vector generated
        if (!npt1.isParallel(npt2) || !npt1.isOriented(npt2)) { 
            return null; 
        }
                
        StdLine line = npt1.toStdLine();
        double distance = line.distanceTo(npt2.pt);
        // If a line can be created that sufficiently contains both NavPoints with their headings, then create a Vector
        if (distance < 0.000001) {
            return new Vector(npt1, npt2);
        }
        return null;        
    }
    
    /**
     * Create a vector of opposite direction and same magnitude of a given vector.
     */
    public Vector reverse() {
        return new Vector(o.pt, i.pt, this.stop);
    }
    
    /**
     * Return a line of standard form ax+by+c=0 from this vector.
     */
    public StdLine toStdLine() {
        NavPoint npt = new NavPoint(this.o.pt, this.heading);
        return npt.toStdLine();
    }
    
    /**
     * Return a String with description of a given Vector.
     */
    public String toString() {
        return this.getClass().getSimpleName()+" "+super.toString();
    }    
    public String toString(boolean flag) {
        if (flag) { return "Vector magnitude: "+magnitude+" heading: "+heading; }
        else      { return toString(); }
    }
    
    /**
     * Constructors
     */
    public Vector(double x1, double y1, double x2, double y2) {
        this.heading = Math.atan2((y2 - y1),(x2 - x1));      
        this.i = new NavPoint(x1, y1, this.heading);
        this.o = new NavPoint(x2, y2, this.heading);
        this.stop = false;
        this.magnitude = Point.distance(this.i.pt, this.o.pt);
    }                
    public Vector(Point i, Point o, boolean stop) {
        this.heading = Math.atan2((o.y - i.y),(o.x - i.x)); 
        this.i = new NavPoint(i, this.heading);
        this.o = new NavPoint(o, this.heading);
        this.stop = stop;
        this.magnitude = Point.distance(this.i.pt, this.o.pt);
    }                
    public Vector(Point i, Point o) {
        this.heading = Math.atan2((o.y - i.y),(o.x - i.x)); 
        this.i = new NavPoint(i, this.heading);
        this.o = new NavPoint(o, this.heading);
        this.stop = false;
        this.magnitude = Point.distance(this.i.pt, this.o.pt);
    }    
    public Vector(NavPoint b, NavPoint e) {
        this.i = b;
        this.o = e;
        this.stop = e.stop;
        this.magnitude = Point.distance(b.pt, e.pt);
        this.heading   = Math.atan2((this.o.pt.y - this.i.pt.y),(this.o.pt.x - this.i.pt.x)); 
    }    
    public Vector() {
        this.i = new NavPoint();
        this.o = new NavPoint();
        this.stop = false;
        this.magnitude = 0.0;
        this.heading = 0.0;
    }        
}
