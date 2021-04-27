package waypoint;  

import java.util.Random;

/**
 * Class to represent a robot. 
 * Although this robot is in tank-drive mode, it can be configured for mecanum.
 */
public class SimRobot {


    private double leftEncoder = 0.0;
    private double rightEncoder = 0.0;
    private double strafeEncoder = 0.0;
    
    // Amount of error to inject, as a ratio of desired outcome
    private double perturbation = 0.1; 
    private Random rand;
    
    // This is very much dependent on the robot:
    //   - diameter of wheels
    //   - linearity of motor reponse
    //   - torque and speed of motors
    public final double TICKS_PER_INCH = 100.0;
    public final double TICKS_PER_MS_PER_FULL_POWER = 2.0;
    
    public double robotWidthInches = 16.0;
    public double robotWidthTicks = robotWidthInches * TICKS_PER_INCH;
    
    /**
     * Update encoders to account for a given unit of time and motors at given
     * power levels, with an injection of some error.
     */
    public void moveRobot(double time, double leftPower, double rightPower) {        
        double deltaLeftEncoder  = time*leftPower*TICKS_PER_MS_PER_FULL_POWER *(1.0 + perturbation * (Math.random() - 0.5));
        double deltaRightEncoder = time*rightPower*TICKS_PER_MS_PER_FULL_POWER*(1.0 + perturbation * (Math.random() - 0.5));
        double xx = time*leftPower*TICKS_PER_MS_PER_FULL_POWER;
        double XX = perturbation * (Math.random() - 0.5);
        System.out.println("xx: "+xx);
        System.out.println("XX: "+XX); 
        System.out.println("simR-LeftE: "+deltaLeftEncoder+" simR-RightE: "+deltaRightEncoder+" time: "+time+" lP: "+leftPower+" rP: "+rightPower);
        leftEncoder += deltaLeftEncoder;
        rightEncoder += deltaRightEncoder;
    }
        
    // Get methods to retrieve encoder readings
    public double getLeftEncoder()   { return leftEncoder; }
    public double getRightEncoder()  { return rightEncoder; }
    public double getStrafeEncoder() { return strafeEncoder; }
    
    
    public SimRobot() {
        rand = new Random();
    }
}
