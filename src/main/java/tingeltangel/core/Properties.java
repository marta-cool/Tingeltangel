
package tingeltangel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import tingeltangel.tools.Preferences;

public class Properties {
    
    private final static String PROPERTY_FILE = "tt.properties";
    
    private final static HashMap<String, String> PROPERTIES = new HashMap<String, String>();
    
    // todo: remove this
    public final static String WIN_MPG123 ="win_mpg123";
    public final static String _PATH =".path";
    public final static String _ENABLED=".enabled";
    
    static {
        try {
            File propertyFile = new File(PROPERTY_FILE);
            if(propertyFile.createNewFile()) {
                // init of propertys
                Iterator<String> i = Preferences.getKeys().iterator();
                while(i.hasNext()) {
                    String key = i.next();
                    setProperty(key, Preferences.getDefault(key), false);
                }
                save();
            }
            BufferedReader in = new BufferedReader(new FileReader(propertyFile));
            String row;
            while((row = in.readLine()) != null) {
                row = row.trim();
                if((!row.isEmpty()) && (!row.startsWith("#"))) {
                    int p = row.indexOf("=");
                    if(p == -1) {
                        throw new IOException();
                    }
                    PROPERTIES.put(row.substring(0, p).trim(), row.substring(p + 1).trim());
                }
            }
            in.close();
        } catch(IOException ioe) {
            ioe.printStackTrace(System.out);
            throw new Error(ioe);
        }
    }
    
    
    private static void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(PROPERTY_FILE));
            Iterator<String> keys = PROPERTIES.keySet().iterator();
            while(keys.hasNext()) {
                String key = keys.next();
                out.println(key + " = " + PROPERTIES.get(key));
            }
            out.close();
        } catch(IOException ioe) {
            System.err.println("property file can not be saved. Your changes will be lost on next start.");
            ioe.printStackTrace(System.err);
        }
    }
    
    private static void setProperty(String name, String value, boolean save) {
        name = name.trim();
        if(value == null) {
            PROPERTIES.remove(name);
        } else {
            PROPERTIES.put(name, value.trim());
        }
        if(save) {
            save();
        }
    }
    
    public static void setProperty(String name, int value) {
        setProperty(name, Integer.toString(value), true);
    }
    
    public static void setProperty(String name, String value) {
        setProperty(name, value, true);
    }
    
    public static void setProperty(String name, boolean value) {
        setProperty(name, Boolean.toString(value), true);
    }
    
    public static String getStringProperty(String name) {
        return(PROPERTIES.get(name));
    }
    
    public static int getIntegerProperty(String name) {
        try {
            return(Integer.parseInt(PROPERTIES.get(name)));
        } catch(NumberFormatException nfe) {
            throw new Error("property '" + name.trim() + "' is not of type int");
        }
    }
    
    public static boolean getBooleanProperty(String name) {
        try {
            return(Boolean.parseBoolean(PROPERTIES.get(name)));
        } catch(NumberFormatException nfe) {
            throw new Error("property '" + name.trim() + "' is not of type bool");
        }
    }
}
