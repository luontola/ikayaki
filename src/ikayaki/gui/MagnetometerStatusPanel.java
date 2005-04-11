/*
* MagnetometerStatusPanel.java
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

import ikayaki.squid.Squid;

import java.awt.*;
import javax.swing.*;
import java.io.*;

/**
 * Picture of current magnetometer status, with sample holder position and rotation. Status is updated according to
 * MeasurementEvents received by MeasurementControlsPanel.
 *
 * @author Samuli Kaipiainen
 */
public class MagnetometerStatusPanel extends JPanel {

    private int position, rotation;
    private int maxposition = 1 << 16, maxrotation = 2000;

    /**
     * Sets magnetometer status to current position.
     */
    public MagnetometerStatusPanel() {
        setPreferredSize(new Dimension(300, 400));
        //setMinimumSize(new Dimension(300, 400));
        //updateStatus();
        updateStatus(12345678, 400);
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     *
     * @param position sample holder position, from 1 to 16777215.
     * @param rotation sample holder rotation, from 0 (angle 0) to 2000 (angle 360).
     * @deprecated we read position and rotation ourself
     */
    public void updateStatus(int position, int rotation) {
        this.position = position;
        this.rotation = rotation;
        repaint();
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     * Reads current handler position and rotation from Squid.getHandler().
     */
    public void updateStatus() {
        // TODO: this is where to read current status?
        try {
            this.position = Squid.instance().getHandler().getPosition();
            this.rotation = Squid.instance().getHandler().getRotation();
        } catch (IOException ex) { }
        repaint();
    }

    /**
     * Paints the magnetometer status picture.
     *
     * @param g mursu
     */
    public void paintComponent(Graphics g) {
        // let Swing erase the background
        super.paintComponent(g);

        // save our width and height to be handly available
        int w = getWidth();
        int h = getHeight();

        // sample handler base line x position
        int base1x = w / 3;

        // magnetometer boxes' y positions and widths
        // TODO: read position marks from Settings
        int box1y = h / 2;
        int box2y = box1y + h / 8;
        int box1w = w / 5;
        int box2w = w / 3;

        // moving sample handler base line x position
        int base2x = 3*w / 4;

        // "sample" width, height and depth, rotation arrow length
        int samplew = w / 3;
        int sampleh = w / 4;
        int sampled = h / 12;
        int rotl = w / 5;

        // sample position
        int samplep = position / maxposition;

        // do the drawing...

        g.drawLine(base1x, 0, base1x, box1y);

        // magnetometer boxes
        g.drawRect(base1x - box1w / 2, box1y, box1w, box2y - box1y);
        g.drawRect(base1x - box2w / 2, box2y, box2w, h - box2y);

        g.drawLine(base2x, 0, base2x, h);

        // "sample"
        Color saved = getBackground();
        setBackground(Color.WHITE);
        drawFillOval(g, base2x - samplew / 2, samplep - sampled, samplew, sampleh);
        drawFillSideRect(g, base2x - samplew / 2, samplep - sampled + sampleh / 2, samplew, sampled);
        drawFillOval(g, base2x - samplew / 2, samplep, samplew, sampleh);
        setBackground(saved);
    }

    /**
     * Draws a getBackground-filled oval with line.
     */
    private void drawFillOval(Graphics g, int x, int y, int width, int height) {
        Color saved = g.getColor();
        g.setColor(getBackground());
        g.fillOval(x, y, width, height);
        g.setColor(saved);
        g.drawOval(x, y, width, height);
    }

    /**
     * Draws a getBackground-filled rectangle with lines on left and right side.
     */
    private void drawFillSideRect(Graphics g, int x, int y, int width, int height) {
        Color saved = g.getColor();
        g.setColor(getBackground());
        g.fillRect(x, y, width, height);
        g.setColor(saved);
        g.drawLine(x, y, x, y + height);
        g.drawLine(x + width, y, x + width, y + height);
    }
}
