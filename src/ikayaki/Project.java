/*
* Project.java
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

import ikayaki.squid.Squid;
import ikayaki.util.DocumentUtilities;
import ikayaki.util.LastExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix3d;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static ikayaki.ProjectEvent.Type.DATA_CHANGED;
import static ikayaki.ProjectEvent.Type.STATE_CHANGED;
import static ikayaki.ProjectEvent.Type.FILE_SAVED;
import static ikayaki.MeasurementStep.State.DONE;
import static ikayaki.MeasurementStep.State.DONE_RECENTLY;
import static ikayaki.Project.State.*;
import static ikayaki.Project.Type.*;
import static ikayaki.Project.SampleType.*;

/**
 * Represents a measurement project file. Project is responsible for managing and storing the data that is recieved from
 * the magnetometer measurements. Any changes made to the project will be written to file regularly (autosave).
 * <p/>
 * Project is responsible for controlling the magnetometer through the SQUID API. Controlling the SQUID will be done in
 * a private worker thread. Only one project at a time may access the SQUID.
 * <p/>
 * All operations are thread-safe.
 *
 * @author Esko Luontola
 */
public class Project {
/*
Event A: On property change - Autosaving will be invoked and the project written to file
after a short delay.
*/
/*
Event B: On measurement started/ended/paused/aborted - ProjectEvent about the state
change will be fired to all project listeners.
*/
/*
Event C: On measurement subphase started/completed - MeasurementEvent will be fired
to all measurement listeners.
*/
/*
Event D: On strike/dip/volume etc. changed - The updated transformation matrix will be
applied to all measurements and a ProjectEvent about the data change will be fired to all
project listeners.
*/
/*
Event E: On project file saved - ProjectEvent about the file saving will be fired to all
project listeners.
*/

    /**
     * Caches the created and loaded Project objects to make sure that no more than one object will be created for each
     * physical file.
     */
    private static Hashtable<File, Project> projectCache = new Hashtable<File, Project>();

    /**
     * Caches the types of the project files, as read by getType(Project). The value is a Type for valid project files,
     * or an Object for invalid or unknown files.
     */
    private static Hashtable<File, Object> projectTypeCache = new Hashtable<File, Object>();

    /**
     * Location of the project file in the local file system. Autosaving will save the project to this file.
     */
    private File file;
    /**
     * Type of the measurement project. This will affect which features of the project are enabled and disabled.
     */
    private Type type;

    /**
     * Current state of the measurements. If no measurement is running, then state is IDLE. Only one measurement may be
     * running at a time.
     */
    private State state = IDLE;

    /**
     * Pointer to the SQUID device interface, or null if this project is not its owner.
     */
    private Squid squid = null;

    /**
     * Custom properties of this project stored in a map. The project is not interested in what properties are stored;
     * it only saves them.
     */
    private Properties properties = new Properties();

    /**
     * Measurement sequence of this project. In the beginning are all completed measurement steps, and in the end are
     * planned measurement steps. Completed measurements may NOT be deleted.
     */
    private MeasurementSequence sequence = new MeasurementSequence();

    /**
     * Strike of the sample. Will be used to create the transform matrix.
     */
    private double strike = 0.0;

    /**
     * Dip of the sample. Will be used to create the transform matrix.
     */
    private double dip = 0.0;

    /**
     * Type of the sample. Will be used to create the transform matrix.
     */
    private SampleType sampleType = CORE;

    /**
     * Orientation of the sample. true if the sample orientation is +Z, or false if it is -Z. Will be used to create the
     * transform matrix.
     */
    private boolean orientation = true;

    /**
     * Matrix for correcting the sample’s orientation. The matrix will be updated whenever the strike, dip, sampleType
     * or orientation is changed. After that the updated matrix will be applied to all measurements.
     */
    private Matrix3d transform = new Matrix3d();

    /**
     * Mass of the sample, or a negative value if no mass is defined.
     */
    private double mass = -1.0;

    /**
     * Volume of the sample, or a negative value if no volume is defined.
     */
    private double volume = -1.0;

    /**
     * Current measurement step, or null if no measurement is running.
     */
    private MeasurementStep currentStep = null;

    /**
     * Listeners for this project.
     */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Scheduler for automatically writing the modified project to file after a short delay.
     */
    private LastExecutor autosaveQueue = new LastExecutor(500, true);

    /**
     * Operation that will save the project to file.
     */
    private Runnable autosaveRunnable = new Runnable() {
        public void run() {
            saveNow();
        }
    };

    /**
     * Creates a calibration project file.
     *
     * @param file path for the new project file.
     * @return the created project, or null if file was not writable or it already existed.
     * @throws NullPointerException if file is null.
     */
    public static Project createCalibrationProject(File file) {
        return createProject(file, CALIBRATION);
    }

    /**
     * Creates an AF project file.
     *
     * @param file path for the new project file.
     * @return the created project, or null if file was not writable or it already existed.
     * @throws NullPointerException if file is null.
     */
    public static Project createAFProject(File file) {
        return createProject(file, AF);
    }

    /**
     * Creates a thellier project file.
     *
     * @param file path for the new project file.
     * @return the created project, or null if file was not writable or it already existed.
     * @throws NullPointerException if file is null.
     */
    public static Project createThellierProject(File file) {
        return createProject(file, THELLIER);
    }

    /**
     * Creates a thermal project file.
     *
     * @param file path for the new project file.
     * @return the created project, or null if file was not writable or it already existed.
     * @throws NullPointerException if file is null.
     */
    public static Project createThermalProject(File file) {
        return createProject(file, THERMAL);
    }

    /**
     * Creates a project file of the specified type. Ensures that the project file has been written to disk. Adds the
     * created Project object to projectCache.
     *
     * @param file path for the new project file.
     * @param type type of the project.
     * @return the created project, or null if file was not writable or it already existed.
     * @throws NullPointerException if file or type is null.
     */
    private static synchronized Project createProject(File file, Type type) {
        if (file == null || type == null) {
            throw new NullPointerException();
        }

        // create a new file, do not overwrite an old one
        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        // create project, write to file and add to cache
        Project project = new Project(file, type);
        if (!project.saveNow()) {
            return null;
        }
        projectCache.put(file, project);
        return project;
    }

    /**
     * Loads a saved project file. If the file has already been loaded, will return a reference to the existing Project
     * object.
     *
     * @param file project file to be loaded.
     * @return the loaded project, or null if file is not a valid project file or it was not readable.
     * @throws NullPointerException if file is null.
     */
    public static synchronized Project loadProject(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.canRead() || !file.isFile()) {
            return null;
        }

        // check cache
        Project project = projectCache.get(file);
        if (project != null) {
            return project;
        }

        // load file and add to cache
        try {
            Document document = DocumentUtilities.loadFromXML(file);
            if (document == null) {
                return null;
            }
            project = new Project(file, document);
            projectCache.put(file, project);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return project;
    }

    /**
     * Ensures that the project file is saved and frees the resources taken by the project. A project should not be used
     * after it has been closed – any further use of the object is undefined (probably will create
     * NullPointerExceptions). The closed project is removed from the projectCache. A project can not be closed if it
     * has a measurement running.
     *
     * @param project project to be closed.
     * @return true if the project has been closed, false if a measurement is running and the project can not be
     *         closed.
     * @throws NullPointerException if the project is null.
     */
    public static synchronized boolean closeProject(Project project) {
        if (project == null) {
            throw new NullPointerException();
        }

        synchronized (project) {
            // save the project to file and remove it from cache
            if (project.getState() != IDLE) {
                return false;
            }
            if (!project.setSquid(null) || !project.saveNow()) {
                return false;
            }
            projectCache.remove(project.getFile());

            // clear the project's data stuctures to prevent further use
            project.autosaveQueue = null;
            project.autosaveRunnable = null;
            project.currentStep = null;
            project.file = null;
//          project.listenerList = null; <-- this could cause NullPointerExceptions in ProjectComponent.setProject()
            project.properties = null;
            project.sampleType = null;
            project.sequence = null;
            project.state = null;
            project.transform = null;
            project.type = null;
        }
        return true;
    }

    /**
     * Returns the type of a project file. Reads the type of the project from the specified file quickly, without fully
     * loading the Project. The first request for each file reads from the file system, but after that the results are
     * cached for an unspecified time.
     *
     * @param file the path of the project file.
     * @return the type of the project, or null if the file was not a project file or it was not possible to read it.
     * @throws NullPointerException if file is null.
     */
    public static Type getType(File file) {
        if (file == null) {
            throw new NullPointerException();
        }

        // check the cache
        Object value = projectTypeCache.get(file);
        if (value != null) {
            if (value instanceof Type) {
                return (Type) value;
            } else {
                return null;
            }
        }

        Type type = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // check that it is a XML file
            String line = reader.readLine();
            if (line.indexOf("<?xml") < 0) {
                return null;
            }

            // the second line of the file should be something like:
            // <project type="TYPE" version="1.0">
            line = reader.readLine();
            int start = line.indexOf("<project type=\"");
            if (start < 0) {
                return null;
            }
            start += 15;
            int end = line.indexOf("\"", start);
            if (end < 0) {
                return null;
            }
            type = Type.valueOf(line.substring(start, end));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // save the results to cache
        if (type != null) {
            projectTypeCache.put(file, type);
        } else {
            projectTypeCache.put(file, new Object());
        }
        return type;
    }

    /**
     * Creates a new project of the specified type. This constructor will not write to file, so the user of this method
     * should call the saveNow() method after the project is initialized.
     *
     * @param file path for this project file. The file should exist (may be empty) and be writable, but this
     *             constructor will not check it.
     * @param type type of the project.
     * @throws NullPointerException if any of the parameters is null.
     */
    private Project(File file, Type type) {
        if (file == null || type == null) {
            throw new NullPointerException();
        }
        this.file = file;
        this.type = type;
        updateTransforms();
    }

    /**
     * Creates a new project from the specified document. This constructor will assume that the specified file is the
     * same from which the document was read.
     *
     * @param file     path for this project file. The file should be the same from which document was read and be
     *                 writable, but this constructor will not check it.
     * @param document the document from which this project will be created.
     * @throws NullPointerException     if any of the parameters is null.
     * @throws IllegalArgumentException if the document was not in the right format.
     */
    private Project(File file, Document document) {
        if (file == null || document == null) {
            throw new NullPointerException();
        }
        this.file = file;
        String s = null;

        // verify project file's version
        Element root = document.getDocumentElement();
        if (!root.getTagName().equals("project")) {
            throw new IllegalArgumentException("Invalid tag name: " + root.getTagName());
        }
        String version = root.getAttribute("version");
        if (version.equals("1.0")) {

            // get type
            s = root.getAttribute("type");
            try {
                type = Type.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown project type: " + s);
            }

            // get properties element
            NodeList propertiesList = root.getElementsByTagName("properties");
            if (propertiesList.getLength() != 1) {
                throw new IllegalArgumentException("One properties required, found " + propertiesList.getLength());
            }
            Element properties = (Element) propertiesList.item(0);

            // get default properties
            s = properties.getAttribute("strike");
            try {
                strike = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid strike: " + s);
            }
            s = properties.getAttribute("dip");
            try {
                dip = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid dip: " + s);
            }
            s = properties.getAttribute("mass");
            try {
                mass = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid mass: " + s);
            }
            s = properties.getAttribute("volume");
            try {
                volume = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid volume: " + s);
            }
            s = properties.getAttribute("sampletype");
            try {
                sampleType = SampleType.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sampletype: " + s);
            }
            s = properties.getAttribute("orientation");
            orientation = s.equals("1") ? true : false;

            // get custom properties
            NodeList propertyList = properties.getElementsByTagName("property");
            for (int i = 0; i < propertyList.getLength(); i++) {
                Element property = (Element) propertyList.item(i);
                this.properties.put(property.getAttribute("key"), property.getAttribute("value"));
            }

            // get sequence
            NodeList sequenceList = root.getElementsByTagName("sequence");
            if (sequenceList.getLength() != 1) {
                throw new IllegalArgumentException("One sequence required, found " + sequenceList.getLength());
            }
            updateTransforms();     // transforms must be updated before running MeasurementSequence's constructor
            sequence = new MeasurementSequence((Element) sequenceList.item(0), this);

//      } else if (version.equals("x.y")) {
//          ... importing of file version x.y ...
        } else {
            throw new IllegalArgumentException("Unknown version: " + version);
        }
    }

    /**
     * Exports this project to a DOM document.
     *
     * @return the exported document, or null if there was a error.
     */
    public synchronized Document getDocument() {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            return null;
        }

        // create document's root element
        Element root = document.createElement("project");
        root.setAttribute("version", "1.0");
        root.setAttribute("type", type.name());

        // create default properties
        Element properties = document.createElement("properties");
        properties.setAttribute("strike", Double.toString(strike));
        properties.setAttribute("dip", Double.toString(dip));
        properties.setAttribute("mass", Double.toString(mass));
        properties.setAttribute("volume", Double.toString(volume));
        properties.setAttribute("sampletype", sampleType.name());
        properties.setAttribute("orientation", orientation ? "1" : "0");

        // create custom properties
        Set<Map.Entry<Object, Object>> entries = this.properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            Element property = document.createElement("property");
            property.setAttribute("key", entry.getKey().toString());
            property.setAttribute("value", entry.getValue().toString());
            properties.appendChild(property);
        }

        // create sequence
        Element sequence = this.sequence.getElement(document);

        // put all together
        root.appendChild(properties);
        root.appendChild(sequence);
        document.appendChild(root);
        return document;
    }

    /**
     * Invokes autosaving. This method will schedule a saving operation and return. After this method has not been
     * called for a short while, the project will be written to file.
     */
    public synchronized void save() {
        autosaveQueue.execute(autosaveRunnable);
    }

    /**
     * Writes this project to its project file and waits for the operation to complete. Clears any delaying autosave
     * operations.
     *
     * @return true if the file was successfully written, otherwise false.
     */
    public boolean saveNow() {
        File file;
        Document document;
        synchronized (this) {
            this.autosaveQueue.clear();     // clear any delaying autosave operations
            file = this.getFile();
            document = this.getDocument();
        }
        if (DocumentUtilities.storeToXML(file, document)) {
            fireProjectEvent(FILE_SAVED);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes the project to a file in DAT format.
     *
     * @param file the file to save to.
     * @return true if the file was successfully written, otherwise false.
     * @throws NullPointerException if file is null.
     */
    public boolean exportToDAT(File file) {
        return false; // TODO
    }

    /**
     * Writes the project to a file in SRM format.
     *
     * @param file the file to save to.
     * @return true if the file was successfully written, otherwise false.
     * @throws NullPointerException if file is null.
     */
    public boolean exportToSRM(File file) {
        return false; // TODO
    }

    /**
     * Writes the project to a file in TDT format.
     *
     * @param file the file to save to.
     * @return true if the file was successfully written, otherwise false.
     * @throws NullPointerException if file is null.
     */
    public boolean exportToTDT(File file) {
        return false; // TODO
    }

    /**
     * Returns the project file of this project.
     */
    public synchronized File getFile() {
        return file;
    }

    /**
     * Returns the type of this project.
     */
    public synchronized Type getType() {
        return type;
    }

    /**
     * Returns the current measurement state of this project.
     */
    public synchronized State getState() {
        return state;
    }

    /**
     * Returns the name of this project. The name is equal to the name of the project file without the file extension.
     */
    public synchronized String getName() {
        String name = getFile().getName();
        if (name.endsWith(Ikayaki.FILE_TYPE)) {
            name = name.substring(0, name.length() - Ikayaki.FILE_TYPE.length());
        }
        return name;
    }

    /**
     * Returns the timestamp of the last completed measurement. This is usually less than the last modified date of the
     * file, because this is not affected by changing the project’s properties.
     *
     * @return the timestamp of the last measurement, or null if no measurements are completed.
     */
    public synchronized Date getTimestamp() {
        Date last = null;
        for (int i = 0; i < sequence.getSteps(); i++) {
            Date d = sequence.getStep(i).getTimestamp();
            if (d != null && (last == null || d.after(last))) {
                last = d;
            }
        }
        return last;
    }

    /**
     * Returns the Squid if this project is its owner, otherwise returns null.
     * <p/>
     * (NOTE: Make this method public? Or return a Proxy (see design patterns), so others can know where the handler is
     * moving but not control it?)
     */
    private synchronized Squid getSquid() {
        return squid;
    }

    /**
     * Sets this project the owner of the Squid. Tries to detach the previous owner of the squid. Uses the setOwner()
     * method of the specified Squid.
     * <p/>
     * Only one project may own the Squid at a time. The Squid must be first detached with "setSquid(null)" from its
     * owner before it can be given to another project. Detaching the Squid is possible only when the project’s state is
     * IDLE.
     *
     * @param squid pointer to the SQUID interface, or null to detach this project from it.
     * @return true if the operation was completed, false if the Squid has another owner or a measurement is running (in
     *         which case nothing was changed).
     */
    public synchronized boolean setSquid(Squid squid) {
        // detach the squid from this project
        if (squid == null) {
            if (getSquid() == null) {
                return true;        // already detached
            }
            if (getState() == IDLE && getSquid().setOwner(null)) {
                this.squid = null;
                fireProjectEvent(STATE_CHANGED);
                return true;
            }
            return false;       // a measurement is running - can not detach
        }

        // attach the squid to this project
        synchronized (squid) {
            if (squid.getOwner() == this) {
                return true;        // already attached
            }
            if (squid.getOwner() != null) {
                squid.getOwner().setSquid(null);        // try to detach from the old project
            }
            if (squid.getOwner() == null && squid.setOwner(this)) {
                this.squid = squid;
                fireProjectEvent(STATE_CHANGED);
                return true;
            }
            return false;       // the old project has a measurement running - can not attach to this one
        }
    }

    /**
     * Returns a project information property.
     *
     * @param key the key which is associated with the property.
     * @return the specified property, or null if the property is not set.
     */
    public synchronized String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Returns a project information property.
     *
     * @param key          the key which is associated with the property.
     * @param defaultValue a default value
     * @return the specified property, or defaultValue if the property is not set.
     */
    public synchronized String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Sets a project information property.
     *
     * @param key   the key which is associated with the property.
     * @param value new value for the property, or null to remove the property.
     */
    public synchronized void setProperty(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    /**
     * Returns the strike of the sample.
     */
    public synchronized double getStrike() {
        return strike;
    }

    /**
     * Sets the strike of the sample and calls updateTransforms().
     */
    public synchronized void setStrike(double strike) {
        this.strike = strike;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the dip of the sample.
     */
    public synchronized double getDip() {
        return dip;
    }

    /**
     * Sets the dip of the sample and calls updateTransforms().
     */
    public synchronized void setDip(double dip) {
        this.dip = dip;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the type of the sample.
     */
    public synchronized SampleType getSampleType() {
        return sampleType;
    }

    /**
     * Sets the type of the sample and calls updateTransforms().
     *
     * @throws NullPointerException if sampleType is null.
     */
    public synchronized void setSampleType(SampleType sampleType) {
        if (sampleType == null) {
            throw new NullPointerException();
        }
        this.sampleType = sampleType;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the orientation of the sample.
     *
     * @return true if the sample orientation is +Z, or false if it is -Z.
     */
    public synchronized boolean getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of the sample and calls updateTransforms().
     *
     * @param orientation true if the sample orientation is +Z, or false if it is -Z.
     */
    public synchronized void setOrientation(boolean orientation) {
        this.orientation = orientation;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the current transformation matrix for the sample. For performance reasons, this method returns a
     * reference to the internal data structure and not a copy of it.
     * <p/>
     * WARNING!!! Absolutely NO modification of the data contained in this matrix should be made – if any such
     * manipulation is necessary, it should be done on a copy of the matrix returned rather than the matrix itself.
     *
     * @return reference to the transformation matrix.
     */
    synchronized Matrix3d getTransform() {
        return transform;
    }

    /**
     * Recalculates the transformation matrix and updates all measurements. This method is called automatically by the
     * setStrike(), setDip() and setSampleType() methods.
     */
    private synchronized void updateTransforms() {
        double d = getDip();
        double s = getStrike();
        if (sampleType == CORE) {
            // core sample: sample -> geographic
            transform.setRow(0, sin(d) * cos(s), sin(s), cos(s) * cos(d));
            transform.setRow(1, sin(s) * sin(d), cos(s), cos(d) * sin(s));
            transform.setRow(2, -cos(d), 0, sin(d));
        } else if (sampleType == HAND) {
            // hand sample: sample -> geographic
            transform.setRow(0, cos(s), -sin(s) * cos(d), sin(s) * sin(d));
            transform.setRow(1, sin(s), cos(s) * cos(d), -sin(d) * cos(s));
            transform.setRow(2, 0, sin(d), cos(d));
        } else {
            assert false;
        }
        if (!orientation) {
            // -Z position -> +Z position
            /*
             *  transform multipied by
             *   [[ 1  0  0 ]
             *    [ 0 -1  0 ]
             *    [ 0  0 -1 ]]
             */
            transform.setColumn(1, -transform.m01, -transform.m11, -transform.m21);
            transform.setColumn(2, -transform.m02, -transform.m12, -transform.m22);
        }
        for (int i = 0; i < sequence.getSteps(); i++) {
            sequence.getStep(i).updateTransforms();
        }
    }

    /**
     * Returns the mass of the sample.
     *
     * @return mass of the sample, or a negative number if no mass is specified.
     */
    public synchronized double getMass() {
        return mass;
    }

    /**
     * Sets the mass of the sample.
     *
     * @param mass mass of the sample, or a negative number to clear it.
     */
    public synchronized void setMass(double mass) {
        if (mass < 0.0) {
            mass = -1.0;
        }
        this.mass = mass;
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the volume of the sample.
     *
     * @return volume of the sample, or a negative number if no volume is specified.
     */
    public synchronized double getVolume() {
        return volume;
    }

    /**
     * Sets the volume of the sample.
     *
     * @param volume volume of the sample, or a negative number to clear it.
     */
    public synchronized void setVolume(double volume) {
        if (volume < 0.0) {
            volume = -1.0;
        }
        this.volume = volume;
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Adds a ProjectListener to the project.
     *
     * @param l the listener to be added.
     */
    public synchronized void addProjectListener(ProjectListener l) {
        listenerList.add(ProjectListener.class, l);
    }

    /**
     * Removes a ProjectListener from the project.
     *
     * @param l the listener to be removed
     */
    public synchronized void removeProjectListener(ProjectListener l) {
        listenerList.remove(ProjectListener.class, l);
    }

    /**
     * Notifies all listeners that have registered for ProjectEvents.
     *
     * @param type type of the event.
     */
    private synchronized void fireProjectEvent(ProjectEvent.Type type) {
        final ProjectEvent event = new ProjectEvent(this, type);
        final ProjectListener[] listeners = listenerList.getListeners(ProjectListener.class);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (ProjectListener l : listeners) {
                    try {
                        l.projectUpdated(event);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Adds a MeasurementListener to the project.
     *
     * @param l the listener to be added.
     */
    public synchronized void addMeasurementListener(MeasurementListener l) {
        listenerList.add(MeasurementListener.class, l);
    }

    /**
     * Removes a MeasurementListener from the project.
     *
     * @param l the listener to be removed
     */
    public synchronized void removeMeasurementListener(MeasurementListener l) {
        listenerList.remove(MeasurementListener.class, l);
    }

    /**
     * Notifies all listeners that have registered for MeasurementEvents.
     *
     * @param step the measurement step that has generated the event.
     * @param type the type of the event.
     */
    private synchronized void fireMeasurementEvent(MeasurementStep step, MeasurementEvent.Type type) {
        final MeasurementEvent event = new MeasurementEvent(this, step, type);
        final MeasurementListener[] listeners = listenerList.getListeners(MeasurementListener.class);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (MeasurementListener l : listeners) {
                    try {
                        l.measurementUpdated(event);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Appends a sequence to this project’s sequence. Only the stepValues will be copied from the specified sequence and
     * added as new steps to this project.
     * <p/>
     * If isSequenceEditEnabled() is false, nothing will be done.
     *
     * @param append the measurement sequence to be appended.
     * @return true if the steps were added, or false if isSequenceEditEnabled() was false.
     * @throws NullPointerException if sequence is null.
     */
    public synchronized boolean addSequence(MeasurementSequence append) {
        if (append == null) {
            throw new NullPointerException();
        }
        if (!isSequenceEditEnabled()) {
            return false;
        }
        for (int i = 0; i < append.getSteps(); i++) {
            MeasurementStep step = new MeasurementStep(this);
            step.setStepValue(append.getStep(i).getStepValue());
            sequence.addStep(step);
        }
        fireProjectEvent(DATA_CHANGED);
        save();
        return true;
    }

    /**
     * Returns a copy of this project’s sequence. Only the stepValues will be copied from this project’s sequence. The
     * returned sequence will have no name.
     *
     * @param start index of the first step in the sequence.
     * @param end   index of the last step in the sequence. If end < start, then an empty sequence will be returned.
     * @return copy of the sequence with only stepValues and no results.
     * @throws IndexOutOfBoundsException if the index is out of range (start < 0 || end >= getSteps()).
     */
    public synchronized MeasurementSequence copySequence(int start, int end) {
        if (start < 0 || end >= getSteps()) {
            throw new IndexOutOfBoundsException();
        }
        MeasurementSequence copy = new MeasurementSequence();
        for (int i = start; i <= end; i++) {
            MeasurementStep step = new MeasurementStep();
            step.setStepValue(sequence.getStep(i).getStepValue());
            copy.addStep(step);
        }
        return copy;
    }

    /**
     * Appends a step to this project’s sequence. Only the stepValue will be copied from the specified step and added as
     * a new step to this project.
     * <p/>
     * If isSequenceEditEnabled() is false, nothing will be done.
     *
     * @param step the measurement step to be added.
     * @return true if the step was added, or false if isSequenceEditEnabled() was false.
     * @throws NullPointerException if step is null.
     */
    public synchronized boolean addStep(MeasurementStep step) {
        if (step == null) {
            throw new NullPointerException();
        }
        if (!isSequenceEditEnabled()) {
            return false;
        }
        double stepValue = step.getStepValue();
        step = new MeasurementStep(this);
        step.setStepValue(stepValue);
        sequence.addStep(step);

        fireProjectEvent(DATA_CHANGED);
        save();
        return true;
    }

    /**
     * Adds a step to the specified index of this project’s sequence. Only the stepValue will be copied from the
     * specified step and added as a new step to this project.
     * <p/>
     * The index must be such, that the indices of the completed measurements will not change.
     * <p/>
     * If isSequenceEditEnabled() is false, nothing will be done.
     *
     * @param index the index to which the step will be added.
     * @param step  the measurement step to be added.
     * @return true if the step was added, or false if isSequenceEditEnabled() was false.
     * @throws IndexOutOfBoundsException if the index is out of range (index < getCompletedSteps() || index >
     *                                   getSteps()).
     * @throws NullPointerException      if step is null.
     */
    public synchronized boolean addStep(int index, MeasurementStep step) {
        if (step == null) {
            throw new NullPointerException();
        }
        if (index < getCompletedSteps() || index > getSteps()) {
            throw new IndexOutOfBoundsException();
        }
        if (!isSequenceEditEnabled()) {
            return false;
        }
        double stepValue = step.getStepValue();
        step = new MeasurementStep(this);
        step.setStepValue(stepValue);
        sequence.addStep(index, step);

        fireProjectEvent(DATA_CHANGED);
        save();
        return true;
    }

    /**
     * Removes a step from this project’s sequence. Completed measurements can not be removed.
     * <p/>
     * If isSequenceEditEnabled() is false, nothing will be done.
     *
     * @param index the index of the step to be removed.
     * @return true if the step was removed, or false if isSequenceEditEnabled() was false.
     * @throws IndexOutOfBoundsException if the index is out of range (index < getCompletedSteps() || index >=
     *                                   getSteps()).
     */
    public synchronized boolean removeStep(int index) {
        if (index < getCompletedSteps() || index >= getSteps()) {
            throw new IndexOutOfBoundsException();
        }
        if (!isSequenceEditEnabled()) {
            return false;
        }
        sequence.removeStep(index);

        fireProjectEvent(DATA_CHANGED);
        save();
        return true;
    }

    /**
     * Removes a series of steps from this project’s sequence. Completed measurements can not be removed.
     * <p/>
     * If isSequenceEditEnabled() is false, nothing will be done.
     *
     * @param start the first index to be removed.
     * @param end   the last index to be removed. If end < start, no steps will be removed.
     * @return true if the steps were removed, or false if isSequenceEditEnabled() was false.
     * @throws IndexOutOfBoundsException if the index is out of range (start < getCompletedSteps() || end >=
     *                                   getSteps()).
     */
    public synchronized boolean removeStep(int start, int end) {
        if (start < getCompletedSteps() || end >= getSteps()) {
            throw new IndexOutOfBoundsException();
        }
        if (!isSequenceEditEnabled()) {
            return false;
        }
        for (int i = end; i >= start; i--) {
            sequence.removeStep(i);
        }
        fireProjectEvent(DATA_CHANGED);
        save();
        return true;
    }

    /**
     * Returns the number of steps in this project.
     */
    public synchronized int getSteps() {
        return sequence.getSteps();
    }

    /**
     * Returns the number of completed steps in this project. Steps that are currently being measured, are included in
     * this count. Completed steps are always first in the sequence.
     */
    public synchronized int getCompletedSteps() {
        int i;
        for (i = 0; i < sequence.getSteps(); i++) {
            MeasurementStep.State state = sequence.getStep(i).getState();
            if (state == DONE || state == DONE_RECENTLY) {
                continue;
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * Returns a step from the sequence.
     *
     * @param index the index of the step.
     * @return the specified step.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized MeasurementStep getStep(int index) {
        return sequence.getStep(index);
    }

    /**
     * Returns the step that is currently being measured.
     *
     * @return the currently measured step, or null if no measurement is active.
     */
    public synchronized MeasurementStep getCurrentStep() {
        return currentStep;
    }

    /**
     * Calculates and returns a value from a measurement step. The specified MeasurementValue’s algorithm will be used
     * and the results returned.
     *
     * @param index     the measurement step from which the value is calculated.
     * @param algorithm the algorithm for calculating the desired value.
     * @return the value returned by the algorithm, or null if it was not possible to calculate it.
     * @throws NullPointerException      if algorithm is null.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized <A> A getValue(int index, MeasurementValue<A> algorithm) {
        return algorithm.getValue(sequence.getStep(index));
    }

    /**
     * Tells whether it is allowed to use the degausser in this project. The returned value depends on the type and
     * state of this project.
     */
    public synchronized boolean isDegaussingEnabled() {
        if (type == CALIBRATION || type == THELLIER || type == THERMAL) {
            return false;
        } else if (type == AF) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is allowed to edit the sequence. The returned value depends on the type and state of this
     * project.
     */
    public synchronized boolean isSequenceEditEnabled() {
        if (type == CALIBRATION) {
            return false;
        } else if (type == AF || type == THELLIER || type == THERMAL) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is allowed to control the Squid manually. The returned value depends on the type and state of
     * this project.
     */
    public synchronized boolean isManualControlEnabled() {
        if (type == CALIBRATION) {
            return false;
        } else if (type == AF || type == THELLIER || type == THERMAL) {
            if (state == IDLE) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is allowed to do an auto step measurement. The returned value depends on the type and state of
     * this project.
     */
    public synchronized boolean isAutoStepEnabled() {
        if (type == CALIBRATION || type == THELLIER || type == THERMAL) {
            return false;
        } else if (type == AF) {
            if (state == IDLE) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is allowed to do a single step measurement. The returned value depends on the type and state of
     * this project.
     */
    public synchronized boolean isSingleStepEnabled() {
        if (type == CALIBRATION || type == AF || type == THELLIER || type == THERMAL) {
            if (state == IDLE) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is possible to pause the measurement. The returned value depends on the type and state of this
     * project.
     */
    public synchronized boolean isPauseEnabled() {
        if (type == CALIBRATION || type == THELLIER || type == THERMAL) {
            return false;
        } else if (type == AF) {
            if (state == MEASURING) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is possible to abort the measurement. The returned value depends on the type and state of this
     * project.
     */
    public synchronized boolean isAbortEnabled() {
        if (state == MEASURING || state == PAUSED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts an auto step measurement. If isAutoStepEnabled() is false but is isSingleStepEnabled() is true, will start
     * a single step measurement. Will do nothing if both are false.
     * <p/>
     * The measurement will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the measurement was started, otherwise false.
     */
    public synchronized boolean doAutoStep() {
        if (state == IDLE) {
            if (isAutoStepEnabled()) {
                state = MEASURING;
            } else if (isSingleStepEnabled()) {
                state = PAUSED;
            } else {
                return false;
            }

            new Thread() {
                @Override public void run() {
                    for (int i = getCompletedSteps(); i < getSteps(); i++) {
                        System.out.println("Measuring step " + i + "...");
                        try {
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.BG,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                getStep(i).setDone();
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.DEG0,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                getStep(i).setDone();
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.DEG90,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                getStep(i).setDone();
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.DEG180,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                getStep(i).setDone();
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.DEG270,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            if (state == ABORTED) {
                                state = IDLE;
                                getStep(i).setDone();
                                return;
                            }
                            getStep(i).addResult(new MeasurementResult(MeasurementResult.Type.BG,
                                    Math.random(), Math.random(), Math.random()));
                            Thread.sleep(500);
                            getStep(i).setDone();
                        } catch (InterruptedException e) {
                        }

                        if (state == PAUSED) {
                            state = IDLE;
                            return;
                        }

                        // TODO
                    }
                }
            }.start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts a single step measurement. Will do nothing if isSingleStepEnabled() is false.
     * <p/>
     * The measurement will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the measurement was started, otherwise false.
     */
    public synchronized boolean doSingleStep() {
        if (!isSingleStepEnabled()) {
            return false;
        }
        if (doAutoStep()) {
            return doPause();
        } else {
            return false;
        }
    }

    /**
     * Pauses the currently running measurement. A paused measurement will halt after it finishes the current
     * measurement step. Will do nothing if isPauseEnabled() is false.
     * <p/>
     * This method will notify the measurement thread to pause, but will not wait for it to finish.
     *
     * @return true if the measurement will pause, otherwise false.
     */
    public synchronized boolean doPause() {
        if (!isPauseEnabled()) {
            return false;
        }
        if (state == IDLE) {
            return false;
        } else if (state == MEASURING) {
            state = PAUSED;
            return true;
        } else if (state == PAUSED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Aborts the currently running measurement. An aborted measurement will halt immediately and leave the handler
     * where it was (enables manual control). Will do nothing if isAbortEnabled() is false.
     * <p/>
     * This method will notify the measurement thread to abort, but will not wait for it to finish.
     *
     * @return true if the measurement will abort, otherwise false.
     */
    public synchronized boolean doAbort() {
        if (!isAbortEnabled()) {
            return false;
        }
        if (state == IDLE) {
            return false;
        } else {
            state = ABORTED;
            return true;
        }
    }

    /**
     * Moves the sample handler to the specified position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @param position the position to move the handler to.
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMove(int position) {
        return false; // TODO
    }

    /**
     * Rotates the sample handler to the specified angle. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @param angle the angle to rotate the handler to.
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualRotate(int angle) {
        return false; // TODO
    }

    /**
     * Measures the X, Y and Z of the sample. Adds the results as a new measurement step to the project. Will do nothing
     * if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMeasure() {
        return false; // TODO
    }

    /**
     * Demagnetizes the sample in Z direction with the specified amplitude. Will do nothing if isManualControlEnabled()
     * is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @param amplitude the amplitude to demagnetize in mT.
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualDemagZ(double amplitude) {
        return false; // TODO
    }

    /**
     * Demagnetizes the sample in Y direction with the specified amplitude. Will do nothing if isManualControlEnabled()
     * is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @param amplitude the amplitude to demagnetize in mT.
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualDemagY(double amplitude) {
        return false; // TODO
    }

    /**
     * The type of the project.
     */
    public enum Type {
        CALIBRATION("Calibration"), AF("AF"), THELLIER("Thellier"), THERMAL("Thermal");

        private String name;

        private Type(String name) {
            this.name = name;
        }

        @Override public String toString() {
            return name;
        }
    }

    /**
     * The state of the project’s measurements.
     */
    public enum State {
        IDLE, MEASURING, PAUSED, ABORTED
    }

    /**
     * The type of a measured sample.
     */
    public enum SampleType {
        CORE, HAND
    }

    public static void main(String[] args) {
        File file = new File("test.ika");
        file.delete();

        Project p;
        p = Project.createThermalProject(file);
        System.out.println("created");
        System.out.println("load from cache: " + (p == Project.loadProject(file)));

        p.setProperty("testProperty", "hulabaloo1");
        p.setProperty("testProperty", "hulabaloo2");
        p.setProperty("testProperty3", "hulabaloo3");

        System.out.println("begin close");
        Project.closeProject(p);
        System.out.println("end close");

        System.out.println("load from cache: " + (p == Project.loadProject(file)));

        p = Project.loadProject(file);
        p.saveNow();
    }
}