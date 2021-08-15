package waypoint;  

/**
 * Abstract Class to hold basic contents of NavPath subclasses:
 *   Vector, Arc, Gap
 */
public abstract class NavPath {

    NavPoint i;           // start Point
    NavPoint o;           // end Point
    boolean stop = false; // = true if this path element concludes with a stop
    double  orientation;  // orientation of robot relative to heading in NavPoint i
    
    public String toString() {
        return " i:"+i.toString()+" o:"+o.toString()+ " "+stop;
    }

}
