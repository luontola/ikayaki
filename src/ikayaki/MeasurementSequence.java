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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of measurement steps. Steps can be added or removed from the sequence.
 * <p/>
 * All operations are thread-safe.
 *
 * @author Esko Luontola
 */
public class MeasurementSequence implements Comparable<MeasurementSequence> {

    /**
     * Name of the sequence. Empty string if it has no name.
     */
    private String name;

    /**
     * The measurement steps of this sequence.
     */
    private List<MeasurementStep> steps = new ArrayList<MeasurementStep>();

    /**
     * Creates an empty sequence with no name.
     */
    public MeasurementSequence() {
        setName("");
    }

    /**
     * Creates an empty sequence with the specified name.
     *
     * @param name name of the sequence.
     * @throws NullPointerException if name is null.
     */
    public MeasurementSequence(String name) {
        setName(name);
    }

    /**
     * Creates a sequence from the specified element.
     *
     * @param element the element from which this sequence will be created.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementSequence(Element element) {
        this(element, null);
    }

    /**
     * Creates a sequence from the specified element for a project.
     *
     * @param element the element from which this sequence will be created.
     * @param project the project whose sequence this will be, or null if this is not owned by a project. Needed for
     *                importing the measurement steps correctly.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementSequence(Element element, Project project) {
        if (element == null) {
            throw new NullPointerException();
        }

        // verify tag name
        if (!element.getTagName().equals("sequence")) {
            throw new IllegalArgumentException("Invalid tag name: " + element.getTagName());
        }

        // get name
        setName(element.getAttribute("name"));

        // get steps
        NodeList steps = element.getElementsByTagName("step");
        for (int i = 0; i < steps.getLength(); i++) {
            Element step = (Element) steps.item(i);
            this.steps.add(new MeasurementStep(step, project));
        }
    }

    /**
     * Exports this sequence to a DOM element.
     *
     * @param document the document that will contain this element.
     */
    public synchronized Element getElement(Document document) {
        Element element = document.createElement("sequence");
        element.setAttribute("name", name);
        for (MeasurementStep step : steps) {
            element.appendChild(step.getElement(document));
        }
        return element;
    }

    /**
     * Returns the name of this sequence.
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * Sets the name of this sequence.
     *
     * @throws NullPointerException if name is null.
     */
    public synchronized void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    /**
     * Returns the number of steps in this sequence.
     */
    public synchronized int getSteps() {
        return steps.size();
    }

    /**
     * Returns the specified step from this sequence.
     *
     * @param index the index of the step.
     * @return the specified step.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized MeasurementStep getStep(int index) {
        return steps.get(index);
    }

    /**
     * Appends a step to this sequence.
     *
     * @param step the measurement step to be added.
     * @throws NullPointerException if step is null.
     */
    public synchronized void addStep(MeasurementStep step) {
        if (step == null) {
            throw new NullPointerException();
        }
        steps.add(step);
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
        if (step == null) {
            throw new NullPointerException();
        }
        steps.add(index, step);
    }

    /**
     * Removes a step from this sequence.
     *
     * @param index the index of the step to be removed.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= getSteps()).
     */
    public synchronized void removeStep(int index) {
        steps.remove(index);
    }

    /**
     * Orders the sequences by their name. If two different sequences have the same name, one of them if always greater
     * than the other.
     *
     * @param other the sequence to be compared to.
     * @return less than 0 if this precedes other, or 0 if they are the same sequence, or else greater than 0.
     */
    public int compareTo(MeasurementSequence other) {
        int val = this.getName().compareTo(other.getName());
        if (val == 0) {
            return this.hashCode() - other.hashCode();
        } else {
            return 0;
        }
    }
}