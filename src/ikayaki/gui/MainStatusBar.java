/*
* MainStatusBar.java
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

package ikayaki.gui;

import javax.swing.*;

/**
 * Creates its components and listens project events on status change and calculates estimated time for measurement
 *
 * @author
 */
public class MainStatusBar extends ProjectComponent {
/*
Event A: On Measurement Event - recalculates progress and updates status for current
measurement
*/

    /**
     * text comment of current status(moving,measurement,demagnetization)
     */
    private JLabel measurementStatus;

    /**
     * progress of sequence/measurement as per cent of whole process
     */
    private JProgressBar measurementProgress;

    /**
     * current projects sequence
     */
    private int[] currentSequence;

    /**
     * current projects type (we know if we are doing demagnetization or not)
     */
    private int projectType;

    /**
     * Creates all components with default settings and sets Listener for MeasurementEvent.
     */
    public MainStatusBar() {
        add(new JLabel("Status bar"));
        return; // TODO
    }

    /**
     * Recalculates current progress and updates status.
     */
    private void calculateStatus(String phase, int sequenceStep, int currentStep) {
        return; // TODO
    }

    /**
     * Formats status and creates new measurement status values.
     */
    private void setMeasurement(int projectType, int[] sequence) {
        return; // TODO
    }
}