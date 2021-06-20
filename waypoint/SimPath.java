package waypoint;  

import java.util.ArrayList;
import java.util.List;

/**
 * Class to run through a path simulation.
 */
public class SimPath {

    // SimPath abides by encapsulation such that all methods declared public are intended to be
    // accessible from other classes and all methods declared private are strictly for use only by
    // SimPath.
    //   public methods:
    //     SimPath (constructor) - The dimensions of the game field are used for path error checking.
    //     updateSimPath()       - Use this method to update any parameters used in the simulation,
    //                             like path for the robot to traverse, given as an argument of List of NavPaths.
    //     doSimPath()           - Use this method to run a simulation of the robot traversing a path. 
    //  private methods:

    private List<NavPath> robotIntendedPath; // Path for robot to traverse
    private SimRobot      simRobot;          // Instantiation of a hypothetical robot
    private GameField     gameField;         // Reference to playing field the Path resides in
    
    
    private double coincidenceDistance = 0.1;    
    private double perturbationPct = 0.1;    
    private double unitTime;                  // parameter holding time between iterations of the control loop
    
    // Variables to hold robot's encoder readings
    private double leftEncoder;   
    private double rightEncoder;
    private double strafeEncoder;
    
    private Vector errorVector;             // placeholder for storing deviation of robot from the path
    private boolean reverse = false;
    
    /**
     *  Calculate the positional and heading error from the path given a 
     *  navigation point of (x,y,theta) and last-known-element.
     *  @return - List of NavPoints as a record of robot's travel along path
     */
    public List<NavPoint> doSimPath() {
    
        // SimPath Initialization
    
        // 1. Create a simulated robot. This gives something for this path simulator
        // to apply motor power levels to and to read encoder values from.
        simRobot = new SimRobot();
    
        // 2. Create a List of NavPoints. These will record the robot's poses as it
        // traverses the path.
        List<NavPoint> navPoints = new ArrayList<>();
    
        // 3. Determine robot's pose at start of the path
        // Create a NavPoint that represents the very beginning of path.
        // Add this NavPoint to the List of recorded robot poses.
        NavPath p = robotIntendedPath.get(0);
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
        navPoints.add(npt);
        //
        // Use the initial NavPoint, plus the index 0 to refer to the first path element
        // and create a PathPoint. 
        PathPoint pathPt = new PathPoint(npt, 0);
        
        // 4. Set variables to check for termination of control loop
        boolean inBounds = true;                            // true, while robot is in the game field
        int finalElementIndex = robotIntendedPath.size();   // The size of the List of path elements marks the end of the path
        
        // 5. Read encoders to get starting values
        leftEncoder  = simRobot.getLeftEncoder();
        rightEncoder = simRobot.getRightEncoder();
        
        
        //
        // SimPath Control Loop 
        //
        //   Stay in this loop until one of the following happens:
        //     - last path element in the path is traversed
        //     - (optional) user sets a limit to number of NavPoint generated while traversing the path
        //     - robot is determined to have gone outside the game field
        //
        while ((pathPt.index < finalElementIndex) &&
               //(navPoints.size() < 180) &&  // for debugging, use this to end the simulation after so many loop iterations
               inBounds &&
               true) {
               
               
            // 1. Apply power
            //    Note: in this simplistic model there's no error correction
            setRobotDriveMotorPower(unitTime, robotIntendedPath.get(pathPt.index));
            
            // 2. Calculate new pose of the robot, based on encoder readings
            pathPt = getRobotPose(pathPt);            
            // keep for debugging
            //System.out.println(" pathPt: "+pathPt.toString());
        
            // 3. Calculate which path element the robot is now following
            pathPt = traversePath(robotIntendedPath, pathPt);
                        
            // 4. Calculate the deviation from the path        
            errorVector = calcErrorVector(robotIntendedPath, pathPt);
            
            // record the robot's progress
            navPoints.add(new NavPoint(pathPt));
            // check if the simulated robot is still in the field, or if it would have smacked into a perimeter wall
            inBounds = (pathPt.pt.x >= gameField.FIELD_ORIGIN_X) && (pathPt.pt.x <= (gameField.FIELD_ORIGIN_X + gameField.FIELD_WIDTH_X)) &&
                       (pathPt.pt.y >= gameField.FIELD_ORIGIN_Y) && (pathPt.pt.y <= (gameField.FIELD_ORIGIN_Y + gameField.FIELD_WIDTH_Y));
        }
        return navPoints;        
    }
        
    /**
     *  Set power levels of drive motors.
     *  @param unitTime - control loop cycle time
     *  @param p        - 'current' path element
     */
    private void setRobotDriveMotorPower(double unitTime, NavPath p) { //, boolean reverse) {
        
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
        
        //if (reverse) {
            //double tempPower = leftPower;
            //leftPower  = 0.0 - rightPower;
            //rightPower = 0.0 - tempPower;
        //}
        
        // apply simulated movement with power to simRobot motors over some time interval
        simRobot.moveRobot(unitTime, leftPower, rightPower);
    }
     
    /**
     *  Read robot encoders and calculate robot pose.
     *  @param pathPt   - 'current' robot pose, expressed as a PathPoint
     *  @return         - 'new' robot pose after applying power to robot over unitTime time interval
     */
    private PathPoint getRobotPose(PathPoint pathPt) {
    
        // Create a new PathPoint
        // This new PathPoint will refer to the same path element as our current PathPoint
        PathPoint newPathPt = new PathPoint();  
        newPathPt.index     = pathPt.index;
    
        // read encoders to get new encoder values since last reading
        double nextLeftEncoder  = simRobot.getLeftEncoder();
        double nextRightEncoder = simRobot.getRightEncoder();
        
        // find difference in encoder values since last reading
        double deltaLeftEncoder  = nextLeftEncoder  - leftEncoder;
        double deltaRightEncoder = nextRightEncoder - rightEncoder;
        
        // save the new encoder values for next iteration through control loop
        leftEncoder  = nextLeftEncoder;
        rightEncoder = nextRightEncoder;
        
        
        // Use an approximation of movement to calculate new pose (x,y,theta)
        // based on the difference between the current and previous encoder readiings.
        //
        // A simple algorithm is to assume the robot traveled 'straight' at a distance that is the average
        // of the two left-side and right-side encoders. This approximation is only valid over very
        // short distances. 
        double midTravelInches = (deltaLeftEncoder + deltaRightEncoder)/ (2.0 * simRobot.TICKS_PER_INCH);
        newPathPt.pt.x = pathPt.pt.x + midTravelInches * Math.cos(pathPt.heading);
        newPathPt.pt.y = pathPt.pt.y + midTravelInches * Math.sin(pathPt.heading);
        // keep for debugging:
        //System.out.println("leftE: "+deltaLeftEncoder+" rightE: "+deltaRightEncoder+" midI: "+midTravelInches);
        //
        // The change in heading of the robot is approximated to be the arctan of the difference between
        // the left-side and right-side encoders. 
        double bearing = Math.atan ( Math.abs(deltaLeftEncoder - deltaRightEncoder) / simRobot.robotWidthTicks);
        if (deltaLeftEncoder > deltaRightEncoder) {
            newPathPt.heading = pathPt.heading - bearing;
        }
        else {
            newPathPt.heading = pathPt.heading + bearing;
        }
        // Return the newly calcuated pose (x,y,theta)
        return newPathPt;
    }
    
    /**
     * Given robot's pose and last known path element being traversed,
     * calculate which path element is now being tracked.
     * @param path   - full robot path, expressed as List of path elements
     * @param pathPt - 'current' robot pose
     * @return       - 'current' robot pose indexed to the 'current' path element
     */
    private PathPoint traversePath(List<NavPath> path, PathPoint pathPt) {
        // Tracking a path involves moving past the endpoints of successive path elements.
        PathPoint newPathPt = new PathPoint(pathPt);
        int finalPathIndex = path.size()-1;
        int currentPathIndex = pathPt.index;
        boolean keepTraversing = true;
        //
        while (keepTraversing && (currentPathIndex <= finalPathIndex)) {        
            //System.out.println("path element: "+idx);
            // Call method to check if endpoint of the 'current' path element has been reached
            boolean goNextElement = reachedEndpoint(path.get(currentPathIndex), pathPt);
            // If the endpoint of a path element has been reached, then increment the
            // path element index to examine the next path element
            if (goNextElement) { currentPathIndex += 1; }
            // Else if the endpoing has not been reached then we can exit the loop
            else { keepTraversing = false; }
            //keep for debugging
            System.out.println("Reached endpoint="+goNextElement);
        }
        // if going to a new path element, set the pathpoint heading to the nav point
        // of the new path element
        if ((newPathPt.index != currentPathIndex) && (currentPathIndex <= finalPathIndex)) {
            newPathPt.heading = path.get(currentPathIndex).i.heading;            
        }
        newPathPt.index = currentPathIndex;
        return newPathPt;
    }
    
    
    /**
     * Determine if location of pathPt exceeds the distance of the path element
     * or is near-coincident with the endpoint if the path element is a stop.
     * @param p      - 'current' NavPath
     * @param pathPt - 'current' robot pose (x,y,theta), expressed as a PathPoint
     * @return       - boolean, true if this algorithm determines the robot's pose reached 
     *                          the endpoint of the current path element
     */
    private boolean reachedEndpoint(NavPath p, PathPoint pathPt) {
    
        StdLine inLine;
        StdLine normalLine;
        
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
            //keep for debugging:
            System.out.println("Checking against Vector: "+v.toString());
            
            // Find the distance the pathPoint is normal to the Vector.
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
            //keep for debugging
            //System.out.println("inLineH: "+iv.heading+" v-H: "+v.heading+" iL: "+inLine.toString()+" nL: "+normalLine.toString()+" pL: "+paraLine.toString()+" ipt: "+ipt.toString());
            if (Math.abs(iv.heading - v.heading) > 0.1) {inLineDistance = 0 - inLineDistance;}
            // return true of given pathPt is located past the endpoint of the vector,
            // as relative to the startpoint of the vector.
            //keep for debugging
            //System.out.println("inLine: "+inLineDistance+ " v-mag: "+v.magnitude);
            if (inLineDistance >= v.magnitude) { return true; }
            // If this vector is a stop, allow for near-coincidence to endpoint
            double ptDistance = Point.distance(pathPt.pt, p.o.pt);
            //keep for debugging:
            //if (ptDistance <= coincidenceDistance) { System.out.println("coincident"); }
            return (ptDistance <= coincidenceDistance);
        }
        else if (p instanceof Arc) {
            Arc a = (Arc) p;
            //keep for debugging:
            System.out.println("Checking against Arc: "+a.toString());
            
            // Find the distance along the arc the given pathPoint is.
            // Given that the pathPoint may not be exactly on the arc, then calculate the distance
            // based on a projection of the pathPoint onto the arc from the center of the arc.
            double rtheta = Point.radianAngle(a.center, pathPt.pt);
            double inArcDistance = Math.min(Arc.calcLength(a.clockwise, a.startAngle, rtheta, a.radius),
                                            Arc.calcLength(!a.clockwise, a.startAngle, rtheta, a.radius)); 
            //keep for debugging:
            //System.out.println("aL: "+a.length+" inArc: "+inArcDistance);
            // If the pathPoint is further along the arc than the arc endpoint then return TRUE
            if (inArcDistance >= a.length) { 
                return true; 
            }
            // Calculate the distance between the pathPoint and the endpoint of the Arc
            double ptDistance = Point.distance(pathPt.pt, p.o.pt);
            // If the pathPoint is near-coincident to the endpoint of the Arc then return TRUE
            return (ptDistance <= coincidenceDistance);
        }
        else {
            // error!
        }
        return false;
    
    }
    
    /**
     * Calculate position error and heading error of the path point in the
     * frame of reference of the path element the path point is indexed to.
     * @param path   - List of path elements
     * @param pathPt - current robot pose (x,y,theta), expressed as a PathPoint
     * @return       - Vector, which gives magnitude of error from path 
     *                         but the direction is the heading difference between robot and path element
     */
    private Vector calcErrorVector(List<NavPath> path, PathPoint pathPt) {
    
        StdLine inLine;
        Vector errorVector = new Vector();
    
        int idx = pathPt.index;
        if (idx >= path.size()) { idx = path.size()-1; }
        
        NavPath p = path.get(idx);
        if (p instanceof Gap) {
            // ...get the normal distance of the pathPt to the Gap
            // create a Vector from the Gap path element
            Vector v = new Vector(p.i, p.o);
            // create a line from the (Vector) Gap path element
            inLine = v.toStdLine();
            // get the distance from the robot's location to the line
            errorVector.magnitude = inLine.distanceTo(pathPt.pt);
            errorVector.heading = v.heading - pathPt.heading;
            return errorVector;                        
        }
        else if (p instanceof Vector) {
            // ...get the normal distance of the pathPt to the Vector
            Vector v = (Vector) p;
            // create a line from the Vector path element
            inLine = v.toStdLine();
            // get the distance from the robot's location to the line
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
     *  Update the path parameters for this simulator to traverse.
     *  @param simPath  - List of path elements to traverse
     *  @param unitTime - control loop cycle time
     */
    public void updateSimPath(List<NavPath> simPath, double unitTime) {
        this.robotIntendedPath = new ArrayList<>();
        for (NavPath p : simPath) {
            this.robotIntendedPath.add(p);
        }    
        this.unitTime = unitTime;
    }
    
    /**
     *  Class constructor
     *  @param gf - reference to parent class, GameField, calling this simulator
     *              The dimensions of the game field are used for path error checking.
     */
    public SimPath(GameField gf) {
        this.gameField = gf;
    }
}
