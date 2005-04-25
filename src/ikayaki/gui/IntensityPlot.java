/*
 * IntensityPlot.java
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
 * Implements intensity graph plot.
 *
 * @author Aki Sysmäläinen
 */
public class IntensityPlot extends AbstractPlot {

    /**
     * Contains all the data that is shown in this graph.
     */
    private Vector<Point2D> points = new Vector<Point2D>();

    private Project project = null;

    public void add(MeasurementStep step) {
        if (step.getProject() != null) {
            project = step.getProject();
        }
        Double value = MeasurementValue.RELATIVE_MAGNETIZATION.getValue(step);
        if (value != null) {
            points.add(new Point2D.Double(Math.max(step.getStepValue(), 0.0), Math.max(value.doubleValue(), 0.0)));
        }
    }

    public void reset() {
        points.clear();
        repaint();
    }

    public int getNumMeasurements() {
        return points.size();
    }

    /**
     * Draws the contents of the plot
     *
     * @param w
     * @param h
     * @param g2
     */
    public void render(int w, int h, Graphics2D g2) {
        // margin
        int m = 10;
        // arrow width
        int aw = 4;
        // arrow length
        int al = 8;
        // y-axis padding from arrow top to max values
        int yPad = 20;
        // x-axis padding from arrow top to max values
        int xPad = 20;
        // maximum value of y-axis
        double yMax = 1.0;
        // maximum value of x-axis
        double xMax = 100.0;

        for (Point2D point : points) {
            yMax = Math.max(yMax, point.getY());
            xMax = Math.max(xMax, point.getX() * 1.1);
        }

        // pixels on y-area
        int yArea = getSize().height - ((2 * m) + yPad);
        // pixels on x-area
        int xArea = getSize().width - ((2 * m) + xPad);
        // font for texts
        g2.setFont(new Font("Arial", Font.PLAIN, 8 + (Math.min(xArea, yArea) / 60)));

        // x-fix
        int xFix = m + aw;
        // y-fix
        int yFix = h - (m + aw);

        // draw y-axis
        g2.drawLine(xFix, m, xFix, yFix);
        // draw x-axis
        g2.drawLine(xFix, yFix, w - m, yFix);
        // draw y-arrow
        g2.drawLine(m, m + al, m + aw, m);
        g2.drawLine(m + (2 * aw), m + al, m + aw, m);
        // draw x-arrow
        g2.drawLine((w - m) - al, (h - m) - (2 * aw), w - m, (h - m) - aw);
        g2.drawLine((w - m) - al, h - m, w - m, (h - m) - aw);

        // y-axis ticks
        // TODO draw ticks and numbers for y-axis
        // x-axis ticks
        // TODO draw ticks and numbers for x-axis
        // y-axis unit
        g2.drawString(SequenceColumn.RELATIVE_MAGNETIZATION.getColumnName(project), m + 30, m + 10);
        // x-axis unit
        g2.drawString(SequenceColumn.STEP.getColumnName(project), (w - m) - 30, h - (m + 30));
        // origo 0
        g2.drawString("0", m, h - m);

        // draw points
        int ps = (Math.min(xArea, yArea) / 60) + 4; // points size
        for (int i = 0; i < points.size(); i++) {
            int x = new Double((points.elementAt(i).getX() / xMax) * xArea).intValue();
            int y = new Double((points.elementAt(i).getY() / yMax) * yArea).intValue();
            g2.fillOval((xFix + x) - (ps / 2), (yFix - y) - (ps / 2), ps, ps);
        }
        if (points.size() >= 2) {
            for (int i = 1; i < points.size(); i++) {
                int x1 = new Double((points.elementAt(i - 1).getX() / xMax) * xArea).intValue();
                int y1 = new Double((points.elementAt(i - 1).getY() / yMax) * yArea).intValue();
                int x2 = new Double((points.elementAt(i).getX() / xMax) * xArea).intValue();
                int y2 = new Double((points.elementAt(i).getY() / yMax) * yArea).intValue();

                g2.drawLine(xFix + x1, yFix - y1, xFix + x2, yFix - y2);
            }
        }


    }
}