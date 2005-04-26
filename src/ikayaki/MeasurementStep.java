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
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
public class MeasurementStep implements Iterable<MeasurementResult> {

    /**
     * The project that owns this step, or null if there is no owner.
     */
    private final Project project;

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
     * The AF or Thermal value of this step, or a negative number if it has not been specified. The unit is millitesla
     * (when AF) or Celcius (when thermal).
     */
    private double stepValue = -1.0;

    /**
     * The mass of this step's sample, or a negative number to use the project's default mass. The unit is gram.
     */
    private double mass = -1.0;

    /**
     * The volume of this step's sample, or a negative number to use the project's default volume. The unit is cm^3.
     */
    private double volume = -1.0;

    /**
     * The susceptibility of this step's sample, or a negative number to use the project's default volume.
     * Susceptibility has no unit.
     */
    private double susceptibility = -1.0;

    /**
     * The individual measurement results that are part of this measurement step.
     */
    private final List<MeasurementResult> results = new ArrayList<MeasurementResult>();

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
            double stepValue = Double.parseDouble(s);
            if (element.getAttribute("done").equals("1")) {
                if (stepValue < 0.0) {
                    stepValue = -1.0;
                }
                this.stepValue = stepValue;     // for completed steps, bypass the degausser limits
            } else {
                setStepValue(stepValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid stepvalue: " + s, e);
        }
        s = element.getAttribute("mass");
        try {
            setMass(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid mass: " + s, e);
        }
        s = element.getAttribute("volume");
        try {
            setVolume(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid volume: " + s, e);
        }
        s = element.getAttribute("susceptibility");
        try {
            setSusceptibility(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid susceptibility: " + s, e);
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
                throw new IllegalArgumentException("Invalid timestamp: " + s, e);
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

        if (results.size() == 0) {
            element.setAttribute("done", "0");
            element.setAttribute("timestamp", "");
        } else {
            element.setAttribute("done", "1");
            element.setAttribute("timestamp", Long.toString(timestamp.getTime()));
        }
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
        if (!state.isDone()) {
            return null;
        }
        if (timestamp == null) {
            return null;
        } else {
            return (Date) timestamp.clone();
        }
    }

    /**
     * Returns the AF/Thermal value of this step, or a negative number if it has not been specified. The unit is
     * millitesla (when AF) or Celcius (when thermal).
     */
    public synchronized double getStepValue() {
        return stepValue;
    }

    /**
     * Sets the value of this step. A negative value will clear it. The unit is millitesla (when AF) or Celcius (when
     * thermal).
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
        if (getProject() != null && getProject().getType() == Project.Type.AF) {
            if (stepValue > 0.0 && stepValue < Settings.getDegausserMinimumField()) {
                stepValue = Settings.getDegausserMinimumField();
            }
            stepValue = Math.min(stepValue, Settings.getDegausserMaximumField());
        }
        this.stepValue = stepValue;
        save();
    }

    /**
     * Returns the mass of this step's sample, or a negative number to use the project's default mass. The unit is
     * gram.
     */
    public synchronized double getMass() {
        return mass;
    }

    /**
     * Sets the mass of this step's sample. A negative value will clear it. The unit is gram.
     */
    public synchronized void setMass(double mass) {
        if (mass < 0.0) {
            mass = -1.0;
        }
        this.mass = mass;
        save();
    }

    /**
     * Returns the volume of this step's sample, or a negative number to use the project's default volume. The unit is
     * cm^3.
     */
    public synchronized double getVolume() {
        return volume;
    }

    /**
     * Sets the volume of this step's sample. A negative value will clear it. The unit is cm^3.
     */
    public synchronized void setVolume(double volume) {
        if (volume < 0.0) {
            volume = -1.0;
        }
        this.volume = volume;
        save();
    }

    /**
     * Returns the susceptibility of this step's sample, or a negative number to use the project's default
     * susceptibility. Susceptibility has no unit.
     */
    public synchronized double getSusceptibility() {
        return susceptibility;
    }

    /**
     * Sets the susceptibility of this step's sample. A negative value will clear it. Susceptibility has no unit.
     */
    public synchronized void setSusceptibility(double susceptibility) {
        if (susceptibility < 0.0) {
            susceptibility = -1.0;
        }
        this.susceptibility = susceptibility;
        save();
    }

    /**
     * Updates all of the measurement results with the owner project's transformation matrix and applies the noise and
     * holder fixes. If there is no owner, an identity matrix will be used.
     */
    protected synchronized void updateTransforms() {
        Matrix3d transform = null;
        if (project != null) {
            transform = project.getTransform();
        }
        for (MeasurementResult result : results) {
            result.applyFixes(this);
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
        if (state.isDone()) {
            throw new IllegalStateException("Unable to add results, state is: " + state);
        }

        setMeasuring();
        if (results.size() == 0 && (getProject() == null || !getProject().isHolderCalibration())) {
            // holder calibration value for all except the holder calibration project itself
            MeasurementResult holder = Settings.getHolderCalibration();
            if (holder != null) {
                results.add(holder);
            }
        }
        results.add(result);
        timestamp = new Date();
        updateTransforms();
        save();
    }

    /**
     * Called when the step's measurements are started. Sets the step's state to MEASURING.
     *
     * @throws IllegalStateException if this method is called when the state is marked as DONE.
     */
    public synchronized void setMeasuring() {
        if (state.isDone()) {
            throw new IllegalStateException("Unable set state to MEASURING, state is: " + state);
        }
        state = MEASURING;
        save();
    }

    /**
     * Called after all results have been added. Sets the step's status to DONE_RECENTLY and prevents the adding of
     * further results. If there are no results (maybe the measurement was cancelled), will set the state back to READY.
     * If the state is already DONE or DONE_RECENTLY, then nothing will be changed.
     */
    public synchronized void setDone() {
        if (!state.isDone()) {
            // if the measurement was aborted before any steps were measured, return to an unmeasured state
            if (getResults() == 0) {
                state = READY;
            } else {
                state = DONE_RECENTLY;
            }
            updateTransforms();     // need to update NOISE fixes
            save();
        }
    }

    /**
     * Returns the average of the holder results (raw values). If there are no holder results or this is the holder
     * calibration project itself, will return a zero-filled vector.
     */
    public synchronized Vector3d getHolder() {
        Vector3d v = new Vector3d();
        int count = 0;
        if (getProject() != null && getProject().isHolderCalibration()) {
            return v;
        }
        for (MeasurementResult result : results) {
            if (result.getType() != MeasurementResult.Type.HOLDER) {
                continue;
            }
            // all rotations are assumed to be 0
            // TODO: find out where and when the holder's noise fix is done. it should be before we can return from this method.
            v.add(result.getRawVector());
            count++;
        }
        if (count > 0) {
            v.scale(1.0 / count);
        }
        return v;
    }

    /**
     * Returns the average of the noise results (raw values). If there are no noise results, will return a zero-filled
     * vector.
     */
    public synchronized Vector3d getNoise() {
        Vector3d v = new Vector3d();
        int count = 0;
        for (MeasurementResult result : results) {
            if (result.getType() != MeasurementResult.Type.NOISE) {
                continue;
            }
            // all rotations are assumed to be 0
            v.add(result.getRawVector());
            count++;
        }
        if (count > 0) {
            v.scale(1.0 / count);
        }
        return v;
    }

    /**
     * Returns an iterator for iterating through this step's measurement results.
     */
    public Iterator<MeasurementResult> iterator() {
        final MeasurementStep step = this;

        return new Iterator<MeasurementResult>() {

            private int next = 0;

            public boolean hasNext() {
                return next < step.getResults();
            }

            public MeasurementResult next() {
                return step.getResult(next++);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * The state of a measurement step.
     */
    public enum State {
        READY(false), MEASURING(false), DONE_RECENTLY(true), DONE(true);

        private boolean done;

        private State(boolean done) {
            this.done = done;
        }

        /**
         * When the step's state is "done", no changes to the measurements are any more allowed.
         */
        public boolean isDone() {
            return done;
        }
    }
}