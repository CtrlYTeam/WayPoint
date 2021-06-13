package waypoint;  

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.Toolkit;

import javax.swing.JPanel;

import static waypoint.GameField.INNER_TILE_WIDTH;
import static waypoint.GameField.MESH_TILE_WIDTH;
import static waypoint.GameField.FIELD_WIDTH;
import static waypoint.GameField.TAPE_WIDTH;

/**
 * Class to hold methods to draw onto a graphical game field panel.
 */
public class DrawField extends JPanel {

    private GameField gf;
    
    private double SCALE; // default was 5.0 pixels per inch, now is scaled to display running app
    private static final double BORDER_WIDTH = 3.0;                    // border around field for panel display
    
    private int FIELD_PIXEL_ORIGIN_X;  // field origin in pixels, offset from 0,0 in inches
    private int FIELD_PIXEL_ORIGIN_Y;  // field origin in pixels, offset from 0,0 in inches
    private int FIELD_PIXEL_SIZE_X;    // field horizontal pixel size
    private int FIELD_PIXEL_SIZE_Y;    // field vertical pixel size
    private int BORDER;                // field inset into panel gui, in pixels
    private int FIELD_PANEL_SIZE;      // size in pixels of panel displaying field
    
    // https://teaching.csse.uwa.edu.au/units/CITS1001/colorinfo.html
    private static final Color lightBlue = new Color(51,153,255);
    private static final Color darkGreen = new Color(0,102,0);
    

    /**
     * Set field panel dimensions based on GameField settings.
     */
    public void setFieldPanelDimensions() {
    
        // Find SCALE that is display-appropriate for this app
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double availableHeight = (double)(screenSize.getHeight()) - 300.0 - 50.0; // 300 pixels for user panel, 50 pixels for OS toolbars
        //SCALE = availableHeight / (gf.FIELD_WIDTH_Y+2.0*BORDER_WIDTH);
        SCALE = (double)((int) (availableHeight / (gf.FIELD_WIDTH_Y+2.0*BORDER_WIDTH)));
        //SCALE = 5.0;
        System.out.println("FIELD_WIDTH_Y="+gf.FIELD_WIDTH_Y);
        System.out.println("SCALE="+SCALE);
    
        // Set field dimensions scaled according to available display size
        FIELD_PIXEL_ORIGIN_X = (int)(gf.FIELD_ORIGIN_X*SCALE);
        FIELD_PIXEL_ORIGIN_Y = (int)(gf.FIELD_ORIGIN_Y*SCALE);
        FIELD_PIXEL_SIZE_X   = (int)(gf.FIELD_WIDTH_X *SCALE);
        FIELD_PIXEL_SIZE_Y   = (int)(gf.FIELD_WIDTH_Y *SCALE);
        BORDER               = (int)(BORDER_WIDTH     *SCALE);
        FIELD_PANEL_SIZE     = Math.max(FIELD_PIXEL_SIZE_X+FIELD_PIXEL_ORIGIN_X, FIELD_PIXEL_SIZE_Y+FIELD_PIXEL_ORIGIN_Y)+BORDER*2;
        System.out.println("FIELD_PANEL_SIZE="+FIELD_PANEL_SIZE);
    }
    
    public int getFieldPanelSize() {
        return FIELD_PANEL_SIZE;
    }
    
    
    /**
     * Convert x-coordinate of path to x-pixel of graphic display.
     */
    public int toGraphX(double x) {
        double xx = x*SCALE;
        return (int)xx + BORDER;
    }
    
    /**
     * Convert y-coordinate of path to y-pixel of graphic display.
     */
    public int toGraphY(double y) {
        double yy = y*SCALE;
        return FIELD_PIXEL_SIZE_Y+BORDER-(int)yy;
    }
        
    /**
     * Convert line segment of path to graphical line.
     */
    public void graphLine(Graphics g, double x1, double y1, double x2, double y2, Color color) {
        int xx1 = toGraphX(x1);
        int yy1 = toGraphY(y1);
        int xx2 = toGraphX(x2);
        int yy2 = toGraphY(y2);
        //        ((x1,y1)->(x2,y2))
        g.setColor(color);
        g.drawLine(xx1, yy1, xx2, yy2);
        //debug:
        //System.out.println("line:"+xx1+" "+yy1+" "+xx2+" "+yy2);
    }

    /** 
     * Convert rectangle vertices to graphical rectangle.
     */
    public void graphRect(Graphics g, double x1, double y1, double x2, double y2, Color color, boolean fill) {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);
        int xx1 = toGraphX(minX);
        int yy1 = toGraphY(maxY);
        int width = (int) ((maxX-minX)*SCALE);
        int height = (int) ((maxY-minY)*SCALE);
        g.setColor(color);
        if (fill) {
            g.fillRect(xx1, yy1, width, height);
        }
        else {
            g.drawRect(xx1, yy1, width, height);
        }
    }

    /** 
     * Convert rectangle vertices to graphical rectangle.
     */
    public void graphPolygon(Graphics g, double[] xpts, double[] ypts, Color color, boolean fill) {
        int[] xPoints = new int[xpts.length];
        int[] yPoints = new int[xpts.length];
        for (int i = 0; i < xpts.length; i++) {
            xPoints[i] = toGraphX(xpts[i]);
            yPoints[i] = toGraphY(ypts[i]);
        }
        g.setColor(color);
        if (fill) {
            g.fillPolygon(xPoints, yPoints, xpts.length);
        }
        else {
            g.drawPolygon(xPoints, yPoints, xpts.length);
        }
    }
    

    /**
     * Convert arc of path to graphical arc.
     */
    public void graphArc(Graphics2D g2, double cx, double cy, double r, double sa, double ea, Color color) {
        // Rectangle is bounded by arc center +/- radius
        double x = (double)toGraphX(cx-r);
        double width  = (2.0*r*SCALE);
        double y = (double)toGraphY(cy+r);
        double height = (2.0*r*SCALE);
        double startAngle = sa;
        double arcAngle   = ea-sa;
        // x y width height startAngle arcAngle
        g2.setColor(color);
        g2.draw(new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
        //debug:
        //System.out.println("cx:"+cx+" cy:"+cy+" r:"+r+" sa:"+sa+" ea:"+ea);
        //System.out.println("arc:"+x+" "+y+" "+width+" "+height+" "+startAngle+" "+arcAngle);
    }

    /**
     * Draw graphical circle from path coordinates.
     */
    public void graphCircle(Graphics g, double x, double y, double size, Color color, boolean fill) {
        int xx = toGraphX(x-size/2.0);
        int yy = toGraphY(y+size/2.0);
        int width = (int) (size*SCALE);
        int height = (int) (size*SCALE);
        g.setColor(color);
        if (fill) {
            g.fillOval(xx, yy, width, height);
        }
        else {
            g.drawOval(xx, yy, width, height);
        }
        //Debug:
        //System.out.println("circle x:"+x+" y:"+y+" size:"+size);
        //System.out.println("circle xx:"+xx+" yy:"+yy+" width:"+width+" height:"+height);
    }
                           
    /**
     * Draw graphical text from path coordinates.
     */                           
    public void graphText(Graphics2D g2, double x, double y, String msg, Color color) {
        int xx = toGraphX(x);
        int yy = toGraphY(y);
        g2.setColor(color);
        g2.drawString(msg, xx, yy);
    }

    /**
     * Position text to be slightly displaced away from center of graphical field.
     */
    public void graphAntiCenteredText(Graphics2D g2, double x, double y, String msg, Color color) {
        double xoffset = 0.0;
        double yoffset = 0.0;
        if      (x < 5.0)             { xoffset =  2.0; }
        else if (x > FIELD_WIDTH-5.0) { xoffset = -3.0; }
        else if (x < FIELD_WIDTH/2.0) { xoffset = -3.0; }
        else                          { xoffset =  2.0; }
        if      (y < 5.0)             { yoffset =  2.0; }
        else if (y > FIELD_WIDTH-5.0) { yoffset = -2.0; }
        else if (y < FIELD_WIDTH/2.0) { yoffset = -2.0; }
        else                          { yoffset =  2.0; }
        graphText(g2, x+xoffset, y+yoffset, msg, color);
        //debug:
        //System.out.println("text x:"+x+" xoffset:"+xoffset+" y:"+y+" yoffset:"+yoffset);
    }


    /**
     * Draw field elements onto graphical field.
     */
    public void drawFieldGraphics(Graphics g, Graphics2D g2) {
        
        // If there's nothing to draw then simply return
        if (gf.fieldGraphics == null) { return; }
        Color color;
        boolean noWidthInt;
        boolean noWidthDouble;
        int intValue = 0;
        double doubleValue = 0.0;
        int stroke;
        boolean fill;
        
        // FieldGraphics in String format are as follows:
        // [Type] [Color-R] [Color-G] [Color-B] [width] <args...>        
        
        for (String f : gf.fieldGraphics) {
            String[] chunks = f.split("\\s+");
            color = new Color(Integer.valueOf(chunks[1]), Integer.valueOf(chunks[2]), Integer.valueOf(chunks[3]));
            
            // For width:
            //  (double) indicates an expression in inches that is scaled to pixels
            //  (int) indicates pixels
            noWidthInt = false;
            noWidthDouble = false;
            try {
                intValue = Integer.parseInt(chunks[4]);
            } catch (NumberFormatException e) {
                noWidthInt = true;
            }
            try {
                doubleValue = Double.parseDouble(chunks[4]);
            } catch (NumberFormatException e) {
                noWidthDouble = true;
            }
            // Set stroke to 0 if [width] isn't parsable else to its parsable integer value else its parsable scaled double value
            stroke = noWidthInt ? (noWidthDouble ? 0 : (int) (doubleValue*SCALE) ) : intValue;
            g2.setStroke(new BasicStroke(stroke));
            
            fill = false;
            
            switch (chunks[0]) {
                case "FILLCIRCLE":
                case "FILLPOLYGON":
                case "FILLRECT":
                    fill = true;
                    break;
            }
            
            switch (chunks[0]) {
                case "LINE":
                    graphLine(g, Double.valueOf(chunks[5]), Double.valueOf(chunks[6]), Double.valueOf(chunks[7]), Double.valueOf(chunks[8]), color);
                    break;
                case "RECT":
                case "FILLRECT":
                    graphRect(g, Double.valueOf(chunks[5]), Double.valueOf(chunks[6]), Double.valueOf(chunks[7]), Double.valueOf(chunks[8]), color, fill);
                    break;
                case "CIRCLE":
                case "FILLCIRCLE":
                    graphCircle(g, Double.valueOf(chunks[5]), Double.valueOf(chunks[6]), Double.valueOf(chunks[7]), color, fill);
                    break;
                case "POLYGON":
                case "FILLPOLYGON":
                    int vertices = (chunks.length-5)/2;
                    double[] xpts = new double[vertices];
                    double[] ypts = new double[vertices];
                    for (int i = 0; i < vertices; i++) {
                        xpts[i] = Double.valueOf(chunks[(i*2)+5]);
                        ypts[i] = Double.valueOf(chunks[(i*2)+6]);
                    }
                    graphPolygon(g, xpts, ypts, color, fill);
                    break;
             }                
        }        
    }


    /**
     * Draw navigation path elements onto graphical field.
     */
    public void drawNavPath(Graphics g, Graphics2D g2) {
    
        // get the translation of the midpoint of the robot relative to Path coordinate system.
        double robotOffsetx = Double.parseDouble(gf.myRobot.get("ORIGIN_X_OFFSET"));
        // get dimensions of robot
        double robotx = Double.parseDouble(gf.myRobot.get("SIDE_TO_SIDE"));
        
        double rightSideX = robotx/2.0 - robotOffsetx;
        double leftSideX  = robotx/2.0 + robotOffsetx;
    
        // If there's nothing to draw then simply return
        if (gf.robotNavPaths == null) { return; }
    
        for (NavPath p : gf.robotNavPaths) {
            if (p instanceof Vector) {
                Vector v = (Vector) p;
                // robot path
                graphLine(g, v.i.pt.x, v.i.pt.y, v.o.pt.x, v.o.pt.y, Color.blue);
                if (gf.showLength) {
                    double angle = v.heading - Math.PI/2.0;
                    graphLine(g, v.i.pt.x+rightSideX*Math.cos(angle), v.i.pt.y+rightSideX*Math.sin(angle), 
                                 v.o.pt.x+rightSideX*Math.cos(angle), v.o.pt.y+rightSideX*Math.sin(angle), Color.red);
                }
                
            }
            else if (p instanceof Arc) {
                Arc a = (Arc) p;
                Color sideColor = null;
                // path of center of robot
                graphArc(g2, a.center.x, a.center.y, a.radius, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, Color.blue);
                if (gf.showLength) {
                    // path of right side of robot                    
                    if ((a.endAngle>a.startAngle) && (Math.abs(a.orientation) < 0.000001)) {
                        graphArc(g2, a.center.x, a.center.y, a.radius+rightSideX, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, Color.red);                        
                    }
                    // path of left side of robot
                    else {
                        graphArc(g2, a.center.x, a.center.y, a.radius+leftSideX, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, Color.red);
                    }
                }
                
            }
            else if (p instanceof Gap) {
                graphLine(g, p.i.pt.x, p.i.pt.y, p.o.pt.x, p.o.pt.y, Color.yellow);
            }
        }
    }
    
    /**
     * Draw robot stop outlines onto graphical field.
     */
    public void drawRobotStops(Graphics g, Graphics2D g2) {
    
        // If there's nothing to draw then simply return
        if (gf.robotNavPaths == null) { return; }
        
        // get the translation of the midpoint of the robot relative to Path coordinate system.
        double robotOffsetx = 0.0 - Double.parseDouble(gf.myRobot.get("ORIGIN_X_OFFSET"));
        double robotOffsety = 0.0 - Double.parseDouble(gf.myRobot.get("ORIGIN_Y_OFFSET"));
        System.out.println("rOx="+robotOffsetx);
        
        // get dimensions of robot
        double robotx = Double.parseDouble(gf.myRobot.get("SIDE_TO_SIDE"));
        double roboty = Double.parseDouble(gf.myRobot.get("FRONT_TO_BACK"));
        
        NavPoint br = new NavPoint(robotOffsetx + robotx/2.0, robotOffsety - roboty/2.0);
        NavPoint fr = new NavPoint(robotOffsetx + robotx/2.0, robotOffsety + roboty/2.0);
        NavPoint bl = new NavPoint(robotOffsetx - robotx/2.0, robotOffsety - roboty/2.0);
        NavPoint fl = new NavPoint(robotOffsetx - robotx/2.0, robotOffsety + roboty/2.0);
        NavPoint cc = new NavPoint(robotOffsetx, robotOffsety);
        NavPoint co = new NavPoint(robotOffsetx, robotOffsety+2.0);
        
        System.out.println("br="+br.toString());
        
        NavPoint brN;
        NavPoint frN;
        NavPoint blN;
        NavPoint flN;
        NavPoint ccN;
        NavPoint coN;
        
        Color color = Color.magenta;
    
        for (int i = 0; i < gf.waypoints.size(); i++) {
            // we need the waypoint as a reference
            // we need to set the heading of this reference as to the robot's orientation
            NavPoint npt = new NavPoint(gf.waypoints.get(i));
            npt.heading += gf.waypoints.get(i).orientation;
            
            System.out.println(npt.toString());
            if (i==0 || npt.stop) {
                // Put the corners of the robot into Path coordinate system, referenced by the
                // NavPoint we created from the waypoint.
                brN = br.displacedBy(npt);
                frN = fr.displacedBy(npt);
                blN = bl.displacedBy(npt);
                flN = fl.displacedBy(npt);
                ccN = cc.displacedBy(npt);
                coN = co.displacedBy(npt);
                System.out.println("npt="+npt.toString());
                System.out.println("brN="+brN.toString());
                
                // draw four sides of the robot
                graphLine(g, brN.pt.x, brN.pt.y, frN.pt.x, frN.pt.y, color);
                graphLine(g, frN.pt.x, frN.pt.y, flN.pt.x, flN.pt.y, color);
                graphLine(g, flN.pt.x, flN.pt.y, blN.pt.x, blN.pt.y, color);
                graphLine(g, blN.pt.x, blN.pt.y, brN.pt.x, brN.pt.y, color);
                
                // draw circle at center of robot and tick indicator for direction
                graphCircle(g, ccN.pt.x, ccN.pt.y, 3.0, color, true);
                graphLine(g, ccN.pt.x, ccN.pt.y, coN.pt.x, coN.pt.y, color);
            }
        }                
    }
    
    /**
     * Draw robot tracks onto graphical field.
     */
    public void drawRobotTracks(Graphics g, Graphics2D g2) {
    
        // If there's nothing to draw then simply return
        if (gf.robotNavPaths == null) { return; }
    
        for (NavPath p : gf.robotNavPaths) {
            if (p instanceof Vector) {
                Vector v = (Vector) p;
                // robot path
                graphLine(g, v.i.pt.x, v.i.pt.y, v.o.pt.x, v.o.pt.y, Color.blue);
                
                if (gf.showRobotTracks) {
                    double angle = v.heading - Math.PI/2.0;
                    // path of right side of robot
                    Color color = Color.green;
                    graphLine(g, v.i.pt.x+8.0*Math.cos(angle), v.i.pt.y+8.0*Math.sin(angle), v.o.pt.x+8.0*Math.cos(angle), v.o.pt.y+8.0*Math.sin(angle), color);
                    // path of left side of robot
                    graphLine(g, v.i.pt.x-8.0*Math.cos(angle), v.i.pt.y-8.0*Math.sin(angle), v.o.pt.x-8.0*Math.cos(angle), v.o.pt.y-8.0*Math.sin(angle), darkGreen);                    
                }
            }
            else if (p instanceof Arc) {
                Arc a = (Arc) p;
                Color sideColor = null;
                // path of center of robot
                graphArc(g2, a.center.x, a.center.y, a.radius, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, Color.blue);
                if (gf.showRobotTracks) {
                    // path of right side of robot                    
                    double rightSide = (a.endAngle<a.startAngle) ? -1.0 : 1.0;
                    sideColor = Color.green;
                    if (sideColor != null) {
                        graphArc(g2, a.center.x, a.center.y, a.radius+8.0*rightSide, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, sideColor);
                    }
                    // path of left side of robot
                    rightSide = (a.endAngle>a.startAngle) ? -1.0 : 1.0;
                    sideColor = darkGreen;
                    if (sideColor != null) {
                        graphArc(g2, a.center.x, a.center.y, a.radius+8.0*rightSide, a.startAngle*180/Math.PI, a.endAngle*180/Math.PI, sideColor);
                    }
                }
            }
            else if (p instanceof Gap) {
                graphLine(g, p.i.pt.x, p.i.pt.y, p.o.pt.x, p.o.pt.y, Color.yellow);
            }
        }
        
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
            
        setBackground(Color.DARK_GRAY);            
        Graphics2D g2 = (Graphics2D) g;

        // draw floor of field
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(BORDER+FIELD_PIXEL_ORIGIN_X,BORDER+FIELD_PIXEL_ORIGIN_Y,FIELD_PIXEL_SIZE_X,FIELD_PIXEL_SIZE_Y);
            
        // draw field markings
        drawFieldGraphics(g, g2);

        g2.setStroke(new BasicStroke(3));
        // draw Nav Path and Length
        drawNavPath(g, g2);
            
        // draw robot-stop overlays
        g2.setStroke(new BasicStroke(5));
        if (gf.showRobotStops) {
            drawRobotStops(g, g2);
        }
        
        // draw robot-track overlays
        g2.setStroke(new BasicStroke(3));        
        if (gf.showRobotTracks) {
            drawRobotTracks(g, g2);
        }
            
        Color color;
        // draw Navpoint overlays, if necessary
        if (gf.showNavPoints && (gf.sourceNavPoints != null)) {
            int i = 0;
            for (NavPoint npt : gf.sourceNavPoints) {
                color = npt.stop ? Color.red : Color.black;
                graphCircle(g, npt.pt.x, npt.pt.y, 2.0, color, true);     
                graphAntiCenteredText(g2, npt.pt.x, npt.pt.y, Integer.toString(i), color);
                i = i+1;
            }
        }
        // draw waypoint overlays, if necessary
        if (gf.showWaypoints) {
            int i = 0;
            for (NavPoint npt : gf.waypoints) {
                color = npt.stop ? Color.red : Color.black;
                graphCircle(g, npt.pt.x, npt.pt.y, 2.0, color, true);
                graphAntiCenteredText(g2, npt.pt.x, npt.pt.y, Integer.toString(i), color);
                i = i+1;
            }
        }
                    
        // draw grid overlay, if necessary
        double ii;
        double x;
        double y;
        double ymin = Math.max(gf.FIELD_ORIGIN_Y, 0.0);
        double ymax = Math.min(FIELD_WIDTH, gf.FIELD_ORIGIN_Y+gf.FIELD_WIDTH_Y);
        double xmin = Math.max(gf.FIELD_ORIGIN_X, 0.0);
        double xmax = Math.min(FIELD_WIDTH, gf.FIELD_ORIGIN_X+gf.FIELD_WIDTH_X);
        if (gf.showGrid) {
            g2.setStroke(new BasicStroke(1));
            for (int i = 1; i < 6; i++) {
                ii = (double) i;
                x = ii*INNER_TILE_WIDTH+(ii-1)*MESH_TILE_WIDTH;
                if ((x >= gf.FIELD_ORIGIN_X) && (x <= (gf.FIELD_ORIGIN_X + gf.FIELD_WIDTH_X))) {
                    graphLine(g, x, ymin, x, ymax, darkGreen);
                }
                x = ii*(INNER_TILE_WIDTH+MESH_TILE_WIDTH);
                if ((x >= gf.FIELD_ORIGIN_X) && (x <= (gf.FIELD_ORIGIN_X + gf.FIELD_WIDTH_X))) {
                    graphLine(g, x, ymin, x, ymax, darkGreen);
                }
                    
                y = ii*INNER_TILE_WIDTH+(ii-1)*MESH_TILE_WIDTH; 
                if ((y >= gf.FIELD_ORIGIN_Y) && (y <= (gf.FIELD_ORIGIN_Y + gf.FIELD_WIDTH_Y))) {
                    graphLine(g, xmin, y, xmax, y, darkGreen);
                }
                y = ii*(INNER_TILE_WIDTH+MESH_TILE_WIDTH);
                if ((y >= gf.FIELD_ORIGIN_Y) && (y <= (gf.FIELD_ORIGIN_Y + gf.FIELD_WIDTH_Y))) {
                    graphLine(g, xmin, y, xmax, y, darkGreen);
                }
            }
            
            g2.setColor(darkGreen);
                
            // Getting vertical text is tricky. The component coordinate system need to be rotated
            // and the x,y to place the text is also rotated! So beware when editing this!!
            // X-axis text
            AffineTransform at = g2.getTransform();
            AffineTransform rt = new AffineTransform();
            rt.rotate(-Math.PI/2.0);
            g2.setTransform(rt);
            double xx;
            for (int i = 0; i < 7; i++) {
                if (i > 0) {
                    xx = i*INNER_TILE_WIDTH+(i-1)*MESH_TILE_WIDTH;
                    if ((xx >= gf.FIELD_ORIGIN_X) && (xx <= gf.FIELD_ORIGIN_X + gf.FIELD_WIDTH_X)) {
                        g2.drawString(String.format("%.2f", xx), 0-FIELD_PIXEL_SIZE_Y-BORDER+2, toGraphX(xx-1.0));
                    }
                }
                if (i < 7) {
                    xx = i*(INNER_TILE_WIDTH+MESH_TILE_WIDTH);
                    if ((xx >= gf.FIELD_ORIGIN_X) && (xx <= gf.FIELD_ORIGIN_X + gf.FIELD_WIDTH_X)) {
                        g2.drawString(String.format("%.2f", xx), 0-FIELD_PIXEL_SIZE_Y-BORDER+2, toGraphX(xx+3.0));
                    }
                }
            }                
            g2.setTransform(at);

            // Y-axis text                
            double yy;
            for (int i = 0; i < 7; i++) {
                if (i > 0) {
                    yy = i*INNER_TILE_WIDTH+(i-1)*MESH_TILE_WIDTH;
                    if ((yy >= gf.FIELD_ORIGIN_Y) && (yy <= gf.FIELD_ORIGIN_Y + gf.FIELD_WIDTH_Y)) {
                        g2.drawString(String.format("%.2f", yy), BORDER+2, toGraphY(yy-3.0));
                    }
                }
                if (i < 7) {
                    yy = i*(INNER_TILE_WIDTH+MESH_TILE_WIDTH);
                    if ((yy >= gf.FIELD_ORIGIN_Y) && (yy <= gf.FIELD_ORIGIN_Y + gf.FIELD_WIDTH_Y)) {
                        g2.drawString(String.format("%.2f", yy), BORDER+2, toGraphY(yy+1.0));
                    }
                }
            }
        }
            
        // draw simulation result overlay, if necessary
        if (gf.showSim && (gf.simNavPoints != null)) {
            for (NavPoint npt : gf.simNavPoints) {
                graphCircle(g, npt.pt.x, npt.pt.y, 1.0, Color.black, true);                    
            }
        }
            
        // draw field perimeter
        g.setColor(Color.black);
        g2.setStroke(new BasicStroke(3));
        g.drawRect(BORDER-2,BORDER-2,FIELD_PIXEL_SIZE_X+2,FIELD_PIXEL_SIZE_Y+2);
    }
    
    public DrawField(GameField me) {
        this.gf = me;
        //this.SCALE = Double.parseDouble(gf.mySettings.parameters.get("SCALE"));
    }
}
