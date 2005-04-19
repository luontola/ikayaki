/*
* Settings.java
*
* Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
*
* This file is part of Ikayaki.
*
* Ikayaki is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Ikayaki is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Ikayaki; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package ikayaki;

import ikayaki.gui.StyledWrapper;
import ikayaki.util.DocumentUtilities;
import ikayaki.util.LastExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Singleton class for holding all global settings. All changes are automatically written to file after a short delay.
 *
 * @author Esko Luontola
 */
public class Settings {

    private static final int DIRECTORY_HISTORY_SIZE = 30;   // TODO: make methods for changing the history sizes
    private static final int PROJECT_HISTORY_SIZE = 10;

    private static final StyledWrapper defaultWrapper = new StyledWrapper();
    private static final StyledWrapper measuringWrapper = new StyledWrapper();
    private static final StyledWrapper doneRecentlyWrapper = new StyledWrapper();

    /**
     * Singleton instance of the Settings object.
     */
    private static Settings instance;

    /**
     * All properties in a map.
     */
    private static Properties properties = new Properties();

    /**
     * File where the properties will be saved in XML format
     */
    private static File propertiesFile = Ikayaki.PROPERTIES_FILE;

    /**
     * true if the properties have been modified, otherwise false
     */
    private static boolean propertiesModified = false;

    /**
     * All saved sequences
     */
    private static List<MeasurementSequence> sequences = new ArrayList<MeasurementSequence>();

    /**
     * File where the sequences will be saved in XML format
     */
    private static File sequencesFile = Ikayaki.SEQUENCES_FILE;

    /**
     * true if the sequences have been modified, otherwise false
     */
    private static boolean sequencesModified = false;

    /**
     * List for holding the recently used directories. Used to cache the values read from the properties.
     */
    private static List<File> directoryHistory = new LinkedList<File>();

    /**
     * List for holding the recently used project files. Used to cache the values read from the properties.
     */
    private static List<File> projectHistory = new LinkedList<File>();

    /**
     * Queue for scheduling save operations after properties/sequences have been changed
     */
    private static LastExecutor autosaveQueue = new LastExecutor(500, true);

    /**
     * Operation that will save the properties/sequences.
     */
    private static Runnable autosaveRunnable = new Runnable() {
        public void run() {
            saveNow();
        }
    };

    static {

        // ensure that the configuration files and directories exist
        if (!Ikayaki.CALIBRATION_PROJECT_DIR.exists()) {
            if (!Ikayaki.CALIBRATION_PROJECT_DIR.mkdir()) {
                System.err.println("Unable to create directory: " + Ikayaki.CALIBRATION_PROJECT_DIR);
            }
        }
        if (!Ikayaki.CALIBRATION_PROJECT_DIR.isDirectory()) {
            System.err.println("No such directory: " + Ikayaki.CALIBRATION_PROJECT_DIR);
        }

        // load saved properties
        if (propertiesFile.exists()) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(propertiesFile));
                properties.loadFromXML(in);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (InvalidPropertiesFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // load saved sequences
        if (sequencesFile.exists()) {
            Document document = null;
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = builder.parse(sequencesFile);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Element root = document.getDocumentElement();
            NodeList sequenceList = root.getElementsByTagName("sequence");
            for (int i = 0; i < sequenceList.getLength(); i++) {
                sequences.add(new MeasurementSequence((Element) sequenceList.item(i)));
            }
        }

        // load custom properties
        loadDirectoryHistory();
        loadProjectHistory();

        // set background colors for the styled wrappers
        defaultWrapper.opaque = true;
        defaultWrapper.background = new Color(0xFFFFFF);
        defaultWrapper.selectedBackground = new Color(0xC3D4E8);
        defaultWrapper.focusBackground = new Color(0xFFFFFF);
        defaultWrapper.selectedFocusBackground = new Color(0xC3D4E8);

        measuringWrapper.opaque = true;
        measuringWrapper.background = new Color(0xEEBAEE);
        measuringWrapper.selectedBackground = new Color(0xFFCCFF);
        measuringWrapper.focusBackground = new Color(0xEEBAEE);
        measuringWrapper.selectedFocusBackground = new Color(0xFFCCFF);

        doneRecentlyWrapper.opaque = true;
        doneRecentlyWrapper.background = new Color(0xBAEEBA);
        doneRecentlyWrapper.selectedBackground = new Color(0xCCFFCC);
        doneRecentlyWrapper.focusBackground = new Color(0xBAEEBA);
        doneRecentlyWrapper.selectedFocusBackground = new Color(0xCCFFCC);
    }

    /**
     * Returns the global Settings object. If not yet created, will first create one.
     *
     * @deprecated use the static class methods instead.
     */
    public static Settings instance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Creates a new Settings instance. Loads settings from the configuration files.
     *
     * @deprecated use the static class methods instead.
     */
    private Settings() {
    }

    /**
     * Saves the settings after a while when no changes have come. The method call will return immediately and will not
     * wait for the file to be written.
     */
    public static synchronized void save() {
        autosaveQueue.execute(autosaveRunnable);
    }

    /**
     * Saves the settings and keeps waiting until its done. If no settings have been modified, will do nothing.
     *
     * @return true if there were no errors in writing the files or everything was already saved. Otherwise false.
     */
    public static synchronized boolean saveNow() {
        boolean ok = true;

        // save properties to file
        if (propertiesModified) {
            try {
                OutputStream out = new FileOutputStream(propertiesFile);
                properties.storeToXML(out, null);
                out.close();
                propertiesModified = false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ok = false;
            } catch (IOException e) {
                e.printStackTrace();
                ok = false;
            }
        }

        // save sequences to file
        if (sequencesModified) {
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element root = document.createElement("sequences");
                for (MeasurementSequence sequence : sequences) {
                    root.appendChild(sequence.getElement(document));
                }
                document.appendChild(root);

                if (DocumentUtilities.storeToXML(sequencesFile, document)) {
                    sequencesModified = false;
                } else {
                    ok = false;
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Returns the value that maps to the specified key.
     *
     * @param key key whose associated value is to be returned.
     * @return the value associated with key, or null if none exists.
     */
    private static synchronized String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Returns the value that maps to the specified key.
     *
     * @param key          key whose associated value is to be returned.
     * @param defaultValue a default value
     * @return the value associated with key, or defaultValue if none exists.
     */
    private static synchronized String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Associates the specified value with the specified key. Will invoke autosaving.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key, or null to remove the value.
     * @throws NullPointerException if key is null.
     */
    private static synchronized void setProperty(String key, String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
        propertiesModified = true;
        save();
    }

    /**
     * Generic accessor for all properties. Returns the value from Properties in appropriate type.
     *
     * @return Value associated with key
     */
    public static synchronized Object getXXX() {
        return null;
    }

    /**
     * Generic accessor for all properties. Checks whether the value is ok and sets it. Will invoke autosaving.
     *
     * @return true if value was correct, otherwise false.
     */
    public static synchronized boolean setXXX(Object value) {
        return false;
    }

    /* Serial ports */

    public static synchronized String getMagnetometerPort() {
        return getProperty("squid.magnetometer.port", "");
    }

    public static synchronized boolean setMagnetometerPort(String value) {
        setProperty("squid.magnetometer.port", value);
        return true;
    }

    public static synchronized String getHandlerPort() {
        return getProperty("squid.handler.port", "");
    }

    public static synchronized boolean setHandlerPort(String value) {
        setProperty("squid.handler.port", value);
        return true;
    }

    public static synchronized String getDegausserPort() {
        return getProperty("squid.degausser.port", "");
    }

    public static synchronized boolean setDegausserPort(String value) {
        setProperty("squid.degausser.port", value);
        return true;
    }

    /* Magnetometer */

    public static synchronized double getMagnetometerXAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.xaxiscalibration", "0.0"));
    }

    public static synchronized boolean setMagnetometerXAxisCalibration(double value) {
        setProperty("squid.magnetometer.xaxiscalibration", Double.toString(value));
        return true;
    }

    public static synchronized double getMagnetometerYAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.yaxiscalibration", "0.0"));
    }

    public static synchronized boolean setMagnetometerYAxisCalibration(double value) {
        setProperty("squid.magnetometer.yaxiscalibration", Double.toString(value));
        return true;
    }

    public static synchronized double getMagnetometerZAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.zaxiscalibration", "0.0"));
    }

    public static synchronized boolean setMagnetometerZAxisCalibration(double value) {
        setProperty("squid.magnetometer.zaxiscalibration", Double.toString(value));
        return true;
    }

    /* Degausser */

    public static synchronized int getDegausserRamp() {
        return Integer.parseInt(getProperty("squid.degausser.ramp", "0"));
    }

    public static synchronized boolean setDegausserRamp(int value) {
        if (value == 3 || value == 5 || value == 7 || value == 9) {
            setProperty("squid.degausser.ramp", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getDegausserDelay() {
        return Integer.parseInt(getProperty("squid.degausser.delay", "0"));
    }

    public static synchronized boolean setDegausserDelay(int value) {
        if (value > 0 && value < 10) {
            setProperty("squid.degausser.delay", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized boolean setDegausserMaximumField(int value) {
        if (value >= 0 && value <= 4000) {
            setProperty("squid.degausser.maximumfield", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getDegausserMaximumField() {
        return Integer.parseInt(getProperty("squid.degausser.maximumfield", "0"));
    }

    /* Sample handler */

    public static synchronized int getHandlerAcceleration() {
        return Integer.parseInt(getProperty("squid.handler.acceleration", "0"));
    }

    public static synchronized boolean setHandlerAcceleration(int value) {
        if (value >= 0 && value <= 127) {
            setProperty("squid.handler.acceleration", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerDeceleration() {
        return Integer.parseInt(getProperty("squid.handler.deceleration", "0"));
    }

    public static synchronized boolean setHandlerDeceleration(int value) {
        if (value >= 0 && value <= 127) {
            setProperty("squid.handler.deceleration", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerVelocity() {
        return Integer.parseInt(getProperty("squid.handler.velocity", "0"));
    }

    public static synchronized boolean setHandlerVelocity(int value) {
        if (value >= 50 && value <= 8500) {
            setProperty("squid.handler.velocity", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerMeasurementVelocity() {
        return Integer.parseInt(getProperty("squid.handler.measurementvelocity", "0"));
    }

    public static synchronized boolean setHandlerMeasurementVelocity(int value) {
        if (value >= 50 && value <= 8500) {
            setProperty("squid.handler.measurementvelocity", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerRotationVelocity() {
        return Integer.parseInt(getProperty("squid.handler.rotationvelocity", "50"));
    }

    public static synchronized boolean setHandlerRotationVelocity(int value) {
        if (value >= 50 && value <= 8500) {
            setProperty("squid.handler.rotationvelocity", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerRotationAcceleration() {
        return Integer.parseInt(getProperty("squid.handler.rotationacceleration", "0"));
    }

    public static synchronized boolean setHandlerRotationAcceleration(int value) {
        if (value >= 0 && value <= 127) {
            setProperty("squid.handler.rotationacceleration", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerRotationDeceleration() {
        return Integer.parseInt(getProperty("squid.handler.rotationdeceleration", "0"));
    }

    public static synchronized boolean setHandlerRotationDeceleration(int value) {
        if (value >= 50 && value <= 2000) {
            setProperty("squid.handler.rotationdeceleration", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerTransverseYAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.transverseyaf", "0"));
    }

    public static synchronized boolean setHandlerTransverseYAFPosition(int value) {
        if (value >= 1 && value <= 16777215) {
            setProperty("squid.handler.pos.transverseyaf", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerAxialAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.axialaf", "0"));
    }

    public static synchronized boolean setHandlerAxialAFPosition(int value) {
        if (value >= 1 && value <= 16777215) {
            setProperty("squid.handler.pos.axialaf", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerSampleLoadPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.sampleload", "0"));
    }

    public static synchronized boolean setHandlerSampleLoadPosition(int value) {
        if (value >= 0 && value <= 16777215) {
            setProperty("squid.handler.pos.sampleload", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerBackgroundPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.background", "0"));
    }

    public static synchronized boolean setHandlerBackgroundPosition(int value) {
        if (value >= 1 && value <= 16777215) {
            setProperty("squid.handler.pos.background", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerMeasurementPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.measurement", "0"));
    }

    public static synchronized boolean setHandlerMeasurementPosition(int value) {
        if (value >= 1 && value <= 16777215) {
            setProperty("squid.handler.pos.measurement", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerRightLimit() {
        return Integer.parseInt(getProperty("squid.handler.pos.rightlimit", "0"));
    }

    public static synchronized boolean setHandlerRightLimit(int value) {
        if (value == 0 || value == 1) {
            setProperty("squid.handler.pos.rightlimit", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized int getHandlerRotation() {
        return Integer.parseInt(getProperty("squid.handler.rotation", "0"));
    }

    public static synchronized boolean setHandlerRotation(int value) {
        setProperty("squid.handler.rotation", Integer.toString(value));
        return true;
    }

    /* Other settings for measurements */

    /**
     * How many times the handler should rotate itself when taking the measurements. Possible values are 0, 1 or more.
     */
    public static synchronized int getMeasurementRotations() {
        return Integer.parseInt(getProperty("measurement.rotations", "1"));
    }

    public static synchronized boolean setMeasurementRotations(int value) { // TODO: gui for changing this value
        if (value >= 0) {
            setProperty("measurement.rotations", Integer.toString(value));
            return true;
        } else {
            return false;
        }
    }

    /* Program window */

    public static synchronized int getWindowWidth() {
        int i = Integer.parseInt(getProperty("gui.window.width", "1000"));
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (i < 400) {
            i = 400;
        } else if (i > maxBounds.width) {
            i = maxBounds.width;
        }
        return i;
    }

    public static synchronized boolean setWindowWidth(int value) {
        setProperty("gui.window.width", Integer.toString(value));
        return true;
    }

    public static synchronized int getWindowHeight() {
        int i = Integer.parseInt(getProperty("gui.window.height", "700"));
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (i < 300) {
            i = 300;
        } else if (i > maxBounds.height) {
            i = maxBounds.height;
        }
        return i;
    }

    public static synchronized boolean setWindowHeight(int value) {
        setProperty("gui.window.height", Integer.toString(value));
        return true;
    }

    public static synchronized boolean getWindowMaximized() {
        return Boolean.parseBoolean(getProperty("gui.window.maximized", "false"));
    }

    public static synchronized boolean setWindowMaximized(boolean value) {
        setProperty("gui.window.maximized", Boolean.toString(value));
        return true;
    }

    /* Directory history */

    public static synchronized File getLastDirectory() {
        File[] dirs = getDirectoryHistory();
        if (dirs.length > 0) {
            return dirs[0];
        } else {
            return new File("").getAbsoluteFile();
        }
    }

    public static synchronized File[] getDirectoryHistory() {
        return directoryHistory.toArray(new File[directoryHistory.size()]);
    }

    public static synchronized boolean updateDirectoryHistory(File visited) {
        if (!visited.isAbsolute()) {
            visited = visited.getAbsoluteFile();
        }

        // update history list
        while (directoryHistory.remove(visited)) ;
        directoryHistory.add(0, visited);
        while (directoryHistory.size() > DIRECTORY_HISTORY_SIZE) {
            directoryHistory.remove(directoryHistory.size() - 1);
        }

        // save as properties
        for (int i = 0; i < Math.max(directoryHistory.size(), DIRECTORY_HISTORY_SIZE); i++) {
            if (i < directoryHistory.size()) {
                setProperty("history.dir." + i, directoryHistory.get(i).getAbsolutePath());
            } else {
                setProperty("history.dir." + i, null);
            }
        }
        return true;
    }

    private static synchronized void loadDirectoryHistory() {
        // reset history list
        directoryHistory.clear();

        // load from properties
        int i = 0;
        while (true) {
            String s = getProperty("history.dir." + i);
            if (s == null) {
                break;
            } else {
                File file = new File(s);
                if (file.isDirectory()) {
                    directoryHistory.add(file);
                }
            }
            i++;
        }
    }

    /* Project history */

    public static synchronized File[] getProjectHistory() {
        return projectHistory.toArray(new File[projectHistory.size()]);
    }

    public static synchronized boolean updateProjectHistory(File visited) {
        if (visited == null) {
            return false;
        }
        if (!visited.isAbsolute()) {
            visited = visited.getAbsoluteFile();
        }

        // update history list
        while (projectHistory.remove(visited)) ;
        projectHistory.add(0, visited);
        while (projectHistory.size() > PROJECT_HISTORY_SIZE) {
            projectHistory.remove(projectHistory.size() - 1);
        }

        // save as properties
        for (int i = 0; i < Math.max(projectHistory.size(), PROJECT_HISTORY_SIZE); i++) {
            if (i < projectHistory.size()) {
                setProperty("history.project." + i, projectHistory.get(i).getAbsolutePath());
            } else {
                setProperty("history.project." + i, null);
            }
        }
        return true;
    }

    private static synchronized void loadProjectHistory() {
        // reset history list
        projectHistory.clear();

        // load from properties
        int i = 0;
        while (true) {
            String s = getProperty("history.project." + i);
            if (s == null) {
                break;
            } else {
                File file = new File(s);
                if (file.isFile()) {
                    projectHistory.add(file);
                }
            }
            i++;
        }
    }

    /**
     * Returns all saved sequences in no particular order.
     */
    public static synchronized MeasurementSequence[] getSequences() {
        return sequences.toArray(new MeasurementSequence[sequences.size()]);
    }

    /**
     * Adds a sequence to the sequence list. Each sequence may be added only once.
     */
    public static synchronized void addSequence(MeasurementSequence sequence) {
        if (sequence != null && !sequences.contains(sequence)) {
            sequences.add(sequence);
            sequencesModified = true;
            save();
        }
    }

    /**
     * Removes a sequence from the sequence list. If the specified sequence is not in the list, it will be ignored.
     */
    public static synchronized void removeSequence(MeasurementSequence sequence) { // TODO: gui for renaming and removing sequences
        if (sequence != null) {
            sequences.remove(sequence);
            sequencesModified = true;
            save();
        }
    }

    // TODO: method for notifying that somebody has changed the saved sequences (invoke autosave)

    /**
     * Returns a copy of the default StyledWrapper.
     */
    public static StyledWrapper getDefaultWrapperInstance() {
        return (StyledWrapper) defaultWrapper.clone();
    }

    /**
     * Returns a copy of the StyledWrapper for measuring projects.
     */
    public static StyledWrapper getMeasuringWrapperInstance() {
        return (StyledWrapper) measuringWrapper.clone();
    }

    /**
     * Returns a copy of the StyledWrapper for recently measured projects.
     */
    public static StyledWrapper getDoneRecentlyWrapperInstance() {
        return (StyledWrapper) doneRecentlyWrapper.clone();
    }
}
