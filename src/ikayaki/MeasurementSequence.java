/*
* MeasurementSequence.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * A list of measurement steps. Steps can be added or removed from the sequence. All operations are thread-safe.
 *
 * @author
 */
public class MeasurementSequence {

    /**
     * Name of the sequence or null if it has no name.
     */
    private String name = null;

    /**
     * The measurement steps of this sequence.
     */
    private List<MeasurementStep> steps = new ArrayList<MeasurementStep>();

    /**
     * Creates an empty sequence with no name.
     */
    public MeasurementSequence() {
        return; // TODO
    }

    /**
     * Creates an empty sequence with the specified name.
     *
     * @param name name of the sequence.
     */
    public MeasurementSequence(String name) {
        return; // TODO
    }

    /**
     * Creates a sequence from the specified element.
     *
     * @param element the element from which this sequence will be created.
     * @throws NullPointerException     if import is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementSequence(Element element) {
        return; // TODO
    }

    /**
     * Creates a sequence from the specified element for a project.
     *
     * @param element the element from which this sequence will be created.
     * @param project the project whose sequence this will be. Needed for importing the measurement steps correctly.
     * @throws NullPointerException     if import is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementSequence(Element element, Project project) {
        return; // TODO
    }

    /**
     * Exports this sequence to a DOM element.
     */
    public synchronized Element getElement() {
        return null; // TODO
    }

    /**
     * Returns the name of this sequence.
     *
     * @return the name, or null if it has no name
     */
    public synchronized String getName() {
        return null; // TODO
    }

    /**
     * Sets the name of this sequence.
     */
    public synchronized void setName(String name) {
        return; // TODO
    }

    /**
     * Returns the number of steps in this sequence.
     */
    public synchronized int getSteps() {
        return 0; // TODO
    }

    /**
     * Returns the specified step from this sequence.
     *
     * @param index the index of the step.
     * @return the specified step.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized MeasurementStep getStep(int index) {
        return null; // TODO
    }

    /**
     * Appends a step to this sequence.
     *
     * @param step the measurement step to be added.
     * @throws NullPointerException if step is null.
     */
    public synchronized void addStep(MeasurementStep step) {
        return; // TODO
    }

    /**
     * Adds a step to the specified index of this sequence.
     *
     * @param index the index to which the step will be added.
     * @param step  the measurement step to be added.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > getSteps()).
     * @throws NullPointerException      if step is null.
     */
    public synchronized void addStep(int index, MeasurementStep step) {
        return; // TODO
    }

    /**
     * Removes a step from this sequence.
     *
     * @param index the index of the step to be removed.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized void removeStep(int index) {
        return; // TODO
    }
}