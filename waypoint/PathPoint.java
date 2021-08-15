package waypoint;  

import java.util.Locale;

/**
 * Class to hold a 2D point, direction and index into Navigation Path.
 */
public class PathPoint extends NavPoint {

    int index;         // index to an element in a Navigation Path
    
    
    /**
     * Return a String with description of a given PathPoint.
     */
    public String toString() {
        return String.format(Locale.US, "%s heading: %6.3f (%7.2f) index: %d",
                pt.toString(),heading,heading*180.0/Math.PI,index);

    }

    /**
     * Class constructor from no arguments.
     */    
    public PathPoint() {
        this.pt = new Point(0.0, 0.0);
        this.heading = 0.0;
        this.stop = false;
        this.index = 0;
    }                
    /**
     * Class constructor from Point and index.
     */
    public PathPoint(Point pt, int index) {
        this.pt = pt;
        this.heading = 0.0;
        this.stop = false;
        this.index = index;
    }        
    /**
     * Class constructor from NavPoint and index.
     */
    public PathPoint(NavPoint npt, int index) {
        this.pt = npt.pt;
        this.heading = npt.heading;
        this.stop = npt.stop;
        this.index = index;
    }    
    /**
     * Class constructor from a PathPoint.
     */
    public PathPoint(PathPoint ppt) {
        this.pt = ppt.pt;
        this.heading = ppt.heading;
        this.stop = ppt.stop;
        this.index = ppt.index;
    }    
    
}
