package waypoint;  

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;


/**
 * Class to display a gui for a robot path on a game field.
 */
public class GameField extends JFrame {

    // How GameField works:
    //


    // These are standard FIRST Tech Challenge dimensions:
    // See AndyMark.com for field dimensions.
    // These values are used by GameFieldFileHandler:
    protected static final double INNER_TILE_WIDTH = 22.85;            // (inches) dimension of non-meshing tile width
    protected static final double MESH_TILE_WIDTH  =  0.80;            // (inches) dimension of adjact tile mesh width
    // The interior dimension of a standard field is 6 tiles + the 5 mesh overlaps between tiles.
    // For a standard (approximate) 12'x12' field this comes to 141.10 inches.    
    protected static final double FIELD_WIDTH      =  6.0*INNER_TILE_WIDTH + 5.0*MESH_TILE_WIDTH; // (inches) dimension of standard full field width
    protected static final double TAPE_WIDTH       =  2.0;             // (inches) width of Gaffer Tape used to mark field
    
    // These field values are used by SimPath:
    protected double FIELD_ORIGIN_X =   0.0; // field x-coordinate origin, in inches, typically = 0.0
    protected double FIELD_ORIGIN_Y =   0.0; // field y-coordinate origin, in inches, typically = 0.0
    protected double FIELD_WIDTH_X  = 141.1; // field x-coordinate corner opposite origin, in inches, typically = 141.1
    protected double FIELD_WIDTH_Y  = 141.1; // field y-coordinate corner opposite origin, in inches, typically = 141.1
    
    private int FIELD_PANEL_SIZE = 0; // local copy from DrawField of its panel size.
                
    private JFrame frame;
    private DefaultTableModel navpointTM;
    private JTable nptTable;
    private JLabel lengthLabel;
    private JLabel timeLabel;
    private JButton navpointOverlayB;
    private JButton waypointOverlayB;
    private JButton showSimB;
    private JTextField loadNPFileTF;
    private JTextField saveNPFileTF;
    private JButton updateB;
    protected JButton settingsB; // accessed by (SettingsFrame) mySettings
    
    protected SettingsFrame mySettings;
    private CalcPath  calcPath;    
    private DrawField fieldPanel;
    private String[] nullArgs;
    private double pathLength = 0.0;
    
    // accessed by DrawField
    protected List<NavPoint> sourceNavPoints;
    protected List<NavPoint> waypoints;
    protected List<NavPath>  robotNavPaths;
    protected List<NavPoint> simNavPoints;
    protected List<String>   fieldGraphics;
    protected Map<String, String> myRobot;

    // accessed by DrawField
    protected boolean showRobotStops = false;
    protected boolean showRobotTracks = false;
    protected boolean showNavPoints = false;
    protected boolean showWaypoints = false;
    protected boolean showGrid = false;
    protected boolean showLength = false;
    protected boolean showSim = false;
    protected boolean showSettingsFrame = false; // accessed by (SettingsFrame) mySettings
    
    /**
     * Class constructor, creates gui of game field and controls to generate and simulate a robot path.
     */
    public GameField(String[] args){
    
        
        // This checks the command line arguments and loads
        // parameters into the SettingsFrame mySettings.
        parseArgs(args);
        
        //--------------------------
        // JPanel for Game Field
        //      Game Field panel
        //
        // this must be declared before getFieldDimensions() is called
        fieldPanel = new DrawField(this);
        
        // get fieldGraphics
        fieldGraphics = GameFieldFileHandler.getFieldGraphicsFromFile(mySettings.parameters.get("FIELD_FILE"));
        if (fieldGraphics == null) { 
            System.out.println("Failed to get field graphics from FIELD_FILE: "+mySettings.parameters.get("FIELD_FILE"));
            System.exit(0); 
        }
        getFieldDimensions();
        fieldPanel.setPreferredSize(new Dimension(FIELD_PANEL_SIZE, FIELD_PANEL_SIZE));

        // get robot
        myRobot = GameFieldFileHandler.getRobotFromFile(mySettings.parameters.get("ROBOT_FILE"));
        
        
        //debug        
        //System.out.println("-->"+fieldGraphics);
        if (fieldGraphics == null) { return; }
        calcPath = new CalcPath(mySettings);    
        SimPath simPath = new SimPath(this);
        
                
    
        frame = new JFrame();
        lengthLabel = new JLabel("Length: ");
        timeLabel   = new JLabel("Time: ");
                
    
        //--------------------------
        // JPanel for Controls
        //      NavPoint File JPanel
        //      Navpoint ScrollPane
        //      Navpoint Edit JPanel
        //      Simulation JPanel
        //
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));
        
        
        //--------------------------
        // JPanel for NavPoints Files (subpanel to Controls JPanel)
        //      Load File button
        //      Load File TextField
        //      Save File button
        //      Save File TextField
        //
        JPanel npFilePanel = new JPanel();
        npFilePanel.setLayout(new FlowLayout());
        //        
        JButton loadNPB = new JButton("Load NavPoints >>");
        loadNPB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sourceNavPoints = GameFieldFileHandler.getNavPointsFromFile(loadNPFileTF.getText());
                if (sourceNavPoints == null) {
                    System.out.println("Number of Navpoints: null");
                    loadNPB.setBackground(Color.red);
                }
                else {
                    System.out.println("Number of Navpoints: "+sourceNavPoints.size());
                    loadNPB.setBackground(null);
                    updateNavPointScrollPane();
                }
            }
        });
        loadNPFileTF = new JTextField();        
        //
        // JButton to save NavPoints to file
        //
        JButton saveNPB = new JButton("Save NavPoints >>");
        saveNPB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameFieldFileHandler.toNavPointsFile(sourceNavPoints, saveNPFileTF.getText());
            }
        });
        saveNPFileTF = new JTextField();        
        //
        Dimension nbsize = new Dimension(180, 25);
        Dimension ntsize = new Dimension(160, 25);
        loadNPB.setPreferredSize(nbsize);
        loadNPFileTF.setPreferredSize(ntsize);
        saveNPB.setPreferredSize(nbsize);
        saveNPFileTF.setPreferredSize(ntsize);
        npFilePanel.add(loadNPB);
        npFilePanel.add(loadNPFileTF);
        npFilePanel.add(saveNPB);
        npFilePanel.add(saveNPFileTF);
        //
        controlsPanel.add(npFilePanel);
        
        
        //--------------------------
        // JScrollpane constructed with JTable (subpanel to Controls Panel)
        //
        // Create TableModel with editable cells
        navpointTM = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        navpointTM.addColumn("NavPoint");
        navpointTM.addColumn("X-coordinate");
        navpointTM.addColumn("Y-coordinate");
        navpointTM.addColumn("Heading");
        navpointTM.addColumn("Stop");
        // Create JTable 
        nptTable = new JTable(navpointTM);
        nptTable.getColumnModel().getColumn(0).setPreferredWidth(2*FIELD_PANEL_SIZE/10);
        nptTable.getColumnModel().getColumn(1).setPreferredWidth(2*FIELD_PANEL_SIZE/10);
        nptTable.getColumnModel().getColumn(2).setPreferredWidth(2*FIELD_PANEL_SIZE/10);
        nptTable.getColumnModel().getColumn(3).setPreferredWidth(2*FIELD_PANEL_SIZE/10);
        nptTable.getColumnModel().getColumn(4).setPreferredWidth(2*FIELD_PANEL_SIZE/10);
        nptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nptTable.getModel().addTableModelListener( new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                setUpdateBColor();
            }});
        // Create ScrollPane
        JScrollPane navpointSP = new JScrollPane(nptTable);
        navpointSP.setPreferredSize(new Dimension(FIELD_PANEL_SIZE,100));
        //
        controlsPanel.add(navpointSP);
        
        //--------------------------
        // JPanel for NavPoint Edits (subpanel to Controls JPanel)
        //      Add Row button
        //      Delete Row button
        //      Update Path button
        //      Length Label
        //      Advanced Settings button
        //
        JPanel npEditPanel = new JPanel();
        npEditPanel.setLayout(new FlowLayout());
        //        
        // JButton to add NavPoint
        //
        JButton addNPB = new JButton("Add NavPoint");
        addNPB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] idxArray = nptTable.getSelectedRows();
                if (idxArray.length > 0) {
                    navpointTM.insertRow(idxArray[0], new Object[5]);
                }
                else {
                    navpointTM.addRow(new Object[5]);
                }
                reorderNavPointTable();
                setUpdateBColor(); 
            }
        });        
        //        
        // JButton to delete NavPoint(s)
        //
        JButton deleteNPB = new JButton("Delete NavPoint");
        deleteNPB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] idxArray = nptTable.getSelectedRows();
                if (idxArray.length > 0) {
                    for (int idx = idxArray.length - 1; idx >= 0; idx--) {            
                        navpointTM.removeRow(idxArray[idx]);
                    }
                }
                reorderNavPointTable();
                setUpdateBColor();
            }
        });
        //
        // JButton to update path display
        //
        updateB = new JButton("Update Path");
        updateB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sourceNavPoints = pullNavPointTable(true);
                updatePath();      // run pathfinding algorithm
                if (sourceNavPoints != null) {
                    updateB.setBackground(null);
                }
                frame.repaint();
            }
        });
        //
        // JButton to show advanced settings
        //
        settingsB = new JButton("Settings Frame");
        settingsB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettingsFrame = !showSettingsFrame;
                mySettings.showSettingsFrame(showSettingsFrame);
                settingsB.setBackground(showSettingsFrame ? Color.green : null);
                frame.repaint();                
            }
        });
        //
        npEditPanel.add(addNPB);
        npEditPanel.add(deleteNPB);
        npEditPanel.add(updateB);
        npEditPanel.add(lengthLabel);
        npEditPanel.add(settingsB);
        //
        controlsPanel.add(npEditPanel);
       
        
        //--------------------------
        // JPanels for Overlays (subpanels to Controls JPanel)
        //      Label
        //      Grid Overlay button
        //      NavPoint Overlay button
        //      Waypoint Overlay button
        //
        //      Label
        //      Length Overlay button
        //      Robot Overlay button
        //      Robot Overlay button
        //
        JPanel overlayPanel1 = new JPanel();
        overlayPanel1.setLayout(new FlowLayout());
        JPanel overlayPanel2 = new JPanel();
        overlayPanel2.setLayout(new FlowLayout());
        
        JLabel overlay1Label = new JLabel(" Overlays:");
        JLabel overlay2Label = new JLabel(" Overlays:");
        //
        // JButton to toggle Grid overlay
        //
        JButton gridOverlayB = new JButton("Grid");
        gridOverlayB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGrid = !showGrid;
                gridOverlayB.setBackground(showGrid ? Color.green : null);
                frame.repaint();
            }
        });
        //
        // JButton to toggle NavPoint overlay
        //
        navpointOverlayB = new JButton("NavPoints");
        navpointOverlayB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showWaypoints = false;
                showNavPoints = !showNavPoints;
                navpointOverlayB.setBackground(showNavPoints ? Color.green : null);
                waypointOverlayB.setBackground(showWaypoints ? Color.green : null);
                frame.repaint();
            }
        });
        //
        // JButton to toggle Waypoint overlay
        //
        waypointOverlayB = new JButton("Waypoints");
        waypointOverlayB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNavPoints = false;
                showWaypoints = !showWaypoints;
                navpointOverlayB.setBackground(showNavPoints ? Color.green : null);
                waypointOverlayB.setBackground(showWaypoints ? Color.green : null);
                frame.repaint();
            }
        });
        //
        // JButton to toggle Length overlay
        //
        JButton lengthB = new JButton("Length");
        lengthB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLength = !showLength;
                lengthB.setBackground(showLength ? Color.green : null);
                frame.repaint();
            }
        });
        //
        // JButton to toggle robot stops overlay
        //
        JButton robotStopsOverlayB = new JButton("Robot Stops");
        robotStopsOverlayB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRobotStops = !showRobotStops;
                robotStopsOverlayB.setBackground(showRobotStops ? Color.green : null);
                frame.repaint();
            }
        });
        //
        // JButton to toggle robot track overlay
        //
        JButton robotTracksOverlayB = new JButton("Robot Tracks");
        robotTracksOverlayB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRobotTracks = !showRobotTracks;
                robotTracksOverlayB.setBackground(showRobotTracks ? Color.green : null);
                frame.repaint();
            }
        });
        //
        Dimension osize = new Dimension(130, 25);
        overlay1Label.setPreferredSize(new Dimension(85, 25));
        gridOverlayB.setPreferredSize(osize);
        navpointOverlayB.setPreferredSize(osize);
        waypointOverlayB.setPreferredSize(osize);
        overlay2Label.setPreferredSize(new Dimension(85, 25));
        lengthB.setPreferredSize(osize);
        robotStopsOverlayB.setPreferredSize(osize);
        robotTracksOverlayB.setPreferredSize(osize);
        overlayPanel1.add(overlay1Label);
        overlayPanel1.add(gridOverlayB);
        overlayPanel1.add(navpointOverlayB);
        overlayPanel1.add(waypointOverlayB);
        overlayPanel2.add(overlay2Label);
        overlayPanel2.add(lengthB);
        overlayPanel2.add(robotStopsOverlayB);
        overlayPanel2.add(robotTracksOverlayB);
        //
        controlsPanel.add(overlayPanel1);
        controlsPanel.add(overlayPanel2);
        

        //--------------------------
        // JPanel for Simulation (subpanel to Controls JPanel)
        //      Run Simulation button
        //      Time Label button
        //      Simulation Overlay button
        //
        JPanel simPanel = new JPanel();
        simPanel.setLayout(new FlowLayout());
        
        //
        // JButton to run simulation
        //
        JButton simB = new JButton("Run Simulation");
        simB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {   
                simPath.updateSimPath(robotNavPaths, 50.0);
                simNavPoints = simPath.doSimPath();
                GameFieldFileHandler.exportSimPath(simNavPoints);
                showSim = true;
                showSimB.setBackground(showSim ? Color.green : null);
                timeLabel.setText(String.format("Time :%.2f", simNavPoints.size()*50.0/1000.0));
                frame.repaint();
            }
        });
        //
        // JButton to show simulation points
        //
        showSimB = new JButton("Show Simulation");
        showSimB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSim = !showSim;
                showSimB.setBackground(showSim ? Color.green : null);
                frame.repaint();
            }
        });
        //
        Dimension sbsize = new Dimension(180, 25);
        Dimension slsize = new Dimension(160, 25);
        lengthLabel.setPreferredSize(slsize);
        simB.setPreferredSize(sbsize);
        timeLabel.setPreferredSize(slsize);
        showSimB.setPreferredSize(sbsize);
        //
        simPanel.add(simB);
        simPanel.add(timeLabel);
        simPanel.add(showSimB);
        //
        controlsPanel.add(simPanel);        
        
        
        //
        // JButton to export code  -- FIX!!! UNFINISHED!!!
        //
        JButton codeB = new JButton("Export Code");
        codeB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameFieldFileHandler.exportCode(robotNavPaths);
            }
        });
        
        updateGUIfromSettings();
        sourceNavPoints = GameFieldFileHandler.getNavPointsFromFile(loadNPFileTF.getText());
        updateNavPointScrollPane();
        updateB.setBackground(null); // updating the ScrollPane triggers the coloring of the Update Path button to yellow
        updatePath();                // run pathfinding algorithm
        
        //--------------------------
        // JFrame consists of 
        //      Game Field JPanel
        //      Controls JPanel
        frame.add(fieldPanel, BorderLayout.PAGE_START);
        frame.add(controlsPanel, BorderLayout.PAGE_END);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);             
        frame.pack();
        frame.setTitle(" Game Field ");
        frame.setVisible(true);
        
        System.out.println("FRAME HEIGHT="+frame.getHeight());
    }
    
    /**
     * Parse the commane line arugments
     * @param args - Array of command line arguments
     */
    public void parseArgs(String[] args) {
        
        String cmdLineSettingFile = "settingsDefault.txt";
        
        // If '--help' is any of the arguments then 
        // show proper command line usage and exit
        for (String arg : args) {
            //
            // Always good to have an option to show what the valid command arguments are.
            //
            if (arg.equals("--help") || arg.equals("-help")) {
                System.out.println("Usage:");
                System.out.println("GameField [<SettingsFileName>] | [[-]-help] |");
                System.exit(0);
            }
            
            // Settings file name can only be first argument.
            // All commands have a '-' prefix.
            if ((args.length > 0) && (args[0].charAt(0) != '-')) {
                cmdLineSettingFile = args[0];
            }
        }
        mySettings = new SettingsFrame(this, cmdLineSettingFile);
        //debug
        //System.out.println(mySettings.parameters);
        // An empty parameter Map in SettingsFrame is ok,
        // but a null parameter Map means an error happened.
        if (mySettings.parameters == null) {
            System.exit(0);
        }
    }
   
    /**
     * Utility method to update GUI buttons and textAreas with the current
     * parameter settings.
     */
    public void updateGUIfromSettings() {
        if (mySettings.parameters.containsKey("NAVPOINT_INFILE")) {
            loadNPFileTF.setText(mySettings.parameters.get("NAVPOINT_INFILE"));
        }
        if (mySettings.parameters.containsKey("NAVPOINT_OUTFILE")) {
            saveNPFileTF.setText(mySettings.parameters.get("NAVPOINT_OUTFILE"));
        }
    }

    /**
     * Utility method to update the color of the "Update Path" button.
     */
    public void setUpdateBColor() {
        List<NavPoint> checkTable = pullNavPointTable(false);
        Color updateColor = (checkTable == null) ? Color.red : Color.yellow;
        updateB.setBackground(updateColor);
    }
    
    
    /**
     * Calculate length of path.
     */
    public void updateLength(List<NavPath> navPaths) {
        double length = 0.0;

        // get the translation of the midpoint of the robot relative to Path coordinate system.
        double robotOffsetx = Double.parseDouble(myRobot.get("ORIGIN_X_OFFSET"));
        // get dimensions of robot
        double robotx = Double.parseDouble(myRobot.get("SIDE_TO_SIDE"));
        
        double rightSideX = robotx/2.0 - robotOffsetx;
        double leftSideX  = robotx/2.0 + robotOffsetx;
        
        
        if (navPaths != null) {
            for (NavPath p : navPaths) {
                if (p instanceof Vector) {
                    Vector v = (Vector) p;
                    length += v.magnitude;
                }
                else if (p instanceof Arc) {
                    Arc a = (Arc) p;
                    if ((a.endAngle>a.startAngle) && (Math.abs(a.orientation) < 0.000001)) {
                        length += (a.radius+rightSideX)*(Math.abs(a.endAngle - a.startAngle));
                    } 
                    else {
                        length += (a.radius+leftSideX)*(Math.abs(a.endAngle - a.startAngle));
                    }
                }
            }
        }    
        lengthLabel.setText(String.format("Length : %.1f", length));
    }
    
    /**
     * Run the pathfinding algorithm.
     */
    public void updatePath() {
        // run the pathfinding algorithm in calcPath,
        // save the results to file,
        // generate waypoints (all the points between path elements)
        // calculate length of the generated path
        robotNavPaths = calcPath.genPath(sourceNavPoints);
        GameFieldFileHandler.toNavPathFile(robotNavPaths, mySettings.parameters.get("PATH_OUTFILE"));
        waypoints = genWaypoints(robotNavPaths);
        updateLength(robotNavPaths);            
    }
    
    /**
     * Generate List of waypoints, that are NavPoints, from robot navigation path.
     */
    public List<NavPoint> genWaypoints(List<NavPath> navPaths) {
        List<NavPoint> waypoints = new ArrayList<>();
        NavPoint npt;
        if (navPaths != null) {
            // get the initial NavPoint from the first NavPath
            if (navPaths.size() > 0) {
                npt = navPaths.get(0).i;
                npt.stop = navPaths.get(0).stop;
                waypoints.add(npt);
            }
            // get the terminal NavPoints from all the NavPath elements
            for (int i = 0; i < navPaths.size(); i++) {                
                npt = navPaths.get(i).o;
                npt.stop = navPaths.get(i).stop;
                npt.orientation = navPaths.get(i).orientation;
                System.out.println("add_to_waypoint("+(i+1)+"):"+npt.toString());
                waypoints.add(npt);
            }
        }    
        return waypoints;
    }
    
    /**
     * Calculate pixel dimenions to represent a game field.
     */
    public void getFieldDimensions() {
        // If there's no field defined then simply return
        if (fieldGraphics == null) { return; }
        for (String f : fieldGraphics) {
            String[] chunks = f.split("\\s+");
            System.out.println("fieldgraphic:"+chunks[0]);
            if (chunks[0].equals("FIELD")) {
                double x1 = Double.valueOf(chunks[4]);
                double y1 = Double.valueOf(chunks[5]);
                double x2 = Double.valueOf(chunks[6]);
                double y2 = Double.valueOf(chunks[7]);
                System.out.println("FIELD x1:y1:x2:y2:"+x1+" "+y1+" "+x2+" "+y2);
                FIELD_ORIGIN_X = Math.min(x1, x2);
                FIELD_ORIGIN_Y = Math.min(y1, y2);
                FIELD_WIDTH_X  = Math.max(x1, x2) - FIELD_ORIGIN_X;
                FIELD_WIDTH_Y  = Math.max(y1, y2) - FIELD_ORIGIN_Y;
                fieldPanel.setFieldPanelDimensions();
                FIELD_PANEL_SIZE = fieldPanel.getFieldPanelSize();
                break;
            }
        }        
    }
    
    /**
     * Construct List of NavPoints from JTable.
     */
    public List<NavPoint> pullNavPointTable(boolean strongCheck) {
        List<NavPoint> navpoints = new ArrayList<>();
        int cols = nptTable.getColumnCount();
        int rows = nptTable.getRowCount();
        try {
            for (int i = 0; i < rows; i++) {
                                                                                     // identifier is in column 0
                double x = Double.parseDouble(nptTable.getValueAt(i,1).toString());  // x          is in column 1
                double y = Double.parseDouble(nptTable.getValueAt(i,2).toString());  // y          is in column 2
                int heading = Integer.parseInt(nptTable.getValueAt(i,3).toString()); // heading    is in column 3
                boolean stop = Boolean.parseBoolean(nptTable.getValueAt(i,4).toString()); // stop  is in column 4
                navpoints.add(new NavPoint(x, y, heading, stop));
            }
            return navpoints;
        } catch (Exception e) {
            if (strongCheck) {
                System.out.println("Problem in NavPoint Table!");
                e.printStackTrace();
                System.out.println(e);
            }
            return null;
        }
    }
    
    /**
     * Re-index the NavPoint columnt of the navpoint JTable
     */
    public void reorderNavPointTable() {
        int rows = nptTable.getRowCount();
        for (int i = 0 ; i < rows; i++) {
            nptTable.setValueAt(i, i, 0);
        }
    }

    /**
     * Update the contents of JScrollPane that holds the NavPoints from Source List of NavPoints.
     */
    public void updateNavPointScrollPane() {            
        if (sourceNavPoints == null) { return; }
        navpointTM.setRowCount(0);
        // Add elements to TableModel
        for (int i = 0; i < sourceNavPoints.size(); i++) {
            NavPoint npt = sourceNavPoints.get(i);
            System.out.println("->"+npt.stop+"--"+String.format("%s",npt.stop));
            String xStr = String.format("%.1f", npt.pt.x);
            String yStr = String.format("%.1f", npt.pt.y);
            String bearingStr = String.format("%.0f", npt.heading*180/Math.PI);
            String stopStr = String.format("%s", npt.stop);
            navpointTM.addRow(new Object[]{i, npt.pt.x, npt.pt.y, bearingStr, stopStr});
        }        
    }
    
    
    /**
     * Main method run at command line.
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Run the class constructor and pass command line arguments to the constructor
                new GameField(args);
            }
        });
    }
}
