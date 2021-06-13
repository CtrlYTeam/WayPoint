package waypoint;  

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Class to present Settings parameters in a JFrame.
 */
public class SettingsFrame extends JFrame {

    private boolean showFrame = false;
    protected String filename = "";
    protected Map<String, String> parameters;    
        //
        // Default parameter values in settingsDefault.txt
        //
        // FIELD_FILE UltimateGoalBlue.txt
        // ROBOT_FILE UltimateGoalRobot.txt
        // NAVPOINT_INFILE navpoints.txt
        // NAVPOINT_OUTFILE navpoints.txt
        // PATH_OUTFILE path.txt
        // CALC_ALLOW_WAYPOINT_REVERSALS
        // CALC_ALLOW_STRAFE
        // SIM_UNITTIME 50.0
        // SIM_PERTURBATION 0.1
        // SIM_NAVPOINTFILE sim.txt
    private JTextField field_fileTF;
    private JTextField robot_fileTF;
    private JCheckBox calc_reversalCB;
    private JCheckBox calc_strafeCB;
    private JButton updateB;
        
     
    protected void showSettingsFrame(boolean showMe) {
//        if (!showFrame && showMe) {
            
//        }
        this.setVisible(showMe);
        
    }

    /**
     * Update Map of parameters from Settings Panel.
     */
    protected Map<String, String> updateParameters() {
        
        Map<String, String> newParams = new HashMap<>();
        newParams.put("FIELD_FILE", field_fileTF.getText());
        newParams.put("ROBOT_FILE", robot_fileTF.getText());
        newParams.put("CALC_ALLOW_WAYPOINT_REVERSALS", Boolean.toString(calc_reversalCB.isSelected()));
        newParams.put("CALC_ALLOW_STRAFE", Boolean.toString(calc_reversalCB.isSelected()));
        return newParams;
        
    }
    
    /**
     * Class constructor, creates JPanel gui.
     */
    public SettingsFrame(GameField myGameField, String arg) {     
        this.filename = arg;
        // Build Map of parameters from file
        parameters = GameFieldFileHandler.parseSettingsFile(this.filename);
        if (parameters == null) { 
            System.out.println("Failed to get parameters from Settings File: "+this.filename);
            return; 
        }
        
        // Some parameters may not be in the settings file. If the following parameters
        // are not in the settings file, they are assigned default values.
        //if (!parameters.containsKey("SCALE")) {
        //    parameters.put("SCALE", "5.0");
        //}
        
        Dimension tfSize = new Dimension(200, 25);
        
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
        
        JPanel field_fileP = new JPanel();
        field_fileP.add(new JLabel("FIELD_FILE"));
        field_fileTF = new JTextField(parameters.get("FIELD_FILE"));
        field_fileTF.setPreferredSize(tfSize);
        field_fileP.add(field_fileTF);        
        settingsPanel.add(field_fileP);
     
        JPanel robot_fileP = new JPanel();
        robot_fileP.add(new JLabel("ROBOT_FILE"));
        robot_fileTF = new JTextField(parameters.get("ROBOT_FILE"));
        robot_fileTF.setPreferredSize(tfSize);
        robot_fileP.add(robot_fileTF);        
        settingsPanel.add(robot_fileP);
     
        JPanel calc_reversalP = new JPanel();
        calc_reversalP.add(new JLabel("CALC_ALLOW_WAYPOINT_REVERSALS"));
        calc_reversalCB = new JCheckBox("", Boolean.parseBoolean(parameters.get("CALC_ALLOW_WAYPOINT_REVERSALS")));
        calc_reversalP.add(calc_reversalCB);        
        settingsPanel.add(calc_reversalP);
        
        JPanel calc_strafeP = new JPanel();
        calc_strafeP.add(new JLabel("CALC_ALLOW_STRAFE"));
        calc_strafeCB = new JCheckBox("", Boolean.parseBoolean(parameters.get("CALC_ALLOW_STRAFE")));
        calc_strafeP.add(calc_strafeCB);        
        settingsPanel.add(calc_strafeP);
        
        updateB = new JButton("Update Settings");
        updateB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parameters = updateParameters();
            }
        });
        
        
        this.add(settingsPanel, BorderLayout.PAGE_START);
        this.add(updateB, BorderLayout.PAGE_END);
        this.pack();
        this.setTitle("  GameField Settings   ");
        this.setVisible(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                myGameField.showSettingsFrame = false;
                myGameField.settingsB.setBackground(null);
                myGameField.repaint();
            }
        });
    }
}
