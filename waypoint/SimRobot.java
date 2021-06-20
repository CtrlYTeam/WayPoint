package waypoint;  

import java.util.Random;

/**
 * Class to represent a robot. 
 * Although this robot is in tank-drive mode, it can be configured for mecanum.
 */
public class SimRobot {

    // 
    private double leftEncoder = 0.0;
    private double rightEncoder = 0.0;
    private double strafeEncoder = 0.0;
    // Methods to retrieve encoder readings
    public double getLeftEncoder()   { return leftEncoder; }
    public double getRightEncoder()  { return rightEncoder; }
    public double getStrafeEncoder() { return strafeEncoder; }
    
    // This is very much dependent on the robot:
    //   - diameter of wheels
    //   - linearity of motor reponse
    //   - torque and speed of motors
    public final double TICKS_PER_INCH = 100.0;
    public final double TICKS_PER_MS_PER_FULL_POWER = 2.0;
    
    public double robotWidthInches = 16.0;
    public double robotWidthTicks = robotWidthInches * TICKS_PER_INCH;
    
    // Amount of error to inject, as a ratio of desired outcome
    private double perturbation = 0.1; 
    
    /**
     * Update encoders to account for a given unit of time and motors at given
     * power levels, with an injection of some error.
     * The drivetrain for this hypothetical robot is a 2-motor tank drive
     * @param time       - duration to apply power, units of milliseconds
     * @param leftPower  - power to apply to motor on left side of robot   (range: -1.0 to 1.0)
     * @param rightPower - power to apply to motor on rightt side of robot (range: -1.0 to 1.0)     
     */
    public void moveRobot(double time, double leftPower, double rightPower) {    
        //keep for debugging
        System.out.println("leftPower="+leftPower+" rightPower="+rightPower);
        double deltaLeftEncoder  = time*leftPower*TICKS_PER_MS_PER_FULL_POWER *(1.0 + perturbation * (Math.random() - 0.5));
        double deltaRightEncoder = time*rightPower*TICKS_PER_MS_PER_FULL_POWER*(1.0 + perturbation * (Math.random() - 0.5));
        leftEncoder += deltaLeftEncoder;
        rightEncoder += deltaRightEncoder;
    }
        
    /**
     * Constructor
     */
    public SimRobot() {
        //rand = new Random();
    }
}
