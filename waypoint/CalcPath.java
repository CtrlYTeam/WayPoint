package waypoint; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CalcPath - build a robot path from Vectors and Arcs. :)
 */
public class CalcPath {

    SettingsFrame mySettings = null;
    boolean allow_waypoint_reversals = true;
    boolean allow_strafe = true;
    
    /**
     * Root method for running path generation algorithm.
     * @param navpoints - List of NavPoints, in sequence, to calculate a path for
     * @return          - List of NavPaths representing the calculated path
     */
    public List<NavPath> genPath (List<NavPoint> navpoints) {
        updateCalcParameters();
        System.out.println("allow_waypoint_reversals:"+allow_waypoint_reversals);
        if (navpoints == null) {
            System.out.println("No navpoints for genPath.");
            return null;
        }
        if (navpoints.size() < 2) {
            System.out.println("Number of navpoints: "+navpoints.size()+"; too few for a full genPath.");
            return null;
        }
        List<NavPath> path = new ArrayList<>();
        for (int i = 0; i < navpoints.size()-1; i++) {
            System.out.println("Resolving Connection between NavPoints: "+i+","+(i+1));
            List<NavPath> subpath = resolveConnection(navpoints.get(i), navpoints.get(i+1));
            for (NavPath p : subpath) {
                path.add(p);
            }
        }
        return path;
    }
    
    /**
     * Find a path, if possible, of Vectors and Arcs between two given waypoints.
     * @param npt1 - Initial NavPoint
     * @param npt2 - Final NavPoint
     * return      - List of NavPaths to get from initial NavPoint to final NavPoint
     */
    public List<NavPath> resolveConnection(NavPoint npt1, NavPoint npt2) {
    
        // A sequence of geometric solutions are proposed to resolve moving from
        // the initial NavPoint, npt1 to the final NavPoint, npt2:
        //
        // 1. Check if NavPoints are coincident
        // 2. Check if NavPoints are colinear
        // 3. Check for Vector+Arc fit
        // 4. Check for Arc+Vector fit
        // 5. Check if an intermediate waypoint guides to a solution
        // 6. Check if simple strafing fits
    
        List<NavPath> path = new ArrayList<>();
        
        // 1. If the NavPoints are coincident then use an Arc (radius=0.0) to connect them.
        if (npt1.isCoincident(npt2)) {
            Arc a = new Arc(npt1.pt, 0.0, npt1.heading, npt2.heading);
            a.stop = true;
            path.add(a);
            System.out.println("Navpoints are coincident");
            return path;
        } 
        
        // 2. If the NavPoints are colinear then use a Vector to connect them.
        Vector vector = Vector.genVector(npt1, npt2);
        if (vector != null) { 
            vector.stop = npt2.stop;
            path.add(vector);
            System.out.println("Navpoints are colinear");
            return path;
        }        
        
        // 3. If the target NavPoint can be reached with a Vector + Arc then connect them.
        //    consider Vector->Arc from initial NavPoint to final NavPoint
        //                
        System.out.println("--Vector->Arc--");
        List<NavPath> vectorCurve = findVectorCurve(npt1, npt2, true);
        if (vectorCurve != null) {
            return vectorCurve;
        }
        
        // 4. Consider in reverse Vector->Arc from final NavPoint to initial NavPoint, 
        //      which equates to Arc->Vector from initial NavPoint to final NavPoint.
        //
        System.out.println("--Arc->Vector--");
        List<NavPath> curveVector = findCurveVector(npt1, npt2);
        if (curveVector != null) {
            return curveVector;
        }
        
        
        // 5. Consider near-parallel, non-collinear NavPoints.
        // Propose a new waypoint between the NavPoints and a path of
        //   initial NavPoint->Arc->waypoint->(Vector+Arc)->final NavPoint
        //
        System.out.println("--Arc->Vector->Arc--");
        Point midpt = new Point((npt1.pt.x+npt2.pt.x)/2.0, (npt1.pt.y+npt2.pt.y)/2.0);
        Arc arc = Arc.calcArcNavPointToPoint(npt1, midpt);
        System.out.println("arc center = "+arc.center.toString());
        System.out.println(arc.toString());
        System.out.println("arc endpt  = "+arc.o.toString());
        //
        List<NavPath> vectorArc = findVectorCurve(arc.o, npt2, true);
        if (vectorArc != null) { System.out.println("vA size = "+vectorArc.size()); }
        else                   { System.out.println("vA is null"); }
        if (arc != null && vectorArc != null) {
            path.add(arc);
            for (NavPath p : vectorArc) {
                path.add(p);
            }
            return path;
        }
        List<NavPath> arcVector = findCurveVector(arc.o, npt2);
        if (arc != null && arcVector != null) {
            path.add(arc);
            for (NavPath p : arcVector) {
                path.add(p);
            }
            return path;
        }
        
        // 6. If the NavPoints are parallel and co-oriented then strafing can connect them
        if (allow_strafe && npt1.isParallel(npt2) && npt1.isOriented(npt2)) {
            Vector strafeVector = new Vector(npt1, npt2);
            strafeVector.i.heading = strafeVector.heading; // kluge
            path.add(strafeVector);
            return path;
        }
        
        // No solution found
        path.add(new Gap(npt1, npt2, "No solution found."));
        return path;
    }
    
    
    
    
    /**
     * Find a connection between two waypoints that is a Vector followed by an Arc.
     * @param npt1      - starting NavPoint
     * @param npt2      - destination NavPoint
     * @param direction - true if npt1 is the true starting point, false getting called by findCurveVector
     * return      - List of NavPaths: could be a single Arc, single Vector, or (0)Vector+(1)Arc
     *               In case of no path, a null value is returned
     */
    public List<NavPath> findVectorCurve(NavPoint npt1, NavPoint npt2, boolean direction) {
    
        // Create a relative framework, where npti is at origin and npto is referenced from origin
        NavPoint npti = new NavPoint();
        NavPoint npto = npt2.relativeTo(npt1);
        System.out.println("findVectorCurve(npt1,npt2)");
        System.out.println(" npt1: "+npt1.toString());
        System.out.println(" npt2: "+npt2.toString());
        System.out.println(" npti: "+npti.toString());
        System.out.println(" npto: "+npto.toString());
        
        // Create a List of Navpaths (which are a List of path elements)
        // to consider all solutions to findCircles().
        List<List<NavPath>> paths = new ArrayList<List<NavPath>>();
        List<Boolean>       stops = new ArrayList<Boolean>();
        
        // Find circles such that they have: 
        //   a point coincident and tangent to the vector extending from npti and
        //   a point coincident to npto and tangent to the heading of npto.
        List<Circle> circles = Circle.findCircles(npti, npto);
        System.out.println(""+circles.size()+" Circle(s) to consider");
        
        if (circles.size() == 0) {
            System.out.println("findVectorCurve: No circles found");
            return null;
        }
        
        // iterate through all the circles in the List of found circles
        for (Circle circle : circles) {
        
            List<NavPath> path = new ArrayList<NavPath>();
        
            // Check if Arc starting at beginning NavPoint was found
            // therefore no preceeding Vector to the Arc is needed
            if (Math.abs(circle.center.y) < 0.000001) {
                NavPoint centerNpt = new NavPoint(circle.center);
                centerNpt = centerNpt.displacedBy(npt1);
                Arc arc = new Arc(centerNpt.pt, npt1.pt, npt2.pt);
                arc.stop = npt2.stop;
                System.out.println("findVectorCurve: Single Arc solution");
                System.out.println(arc.toString());
                path.add(arc);
                paths.add(path);
                stops.add(false);
                continue;
            }
            // A negative-y center of rotation is not (currently) acceptable as this would
            // require a reversed Vector to get to the curve
            if (circle.center.y < 0.0) {
                //path.add(new Gap(npt1, npt2, "No solution found: Circle center with y < 0.0"));
                System.out.println("findVectorCurve: No solution to Circle center with y < 0.0");
                continue;
            }       
        
            // We have a curve, so prepend the curve with a vector
            System.out.println("Consider: "+circle.toString());
            // We are still in a relative framework where the circle is relative to (0.0,0.0),90 deg.
            // We can calculate a Vector in this relative framework as ending at (0.0, circle.center.y)
            // since we interpret the Vector endpoint, which is the Arc startpoint, at the intersection of
            // the Vector and a line normal to the Vector going through the circle center.
            double circleY = circle.center.y;
            NavPoint localEndVector = new NavPoint(0.0, circleY, 90);
            NavPoint globalEndVector = localEndVector.displacedBy(npt1);
            // Construct Vector from npt1 and the (globally-referenced) Vector Endpoint.
            Vector vector = new Vector(npt1.pt, globalEndVector.pt);
            System.out.println("new vector:"+vector.toString());
            //
            // The arc center is given relative to (0.0,0.0),90. We need to convert back to reference npt1
            NavPoint circleCenterNpt = new NavPoint(circle.center, 90);
            // Get circle center in global coordinate system
            NavPoint globalCircleCenterNpt = circleCenterNpt.displacedBy(npt1);
            // The arc starting point is the endpoint of the Vector: pt, already in global coordinate system
            // The arc end point is already npt2, in global coordinate system
            System.out.println("localCenterNpt :"+circleCenterNpt.toString());
            System.out.println("globalCenterNpt:"+globalCircleCenterNpt.toString());
            System.out.println("starting ArcNpt:"+globalEndVector.toString());
            System.out.println("ending ArcNpt  :"+npt2.toString());        
            Arc arc = new Arc(globalCircleCenterNpt.pt, globalEndVector.pt, npt2.pt);
        
            // If the ending arc heading is not aligned to the heading of the target navpoint 
            // then this is not a valid solution.
            // End Angle is either -90 or +90 relative to the target heading 
            // A clockwise arc needs to be +90, a CCW needs to be -90.
            double endBearing = arc.endAngle - npt2.heading;
            // renormalize the difference if +/-270 to -/+90
            if (endBearing < 0-Math.PI) { endBearing += 2*Math.PI; }
            if (endBearing > 0+Math.PI) { endBearing -= 2*Math.PI; }
            if (((endBearing < 0.0) && arc.clockwise) || ((endBearing > 0.0) && !arc.clockwise)) {
                System.out.println("findVectorCurve: No solution to opposite arc heading from target heading");
                continue;
            }
        
            // Start Angle is either -90 or +90 relative to vector heading.   (+180)*     ^(+90)   *(-180)
            double bearing = arc.startAngle - vector.heading;
            // bearing of -270 (<-180) should be taken at +90
            // bearing of +270 (>+180) should be taken as -90
            if (bearing < 0-Math.PI) { bearing += 2*Math.PI; }
            if (bearing > 0+Math.PI) { bearing -= 2*Math.PI; }
            // For VectorCurve solutions: (direction = true)
            //   -90 and CW or +90 and CCW means a reversed direction from vector.
            // For CurveVector solutions: (direction = false)
            //   -90 and CCW or +90 and CW means a reversed direction from vector.
            boolean reversal = false;
            if (direction) { // VectorCurve
                reversal = ((bearing < 0.0) && arc.clockwise) || ((bearing > 0.0) && !arc.clockwise);
                vector.stop = reversal;
                arc.stop = npt2.stop;
            } else {         // CurveVector
                reversal = ((bearing < 0.0) && !arc.clockwise) || ((bearing > 0.0) && arc.clockwise);
                arc.stop = reversal;
                vector.stop = npt2.stop;
            }
            if (reversal) {             
                System.out.println("A stop waypoint needeed"); 
                if (direction) {
                    arc.orientation = Math.PI;
                }
            }            
            if (reversal && !allow_waypoint_reversals) {
                System.out.println("No waypoint reversals permitted but one was needed here");
                continue;
            }
            path.add(vector);
            path.add(arc);
            paths.add(path);
            stops.add(reversal);
        }
        
        // If no solutions found then is what it is
        if (paths.size() < 1) {
            return null;
        }
        // If one solution found then also is what it is
        else if (paths.size() == 1) {
            return paths.get(0);
        }
        // Pick best solution if possible, which is the only path without a reversal
        else {
            int pathsAreClean = 0;
            int idx = 0;
            int cnt = 0;
            for (boolean s : stops) {
                if (!s) { 
                    pathsAreClean++; 
                    idx = cnt;
                }
                cnt++;
            }
            if (pathsAreClean == 1) {
                return paths.get(idx);
            }
            else {
                System.out.println("Couldn't find a sole non-reversal path");
                return null;
            }            
        }    
    }
    
    
    /**
     * Find a connection between two waypoints that is an Arc followed by an Vector.
     * @param npt1 - starting NavPoint
     * @param npt2 - destination NavPoint
     * return      - List of NavPaths: could be a single Arc, single Vector, or (0)Arc+(1)Vector
     *               In case of no path, a null value is returned
     */
    public List<NavPath> findCurveVector(NavPoint npt1, NavPoint npt2) {
        // This does findVectorCurve but in reverse.
        // Check if a Curve + Vector can resolve path profile between NavPoints
        //   Consider the reverse direction of end NavPoint to start Navpoint
        //   as a Vector + Curve.
        List<NavPath> path = new ArrayList<>();
        NavPoint nptr2 = npt2.reverse();
        NavPoint nptr1 = npt1.reverse();
        System.out.println("findCurveVector(npt1,npt2)");
        System.out.println("nptr1: "+nptr1.toString());
        System.out.println("nptr2: "+nptr2.toString());
        
        List<NavPath> vectorCurve = findVectorCurve(nptr2, nptr1, false);
        if (vectorCurve != null) {
            Vector v = (Vector) vectorCurve.get(0);
            v = v.reverse();
            Arc a = (Arc) vectorCurve.get(1);
            double orientation = a.orientation;
            System.out.println("arc pre-reversal: i:"+a.i.toString()+" o:"+a.o.toString());
            a = a.reverse();
            System.out.println("arc pst-reversal: i:"+a.i.toString()+" o:"+a.o.toString());
            a.orientation = orientation;
            a.stop = v.stop;    // switch the vector->arc stop to arc->vector stop 
            v.stop = npt2.stop; // since the vector is the last path element, set its stop to the ending NavPoint stop
            path.add(a);
            path.add(v);
            return path;
        }
        return null;
    }
     
    
    /**
     * Set variables in this class instantiation from global mySettings Map of parameters.
     * This methods needs to be public so that SettingsFrame can update parameters here.
     */
    public void updateCalcParameters() {
        if (mySettings != null) {
            // Pull relevant info from SettingsFrame
            allow_waypoint_reversals = Boolean.parseBoolean(mySettings.parameters.get("CALC_ALLOW_WAYPOINT_REVERSALS"));
            allow_strafe             = Boolean.parseBoolean(mySettings.parameters.get("CALC_ALLOW_STRAFE"));
        }
    }
    
    /**
     * Constructor, when called from GameField class.
     */
    public CalcPath(SettingsFrame settingsFrame) {    
        this.mySettings = settingsFrame;
        updateCalcParameters();
    }
    
  
    /**
     * Constructor, when called from command line.
     */
    public CalcPath(String[] args) {
        List<NavPoint> waypoints = GameFieldFileHandler.getNavPointsFromFile(args[0]);
        List<NavPath> path = genPath(waypoints);
        GameFieldFileHandler.toNavPathFile(path, "path.txt");    
    }  
    /**
     * main - method to allow command line application launch.
     *        (credit to opencv tutorial code provided by the OpenCV website: docs.opencv.org)
     */
    public static void main(String[] args) {

        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CalcPath(args);
            }
        });
    }
}
