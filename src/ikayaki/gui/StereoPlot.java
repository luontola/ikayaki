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

    /**
     * Adds one measurement step to this graph and converts the data to x- and y- coordinates
     */
    public void add(MeasurementStep step) {
        if (step.getProject() != null) {
            project = step.getProject();
        }
        Double incValue = MeasurementValue.INCLINATION.getValue(step);
        Double decValue = MeasurementValue.DECLINATION.getValue(step);

        if (incValue != null && decValue != null) {
            if (incValue.doubleValue() >= 0) {
                points.add(toXY(decValue, incValue));
                incSign.add(new Boolean(true));
            } else {
                points.add(toXY(decValue, incValue));
                incSign.add(new Boolean(false));
            }
        }
    }

    /**
     * Does the stereoplot projection for given declination and inclination values.
     *
     * @param decValue declination
     * @param incValue inclination
     * @return a stereoplot projected point in XY-coordinates
     */

    // TODO Now the projection is linear. It should be biased but how much?
    private Point2D.Double toXY(Double decValue, Double incValue) {
        double inc = Math.toRadians(incValue);
        double dec = Math.toRadians(decValue);
        double x = 0.5 + ((0.5 - (0.5 / (Math.PI / 2.0)) * Math.abs(inc)) * Math.cos(dec - (Math.PI / 2.0)));
        double y = 0.5 + ((0.5 - (0.5 / (Math.PI / 2.0)) * Math.abs(inc)) * Math.sin(dec + (Math.PI / 2.0)));
        return new Point2D.Double(x, y);
    }

    /**
     * Resets and repaints this plot.
     */
    public void reset() {
        points.clear();
        incSign.clear();
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
        int m = 5;
        // area for texts on edges
        int txtArea = 10;
        // minimum dimension of the plot
        int dim = Math.min(w, h);
        // area for points in x and y direction = width = height of the actual plot
        int area = dim - (2 * (m + txtArea));
        // font for texts
        g2.setFont(new Font("Arial", Font.PLAIN, 8 + (area / 60)));
        FontMetrics metrics = g2.getFontMetrics();
        // text width for letter W
        int txtW = 0;
        // text height
        int txtH = metrics.getHeight();

        // draw circle
        g2.drawOval(m + txtArea, m + txtArea, area, area);
        // draw ticks
        int tLength = 5; // circle ticks length in degrees
        int atLength = (area / 40) + 3; // horizontal and vertical ticks length

        for (int i = 0; i < 360; i = i + 10) {
            int x1 = new Double(toXY(new Double(i), new Double(0)).getX() * area).intValue();
            int y1 = new Double(toXY(new Double(i), new Double(0)).getY() * area).intValue();
            int x2, y2;
            if (i % 90 == 0) {
                x2 = new Double(toXY(new Double(i), new Double(tLength * 2)).getX() * area).intValue();
                y2 = new Double(toXY(new Double(i), new Double(tLength * 2)).getY() * area).intValue();
            } else {
                x2 = new Double(toXY(new Double(i), new Double(tLength)).getX() * area).intValue();
                y2 = new Double(toXY(new Double(i), new Double(tLength)).getY() * area).intValue();
            }
            g2.drawLine((m + txtArea) + x1, (m + txtArea) + y1, (m + txtArea) + x2, (m + txtArea) + y2);
        }

        for (int i = 10; i <= 90; i = i + 10) {
            int x1, y1, x2, y2;
            x1 = (new Double(toXY(new Double(0), new Double(i)).getX() * area).intValue()) - (atLength / 2);
            y1 = new Double(toXY(new Double(0), new Double(i)).getY() * area).intValue();
            x2 = x1 + atLength;
            y2 = y1;
            g2.drawLine((m + txtArea) + x1, (m + txtArea) + y1, (m + txtArea) + x2, (m + txtArea) + y2);
        }
        for (int i = 10; i <= 90; i = i + 10) {
            int x1, y1, x2, y2;
            x1 = new Double(toXY(new Double(90), new Double(i)).getX() * area).intValue();
            y1 = new Double(toXY(new Double(90), new Double(i)).getY() * area).intValue() - (atLength / 2);
            x2 = x1;
            y2 = y1 + atLength;
            g2.drawLine((m + txtArea) + x1, (m + txtArea) + y1, (m + txtArea) + x2, (m + txtArea) + y2);
        }
        for (int i = 10; i <= 90; i = i + 10) {
            int x1, y1, x2, y2;
            x1 = (new Double(toXY(new Double(180), new Double(i)).getX() * area).intValue()) - (atLength / 2);
            y1 = new Double(toXY(new Double(180), new Double(i)).getY() * area).intValue();
            x2 = x1 + atLength;
            y2 = y1;
            g2.drawLine((m + txtArea) + x1, (m + txtArea) + y1, (m + txtArea) + x2, (m + txtArea) + y2);
        }
        for (int i = 10; i <= 90; i = i + 10) {
            int x1, y1, x2, y2;
            x1 = new Double(toXY(new Double(270), new Double(i)).getX() * area).intValue();
            y1 = (new Double(toXY(new Double(270), new Double(i)).getY() * area).intValue() - (atLength / 2));
            x2 = x1;
            y2 = y1 + atLength;
            g2.drawLine((m + txtArea) + x1, (m + txtArea) + y1, (m + txtArea) + x2, (m + txtArea) + y2);
        }


        // draw symbols
        txtW = metrics.stringWidth("N");
        g2.drawString("N", m + txtArea + (area / 2) - (txtW / 2), m + (txtArea / 2));
        txtW = metrics.stringWidth("W");
        g2.drawString("W", m + (txtArea / 2) - txtW, m + txtArea + (area / 2) + (txtH / 2));
        txtW = metrics.stringWidth("E");
        g2.drawString("E", m + txtArea + area + (txtArea / 2), m + txtArea + (area / 2) + (txtH / 2));
        txtW = metrics.stringWidth("S");
        g2.drawString("S", m + txtArea + (area / 2) - (txtW / 2), m + txtArea + area + (txtArea / 2) + (txtH / 2));

        // draw points
        int ps = (area / 60) + 4; // points size
        for (int i = 0; i < points.size(); i++) {
            int x = (m + txtArea) + new Double(points.elementAt(i).getX() * area).intValue();
            int y = (m + txtArea) + area - new Double(points.elementAt(i).getY() * area).intValue();
            if (i == 0) {
                g2.drawString("NRM", x + (area / 50), y - (area / 50));
            }

            if (incSign.elementAt(i).booleanValue()) { // positive inclination
                g2.fillOval(x - (ps / 2), y - (ps / 2), ps, ps);
            } else { // negative inclination
                g2.drawOval(x - (ps / 2), y - (ps / 2), ps, ps);
            }
        }

        // draw lines
        if (points.size() >= 2) {
            for (int i = 1; i < points.size(); i++) {
                int x1 = new Double((points.elementAt(i - 1).getX() * area)).intValue();
                int y1 = new Double((points.elementAt(i - 1).getY() * area)).intValue();
                int x2 = new Double((points.elementAt(i).getX() * area)).intValue();
                int y2 = new Double((points.elementAt(i).getY() * area)).intValue();

                g2.drawLine((m + txtArea) + x1, (m + txtArea) + area - y1, (m + txtArea) + x2,
                        (m + txtArea) + area - y2);
            }
        }
    }
}
