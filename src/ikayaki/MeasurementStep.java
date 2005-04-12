/*
* MeasurementStep.java
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.vecmath.Matrix3d;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ikayaki.MeasurementStep.State.*;

/**
 * A single step in a measurement sequence. Each step can include multiple measurements for improved precision. A step
 * can have a different volume and mass than the related project, but by default the volume and mass of the project will
 * be used. Any changes made to the measurement step will invoke the project's autosaving. Only the project may change
 * the state and results of a measurement step.
 * <p/>
 * All operations are thread-safe.
 *
 * @author Esko Luontola
 */
public class MeasurementStep {

    /**
     * The project that owns this step, or null if there is no owner.
     */
    private Project project;

    /**
     * Tells if this step has been completed or not, or if a measurement is still running.
     */
    private State state = State.READY;

    /**
     * The time the measurements were completed, or null if that has not yet happened. This equals the time of the
     * latest measurement result.
     */
    private Date timestamp = null;

    /**
     * The AF/Thermal value of this step, or a negative number if it has not been specified.
     */
    private double stepValue = -1.0;

    /**
     * The mass of this step's sample, or a negative number to use the project's default mass.
     */
    private double mass = -1.0;

    /**
     * The volume of this step's sample, or a negative number to use the project's default volume.
     */
    private double volume = -1.0;

    /**
     * The susceptibility of this step's sample, or a negative number to use the project's default volume.
     */
    private double susceptibility = -1.0;

    /**
     * The individual measurement results that are part of this measurement step.
     */
    private List<MeasurementResult> results = new ArrayList<MeasurementResult>();

    /**
     * Creates a blank measurement step.
     */
    public MeasurementStep() {
        project = null;
    }

    /**
     * Creates a blank measurement step for a project.
     *
     * @param project the project who is the owner of this step.
     */
    public MeasurementStep(Project project) {
        this.project = project;
    }

    /**
     * Creates a measurement step from the specified element. Will update the transformation matrices.
     *
     * @param element the element from which this step will be created.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementStep(Element element) {
        this(element, null);
    }

    /**
     * Creates a measurement step from the specified element for a project. Will update the transformation matrices.
     *
     * @param element the element from which this step will be created.
     * @param project the project who is the owner of this step.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementStep(Element element, Project project) {
        if (element == null) {
            throw new NullPointerException();
        }
        this.project = project;
        String s;

        // verify tag name
        if (!element.getTagName().equals("step")) {
            throw new IllegalArgumentException("Invalid tag name: " + element.getTagName());
        }
        
        // get stepValue, mass, volume
        s = element.getAttribute("stepvalue");
        try {
            setStepValue(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid stepvalue: " + s);
        }
        s = element.getAttribute("mass");
        try {
            setMass(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid mass: " + s);
        }
        s = element.getAttribute("volume");
        try {
            setVolume(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid volume: " + s);
        }
        s = element.getAttribute("susceptibility");
        try {
            setSusceptibility(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            // TODO: import old version
            //throw new IllegalArgumentException("Invalid susceptibility: " + s);
        }

        // get results
        NodeList results = element.getElementsByTagName("result");
        for (int i = 0; i < results.getLength(); i++) {
            Element result = (Element) results.item(i);
            this.results.add(new MeasurementResult(result));
        }

        // get state, must be done after getting results
        if (element.getAttribute("done").equals("1")) {
            state = DONE;
        } else {
            state = READY;
        }

        // get timestamp, must be done after getting results
        s = element.getAttribute("timestamp");
        if (s.equals("")) {
            timestamp = null;
        } else {
            try {
                timestamp = new Date(Long.parseLong(s));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid timestamp: " + s);
            }
        }

        // check that state, timestamp and results are consistent
        if (state.isDone()) {
            if (timestamp == null || this.results.size() == 0) {
                throw new IllegalArgumentException("Inconsistent state");
            }
        } else {
            if (timestamp != null || this.results.size() > 0) {
                throw new IllegalArgumentException("Inconsistent state");
            }
        }

        // finalize
        updateTransforms();
    }

    /**
     * Exports this step to a DOM element.
     *
     * @param document the document that will contain this element.
     */
    public synchronized Element getElement(Document document) {
        Element element = document.createElement("step");

        element.setAttribute("done", state.isDone() ? "1" : "0");
        element.setAttribute("timestamp", timestamp == null ? "" : Long.toString(timestamp.getTime()));
        element.setAttribute("stepvalue", Double.toString(stepValue));
        element.setAttribute("mass", Double.toString(mass));
        element.setAttribute("volume", Double.toString(volume));
        element.setAttribute("susceptibility", Double.toString(susceptibility));

        for (MeasurementResult result : results) {
            element.appendChild(result.getElement(document));
        }

        return element;
    }

    /**
     * Invokes the owner project's autosaving. If there is no owner, will do nothing.
     */
    public void save() {
        if (project != null) {
            project.save();
        }
    }

    /**
     * Returns the owner project of this step, or null if there is no owner.
     */
    public synchronized Project getProject() {
        return project;
    }

    /**
     * Tells if this step has been completed or not, or if a measurement is still running.
     */
    public synchronized State getState() {
        return state;
    }

    /**
     * Returns the time the measurements were completed, or null if that has not yet happened.
     */
    public synchronized Date getTimestamp() {
        if (state != DONE && state != DONE_RECENTLY) {
            return null;
        }
        if (timestamp == null) {
            return null;
        } else {
            return (Date) timestamp.clone();
        }
    }

    /**
     * Returns the AF/Thermal value of this step, or a negative number if it has not been specified.
     */
    public synchronized double getStepValue() {
        return stepValue;
    }

    /**
     * Sets the value of this step. A negative value will clear it.
     *
     * @throws IllegalStateException if the step's state is not READY.
     */
    public synchronized void setStepValue(double stepValue) {
        if (state != READY) {
            throw new IllegalStateException("Unable to set stepValue, state is: " + state);
        }
        if (stepValue < 0.0) {
            stepValue = -1.0;
        }
        this.stepValue = stepValue;
        save();
    }

    /**
     * Returns the mass of this step's sample, or a negative number to use the project's default mass.
     */
    public synchronized double getMass() {
        return mass;
    }

    /**
     * Sets the mass of this step's sample. A negative value will clear it.
     */
    public synchronized void setMass(double mass) {
        if (mass < 0.0) {
            mass = -1.0;
        }
        this.mass = mass;
        save();
    }

    /**
     * Returns the volume of this step's sample, or a negative number to use the project's default volume.
     */
    public synchronized double getVolume() {
        return volume;
    }

    /**
     * Sets the volume of this step's sample. A negative value will clear it.
     */
    public synchronized void setVolume(double volume) {
        if (volume < 0.0) {
            volume = -1.0;
        }
        this.volume = volume;
        save();
    }

    /**
     * Returns the susceptibility of this step's sample, or a negative number to use the project's default susceptibility.
     */
    public synchronized double getSusceptibility() {
        return susceptibility;
    }

    /**
     * Sets the susceptibility of this step's sample. A negative value will clear it.
     */
    public synchronized void setSusceptibility(double susceptibility) {
        if (susceptibility < 0.0) {
            susceptibility = -1.0;
        }
        this.susceptibility = susceptibility;
        save();
    }

    /**
     * Updates all of the measurement results with the owner project's transformation matrix. If there is no owner, an
     * identity matrix will be used.
     */
    synchronized void updateTransforms() {
        Matrix3d transform = null;
        if (project != null) {
            transform = project.getTransform();
        }
        for (MeasurementResult result : results) {
            result.setTransform(transform);
        }
    }

    /**
     * Returns the number of results in this step.
     */
    public synchronized int getResults() {
        return results.size();
    }

    /**
     * Returns the specified result from this step.
     *
     * @param index the index of the result.
     * @return the specified result.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getResults()).
     */
    public synchronized MeasurementResult getResult(int index) {
        return results.get(index);
    }

    /**
     * Appends a measurement result to this step. This method may be called only for a steps whose state is READY or
     * MEASURING.
     * <p/>
     * Sets the timestamp to the current time. Sets the state to MEASURING. The transformation matrix of the result will
     * be updated automatically.
     *
     * @param result the result to be added.
     * @throws NullPointerException  if result is null.
     * @throws IllegalStateException if this step's state is not READY or MEASURING.
     */
    public synchronized void addResult(MeasurementResult result) {
        if (result == null) {
            throw new NullPointerException();
        }
        if (state == READY || state == MEASURING) {
            setMeasuring();
            results.add(result);
            if (timestamp == null) {
                timestamp = new Date();
            }
            updateTransforms();
            save();
        } else {
            throw new IllegalStateException("Unable to add results, state is: " + state);
        }
    }

    /**
     * Called when the step's measurements are started. If the the step's current status is READY, will set it to
     * MEASURING. Otherwise nothing will be changed.
     */
    public synchronized void setMeasuring() {
        if (state == READY) {
            state = MEASURING;
            save();
        }
    }

    /**
     * Called after all results have been added. Sets the step's status to DONE_RECENTLY and prevents the adding of
     * further results. If the state is already DONE or DONE_RECENTLY, then nothing will be changed.
     */
    public synchronized void setDone() {
        if (state != DONE && state != DONE_RECENTLY) {
            state = DONE_RECENTLY;
            save();
        }
    }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[step");
        sb.append(" state=" + state);
        sb.append(" timestamp=" + (timestamp == null ? "null" : "" + timestamp.getTime()));
        sb.append(" stepvalue=" + stepValue);
        sb.append(" mass=" + mass);
        sb.append(" volume=" + volume);

        sb.append(" results=(");
        boolean first = true;
        for (MeasurementResult result : results) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(result.toString());
        }
        sb.append(")]");

        return sb.toString();
    }

    /**
     * The state of a measurement step.
     */
    public enum State {
        READY(false), MEASURING(true), DONE_RECENTLY(true), DONE(true);

        private boolean done;

        private State(boolean done) {
            this.done = done;
        }

        public boolean isDone() {
            return done;
        }
    }

    public static void main(String[] args) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        MeasurementStep step = new MeasurementStep();
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.BG, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.DEG0, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.DEG90, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.DEG180, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.DEG270, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.addResult(new MeasurementResult(MeasurementResult.Type.BG, 1, 2, 3));
        System.out.println(step);
        Thread.sleep(100);
        step.setDone();
        System.out.println(step);

        Element element = step.getElement(document);
        document.appendChild(element);

        step = new MeasurementStep(element);
        System.out.println(step);

        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute("indent-number", new Integer(2));

        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new OutputStreamWriter(System.out, "utf-8"));
        t.transform(source, result);
    }
}