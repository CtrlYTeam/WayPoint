package waypoint;  

/**
 * Class to hold an unresolved Gap between navigation points.
 */
public class Gap extends NavPath {

    String msg;     // internal debugging message
    
    /**
     * String of this Gap's parameters
     */ 
    public String toString() {
        return this.getClass().getSimpleName()+" "+super.toString();
    }
    
    /**
     * Constructors
     */    
    public Gap(double x1, double y1, double x2, double y2) {
        double heading = Math.atan2((y2 - y1),(x2 - x1));          
        this.i = new NavPoint(x1, y1, heading);
        this.o = new NavPoint(x2, y2, heading);
        this.msg = "";
    }    
    public Gap(NavPoint i, NavPoint o, String msg) {
        this.i = i;
        this.o = o;
        this.msg = msg;
    }    
    public Gap(Point i, Point o, String msg) {
        double heading = Math.atan2((o.y - i.y),(o.x - i.x)); 
        this.i = new NavPoint(i, heading);
        this.o = new NavPoint(o, heading);
        this.msg = msg;
    }    
    public Gap(String msg) {
        this.i = new NavPoint();
        this.o = new NavPoint();
        this.msg = msg;
    }
}
