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

import ikayaki.gui.Ikayaki;
import ikayaki.squid.Squid;
import ikayaki.util.LastExecutor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

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
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(file);
            project = new Project(file, document);
            projectCache.put(file, project);
        } catch (SAXException e) {
            return null;
        } catch (ParserConfigurationException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (IllegalArgumentException e) {
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
            project.listenerList = null;
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

        // TODO: load all Project data from the document

        updateTransforms();
    }

    /**
     * Exports this project to a DOM document.
     * 
     * @return the exported document, or null if there was a error.
     */
    public synchronized Document getDocument() {
        return null; // TODO
    }

    /**
     * Invokes autosaving. This method will schedule a saving operation and return. After this method has not been
     * called for a short while, the project will be written to file.
     */
    public synchronized void save() {
        autosaveQueue.execute(autosaveRunnable);
    }

    /**
     * Writes this project to its project file and waits for the operation to complete.
     *
     * @return true if the file was successfully written, otherwise false.
     */
    public boolean saveNow() {
        try {
            FileOutputStream out;
            Document document;
            synchronized (this) {
                out = new FileOutputStream(this.getFile());
                document = this.getDocument();
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));

            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new OutputStreamWriter(out, "utf-8"));
            t.transform(source, result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        fireProjectEvent(FILE_SAVED);
        return true;
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
     * Sets this project the owner of the Squid. Uses the setOwner() method of the specified Squid.
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
                return true;
            }
            if (getState() == IDLE && getSquid().setOwner(null)) {
                this.squid = null;
                fireProjectEvent(STATE_CHANGED);
                return true;
            }
            return false;
        }

        // attach the squid to this project
        synchronized (squid) {
            if (squid.getOwner() == this) {
                return true;
            }
            if (squid.getOwner() == null && squid.setOwner(this)) {
                this.squid = squid;
                fireProjectEvent(STATE_CHANGED);
                return true;
            }
            return false;
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
        return; // TODO
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
     * Starts an auto step measurement. Will do nothing if isAutoStepEnabled() is false.
     * <p/>
     * The measurement will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the measurement was started, otherwise false.
     */
    public synchronized boolean doAutoStep() {
        return false; // TODO
    }

    /**
     * Starts a single step measurement. Will do nothing if isSingleStepEnabled() is false.
     * <p/>
     * The measurement will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the measurement was started, otherwise false.
     */
    public synchronized boolean doSingleStep() {
        return false; // TODO
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
        return false; // TODO
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
        return false; // TODO
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
        CALIBRATION, AF, THELLIER, THERMAL
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
}