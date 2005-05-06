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

    /**
     * Adds one measurement step to this graph and converts the data to x- and y- coordinates
     */
    public void add(MeasurementStep step) {
        if (step.getProject() != null) {
            project = step.getProject();
        }
        Double value = MeasurementValue.RELATIVE_MAGNETIZATION.getValue(step);
        if (value != null) {
            points.add(new Point2D.Double(Math.max(step.getStepValue(), 0.0), Math.max(value.doubleValue(), 0.0)));
        }
    }

    /**
     * Resets the graph data and repaints its contents.
     */
    public void reset() {
        points.clear();
        repaint();
    }

    /**
     * Returns the number of points in this graph.
     *
     * @return the number of points in this graph
     */
    public int getNumMeasurements() {
        return points.size();
    }

    /**
     * Draws the contents of the plot
     *
     * @param w  Width of the drawable area
     * @param h  Height of the drawable area
     * @param g2 Graphics context
     */
    public void render(int w, int h, Graphics2D g2) {
        // margin
        int m = 20;
        // arrow width
        int aw = 4;
        // arrow length
        int al = 8;
        // y-axis padding from arrow top to max values
        int yPad = 20;
        // x-axis padding from arrow top to max values
        int xPad = 20;
        // minimum max value of y-axis
        double yMax = 1.1;
        // minimum max value of x-axis
        double xMax = 100.0;

        for (Point2D point : points) {
            //yMax = Math.max(yMax, Math.min(point.getY(),yMax));
            xMax = Math.max(xMax, point.getX());
        }

        // pixels on y-area
        int yArea = getSize().height - ((2 * m) + yPad);
        // pixels on x-area
        int xArea = getSize().width - ((2 * m) + xPad);
        // font for texts
        g2.setFont(new Font("Arial", Font.PLAIN, 8 + (Math.min(xArea, yArea) / 60)));
        FontMetrics metrics = g2.getFontMetrics();
        // text height
        int txtH = metrics.getHeight();
        // text width
        int txtW = 0;

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

        // 1.0 tick
        int tick1_x1 = xFix;
        int tick1_y1 = yFix - new Double((1.0 / yMax) * yArea).intValue();
        int tick1_x2 = xFix + new Double(5 + 0.02 * xArea).intValue();
        int tick1_y2 = tick1_y1;
        g2.drawLine(tick1_x1, tick1_y1, tick1_x2, tick1_y2);
        // 1.0 number
        txtW = metrics.stringWidth("1.0");
        g2.drawString("1.0", m - (txtW), tick1_y1 + (txtH / 2));

        // x-axis max value
        int tickX_x1 = xFix + new Double(1.0 * xArea).intValue();
        int tickX_y1 = yFix;
        int tickX_x2 = tickX_x1;
        int tickX_y2 = yFix - new Double(5 + 0.02 * yArea).intValue();
        g2.drawLine(tickX_x1, tickX_y1, tickX_x2, tickX_y2);
        // max value
        String maxValStr = new Double(xMax).toString();
        txtW = metrics.stringWidth(maxValStr);
        g2.drawString(maxValStr, tickX_x1 - (txtW / 2), yFix + (m / 2) + (txtH / 2));



        // y-axis unit
        g2.drawString(SequenceColumn.RELATIVE_MAGNETIZATION.getColumnName(project), m + 20, m + 10);
        // x-axis unit
        g2.drawString(SequenceColumn.STEP.getColumnName(project), (w - m) - 30, h - (m + 20));
        // origo 0
        txtW = metrics.stringWidth("0");
        g2.drawString("0", m / 2 - txtW, yFix + (m / 2) + (txtH / 2));

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