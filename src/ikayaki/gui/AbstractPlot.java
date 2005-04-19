/*
 * AbstractPlot.java
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
import java.util.Vector;

/**
 * Abstract class that implements general construction of a graphical plot.
 *
 * @author
 */
public abstract class AbstractPlot extends JPanel {

    /**
     * Contains all the data that is shown in this graph.
     */
    private Vector<Measurement> measurement = null;

    private class Measurement {
        // TODO: maybe this class should not exist?
    }

    /**
     * Adds new measurement data to plot.
     *
     * @param declination Declination coordinate of the measurement.
     * @param inclination Inclination coordinate of the measurement.
     */
    public void addMeasurement(int declination, int inclination) {
        return; // TODO
    }

    /**
     * High lights measurement in plot in the given index.
     *
     * @param index Index of measurement to be highlighted.
     * @throws IndexOutOfBoundsException If no such measurement existed.
     */
    public void highlightMeasurement(int index) {
        return; // TODO
    }

    /**
     * Highlights a set of measurements in given range.
     *
     * @param from Starting index of highlighted measurements.
     * @param to   End index of highlighted meeasurements.
     * @throws IndexOutOfBoundsException If one or both of the indices are out of bounds.
     */
    public void highlightMeasurementRange(int from, int to) {
        return; // TODO
    }

    /**
     * dehighlights all values in this graph.
     */
    public void unHighlightAll() {
        return; // TODO
    }

    /**
     * Removes all measurements from the graph.
     */
    public void resetGraph() {
        return; // TODO
    }

    /**
     * Returns the number of measurements in this graph.
     *
     * @return Number of measurements.
     */
    public int getNumMeasurements() {
        return 0; // TODO
    }
}