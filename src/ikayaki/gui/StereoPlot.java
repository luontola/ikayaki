/*
 * StereoPlot.java
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

import ikayaki.MeasurementStep;
import ikayaki.MeasurementValue;
import ikayaki.Project;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Vector;


/**
 * Implements stereographic plot
 *
 * @author Aki Sysmäläinen
 */
public class StereoPlot extends AbstractPlot {

    /**
     * Contains all the data that is shown in this graph.
     */
    private Vector<Point2D> points = new Vector<Point2D>();

    /**
     * Contains information if inclination was positive or negative positive = true, negative = false; 0 is positive
     */
    private Vector<Boolean> incSign = new Vector<Boolean>();

    private Project project = null;

    public void add(MeasurementStep step) {
        if (step.getProject() != null) {
            project = step.getProject();
        }
        Double inc = MeasurementValue.INCLINATION.getValue(step);
        Double dec = MeasurementValue.DECLINATION.getValue(step);

        if (inc != null && dec != null) {
            double x = 0.5 + ((0.5 - (0.5 / 90) * Math.abs(inc)) * Math.cos(dec - 90));
            double y = 0.5 + ((0.5 - (0.5 / 90) * Math.abs(inc)) * Math.sin(dec + 90));
            if (inc >= 0) {
                points.add(new Point2D.Double(x, y));
                incSign.add(new Boolean(true));
            } else {
                points.add(new Point2D.Double(x, y));
                incSign.add(new Boolean(false));
            }
        }
    }

    public void reset() {
        points.clear();
        repaint();
    }

    public int getNumMeasurements() {
        return points.size();
    }

    public void render(int w, int h, Graphics2D g2) {
        // margin
        int m = 10;
        // area for texts on edges
        int txtArea = 20;
        // area for points in x direction
        int xArea = w - (2 * (m + txtArea));
        // area for points in y direction
        int yArea = h - (2 * (m + txtArea));
        // font for texts
        g2.setFont(new Font("Arial", Font.PLAIN, 10));

        // draw circle
        g2.drawOval(m + txtArea, m + txtArea, xArea, yArea);
        // draw ticks

        // draw symbols
        g2.drawString("N", m + txtArea + (xArea / 2), m + txtArea);
        g2.drawString("W", m + txtArea, m + txtArea + (yArea / 2));
        g2.drawString("E", m + txtArea + xArea, m + txtArea + (yArea / 2));
        g2.drawString("S", m + txtArea + (xArea / 2), m + txtArea + yArea);

        // draw points
        for (int i = 0; i < points.size(); i++) {
            int x = (m + txtArea) + new Double(points.elementAt(i).getX() * xArea).intValue();
            int y = (m + txtArea) + yArea - new Double(points.elementAt(i).getY() * yArea).intValue();

            if (incSign.elementAt(i).booleanValue()) { // positive inclination
                g2.fillOval(x - 2, y - 2, 4, 4);
            } else { // negative inclination
                g2.drawOval(x - 2, y - 2, 4, 4);
            }
        }
    }
}
