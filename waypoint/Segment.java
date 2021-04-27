package waypoint;  

/**
 * Class to hold a 2D segment.
 */
public class Segment {

    Point i;          // start Point
    Point o;          // end Point
        
    // https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/        
    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are colinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    public static int orientation(Point p, Point q, Point r) {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0.0) return 0;   // colinear
        return (val > 0.0) ? 1 : 2; // clockwise or counterclockwise
    }
        
    // https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/        
    public static boolean doIntersect(Segment v1, Segment v2) {
        int o1 = orientation(v1.i, v1.o, v2.i);
        int o2 = orientation(v1.i, v1.o, v2.o);
        int o3 = orientation(v2.i, v2.o, v1.i);
        int o4 = orientation(v2.i, v2.o, v1.o);        
        return ((o1 != o2) && (o3 != o4));
    }
     
    /**
     * Constructors
     */
    public Segment(Point i, Point o) {
        this.i = i;
        this.o = o;
    }    
    public Segment(NavPoint b, NavPoint e) {
        this.i = b.pt;
        this.o = e.pt;
    }    
    public Segment(Vector v) {
        this.i = v.i.pt;
        this.o = v.o.pt;
    }
}
