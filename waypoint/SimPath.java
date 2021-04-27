package waypoint;  

import java.util.ArrayList;
import java.util.List;

/**
 * Class to run through a path simulation.
 */
public class SimPath {

    private StdLine inLine;
    private StdLine normalLine;
    private double coincidenceDistance = 0.1;
    
    private double unitTravelDistance = 1.0;
    private double perturbationPct = 0.1;
    
    private List<NavPath> path; // Path to traverse
    private SimRobot simRobot;
    private GameField gameField;
    private double unitTime;

    /**
     *  Calculate the positional and heading error from the path given a 
     *  navigation point of (x,y,theta) and last-known-element.
     *  @return - List of NavPoints as a record of robot's travel along path
     */
    public List<NavPoint> doSimPath() {
    
        simRobot = new SimRobot();
    
        List<NavPoint> navPoints = new ArrayList<>();
    
        // Create initial PathPoint at very beginning of path.
        NavPath p = path.get(0);
        NavPoint npt = new NavPoint(p.i);
        if (p instanceof Gap) {
            Gap g = (Gap) p;
            npt.heading = Point.radianAngle(p.i.pt, p.o.pt);
        }
        else if (p instanceof Vector) {
            Vector v = (Vector) p;
            npt.heading = v.heading;
        }
        else if (p instanceof Arc) {
            Arc a = (Arc) p;
            if (a.clockwise) { npt.heading = a.startAngle - Math.PI/2.0; }
            else             { npt.heading = a.startAngle + Math.PI/2.0; }
        }                        
        // Get starting point of first path element and save as first NavPoint in resultant simulation path
        PathPoint pathPt = new PathPoint(npt, 0);
        //System.out.println(" pathPt0: "+pathPt.toString());
        navPoints.add(new NavPoint(pathPt));
        boolean inBounds = true;
        
        int doneIdx = path.size();
        while ((pathPt.index < doneIdx) &&
//               (navPoints.size() < 180) &&  // for debugging, use this to end the simulation after so many loop iterations
               inBounds &&
               true) {
        
            pathPt = moveUnitTravelTime(unitTime, path.get(pathPt.index), pathPt);
            System.out.println(" pathPt: "+pathPt.toString());
        
            int lastIndex = pathPt.index;
            pathPt.index = traversePath(path, pathPt);            
            // if going to a new path element, set the pathpoint heading to the nav point
            // of the new path element.
            if ((lastIndex != pathPt.index) && (pathPt.index < doneIdx)) {
                pathPt.heading = path.get(pathPt.index).i.heading;
            }
            // calculate the deviation from the path
            // (but right now, there's no code to respond to this error!)
            Vector errorVector = calcErrorVector(path, pathPt);
            
            //
            navPoints.add(new NavPoint(pathPt));
            // check if the simulated robot is still in the field, or if it would have smacked into a perimeter wall
            inBounds = (pathPt.pt.x >= gameField.FIELD_ORIGIN_X) && (pathPt.pt.x <= (gameField.FIELD_ORIGIN_X + gameField.FIELD_WIDTH_X)) &&
                       (pathPt.pt.y >= gameField.FIELD_ORIGIN_Y) && (pathPt.pt.y <= (gameField.FIELD_ORIGIN_Y + gameField.FIELD_WIDTH_Y));
        }
        return navPoints;        
    }
    
    /**
     *  Interfacing method to SimRobot, that applies power to motors and read encoders
     *  @param unitTime - control loop cycle time
     *  @param p        - 'current' path element
     *  @param pathPt   - 'current' robot pose, expressed as a PathPoint
     *  @return         - 'new' robot pose after applying power to robot over unitTime time interval
     */
    public PathPoint moveUnitTravelTime(double unitTime, NavPath p, PathPoint pathPt) {
    
        PathPoint newPathPt = new PathPoint();  
        newPathPt.heading = pathPt.heading;
        newPathPt.index   = pathPt.index;
    
        // read encoders to get starting values
        double startLeftEncoder  = simRobot.getLeftEncoder();
        double startRightEncoder = simRobot.getRightEncoder();
    
    
        // For Gaps and Vectors, use the default settings:
        double leftPower  = 1.0;
        double rightPower = 1.0;
    
        // For Arc, apply full power to exteroir side of robot to the arc center
        //     and partial power to the interior side of robot to the arc center
        // The ratio of exteror/interior power depends on the radius of the arc
        if (p instanceof Arc) {
            Arc a = (Arc) p;
            double maxPower = 1.0;
            double arcPower = maxPower * ( a.radius - 0.5*simRobot.robotWidthInches ) / ( a.radius + 0.5*simRobot.robotWidthInches );
            if (a.clockwise) {
                leftPower = maxPower;
                rightPower = arcPower;
            }
            else {
                leftPower = arcPower;
                rightPower = maxPower;
            }
        }
        // apply simulated movement with power to simRobot motors over some time interval
        simRobot.moveRobot(unitTime, leftPower, rightPower);
        
        // read encoders to get ending values
        double deltaLeftEncoder  = simRobot.getLeftEncoder()  - startLeftEncoder;
        double deltaRightEncoder = simRobot.getRightEncoder() - startRightEncoder;
        
        // Use simple approximation of movement
        // to calculate new pose (x,y,theta)
        // and save this as a new PathPoint.
        double midTravelInches = (deltaLeftEncoder + deltaRightEncoder)/ (2.0 * simRobot.TICKS_PER_INCH);
        System.out.println("leftE: "+deltaLeftEncoder+" rightE: "+deltaRightEncoder+" midI: "+midTravelInches);
        newPathPt.pt.x = pathPt.pt.x + midTravelInches * Math.cos(pathPt.heading);
        newPathPt.pt.y = pathPt.pt.y + midTravelInches * Math.sin(pathPt.heading);
        //
        double bearing = Math.atan ( Math.abs(deltaLeftEncoder - deltaRightEncoder) / simRobot.robotWidthTicks);
        if (deltaLeftEncoder > deltaRightEncoder) {
            newPathPt.heading -= bearing;
        }
        else {
            newPathPt.heading += bearing;
        }
        // return the newly calcuated pose (x,y,theta)
        return newPathPt;
    }

    /**
     * Calculate position error and heading error of the path point in the
     * frame of reference of the path element the path point is indexed to.
     * @param path   - List of path elements
     * @param pathPt - current robot pose (x,y,theta), expressed as a PathPoint
     * @return       - Vector, which gives magnitude and direction of error from path 
     */
    public Vector calcErrorVector(List<NavPath> path, PathPoint pathPt) {
    
        Vector errorVector = new Vector();
    
        int idx = pathPt.index;
        if (idx >= path.size()) { idx = path.size()-1; }
        
        NavPath p = path.get(idx);
        if (p instanceof Gap) {
            Vector v = new Vector(p.i, p.o);
            inLine = v.toStdLine();
            errorVector.magnitude = inLine.distanceTo(pathPt.pt);
            errorVector.heading = v.heading - pathPt.heading;
            return errorVector;                        
        }
        else if (p instanceof Vector) {
            // get the normal distance of the pathPt to the vector
            Vector v = (Vector) p;
            inLine = v.toStdLine();
            errorVector.magnitude = inLine.distanceTo(pathPt.pt);
            errorVector.heading = v.heading - pathPt.heading;
            return errorVector;            
        }
        else if (p instanceof Arc) {
            Arc a = (Arc) p;
            double rtheta = Point.radianAngle(a.center, pathPt.pt);
            if (a.clockwise) { rtheta -= Math.PI/2.0; }
            else             { rtheta += Math.PI/2.0; }
            errorVector.heading = rtheta - pathPt.heading;
            errorVector.magnitude = Point.distance(a.center, pathPt.pt) - a.radius;
            return errorVector;            
        }
        else {
            // error
        }
        return errorVector;
    }
    
    
    /**
     * Given a point, and last known path element being tracked,
     * calculate which path element is now being tracked.
     * @param path   - List of path element
     * @param pathPt - 'current' robot pose
     * @return       - index into List of path elements the new 'current' path element is
     */
    public int traversePath(List<NavPath> path, PathPoint pathPt) {
        // Tracking a path involves moving past the endpoints of successive path elements.
        int lastIdx = path.size()-1;
        int idx = pathPt.index;
        boolean traversePath = idx <= lastIdx;
        //
        while (traversePath) {        
            System.out.println("path element: "+idx);
            NavPath p = path.get(idx);
            boolean goNextElement = reachedEndpoint(p, pathPt);
            if (goNextElement) { idx += 1; }
            traversePath = (goNextElement && (idx <= lastIdx));        
        }
        return idx;
    }
    
    
    /**
     * Determine if location of pathPt exceeds the distance of the path element
     * or is near-coincident with the endpoint if the path element is a stop.
     * @param p      - 'current' NavPath
     * @param pathPt - 'current' robot pose (x,y,theta), expressed as a PathPoint
     * @return       - boolean, true if this algorithm determines the robot's pose reached 
     *                          the endpoint of the current path element
     */
    public boolean reachedEndpoint(NavPath p, PathPoint pathPt) {
        if (p instanceof Gap) {
            Vector v = new Vector(p.i, p.o);
            System.out.println("Checking against Gap: "+v.toString());
            // Get line between points in gap
            inLine = v.toStdLine();
            // Get line normal to gap that intersects at gap starting point
            normalLine = StdLine.perpLineAtPoint(inLine, v.i.pt);
            // Get line parallel to vector through path point
            StdLine paraLine = StdLine.perpLineAtPoint(normalLine, pathPt.pt);
            // Get intersection of lines normal to and parallel to vector
            Point ipt = StdLine.intersectionPoint(normalLine, paraLine);
            // Create a vector to get a magnitude from (parallel) vector starting point to path point
            Vector iv = new Vector(ipt, pathPt.pt);
            // If parallel vector is opposite direction of path vector then distance is negative.
            double inLineDistance = iv.magnitude;            
            if (Math.abs(iv.heading - v.heading) > 0.1) {inLineDistance = 0 - inLineDistance;}
            // return true of given pathPt is located past the endpoint of the gap,
            // as relative to the startpoint of the gap.
            if (inLineDistance >= v.magnitude) { return true; }
            // If this gap is a stop, allow for near-coincidence to endpoint
            double ptDistance = Point.distance(pathPt.pt, v.o.pt);
            return (ptDistance <= coincidenceDistance);
        }
        else if (p instanceof Vector) {            
            Vector v = (Vector) p;
            System.out.println("Checking against Vector: "+v.toString());
            inLine = v.toStdLine();
            // Get line normal to vector that intersects at vector starting point
            normalLine = StdLine.perpLineAtPoint(inLine, v.i.pt);
            // Get line parallel to vector through path point
            StdLine paraLine = StdLine.perpLineAtPoint(normalLine, pathPt.pt);
            // Get intersection of lines normal to and parallel to vector
            Point ipt = StdLine.intersectionPoint(normalLine, paraLine);
            // Create a vector to get a magnitude from (parallel) vector starting point to path point
            Vector iv = new Vector(ipt, pathPt.pt);
            // If parallel vector is opposite direction of path vector then distance is negative.
            double inLineDistance = iv.magnitude;            
            System.out.println("inLineH: "+iv.heading+" v-H: "+v.heading+" iL: "+inLine.toString()+" nL: "+normalLine.toString()+" pL: "+paraLine.toString()+" ipt: "+ipt.toString());
            if (Math.abs(iv.heading - v.heading) > 0.1) {inLineDistance = 0 - inLineDistance;}
            // return true of given pathPt is located past the endpoint of the vector,
            // as relative to the startpoint of the vector.
            System.out.println("inLine: "+inLineDistance+ " v-mag: "+v.magnitude);
            if (inLineDistance >= v.magnitude) { return true; }
            // If this vector is a stop, allow for near-coincidence to endpoint
            double ptDistance = Point.distance(pathPt.pt, p.o.pt);
            if (ptDistance <= coincidenceDistance) { System.out.println("coincident"); }
            return (ptDistance <= coincidenceDistance);
        }
        else if (p instanceof Arc) {
            Arc a = (Arc) p;
            System.out.println("Checking against Arc: "+a.toString());
            // Find the distance along the arc the given path point is, if it is projected
            // onto the arc at the given radius from the center of the arc.
            double rtheta = Point.radianAngle(a.center, pathPt.pt);
            double inArcDistance = Math.min(Arc.calcLength(a.clockwise, a.startAngle, rtheta, a.radius),
                                            Arc.calcLength(!a.clockwise, a.startAngle, rtheta, a.radius)); 
            System.out.println("aL: "+a.length+" inArc: "+inArcDistance);
            if (inArcDistance >= a.length) { return true; }
            double ptDistance = Point.distance(pathPt.pt, p.o.pt);
            return (ptDistance <= coincidenceDistance);
        }
        else {
            // error!
        }
        return false;
    
    }
    
    /**
     *  Update the path parameters for this simulator to traverse.
     *  @param simPath  - List of path elements to traverse
     *  @param unitTime - control loop cycle time
     */
    public void updateSimPath(List<NavPath> simPath, double unitTime) {
        this.path = new ArrayList<>();
        for (NavPath p : simPath) {
            this.path.add(p);
        }    
        this.unitTime = unitTime;
    }
    
    /**
     *  Class constructor
     *  @param gf - reference to parent class calling this simulator
     */
    public SimPath(GameField gf) {
        this.gameField = gf;
    }
}
