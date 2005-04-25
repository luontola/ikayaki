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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static ikayaki.MeasurementStep.State.READY;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static ikayaki.MeasurementEvent.Type.*;
import static ikayaki.MeasurementResult.Type.*;
import static ikayaki.Project.Normalization.*;
import static ikayaki.Project.Orientation.*;
import static ikayaki.Project.State.*;
import static ikayaki.Project.SampleType.*;
import static ikayaki.Project.Type.*;
import static ikayaki.ProjectEvent.Type.*;

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

    private static final boolean DEBUG = false;      // TODO: used for testing the measurements without a Squid

    /* Property names for saving values to Project */
    public static final String MEASUREMENT_TYPE_PROPERTY = "measurementType";
    public static final String MEASUREMENT_TYPE_AUTO_VALUE = "AUTO";
    public static final String MEASUREMENT_TYPE_MANUAL_VALUE = "MANUAL";
    public static final String OPERATOR_PROPERTY = "operator";
    public static final String DATE_PROPERTY = "date";
    public static final String ROCK_TYPE_PROPERTY = "rockType";
    public static final String LOCATION_PROPERTY = "location";
    public static final String SITE_PROPERTY = "site";
    public static final String COMMENT_PROPERTY = "comment";
    public static final String LATITUDE_PROPERTY = "latitude";
    public static final String LONGITUDE_PROPERTY = "longitude";

    /**
     * Caches the created and loaded Project objects to make sure that no more than one object will be created for each
     * physical file.
     */
    private static final Hashtable<File, Project> projectCache = new Hashtable<File, Project>();

    /**
     * Caches the types of the project files, as read by getType(Project). The value is a Type for valid project files,
     * or an Object for invalid or unknown files.
     */
    private static final Hashtable<File, Object> projectTypeCache = new Hashtable<File, Object>();

    /**
     * Location of the project file in the local file system. Autosaving will save the project to this file.
     */
    private final File file;
    /**
     * Type of the measurement project. This will affect which features of the project are enabled and disabled.
     */
    private final Type type;

    /**
     * Current state of the measurements. If no measurement is running, then state is IDLE. Only one measurement may be
     * running at a time.
     */
    private State state = IDLE;

    /**
     * Tells if this project been closed with closeProject().
     */
    private boolean closed = false;

    /**
     * Pointer to the SQUID device interface, or null if this project is not its owner.
     */
    private Squid squid = null;

    /**
     * Custom properties of this project stored in a map. The project is not interested in what properties are stored;
     * it only saves them.
     */
    private final Properties properties = new Properties();

    /**
     * Measurement sequence of this project. In the beginning are all completed measurement steps, and in the end are
     * planned measurement steps. Completed measurements may NOT be deleted.
     */
    private MeasurementSequence sequence = new MeasurementSequence();   // this instance will be dumped by the constructor Project(File,Document)

    /**
     * Strike of the sample. Will be used to create the transform matrix. The unit is degrees (0 to 360).
     */
    private double strike = 0.0;

    /**
     * Dip of the sample. Will be used to create the transform matrix. The unit is degrees (-90 to 90).
     */
    private double dip = 0.0;

    /**
     * Type of the sample. Will be used to create the transform matrix.
     */
    private SampleType sampleType = HAND;

    /**
     * Orientation of the sample. Will be used to create the transform matrix.
     */
    private Orientation orientation = MINUS_Z;

    /**
     * The type of normalization to use.
     */
    private Normalization normalization = VOLUME;

    /**
     * Matrix for correcting the sample's orientation. The matrix will be updated whenever the strike, dip, sampleType
     * or orientation is changed. After that the updated matrix will be applied to all measurements.
     */
    private Matrix3d transform = new Matrix3d();

    /**
     * Mass of the sample, or a negative value if no mass is defined. The unit is gram.
     */
    private double mass = -1.0;

    /**
     * Volume of the sample, or a negative value if no volume is defined. The unit is cm^3.
     */
    private double volume = -1.0;

    /**
     * Susceptibility of the sample, or a negative value if no susceptibility is defined. Susceptibility has no unit.
     */
    private double susceptibility = -1.0;

    /**
     * Current measurement step, or null if no measurement is running.
     */
    private MeasurementStep currentStep = null;

    /**
     * Listeners for this project.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * true if the project has been modified, otherwise false.
     */
    private boolean modified = false;

    /**
     * Scheduler for automatically writing the modified project to file after a short delay.
     */
    private final LastExecutor autosaveQueue = new LastExecutor(500, true);

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
    public static synchronized Project createProject(File file, Type type) {
        if (file == null || type == null) {
            throw new NullPointerException();
        }

        // must use only absolute file paths. otherwise the cache could include the same file twise.
        if (!file.isAbsolute()) {
            file = file.getAbsoluteFile();
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
        projectTypeCache.put(file, project.getType());
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

        // must use only absolute file paths. otherwise the cache could include the same file twise.
        if (!file.isAbsolute()) {
            file = file.getAbsoluteFile();
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
            projectTypeCache.put(file, project.getType());

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return project;
    }

    /**
     * Ensures that the project file is saved and frees the resources taken by the project. The closed project will
     * automatically detach itself from the Squid. The closed project is removed from the projectCache.
     * <p/>
     * A project should not be used after it has been closed – any further use of the object is undefined (will create
     * an IllegalStateException if somebody tries to modify it). A project can not be closed if it has a measurement
     * running.
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
            if (project.isClosed()) {
                new Exception("closeProject success: the project is already closed!").printStackTrace();
                return true;
            }
            if (project.getState() != IDLE) {
                System.err.println("closeProject failed: the project's state is " + project.getState());
                return false;
            }
            if (!project.setSquid(null) || !project.saveNow()) {
                System.err.println("closeProject failed: unable to detatch the squid or save the file");
                return false;
            }
            projectCache.remove(project.getFile());

            // mark the project as closed
            project.closed = true;
            project.autosaveRunnable = new Runnable() {
                public void run() {
                    assert false;
                    throw new IllegalStateException("Tried to save a closed project!");
                }
            };
        }
        return true;
    }

    /**
     * Returns an array containing all the projects that are in the project cache.
     */
    public static synchronized Project[] getCachedProjects() {
        return projectCache.values().toArray(new Project[0]);
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
        // TODO: should the cache be emptied at some point? - I suppose not.

        Type type = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            // check that it is a XML file
            String line = reader.readLine();
            if (line == null || line.indexOf("<?xml") < 0) {
                return null;
            }

            // the second line of the file should be something like:
            // <project type="TYPE" version="1.0">
            line = reader.readLine();
            if (line == null) {
                return null;
            }
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
        } finally {

            // close the file
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // save the results to cache
            if (type != null) {
                projectTypeCache.put(file, type);
            } else {
                projectTypeCache.put(file, new Object());
            }
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
        this.file = file.getAbsoluteFile();
        this.type = type;
        updateTransforms();
        modified = true;
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
        this.file = file.getAbsoluteFile();
        String s = null;

        synchronized (this) {
            try {

                // verify project file's version
                Element root = document.getDocumentElement();
                if (!root.getTagName().equals("project")) {
                    throw new IllegalArgumentException("Invalid tag name: " + root.getTagName());
                }
                String version = root.getAttribute("version");

                if (version.equals("1.0")) {

                    /* Begin importing version 1.0 */

                    // get type
                    s = root.getAttribute("type");
                    try {
                        type = Type.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown project type: " + s, e);
                    }

                    // get properties element
                    NodeList propertiesList = root.getElementsByTagName("properties");
                    if (propertiesList.getLength() != 1) {
                        throw new IllegalArgumentException(
                                "One properties required, found " + propertiesList.getLength());
                    }
                    Element properties = (Element) propertiesList.item(0);

                    // get default properties
                    s = properties.getAttribute("strike");
                    try {
                        strike = Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid strike: " + s, e);
                    }
                    s = properties.getAttribute("dip");
                    try {
                        dip = Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid dip: " + s, e);
                    }
                    s = properties.getAttribute("mass");
                    try {
                        mass = Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid mass: " + s, e);
                    }
                    s = properties.getAttribute("volume");
                    try {
                        volume = Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid volume: " + s, e);
                    }
                    s = properties.getAttribute("susceptibility");
                    try {
                        susceptibility = Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid susceptibility: " + s, e);
                    }
                    s = properties.getAttribute("sampletype");
                    try {
                        sampleType = SampleType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid sampletype: " + s, e);
                    }
                    s = properties.getAttribute("orientation");
                    try {
                        orientation = Orientation.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid orientation: " + s, e);
                    }
                    s = properties.getAttribute("normalization");
                    try {
                        normalization = Normalization.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid normalization: " + s, e);
                    }

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

                    // check from the measurement step's timestamps and states that the steps are in the right order
                    Date lastTimestamp = new Date(0);
                    MeasurementStep.State lastState = MeasurementStep.State.DONE;
                    for (int i = 0; i < sequence.getSteps(); i++) {
                        MeasurementStep step = sequence.getStep(i);
                        Date currentTimestamp = step.getTimestamp();
                        MeasurementStep.State currentState = step.getState();

                        // check the order of the timestamps
                        if (lastTimestamp != null && currentTimestamp != null
                                && currentTimestamp.before(lastTimestamp)) {
                            throw new IllegalArgumentException("The timestamp of step " + i + " is too early.");
                        }
                        if (lastTimestamp == null && currentTimestamp != null) {
                            throw new IllegalArgumentException(
                                    "The non-null timestamp of step " + i + " follows a null timestamp.");
                        }

                        // check the order of the states
                        switch (currentState) {
                        case DONE_RECENTLY:
                        case MEASURING:
                            // the state of a just opened step can not be DONE_RECENTLY or MEASURING
                            throw new IllegalArgumentException("The state of step " + i + " is " + currentState);
                        case DONE:
                            if (lastState == READY) {
                                throw new IllegalArgumentException("The state of step " + i + " is "
                                        + currentState + " after a " + READY);
                            }
                            break;
                        case READY:
                            // lastState is DONE or READY, so everything is OK
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "The step " + i + " has an unknown state: " + currentState);
                        }

                        lastTimestamp = currentTimestamp;
                        lastState = currentState;
                    }

                    /* End of importing version 1.0 */

//              } else if (version.equals("x.y")) {
//                  ... importing of file version x.y ...
                    /*
                     * FUTURE IMPLEMENTATION NOTE:
                     *
                     * It is recommended to import old versions so that the Document object is first modified to the
                     * format of the latest project file version, after which the importing of the latest version is
                     * used. This avoids the need to change the importing of older versions.
                     *
                     */
                } else {
                    throw new IllegalArgumentException("Unknown version: " + version);
                }

            } catch (RuntimeException e) {
                /*
                 * Catch and rethrow any exceptions, so that the finally block would prevent
                 * the overwriting of a project file whose loading failed.
                 */
                throw e;
            } finally {
                modified = false;   // prevent the automatic save() operations that the importing created
            }
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

        // TODO: use a DTD for the document

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
        properties.setAttribute("susceptibility", Double.toString(susceptibility));
        properties.setAttribute("sampletype", sampleType.name());
        properties.setAttribute("orientation", orientation.name());
        properties.setAttribute("normalization", normalization.name());

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
     * Tells whether the project has been modified and it needs to be saved.
     */
    public synchronized boolean isModified() {
        return modified;
    }

    /**
     * Invokes autosaving. This method will mark the project as modified and schedule a saving operation. After this
     * method has not been called for a short while, the project will be written to file.
     *
     * @throws IllegalStateException if this project has already been closed.
     */
    public synchronized void save() {
        if (isClosed()) {
            throw new IllegalStateException("The project is closed");
        }
        modified = true;
        autosaveQueue.execute(autosaveRunnable);
    }

    /**
     * Writes this project to its project file and waits for the operation to complete. Clears any delaying autosave
     * operations. Will do nothing if the project file has already been saved.
     *
     * @return true if the file has been saved, otherwise false.
     * @throws IllegalStateException if this project has already been closed.
     */
    public boolean saveNow() {
        if (isClosed()) {
            throw new IllegalStateException("The project is closed");
        }
        File file;
        Document document;
        synchronized (this) {
            // clear any delaying autosave operations
            autosaveQueue.clear();

            // do not save if this has already been saved
            if (!isModified()) {
                return true;
            }
            file = getFile();
            document = getDocument();
        }
        if (DocumentUtilities.storeToXML(file, document)) {
            modified = false;
            fireProjectEvent(FILE_SAVED);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes the project to a file in DAT format. Will overwrite the file if it already exists.
     *
     * @param file the file to save to.
     * @return true if the file was successfully written, otherwise false.
     * @throws NullPointerException if file is null.
     */
    public boolean exportToDAT(File file) {
        // TODO: exporting to DAT has priority over SRM and TDT
        PrintStream out = null;
        try {
            out = new PrintStream(file, "ISO-8859-1");

            // locales and formatters for numbers
            Locale locale = new Locale("en");
            DecimalFormat format2Frac = new DecimalFormat("###0.00", new DecimalFormatSymbols(locale));
            DecimalFormat format3Frac = new DecimalFormat("##0.000", new DecimalFormatSymbols(locale));

            // generic headers
            out.print(pad("SQUID", 11, -1));
            out.println(Ikayaki.APP_NAME + " " + Ikayaki.APP_VERSION);

            out.print(pad("Name", 10, -1));
            out.print(":");
            out.println(getName());

            out.print(pad("Rocktype", 10, -1));
            out.print(":");
            out.println(getProperty(ROCK_TYPE_PROPERTY, ""));

            out.print(pad("Site", 10, -1));
            out.print(":");
            out.println(getProperty(LOCATION_PROPERTY, "") + "/" + getProperty(SITE_PROPERTY, ""));

            out.print(pad("Rocktype", 10, -1));
            out.print(":");
            if (getSampleType() == CORE) {
                out.println("core sample");
            } else {
                out.println("hand sample");
            }

            out.print(pad("Comment", 10, -1));
            out.print(":");
            out.println(getProperty(COMMENT_PROPERTY, "").replaceAll("\\s", " "));

            // value headers
            String header = "";
            String values = "";
            double d;

            d = Double.parseDouble(getProperty(LATITUDE_PROPERTY, "0.0"));
            header += pad("Lat ", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = Double.parseDouble(getProperty(LONGITUDE_PROPERTY, "0.0"));
            header += pad("Lon ", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = getStrike();
            header += pad("Str ", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = getDip();
            header += pad("Dip ", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = 0.0;
            header += pad("Bstr", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = 0.0;
            header += pad("Bdip", 8, 1);
            values += pad(format2Frac.format(d), 8, 1);

            d = Math.min(getVolume(), 0.0);
            header += pad("Vol  ", 8, 1);
            values += pad(format3Frac.format(d), 8, 1);

            d = Math.min(getMass(), 0.0);
            header += pad("Mass ", 8, 1);
            values += pad(format3Frac.format(d), 8, 1);

            out.println(header);
            out.println(values);

            // measurement headers
            if (getType() == THELLIER || getType() == THERMAL) {
                out.print("TH");
            } else {
                out.print("AF");
            }
            out.println("      Dec    Inc       Int       Sus    T63       Xkomp      Ykomp      Zkomp");

            for (int i = 0; i < getCompletedSteps(); i++) {
                MeasurementStep step = getStep(i);
                Double dd;

                // AF/TF
                out.print(pad("" + Math.max((int) Math.round(step.getStepValue()), 0), 4, 1));

                // Dec
                dd = MeasurementValue.DECLINATION.getValue(step);
                d = dd != null ? dd : 0.0;
                out.print(pad(format2Frac.format(d), 8, 1));

                // Inc
                dd = MeasurementValue.INCLINATION.getValue(step);
                d = dd != null ? dd : 0.0;
                out.print(pad(format2Frac.format(d), 7, 1));

                // Int
                out.print(pad("", 11, 1));

                // Sus
                out.print(pad("", 9, 1));

                // T63
                out.print(pad("", 7, 1));

                // Xkomp
                out.print(pad("", 11, 1));

                // Ykomp
                out.print(pad("", 11, 1));

                // Zkomp
                out.print(pad("", 11, 1));
            }


            // exporting finished
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return false;
    }

    /**
     * Adds spaces to a string until it is the right length.
     *
     * @param s         the string to be padded.
     * @param length    the desired length for the result string.
     * @param alignment alignmet of the text. -1 for left, 0 for center and 1 for right align.
     * @return the input string appended with spaces. Its length is equal or greater to the specified length.
     */
    private static String pad(String s, int length, int alignment) {
        while (s.length() < length) {
            if (alignment < 0) {
                // left align
                s = s + " ";
            } else if (alignment > 0) {
                // right align
                s = " " + s;
            } else {
                // center
                if (s.length() % 2 == 0) {
                    s = s + " ";
                } else {
                    s = " " + s;
                }
            }
        }
        return s;
    }

    /**
     * Writes the project to a file in SRM format. Will overwrite the file if it already exists.
     *
     * @param file the file to save to.
     * @return true if the file was successfully written, otherwise false.
     * @throws NullPointerException if file is null.
     */
    public boolean exportToSRM(File file) {
        return false; // TODO
    }

    /**
     * Writes the project to a file in TDT format. Will overwrite the file if it already exists.
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
     * Returns true if this project file has been set as the Sample Holder Calibration project in the program settings.
     */
    public synchronized boolean isHolderCalibration() {
        if (getType() == CALIBRATION && getFile().equals(Settings.getHolderCalibrationFile())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the current measurement state of this project.
     *
     * @return the state of the project, or null if the project has been closed.
     */
    public synchronized State getState() {
        return state;
    }

    /**
     * Sets the state of this project. Fires state change events.
     *
     * @param state the new state to change to.
     */
    private void setState(State state) {
        this.state = state;
        fireProjectEvent(STATE_CHANGED);
    }

    /**
     * Returns true if this project has been closed with closeProject(). If it has been closed, no modifications to the
     * project will be allowed.
     */
    public boolean isClosed() {
        return closed;
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
     * file, because this is not affected by changing the project's properties.
     *
     * @return the timestamp of the last measurement, or null if no measurements are completed.
     */
    public synchronized Date getTimestamp() {
        for (int i = sequence.getSteps() - 1; i >= 0; i--) {
            Date d = sequence.getStep(i).getTimestamp();
            if (d != null) {
                return d;
            }
        }
        return null;
    }

    /**
     * Returns the Squid if this project is its owner, otherwise returns null.
     * <p/>
     * (NOTE: Is public too unsafe? Maybe return a Proxy (see design patterns), so others can know where the handler is
     * moving but not control it?)
     */
    public synchronized Squid getSquid() {
        return squid;
    }

    /**
     * Sets this project the owner of the Squid. Tries to detach the previous owner of the squid. Uses the setOwner()
     * method of the specified Squid.
     * <p/>
     * Only one project may own the Squid at a time. The Squid must be first detached with "setSquid(null)" from its
     * owner before it can be given to another project. Detaching the Squid is possible only when the project's state is
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
     * Returns the strike of the sample. The unit is degrees (0 to 360).
     */
    public synchronized double getStrike() {
        return strike;
    }

    /**
     * Sets the strike of the sample and calls updateTransforms(). The unit is degrees (0 to 360).
     */
    public synchronized void setStrike(double strike) {
        this.strike = strike;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the dip of the sample. The unit is degrees (-90 to 90).
     */
    public synchronized double getDip() {
        return dip;
    }

    /**
     * Sets the dip of the sample and calls updateTransforms(). The unit is degrees (-90 to 90).
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
     */
    public synchronized Orientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of the sample and calls updateTransforms().
     *
     * @throws NullPointerException if orientation is null.
     */
    public synchronized void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new NullPointerException();
        }
        this.orientation = orientation;
        updateTransforms();
        fireProjectEvent(DATA_CHANGED);
        save();
    }

    /**
     * Returns the normalization to be used for the measurement values.
     */
    public Normalization getNormalization() {
        return normalization;
    }

    /**
     * Sets the normalization to be used for the measurement values.
     *
     * @throws NullPointerException if normalization is null.
     */
    public void setNormalization(Normalization normalization) {
        if (normalization == null) {
            throw new NullPointerException();
        }
        this.normalization = normalization;
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
    protected synchronized Matrix3d getTransform() {
        return transform;
    }

    /**
     * Recalculates the transformation matrix and updates all measurements. This method is called automatically by the
     * setStrike(), setDip() and setSampleType() methods.
     */
    private synchronized void updateTransforms() {
        double s;
        double d;
        if (orientation == MINUS_Z) {
            s = Math.toRadians(getStrike());
            d = Math.toRadians(getDip() + 180.0);
        } else {
            s = Math.toRadians(getStrike());
            d = Math.toRadians(getDip());
        }

        if (sampleType == CORE) {
            // core sample: sample -> geographic
            transform.setRow(0, sin(d) * cos(s), -sin(s), cos(s) * cos(d));
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
//        System.out.println(transform.m00 + "\t" + transform.m01 + "\t" + transform.m02);
//        System.out.println(transform.m10 + "\t" + transform.m11 + "\t" + transform.m12);
//        System.out.println(transform.m20 + "\t" + transform.m21 + "\t" + transform.m22);
//        System.out.println();
//        if (orientation == PLUS_Z) {
//            // TODO: this method might give wrong values. check the matrices. appears that the +/-Z is not working right.
//            // +Z position -> -Z position
//
//            /*
//             *  transform multipied by
//             *   [[-1  0  0 ]
//             *    [ 0 -1  0 ]
//             *    [ 0  0  1 ]]
//             */
////            transform.setColumn(0, -transform.m00, -transform.m10, -transform.m20);
////            transform.setColumn(1, -transform.m01, -transform.m11, -transform.m21);
//
//            /*
//             *  transform multipied by
//             *   [[ 1  0  0 ]
//             *    [ 0 -1  0 ]
//             *    [ 0  0 -1 ]]
//             */
////            transform.setColumn(1, -transform.m01, -transform.m11, -transform.m21);
////            transform.setColumn(2, -transform.m02, -transform.m12, -transform.m22);
//        }
        for (int i = 0; i < sequence.getSteps(); i++) {
            sequence.getStep(i).updateTransforms();
        }
    }

    /**
     * Returns the mass of the sample. The unit is gram.
     *
     * @return mass of the sample, or a negative number if no mass is specified.
     */
    public synchronized double getMass() {
        return mass;
    }

    /**
     * Sets the mass of the sample. The unit is gram.
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
     * Returns the volume of the sample. The unit is cm^3.
     *
     * @return volume of the sample, or a negative number if no volume is specified.
     */
    public synchronized double getVolume() {
        return volume;
    }

    /**
     * Sets the volume of the sample. The unit is cm^3.
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
     * Returns the susceptibility of the sample. Susceptibility has no unit.
     *
     * @return susceptibility of the sample, or a negative number if no susceptibility is specified.
     */
    public synchronized double getSusceptibility() {
        return susceptibility;
    }

    /**
     * Sets the susceptibility of the sample. Susceptibility has no unit.
     *
     * @param susceptibility susceptibility of the sample, or a negative number to clear it.
     */
    public synchronized void setSusceptibility(double susceptibility) {
        if (susceptibility < 0.0) {
            susceptibility = -1.0;
        }
        this.susceptibility = susceptibility;
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
    protected synchronized void fireProjectEvent(ProjectEvent.Type type) {
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
    protected synchronized void fireMeasurementEvent(MeasurementStep step, MeasurementEvent.Type type) {
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
     * Appends a sequence to this project's sequence. Only the stepValues will be copied from the specified sequence and
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
     * Returns a copy of this project's sequence. Only the stepValues will be copied from this project's sequence. The
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
     * Returns a copy of this project's sequence. Only the stepValues will be copied from this project's sequence. The
     * returned sequence will have no name.
     *
     * @param indices indices of the steps to be included in the sequence. The steps will be included in the same order
     *                as their indices are listed.
     * @return copy of the sequence with only stepValues and no results.
     * @throws IndexOutOfBoundsException if any of the indices is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized MeasurementSequence copySequence(int... indices) {
        MeasurementSequence copy = new MeasurementSequence();
        for (int i : indices) {
            MeasurementStep step = new MeasurementStep();
            step.setStepValue(sequence.getStep(i).getStepValue());
            copy.addStep(step);
        }
        return copy;
    }

    /**
     * Appends a step to this project's sequence. Only the stepValue will be copied from the specified step and added as
     * a new step to this project.
     *
     * @param step the measurement step to be added.
     * @return true, it is always possible to append a step.
     * @throws NullPointerException if step is null.
     */
    public synchronized boolean addStep(MeasurementStep step) {
        if (step == null) {
            throw new NullPointerException();
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
     * Adds a step to the specified index of this project's sequence. Only the stepValue will be copied from the
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
     * Removes a step from this project's sequence. Completed measurements can not be removed.
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
     * Removes a series of steps from this project's sequence. Completed measurements can not be removed.
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
        for (i = sequence.getSteps() - 1; i >= 0; i--) {
            MeasurementStep.State state = sequence.getStep(i).getState();
            if (state == READY) {
                continue;
            } else {
                break;
            }
        }
        return i + 1;
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
     * Calculates and returns a value from a measurement step. The specified MeasurementValue's algorithm will be used
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
     * Runs a measurement sequence until it is paused, aborted or there are no more steps to measure. The project must
     * be in a non-IDLE state before starting a measurement with this method. The measurement should be run in a worker
     * thread and only one at a time.
     *
     * @throws IllegalStateException if the project's state is IDLE or it has no Squid.
     */
    private void runMeasurement() {
        if (getSquid() == null && !DEBUG) {
            throw new IllegalStateException("Unable to run measurement, squid is: " + getSquid());
        }
        if (getState() == IDLE) {
            throw new IllegalStateException("Unable to run measurement, state is: " + getState());
        }

        try {
            if (DEBUG) {
                synchronized (DummyMeasurement.class) {
                    new DummyMeasurement().run();
                }
            } else {
                synchronized (Measurement.class) {
                    new Measurement().run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setState(IDLE);
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
        if (getSquid() == null) {
            return false;
        }
        if (getState() == IDLE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells whether it is allowed to do an auto step measurement. The returned value depends on the type and state of
     * this project.
     */
    public synchronized boolean isAutoStepEnabled() {
        if (getSquid() == null && !DEBUG) {
            return false;
        }
        if (type == CALIBRATION || type == THELLIER || type == THERMAL) {
            return false;
        } else if (type == AF) {
            if (getState() == IDLE) {
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
        if (getSquid() == null && !DEBUG) {
            return false;
        }
        if (type == CALIBRATION || type == AF || type == THELLIER || type == THERMAL) {
            if (getState() == IDLE) {
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
            if (getState() == MEASURING) {
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
        if (getState() == MEASURING || getState() == PAUSED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts an auto step measurement. If isAutoStepEnabled() is false but is isSingleStepEnabled() is true, will start
     * a single step measurement. Will do nothing if both are false. If there are no unmeasured steps in the sequence,
     * will add one for a measurement without demagnetization.
     * <p/>
     * The measurement will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the measurement was started, otherwise false.
     */
    public synchronized boolean doAutoStep() {
        if (getSquid() == null && !DEBUG) {
            return false;
        }
        if (getState() == IDLE) {
            if (isAutoStepEnabled()) {
                setState(MEASURING);
            } else if (isSingleStepEnabled()) {
                setState(PAUSED);
            } else {
                return false;
            }

            // if there are no unmeasured steps, add one for a measurement without demagnetization
            if (getSteps() == getCompletedSteps()) {
                addStep(new MeasurementStep(this));
            }

            new Thread() {
                @Override public void run() {
                    runMeasurement();
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
     * measurement step. <s>Will do nothing if isPauseEnabled() is false.</s> Will work even if isPauseEnabled() is
     * false.
     * <p/>
     * This method will notify the measurement thread to pause, but will not wait for it to finish.
     *
     * @return true if the measurement will pause, otherwise false.
     */
    public synchronized boolean doPause() {
//        if (!isPauseEnabled()) {
//            return false; // will cause problems when singlestepping non-AF projects
//        }
        if (getState() == IDLE) {
            return false;
        } else if (getState() == MEASURING) {
            setState(PAUSED);
            return true;
        } else if (getState() == PAUSED) {
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
        if (getState() == IDLE) {
            return false;
        } else {
            setState(ABORTED);
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
    private synchronized boolean doManualMove(ManualMovePosition position) {
        if (!isManualControlEnabled()) {
            return false;
        }
        setState(PAUSED);
        new Thread(new ManualMove(position)).start();
        return true;
    }

    /**
     * Moves the sample handler to the DegausserY position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMoveDegausserY() {
        return doManualMove(ManualMovePosition.DEGAUSSER_Y);
    }

    /**
     * Moves the sample handler to the DegausserZ position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMoveDegausserZ() {
        return doManualMove(ManualMovePosition.DEGAUSSER_Z);
    }

    /**
     * Moves the sample handler to the Background position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMoveBackground() {
        return doManualMove(ManualMovePosition.BACKGROUND);
    }

    /**
     * Moves the sample handler to the Measurement position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */
    public synchronized boolean doManualMoveMeasurement() {
        return doManualMove(ManualMovePosition.MEASUREMENT);
    }

    /**
     * Moves the sample handler to the Home position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */

    public synchronized boolean doManualMoveHome() {
        return doManualMove(ManualMovePosition.HOME);
    }

    /**
     * Moves the sample handler to the RightLimit position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */

    public synchronized boolean doManualMoveRightLimit() {
        return doManualMove(ManualMovePosition.RIGHT_LIMIT);
    }

    /**
     * Moves the sample handler to the LeftLimit position. Will do nothing if isManualControlEnabled() is false.
     * <p/>
     * The operation will run in its own thread, and this method will not wait for it to finish.
     *
     * @return true if the operation was started, otherwise false.
     */

    public synchronized boolean doManualMoveLeftLimit() {
        return doManualMove(ManualMovePosition.LEFT_LIMIT);
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
        if (!isManualControlEnabled()) {
            return false;
        }
        setState(PAUSED);
        new Thread(new ManualRotate(angle)).start();
        return true;
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
        if (!isManualControlEnabled()) {
            return false;
        }
        setState(PAUSED);
        new Thread(new ManualMeasure()).start();
        return true;
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
        if (!isManualControlEnabled()) {
            return false;
        }
        setState(PAUSED);
        new Thread(new ManualDemag(ManualDemagAxel.Z, amplitude)).start();
        return true;
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
        if (!isManualControlEnabled()) {
            return false;
        }
        setState(PAUSED);
        new Thread(new ManualDemag(ManualDemagAxel.Y, amplitude)).start();
        return true;
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
     * The state of the project's measurements.
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

    /**
     * The orientation of the sample in the measurements.
     */
    public enum Orientation {
        PLUS_Z, MINUS_Z
    }

    /**
     * The type of normalization to use for the measurement values.
     */
    public enum Normalization {
        VOLUME, MASS
    }

    /**
     * Runs the measurements and adds the measurement data to this project. The project's state must be non-IDLE before
     * starting this thread.
     */
    private class Measurement implements Runnable {
        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }
            if (getSquid() == null) {
                throw new IllegalStateException();
            }

            for (int i = getCompletedSteps(); i < getSteps(); i++) {

                // begin measuring the first uncomplete step
                currentStep = getStep(i);
                currentStep.setMeasuring();
                fireMeasurementEvent(currentStep, STEP_START);

                try {
                    // reset the equipment
                    if (getSquid().getHandler().getRotation() != 0) {
                        getSquid().getHandler().rotateTo(0);
                        getSquid().getHandler().join();
                    }
                    checkAborted();

                    // demagnetizing
                    if (currentStep.getStepValue() > 0.05 && isDegaussingEnabled()) {

                        // demagnetize Z
                        getSquid().getHandler().moveToDegausserZ();
                        fireMeasurementEvent(currentStep, HANDLER_MOVE);
                        getSquid().getHandler().join();
                        fireMeasurementEvent(currentStep, HANDLER_STOP);
                        checkAborted();
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_START);
                        if (!getSquid().getDegausser().demagnetizeZ(currentStep.getStepValue())) {
                            throw new InterruptedException("demagnetizeZ = false");
                        }
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_END);
                        checkAborted();

                        // demagnetize Y
                        getSquid().getHandler().moveToDegausserY();
                        fireMeasurementEvent(currentStep, HANDLER_MOVE);
                        getSquid().getHandler().join();
                        fireMeasurementEvent(currentStep, HANDLER_STOP);
                        checkAborted();
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_START);
                        if (!getSquid().getDegausser().demagnetizeY(currentStep.getStepValue())) {
                            throw new InterruptedException("demagnetizeY = false");
                        }
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_END);
                        checkAborted();

                        // demagnetize X
                        getSquid().getHandler().rotateTo(90);
                        fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                        getSquid().getHandler().join();
                        fireMeasurementEvent(currentStep, HANDLER_STOP);
                        checkAborted();
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_START);
                        if (!getSquid().getDegausser().demagnetizeY(currentStep.getStepValue())) {
                            throw new InterruptedException("demagnetizeY = false");
                        }
                        fireMeasurementEvent(currentStep, DEMAGNETIZE_END);
                        checkAborted();
                        getSquid().getHandler().rotateTo(0);
                        fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                        getSquid().getHandler().join();
                        fireMeasurementEvent(currentStep, HANDLER_STOP);
                        checkAborted();
                    }

                    // measure first background noise
                    getSquid().getHandler().moveToBackground();
                    fireMeasurementEvent(currentStep, HANDLER_MOVE);
                    getSquid().getHandler().join();
                    fireMeasurementEvent(currentStep, HANDLER_STOP);
                    checkAborted();
                    // Begin by pulsing feedback loop for each axis And by clearing flux counter for each axis
                    getSquid().getMagnetometer().pulseReset('A');
                    getSquid().getMagnetometer().clearFlux('A');
                    Double[] results = getSquid().getMagnetometer().readData();
                    currentStep.addResult(new MeasurementResult(NOISE, 0, results[0], results[1], results[2]));
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);
                    checkAborted();

                    // begin measuring the sample
                    getSquid().getHandler().moveToMeasurement();
                    fireMeasurementEvent(currentStep, HANDLER_MOVE);
                    getSquid().getHandler().join();
                    fireMeasurementEvent(currentStep, HANDLER_STOP);
                    checkAborted();

                    // measure with the set amount of handler rotations
                    int rotations = Settings.getMeasurementRotations();
                    if (rotations == 0) {

                        // quick measure with no rotations
                        results = getSquid().getMagnetometer().readData();
                        currentStep.addResult(new MeasurementResult(SAMPLE, 0, results[0], results[1], results[2]));
                        fireMeasurementEvent(currentStep, VALUE_MEASURED);
                        checkAborted();

                    } else {

                        // accurate measure with rotations
                        for (int j = 0; j < rotations; j++) {

                            // measure at 0 degrees
                            results = getSquid().getMagnetometer().readData();
                            currentStep.addResult(new MeasurementResult(SAMPLE, 0, results[0], results[1], results[2]));
                            fireMeasurementEvent(currentStep, VALUE_MEASURED);
                            checkAborted();

                            // measure at 90 degrees
                            getSquid().getHandler().rotateTo(90);
                            fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                            getSquid().getHandler().join();
                            fireMeasurementEvent(currentStep, HANDLER_STOP);
                            checkAborted();
                            results = getSquid().getMagnetometer().readData();
                            currentStep.addResult(
                                    new MeasurementResult(SAMPLE, 90, results[0], results[1], results[2]));
                            fireMeasurementEvent(currentStep, VALUE_MEASURED);
                            checkAborted();

                            // measure at 180 degrees
                            getSquid().getHandler().rotateTo(180);
                            fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                            getSquid().getHandler().join();
                            fireMeasurementEvent(currentStep, HANDLER_STOP);
                            checkAborted();
                            results = getSquid().getMagnetometer().readData();
                            currentStep.addResult(
                                    new MeasurementResult(SAMPLE, 180, results[0], results[1], results[2]));
                            fireMeasurementEvent(currentStep, VALUE_MEASURED);
                            checkAborted();

                            // measure at 270 degrees
                            getSquid().getHandler().rotateTo(270);
                            fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                            getSquid().getHandler().join();
                            fireMeasurementEvent(currentStep, HANDLER_STOP);
                            checkAborted();
                            results = getSquid().getMagnetometer().readData();
                            currentStep.addResult(
                                    new MeasurementResult(SAMPLE, 270, results[0], results[1], results[2]));
                            fireMeasurementEvent(currentStep, VALUE_MEASURED);
                            checkAborted();

                            // rotate the handler back to 0 degrees
                            getSquid().getHandler().rotateTo(0);
                            fireMeasurementEvent(currentStep, HANDLER_ROTATE);
                            getSquid().getHandler().join();
                            fireMeasurementEvent(currentStep, HANDLER_STOP);
                            checkAborted();
                        }
                    }

                    // measure second background noise
                    getSquid().getHandler().moveToBackground();
                    fireMeasurementEvent(currentStep, HANDLER_MOVE);
                    getSquid().getHandler().join();
                    fireMeasurementEvent(currentStep, HANDLER_STOP);
                    checkAborted();
                    results = getSquid().getMagnetometer().readData();
                    currentStep.addResult(new MeasurementResult(NOISE, 0, results[0], results[1], results[2]));
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);
                    checkAborted();

                } catch (InterruptedException e) {

                    // the measurement was aborted or some error occurred
                    if (getState() == ABORTED) {
                        System.err.println(e.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } finally {

                    // complete the step
                    currentStep.setDone();
                    fireMeasurementEvent(currentStep, STEP_END);
                    currentStep = null;
                }

                if (getState() == PAUSED || getState() == ABORTED) {
                    setState(IDLE);
                    return;
                }
            }
            setState(IDLE);
        }

        /**
         * Checks whether the measurement has been aborted. Will throw an exception if the measurement has been aborted,
         * otherwise will do nothing.
         *
         * @throws InterruptedException if the measurement has been aborted.
         */
        private void checkAborted() throws InterruptedException {
            if (getState() == ABORTED) {
                fireMeasurementEvent(currentStep, STEP_ABORTED);
                throw new InterruptedException("Measurement aborted");
            }
        }
    }

    /**
     * A measurement that gives random data for testing purposes. The project's state must be non-IDLE before starting
     * this thread.
     */
    private class DummyMeasurement implements Runnable {
        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }

            System.out.println("Measurement started");
            for (int i = getCompletedSteps(); i < getSteps(); i++) {

                System.out.println("Measuring step " + i + "...");
                currentStep = getStep(i);
                currentStep.setMeasuring();
                fireMeasurementEvent(currentStep, STEP_START);

                try {
                    // measure BG (1)
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(NOISE, 0,
                            Math.random() * 0.000001, Math.random() * 0.000001, Math.random() * 0.000001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                    // measure DEG0
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        currentStep.setDone();
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(SAMPLE, 0,
                            Math.random() * 0.0001, Math.random() * 0.0001, Math.random() * 0.0001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                    // measure DEG90
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        currentStep.setDone();
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(SAMPLE, 90,
                            Math.random() * 0.0001, Math.random() * 0.0001, Math.random() * 0.0001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                    // measure DEG180
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        currentStep.setDone();
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(SAMPLE, 180,
                            Math.random() * 0.0001, Math.random() * 0.0001, Math.random() * 0.0001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                    // measure DEG270
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        currentStep.setDone();
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(SAMPLE, 270,
                            Math.random() * 0.0001, Math.random() * 0.0001, Math.random() * 0.0001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                    // measure BG (2)
                    Thread.sleep(500);
                    if (getState() == ABORTED) {
                        System.out.println("Measurement aborted");
                        setState(IDLE);
                        currentStep.setDone();
                        return;
                    }
                    currentStep.addResult(new MeasurementResult(NOISE, 0,
                            Math.random() * 0.000001, Math.random() * 0.000001, Math.random() * 0.000001));
                    System.out.println("Result added");
                    fireMeasurementEvent(currentStep, VALUE_MEASURED);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // complete step
                    currentStep.setDone();
                    System.out.println("Step " + i + " completed");
                    fireMeasurementEvent(currentStep, STEP_END);
                    currentStep = null;
                }

                if (getState() == PAUSED) {
                    System.out.println("Measurement ended (paused)");
                    setState(IDLE);
                    return;
                }
            }
            System.out.println("Measurement ended");
            setState(IDLE);
        }
    }

    private enum ManualMovePosition {
        DEGAUSSER_Y,
        DEGAUSSER_Z,
        BACKGROUND,
        MEASUREMENT,
        HOME,
        RIGHT_LIMIT,
        LEFT_LIMIT
    }

    /**
     * Runs a manual move command. The project's state must be non-IDLE before starting this thread.
     */
    private class ManualMove implements Runnable {

        private ManualMovePosition pos;

        public ManualMove(ManualMovePosition pos) {
            this.pos = pos;
        }

        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }
            if (getSquid() == null) {
                throw new IllegalStateException();
            }

            switch (pos) {
            case DEGAUSSER_Y:
                getSquid().getHandler().moveToDegausserY();
                break;
            case DEGAUSSER_Z:
                getSquid().getHandler().moveToDegausserZ();
                break;
            case BACKGROUND:
                getSquid().getHandler().moveToBackground();
                break;
            case MEASUREMENT:
                getSquid().getHandler().moveToMeasurement();
                break;
            case HOME:
                getSquid().getHandler().moveToSampleLoad();
                break;
            case RIGHT_LIMIT:
                getSquid().getHandler().moveToRightLimit();
                break;
            case LEFT_LIMIT:
                getSquid().getHandler().moveToLeftLimit();
                break;
            default:
                System.err.println("Invalid pos: " + pos);
                assert false;
                break;
            }
            fireMeasurementEvent(null, HANDLER_MOVE);

            try {
                getSquid().getHandler().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fireMeasurementEvent(null, HANDLER_STOP);
            setState(IDLE);
        }
    }

    /**
     * Runs a manual rotate command. The project's state must be non-IDLE before starting this thread.
     */
    private class ManualRotate implements Runnable {

        private int angle;

        public ManualRotate(int angle) {
            this.angle = angle;
        }

        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }
            if (getSquid() == null) {
                throw new IllegalStateException();
            }

            getSquid().getHandler().rotateTo(angle);
            fireMeasurementEvent(null, HANDLER_ROTATE);

            try {
                getSquid().getHandler().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fireMeasurementEvent(null, HANDLER_STOP);

            setState(IDLE);
        }
    }

    /**
     * Runs a manual measure command and adds a new step to this project with the measurement data. The project's state
     * must be non-IDLE before starting this thread.
     */
    private class ManualMeasure implements Runnable {
        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }
            if (getSquid() == null) {
                throw new IllegalStateException();
            }

            // TODO: add a measurement step

            Double[] results = getSquid().getMagnetometer().readData();

            //TODO: check where we are and change SAMPLE to something else.
            currentStep.addResult(new MeasurementResult(SAMPLE, getSquid().getHandler().getRotation(),
                    results[0], results[1], results[2]));
            fireMeasurementEvent(currentStep, VALUE_MEASURED);

            setState(IDLE);
        }
    }

    private enum ManualDemagAxel {
        Z,
        Y
    }

    /**
     * Runs a manual demag command and adds a new step to this project with the demag value. The project's state must be
     * non-IDLE before starting this thread.
     */
    private class ManualDemag implements Runnable {

        private ManualDemagAxel axel;
        private double amplitude;

        public ManualDemag(ManualDemagAxel axel, double amplitude) {
            this.axel = axel;
            this.amplitude = amplitude;
        }

        public void run() {
            if (getState() == IDLE) {
                throw new IllegalStateException();
            }
            if (getSquid() == null) {
                throw new IllegalStateException();
            }
            if (!isDegaussingEnabled()) {
                throw new IllegalStateException();
            }

            // TODO: add a measurement step

            fireMeasurementEvent(null, DEMAGNETIZE_START);
            switch (axel) {
            case Y:
                getSquid().getDegausser().demagnetizeY(amplitude);
                break;
            case Z:
                getSquid().getDegausser().demagnetizeZ(amplitude);
                break;
            default:
                System.err.println("Invalid axel: " + axel);
                assert false;
                break;
            }
            fireMeasurementEvent(null, DEMAGNETIZE_END);

            setState(IDLE);
        }
    }
}
