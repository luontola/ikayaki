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

import ikayaki.util.LastExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

/**
 * Singleton class for holding all global settings. All changes are automatically written to file after a short delay.
 *
 * @author Esko Luontola
 */
public class Settings {

    /**
     * Singleton instance of the Settings object.
     */
    private static Settings instance;

    /**
     * All properties in a map.
     */
    private Properties properties = new Properties();

    /**
     * File where the properties will be saved in XML format
     */
    private File propertiesFile = new File("ikayaki.config");

    /**
     * true if the properties have been modified, otherwise false
     */
    private boolean propertiesModified = false;

    /**
     * All saved sequences
     */
    private List<MeasurementSequence> sequences = new ArrayList<MeasurementSequence>();

    /**
     * File where the sequences will be saved in XML format
     */
    private File sequencesFile = new File("ikayaki.sequences");

    /**
     * true if the sequences have been modified, otherwise false
     */
    private boolean sequencesModified = false;

    /**
     * Queue for scheduling save operations after properties/sequences have been changed
     */
    private LastExecutor autosaveQueue = new LastExecutor(500, true);

    /**
     * Operation that will save the properties/sequences.
     */
    private Runnable autosaveRunnable = new Runnable() {
        public void run() {
            saveNow();
        }
    };

    /**
     * Returns the global Settings object. If not yet created, will first create one.
     */
    public static Settings instance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Creates a new Settings instance. Loads settings from the configuration files.
     */
    private Settings() {
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
            Document document;
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = builder.parse(sequencesFile);

                Element root = document.getDocumentElement();
                NodeList items = root.getChildNodes();
                for (int i = 0; i < items.getLength(); i++) {
                    System.out.println(items.item(i));
                    // TODO: load MeasurementSequence objects from the document and put them to the sequences list
                }

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the settings after a while when no changes have come. The method call will return immediately and will not
     * wait for the file to be written.
     */
    public synchronized void save() {
        autosaveQueue.execute(autosaveRunnable);
    }

    /**
     * Saves the settings and keeps waiting until its done. If no settings have been modified, will do nothing.
     */
    public synchronized void saveNow() {
        // save properties to file
        if (propertiesModified) {
            try {
                OutputStream out = new FileOutputStream(propertiesFile);
                properties.storeToXML(out, null);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            propertiesModified = false;
        }

        // save sequences to file
        if (sequencesModified) {
            // TODO: save the sequences to an XML file understood by the Settings() constructor
            sequencesModified = false;
        }
    }

    /**
     * Returns the value that maps to the specified key.
     *
     * @param key          key whose associated value is to be returned.
     * @param defaultValue a default value
     * @return Value associated with key, or an empty string if none exists.
     */
    private synchronized String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Associates the specified value with the specified key. Will invoke autosaving.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     */
    private synchronized void setProperty(String key, String value) {
        properties.setProperty(key, value);
        propertiesModified = true;
        save();
    }

    /**
     * Generic accessor for all properties. Returns the value from Properties in appropriate type.
     *
     * @return Value associated with key
     */
    public synchronized Object getXXX() {
        return null; // TODO
    }

    /**
     * Generic accessor for all properties. Checks whether the value is ok and sets it. Will invoke autosaving.
     *
     * @return true if value was correct, otherwise false.
     */
    public synchronized boolean setXXX(Object value) {
        return false; // TODO
    }

    public synchronized String getMagnetometerPort() {
        return getProperty("squid.magnetometer.port", "");
    }

    public synchronized boolean setMagnetometerPort(String value) {
        setProperty("squid.magnetometer.port", value);
        return false; // TODO
    }

    public synchronized String getHandlerPort() {
        return getProperty("squid.handler.port", "");
    }

    public synchronized boolean setHandlerPort(String value) {
        return false; // TODO
    }

    public synchronized String getDegausserPort() {
        return getProperty("squid.degausser.port", "");
    }

    public synchronized boolean setDegausserPort(String value) {
        return false; // TODO
    }

    public synchronized double getMagnetometerXAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.xaxiscalibration", "0.0"));
    }

    public synchronized boolean setMagnetometerXAxisCalibration(double value) {
        return false; // TODO
    }

    public synchronized double getMagnetometerYAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.yaxiscalibration", "0.0"));
    }

    public synchronized boolean setMagnetometerYAxisCalibration(double value) {
        return false; // TODO
    }

    public synchronized double getMagnetometerZAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.zaxiscalibration", "0.0"));
    }

    public synchronized boolean setMagnetometerZAxisCalibration(double value) {
        return false; // TODO
    }

    public synchronized int getDegausserRamp() {
        return Integer.parseInt(getProperty("squid.degausser.ramp", "0"));
    }

    public synchronized boolean setDegausserRamp(int value) {
        return false; // TODO
    }

    public synchronized int getDegausserDelay() {
        return Integer.parseInt(getProperty("squid.degausser.delay", "0"));
    }

    public synchronized boolean setDegausserDelay(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerAcceleration() {
        return Integer.parseInt(getProperty("squid.handler.acceleration", "0"));
    }

    public synchronized boolean setHandlerAcceleration(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerDeceleration() {
        return Integer.parseInt(getProperty("squid.handler.deceleration", "0"));
    }

    public synchronized boolean setHandlerDeceleration(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerVelocity() {
        return Integer.parseInt(getProperty("squid.handler.velocity", "0"));
    }

    public synchronized boolean setHandlerVelocity(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerMeasurementVelocity() {
        return Integer.parseInt(getProperty("squid.handler.measurementvelocity", "0"));
    }

    public synchronized boolean setHandlerMeasurementVelocity(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerTransverseYAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.transverseyaf", "0"));
    }

    public synchronized boolean setHandlerTransverseYAFPosition(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerAxialAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.axialaf", "0"));
    }

    public synchronized boolean setHandlerAxialAFPosition(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerSampleLoadPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.sampleload", "0"));
    }

    public synchronized boolean setHandlerSampleLoadPosition(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerBackgroundPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.background", "0"));
    }

    public synchronized boolean setHandlerBackgroundPosition(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerMeasurementPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.measurement", "0"));
    }

    public synchronized boolean setHandlerMeasurementPosition(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerRotation() {
        return Integer.parseInt(getProperty("squid.handler.rotation", "0"));
    }

    public synchronized boolean setHandlerRotation(int value) {
        return false; // TODO
    }

    public synchronized int getHandlerRightLimit() {
        return Integer.parseInt(getProperty("squid.handler.pos.rightlimit", "0"));
    }

    public synchronized boolean setHandlerRightLimit(int value) {
        return false; // TODO
    }

    /**
     * Returns all saved sequences in no particular order.
     */
    public synchronized MeasurementSequence[] getSequences() {
        return sequences.toArray(new MeasurementSequence[sequences.size()]);
    }

    /**
     * Adds a sequence to the sequence list. Each sequence may be added only once.
     */
    public synchronized void addSequence(MeasurementSequence sequence) {
        if (sequence != null && !sequences.contains(sequence)) {
            sequences.add(sequence);
        }
    }

    /**
     * Removes a sequence from the sequence list. If the specified sequence is not in the list, it will be ignored.
     */
    public synchronized void removeSequence(MeasurementSequence sequence) {
        if (sequence != null) {
            sequences.remove(sequence);
        }
    }
}