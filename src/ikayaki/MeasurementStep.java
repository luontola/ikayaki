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

import org.w3c.dom.Element;

import javax.vecmath.Matrix3d;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A single step in a measurement sequence. Each step can include multiple measurements for improved precision. A step
 * can have a different volume and mass than the related project, but by default the volume and mass of the project will
 * be used. Only the project may change the state and results of a measurement step.
 * <p/>
 * All operations are thread-safe.
 *
 * @author
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
     * The time the measurements were completed, or null if that has not yet happened.
     */
    private Date timestamp = null;

    /**
     * The AF/Thermal value of this step, or a negative number if it has not been specified.
     */
    private double stepValue = -1.0;

    /**
     * The mass of this step�s sample, or a negative number to use the project�s default mass.
     */
    private double mass = -1.0;

    /**
     * The volume of this step�s sample, or a negative number to use the project�s default volume.
     */
    private double volume = -1.0;

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
     * Creates a measurement step from the specified element.
     *
     * @param element the element from which this step will be created.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementStep(Element element) {
        this(element, null);
    }

    /**
     * Creates a measurement step from the specified element for a project.
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

        return; // TODO
    }

    /**
     * Exports this step to a DOM element.
     */
    public synchronized Element getElement() {
        return null; // TODO
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
     * Sets the completion status of this step. Only the owner project may set the state.
     *
     * @throws NullPointerException if state is null.
     */
    synchronized void setState(State state) {
        if (state == null) {
            throw new NullPointerException();
        }
        this.state = state;
    }

    /**
     * Returns the time the measurements were completed, or null if that has not yet happened.
     */
    public synchronized Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the AF/Thermal value of this step, or a negative number if it has not been specified.
     */
    public synchronized double getStepValue() {
        return stepValue;
    }

    /**
     * Sets the value of this step. A negative value will clear it.
     */
    public synchronized void setStepValue(double stepValue) {
        if (stepValue < 0.0) {
            stepValue = -1.0;
        }
        this.stepValue = stepValue;
    }

    /**
     * Returns the mass of this step�s sample, or a negative number to use the project�s default mass.
     */
    public synchronized double getMass() {
        return mass;
    }

    /**
     * Sets the mass of this step�s sample. A negative value will clear it.
     */
    public synchronized void setMass(double mass) {
        if (mass < 0.0) {
            mass = -1.0;
        }
        this.mass = mass;
    }

    /**
     * Returns the volume of this step�s sample, or a negative number to use the project�s default volume.
     */
    public synchronized double getVolume() {
        return volume;
    }

    /**
     * Sets the volume of this step�s sample. A negative value will clear it.
     */
    public synchronized void setVolume(double volume) {
        if (volume < 0.0) {
            volume = -1.0;
        }
        this.volume = volume;
    }

    /**
     * Updates all of the measurement results with the owner project�s transformation matrix. If there is no owner, an
     * identity matrix will be used.
     */
    synchronized void updateTransforms() {
        Matrix3d transform;
        if (project == null) {
            transform = new Matrix3d();
            transform.setIdentity();
        } else {
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
     * Appends a measurement result to this step. The transformation matrix of the result will be updated
     * automatically.
     *
     * @param result the result to be added.
     * @throws NullPointerException if result is null.
     */
    public synchronized void addResult(MeasurementResult result) {
        if (result == null) {
            throw new NullPointerException();
        }
        results.add(result);
        updateTransforms();
    }

    /**
     * The state of a measurement step.
     */
    public enum State {
        READY, MEASURING, DONE_RECENTLY, DONE
    }
}