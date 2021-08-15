package waypoint;  

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;


public class GameFieldFileHandler {
    // parseSettingsFile        <-- read GameField Settings file
    // getRobotFromFile         <-- read Robot Dimensions file
    // getFieldGraphicsFromFile <-- read Field Graphics file
    // getNavPointsFromFile     <-- read NavPoint file
    // fromNavPathFile          <-- read NavPath file
    // toNavPointsFile          --> write NavPoint file
    // toNavPathFile            --> write NavPath file


        
    /**
     * Read Game Field Settings file.
     * @param fileName - name of settings file
     * @return         - hashmap of key,value pairs of settings assignments; null if file error
     */
    public static Map<String, String> parseSettingsFile(String fileName) {
        List<String> requiredKeys = new ArrayList<>(Arrays.asList(
                                        "FIELD_FILE", 
                                        "ROBOT_FILE"));
        Map<String, String> myGameMap = new HashMap<>();
        BufferedReader reader;
        boolean failToParse = false;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            int lineNum = 1;
            String line = reader.readLine();
            while (line != null) {
                String[] chunks = line.split("\\s+");
                    
                // KEY,value format to parse:
                //       
                // <KEY> <value>
                if (chunks.length != 0) {
                    if (chunks[0].equals("//")) { }
                    else if (chunks.length != 2) {
                        System.out.println("Only 2 non-white-space entries per line allowed in settings file");
                        failToParse = true;
                    }
                    else {
                        if (requiredKeys.contains(chunks[0])) {
                            myGameMap.put(chunks[0].trim(), chunks[1].trim());
                            requiredKeys.remove(chunks[0]);
                        }
                        myGameMap.put(chunks[0].trim(), chunks[1].trim());
                    }
                }   
                
                if (failToParse) {
                    throw new IOException("Invalid format in settings file: "+fileName+" line: "+lineNum+"\n"+line);
                }                                    
                line = reader.readLine();
                lineNum += 1;                
            }
            reader.close();
            if (requiredKeys.size() > 0) {
                throw new IOException("Required Key missing in settings file: "+fileName+" key: "+requiredKeys.get(0));                
            }
            
        }
        catch (IOException e) { 
            System.out.println(e);
            myGameMap = null;
        }
        return myGameMap;        
    }
    
    /**
     * Read Robot Dimensions file.
     * @param fileName - name of settings file
     * @return         - hashmap of key,value pairs of settings assignments; null if file error
     */
    public static Map<String, String> getRobotFromFile(String fileName) {
        List<String> requiredKeys = new ArrayList<>(Arrays.asList(
                                        "SIDE_TO_SIDE", 
                                        "FRONT_TO_BACK",
                                        "WHEEL_DIAMETER",
                                        "WHEEL_WIDTH",
                                        "ORIGIN_X_OFFSET",
                                        "ORIGIN_Y_OFFSET"));
                
        Map<String, String> myRobot = new HashMap<>();
        BufferedReader reader;
        boolean failToParse = false;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            int lineNum = 1;
            String line = reader.readLine();
            while (line != null) {
                String[] chunks = line.split("\\s+");
                    
                // KEY,value format to parse:
                //       
                // <KEY> <value>
                
                if (chunks.length != 0) {
                    if (chunks[0].equals("//")) { }
                    else if (chunks.length != 2) {
                        System.out.println("Only 2 non-white-space entries per line allowed in robot file");
                        failToParse = true;
                    }
                    else {
                        if (requiredKeys.contains(chunks[0])) {
                            myRobot.put(chunks[0].trim(), chunks[1].trim());
                            requiredKeys.remove(chunks[0]);
                        }
                        else {
                            System.out.println("Unknown key found");
                            failToParse = true;
                        }
                    }
                }                   
                if (failToParse) {
                    throw new IOException("Invalid format in settings file: "+fileName+" line: "+lineNum+"\n"+line);
                }                                    
                line = reader.readLine();
                lineNum += 1;                
            }
            reader.close();
            
            if (requiredKeys.size() > 0) {
                throw new IOException("Required Key missing in settings file: "+fileName+" key: "+requiredKeys.get(0));         
            }
            
        }
        catch (IOException e) { 
            System.out.println(e);
            myRobot = null;
        }
        return myRobot;        
    }
    
    /**
     * Read and parse Field Graphics file.
     * @param fileName - name of settings file
     * @return         - list of Strings composed of series of graphic elements and arguments; null if file error
     */
    public static List<String> getFieldGraphicsFromFile(String fileName) {
    
        boolean hasField = false;
    
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");    
        
        List<String> fieldGraphics = new ArrayList<>();
        Map<String, String> colorMap = new HashMap<>();
        boolean failToParse = false;
        
        BufferedReader reader;
        Object width;

        int lineNum = 1;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(fileName));            
            line = reader.readLine();
            while (line != null) {
                String[] chunks = line.split("\\s+");
                if (chunks.length > 0) {
                    switch (chunks[0]) {
                
                        case "//": 
                            break;
                            
                        case "":
                            if (chunks.length > 1) 
                                throw new IOException("Whitespace leading text in: "+fileName+" line: "+lineNum+"\n"+line);
                            break;
                        
                        case "COLOR": 
                            if (chunks.length != 5)
                                throw new IOException("COLOR needs 5 non-whitespace elements in: "+fileName+" line: "+lineNum+"\n"+line);                        
                            colorMap.put(chunks[1].trim(), String.format("%s %s %s", chunks[2].trim(), chunks[3].trim(), chunks[4].trim()));
                            break;                            
                    
                        case "LINE":
                        case "RECT":
                        case "FILLRECT":
                        case "CIRCLE": 
                        case "FILLCIRCLE":
                        case "POLYGON":
                        case "FILLPOLYGON":  
                        case "FIELD":                        
                            if (chunks.length < 3)
                                throw new IOException("Field graphics element needs at least 3 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                        
                            if (chunks[0].equals("LINE") && (chunks.length != 7))
                                throw new IOException("LINE needs 7 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                        
                            if (chunks[0].equals("RECT") && (chunks.length != 7))
                                throw new IOException("RECT needs 7 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                            if (chunks[0].equals("FILLRECT") && (chunks.length != 7))
                                throw new IOException("FILLRECT needs 7 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                        
                            if (chunks[0].equals("CIRCLE") && (chunks.length != 6))
                                throw new IOException("CIRCLE needs 6 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                            if (chunks[0].equals("FILLCIRCLE") && (chunks.length != 6))
                                throw new IOException("FILLCIRCLE needs 6 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);

                            if (chunks[0].equals("FIELD") && (chunks.length != 6))
                                throw new IOException("FIELD needs 6 non-whitespace words in: "+fileName+" line: "+lineNum+"\n"+line);
                                
                        
                            if (colorMap.containsKey(chunks[1])) {
                                chunks[1] = colorMap.get(chunks[1]);
                            }
                            else {
                                throw new IOException("No valid color given for "+chunks[1]+" in: "+fileName+" line: "+lineNum+"\n"+line);
                            }
                                            
                            for (int i = 2; i < chunks.length; i++) {                                
                                chunks[i] = chunks[i].replaceAll("INNER_TILE_WIDTH", Double.toString(GameField.INNER_TILE_WIDTH));
                                chunks[i] = chunks[i].replaceAll("MESH_TILE_WIDTH", Double.toString(GameField.MESH_TILE_WIDTH));
                                chunks[i] = chunks[i].replaceAll("TAPE_WIDTH", Double.toString(GameField.TAPE_WIDTH));
                                chunks[i] = chunks[i].replaceAll("FIELD_WIDTH", Double.toString(GameField.FIELD_WIDTH));
                                // .eval() will return Integer if possible, else Double if that is possible
                                try {
                                    width = engine.eval(chunks[i]);
                                }
                                catch (ScriptException e) {    
                                    System.out.println("Eval error in line "+lineNum+":"+line);
                                    System.out.println("  Word "+i+":"+chunks[i]);                        
                                    throw new IOException();
                                }
                                if (width instanceof Double) {
                                    chunks[i] = Double.toString((Double)width);
                                } else if (width instanceof Integer) { 
                                    chunks[i] = Integer.toString((Integer)width);
                                }
                                else throw new IOException("Field graphics width: "+chunks[i]+" is not parseable from line: "+lineNum+" file:"+fileName+"\n");
                            }
                            String fieldStr = String.join(" ", chunks);
                            fieldGraphics.add(fieldStr);
                            
                            if (chunks[0].equals("FIELD")) { hasField = true; }
                            break;                        
                          
                        default :
                            throw new IOException("Unrecognized field graphics type:'"+chunks[0]+"' in: "+fileName+" line: "+lineNum+"\n"+line);
                    }                    
                }   
                line = reader.readLine();
                lineNum += 1;                
                            
            }
            reader.close();
        }
        catch (IOException e) { 
            System.out.println(e);
            fieldGraphics = null;
        }
        if (!hasField) {
            System.out.println("FieldGraphics file: "+fileName+" missing FIELD");
            return null;
        }
        return fieldGraphics;
    }
        
        
    /**
     * Read NavPoints from file.
     * @param fileName - name of NavPoint file
     * @return         - list of NavPoints; null if file error
     */
    public static List<NavPoint> getNavPointsFromFile(String fileName) {
        
        List<NavPoint> navpoints = new ArrayList<>();
        
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            System.out.println("Reading NavPoint file: "+fileName);
            int lineNum = 1;
            String line = reader.readLine();
            while (line != null) {
                String[] chunks = line.split("\\s+");
                if ((chunks.length != 0) && !(chunks[0].equals("//"))) {
                    //System.out.println("..."+chunks[3]+":"+Boolean.parseBoolean(chunks[3]));
                    NavPoint npt = new NavPoint(Double.parseDouble(chunks[0]), 
                                                Double.parseDouble(chunks[1]), 
                                                Integer.parseInt(chunks[2]),
                                                Boolean.parseBoolean(chunks[3])
                                                );
                    navpoints.add(npt);
                }   
                line = reader.readLine();
                lineNum += 1;                
            }
            reader.close();
        }
        catch (IOException e) { 
            System.out.println(e);
            return null;
        }
        return navpoints;
    }
        
    /**
     * Read NavPaths from file.
     * @param fileName - name of NavPath file
     * @return         - list of NavPaths; null if file error     
     */
    public static List<NavPath> fromNavPathFile(String fileName) {
    
        List<NavPath> myPath = new ArrayList<>();
    
        double x1, y1, x2, y2;
        double cx, cy, r, sa, ea;
        boolean c;
    
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            int lineNum = 1;
            String line = reader.readLine();
            while (line != null) {
                // Each line in file is expected to have two strings
                // separated by some whitespace. The first string is
                // the key, the second string is the value.
                //
                // If a user provides a non-link file as an argument, then
                // if the file doesn't strictly fit the expected format, an
                // IOException will be thrown.
                String[] chunks = line.split("\\s+");
                if (chunks.length != 0) {
                    switch (chunks[0]) {
                    
                        case "//":
                            break;
                            
                        case "VECTOR":
                            x1 = Double.parseDouble(chunks[1]);
                            y1 = Double.parseDouble(chunks[2]);
                            x2 = Double.parseDouble(chunks[3]);
                            y2 = Double.parseDouble(chunks[4]);
                            myPath.add(new Vector(x1, y1, x2, y2));                        
                            break;
                        
                        case "ARC":
                            sa = Double.parseDouble(chunks[1]);  // starting angle in radians
                            ea = Double.parseDouble(chunks[2]);  // ending angle in radians
                            cx = Double.parseDouble(chunks[3]);
                            cy = Double.parseDouble(chunks[4]);
                            r  = Double.parseDouble(chunks[5]);
                            c  = Boolean.parseBoolean(chunks[6]); // clockwise
                            myPath.add(new Arc(cx, cy, r, sa, ea, c));
                            break;
                                        
                        case "GAP":
                            x1 = Double.parseDouble(chunks[1]);
                            y1 = Double.parseDouble(chunks[2]);
                            x2 = Double.parseDouble(chunks[3]);
                            y2 = Double.parseDouble(chunks[4]);
                            myPath.add(new Gap(x1, y1, x2, y2));
                            break;
                            
                        default:
                            throw new IOException("Unrecognized path element type:'"+chunks[0]+"' in: "+fileName+" line: "+lineNum+"\n"+line);
                    }                                        
                }
                line = reader.readLine();
                lineNum += 1;
            }
            reader.close();
        }
        catch (IOException e) { 
            System.out.println(e);
            return null;
        }    
        return myPath;
    }
    
    /**
     * Write NavPoints to file.
     * @param sourceNavPoints - List of NavPoints to write to file
     * @param fileName        - name of file to write
     */
    public static void toNavPointsFile(List<NavPoint> sourceNavPoints, String fileName) {
    
        if (fileName.equals("")) {
            System.out.println("No filename given for saving NavPoints.");
            return;
        }
        
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            System.out.println("Writing "+sourceNavPoints.size()+" NavPoints to file: "+fileName);
            
            writer.write("// (double)x (double)y (int)heading (boolean)stop\n");
            
            for (NavPoint p : sourceNavPoints) {
                writer.write(p.pt.x+"\t"+p.pt.y+"\t"+(int)(p.heading*180/Math.PI)+"\t"+p.stop+"\n");
            }    
            writer.close();
            //System.out.println("Wrote NavPoint file: "+filename);
        } catch (IOException e) {
            System.out.println(e);
        }                
    }
    
    /**
     * Write NavPaths to file.
     */
    public static void toNavPathFile (List<NavPath> path, String fileName) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));            
            
            writer.write("// GAP (double)x1 (double)y1 (double)x2 (double)y2 (double)orientation\n");
            writer.write("// VECTOR (double)x1 (double)y1 (double)x2 (double)y2 (double)orientation\n");
            writer.write("// ARC (double)startingAngle (double)endingAngle (double)x (double)y \\\n");
            writer.write("//     (double)radius (double)orientation (boolean)clockwise\n");
            writer.write("//\n");
            
            if (path != null) {
                for (NavPath p : path) {
                    if (p instanceof Vector) {
                        writer.write("VECTOR\t"+p.i.pt.x+"\t"+p.i.pt.y+"\t"+p.o.pt.x+"\t"+p.o.pt.y+"\t"+p.orientation+"\n");
                    }
                    else if (p instanceof Arc) {
                        Arc q = (Arc) p;
                        writer.write("ARC\t"+q.startAngle+"\t"+q.endAngle+"\t"+q.center.x+"\t"+q.center.y+"\t"+q.radius+"\t"+q.orientation+"\t"+q.clockwise+"\n");
                    }
                    else if (p instanceof Gap) {
                        writer.write("GAP\t"+p.i.pt.x+"\t"+p.i.pt.y+"\t"+p.o.pt.x+"\t"+p.o.pt.y+"\t"+p.orientation+"\n");
                    }
                    else {
                        System.out.println("Failed to parse NavPath: "+p.toString());
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }    
    }
    
    
    /**
     * Write code to file.
     */
    public static void exportCode(List<NavPath> robotNavPaths) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("code.txt"));
            
            for (NavPath p : robotNavPaths) {
                writer.write(p.i.pt.x+"\t"+p.i.pt.y+"\n");
            }    
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }                    
    }
    
    /**
     * Write sim path to file.
     */
    public static void exportSimPath(List<NavPoint> simNavPoints) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("sim.txt"));
            
            for (NavPoint npt : simNavPoints) {
                writer.write(npt.toString()+"\n");
            }    
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }                    
    }
    
}
