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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton class for holding all global settings. All changes are automatically written to file after a short delay.
 *
 * @author
 */
public class Settings {

    /**
     * All properties in a map.
     */
    private Properties properties = new Properties();

    /**
     * File where the properties will be saved in XML format
     */
    private File propertiesFile;

    /**
     * true if the properties have been modified, otherwise false
     */
    private boolean propertiesModified;

    /**
     * All saved sequences
     */
    private List<MeasurementSequence> sequences = new ArrayList<MeasurementSequence>();

    /**
     * File where the sequences will be saved in XML format
     */
    private File sequencesFile;

    /**
     * true if the sequences have been modified, otherwise false
     */
    private boolean sequencesModified;

    /**
     * Queue for scheduling save operations after properties/sequences have been changed
     */
    private LastExecutor autosaveQueue;

    /**
     * Returns the global Settings object. If not yet created, will first create one.
     */
    public static Settings instance() {
        return null; // TODO
    }

    /**
     * Creates a new Settings instance. Loads settings from the configuration files.
     */
    private Settings() {
        return; // TODO
    }

    /**
     * Saves the settings after a while when no changes have come. The method call will return immediately and will not
     * wait for the file to be written.
     */
    public void save() {
        return; // TODO
    }

    /**
     * Saves the settings and keeps waiting until its done. If no settings have been modified, will do nothing.
     */
    public void saveNow() {
        return; // TODO
    }

    /**
     * Returns the value that maps to the specified key.
     *
     * @param key          key whose associated value is to be returned.
     * @param defaultValue a default value
     * @return Value associated with key, or an empty string if none exists.
     */
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Associates the specified value with the specified key. Will invoke autosaving.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     */
    private void setProperty(String key, String value) {
        properties.setProperty(key, value);
        propertiesModified = true;
        save();
    }

    /**
     * Generic accessor for all properties. Returns the value from Properties in appropriate type.
     *
     * @return Value associated with key
     */
    public Object getXXX() {
        return null; // TODO
    }

    /**
     * Generic accessor for all properties. Checks whether the value is ok and sets it. Will invoke autosaving.
     *
     * @return true if value was correct, otherwise false.
     */
    public boolean setXXX(Object value) {
        return false; // TODO
    }

    public String getMagnetometerPort() {
        return getProperty("squid.magnetometer.port", "");
    }

    public boolean setMagnetometerPort(String value) {
        setProperty("squid.magnetometer.port", value);
        return false; // TODO
    }

    public String getHandlerPort() {
        return getProperty("squid.handler.port", "");
    }

    public boolean setHandlerPort(String value) {
        return false; // TODO
    }

    public String getDegausserPort() {
        return getProperty("squid.degausser.port", "");
    }

    public boolean setDegausserPort(String value) {
        return false; // TODO
    }

    public double getMagnetometerXAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.xaxiscalibration", "0.0"));
    }

    public boolean setMagnetometerXAxisCalibration(double value) {
        return false; // TODO
    }

    public double getMagnetometerYAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.yaxiscalibration", "0.0"));
    }

    public boolean setMagnetometerYAxisCalibration(double value) {
        return false; // TODO
    }

    public double getMagnetometerZAxisCalibration() {
        return Double.parseDouble(getProperty("squid.magnetometer.zaxiscalibration", "0.0"));
    }

    public boolean setMagnetometerZAxisCalibration(double value) {
        return false; // TODO
    }

    public int getDegausserRamp() {
        return Integer.parseInt(getProperty("squid.degausser.ramp", "0"));
    }

    public boolean setDegausserRamp(int value) {
        return false; // TODO
    }

    public int getDegausserDelay() {
        return Integer.parseInt(getProperty("squid.degausser.delay", "0"));
    }

    public boolean setDegausserDelay(int value) {
        return false; // TODO
    }

    public int getHandlerAcceleration() {
        return Integer.parseInt(getProperty("squid.handler.acceleration", "0"));
    }

    public boolean setHandlerAcceleration(int value) {
        return false; // TODO
    }

    public int getHandlerDeceleration() {
        return Integer.parseInt(getProperty("squid.handler.deceleration", "0"));
    }

    public boolean setHandlerDeceleration(int value) {
        return false; // TODO
    }

    public int getHandlerVelocity() {
        return Integer.parseInt(getProperty("squid.handler.velocity", "0"));
    }

    public boolean setHandlerVelocity(int value) {
        return false; // TODO
    }

    public int getHandlerMeasurementVelocity() {
        return Integer.parseInt(getProperty("squid.handler.measurementvelocity", "0"));
    }

    public boolean setHandlerMeasurementVelocity(int value) {
        return false; // TODO
    }

    public int getHandlerTransverseYAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.transverseyaf", "0"));
    }

    public boolean setHandlerTransverseYAFPosition(int value) {
        return false; // TODO
    }

    public int getHandlerAxialAFPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.axialaf", "0"));
    }

    public boolean setHandlerAxialAFPosition(int value) {
        return false; // TODO
    }

    public int getHandlerSampleLoadPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.sampleload", "0"));
    }

    public boolean setHandlerSampleLoadPosition(int value) {
        return false; // TODO
    }

    public int getHandlerBackgroundPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.background", "0"));
    }

    public boolean setHandlerBackgroundPosition(int value) {
        return false; // TODO
    }

    public int getHandlerMeasurementPosition() {
        return Integer.parseInt(getProperty("squid.handler.pos.measurement", "0"));
    }

    public boolean setHandlerMeasurementPosition(int value) {
        return false; // TODO
    }

    public int getHandlerRotation() {
        return Integer.parseInt(getProperty("squid.handler.rotation", "0"));
    }

    public boolean setHandlerRotation(int value) {
        return false; // TODO
    }

    public int getHandlerRightLimit() {
        return Integer.parseInt(getProperty("squid.handler.pos.rightlimit", "0"));
    }

    public boolean setHandlerRightLimit(int value) {
        return false; // TODO
    }

    /**
     * Returns all saved Sequences.
     */
    public MeasurementSequence[] getSequences() {
        return null; // TODO
    }

    /**
     * Adds a sequence to the sequence list.
     */
    public void addSequence(MeasurementSequence sequence) {
        return; // TODO
    }

    /**
     * Removes a sequence from the sequence list. If the specified sequence is not in the list, it will be ignored.
     */
    public void removeSequence(MeasurementSequence sequence) {
        return; // TODO
    }
}