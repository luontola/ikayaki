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

import java.io.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import ikayaki.*;
import ikayaki.squid.*;
import ikayaki.squid.Handler;

/**
 * Picture of current magnetometer status, with sample holder position and rotation. Status is updated according to
 * MeasurementEvents received by MeasurementControlsPanel.
 *
 * @author Samuli Kaipiainen
 */
public class MagnetometerStatusPanel extends JPanel {

    /**
     * Sample hanlder to read current position and rotation from.
     */
    private Handler handler;

    // handler current position and rotation
    private int position, rotation;

    // handler hard-coded max position and max rotation
    private final int maxposition = 1 << 24, maxrotation = 2000;

    // handler positions, read from Settings, thank you autoboxing!
    // TODO: does autoboxing keep the same Integers in the Hashtable, so updating them would show in JSlider?
    private Integer posHome;
    private Integer posDemagZ;
    private Integer posDemagY;
    private Integer posBG;
    private Integer posMeasurement;

    private final JSlider baseSlider;
    private final Hashtable<Integer,JLabel> baseSliderLabels;

    /**
     * Sets magnetometer status to current position.
     */
    public MagnetometerStatusPanel() {
        super(new BorderLayout());

        updatePositions();
        baseSliderLabels = new Hashtable<Integer,JLabel>();
        baseSliderLabels.put(posHome, new JLabel("Home"));
        baseSliderLabels.put(posDemagZ, new JLabel("Demag Z"));
        baseSliderLabels.put(posDemagY, new JLabel("Demag Y"));
        baseSliderLabels.put(posBG, new JLabel("Background"));
        baseSliderLabels.put(posMeasurement, new JLabel("Measurement"));

        baseSlider = new JSlider(JSlider.VERTICAL, 0, maxposition, 0);
        baseSlider.setEnabled(false);
        baseSlider.setInverted(true);
        baseSlider.setPaintLabels(true);
        baseSlider.setPaintTrack(false);
        baseSlider.setOpaque(false);
        baseSlider.setLabelTable(baseSliderLabels);

        add(baseSlider, BorderLayout.EAST);

        setPreferredSize(new Dimension(150, 400));
        //setMinimumSize(new Dimension(100, 400));

        //updateStatus();
        updateStatus(posHome, 400); // NOTE: for testing
    }

    /**
     * Reads current sample handler from Squid.instance().getHandler(), saves it to this.handler.
     */
    private void getHanlderPosition() {
        try {
            this.handler = Squid.instance().getHandler();
        } catch (IOException ex) { }
    }

    /**
     * Reads handler positions from Settings.
     */
    private void updatePositions() {
        Settings settings = Settings.instance();
        this.posHome = settings.getHandlerSampleLoadPosition();
        this.posDemagZ = settings.getHandlerAxialAFPosition();
        this.posDemagY = settings.getHandlerTransverseYAFPosition();
        this.posBG = settings.getHandlerBackgroundPosition();
        this.posMeasurement = settings.getHandlerMeasurementPosition();
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     *
     * @param position sample holder position, from 1 to 16777215.
     * @param rotation sample holder rotation, from 0 (angle 0) to 2000 (angle 360).
     * @deprecated we read position and rotation ourself in updateStatus().
     */
    public void updateStatus(int position, int rotation) {
        this.position = position;
        this.rotation = rotation;
        updatePositions();
        baseSlider.setValue(this.position);
        repaint();
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     * Reads current handler position and rotation from Handler saved to this.handler.
     */
    public void updateStatus() {
        if (this.handler != null) {
            this.position = this.handler.getPosition();
            this.rotation = this.handler.getRotation();
        }
        updatePositions();
        baseSlider.setValue(this.position);
        repaint();
    }

    /**
     * Paints the magnetometer status picture.
     *
     * @param g mursu.
     */
    protected void paintComponent(Graphics g) {
        // let Swing erase the background
        super.paintComponent(g);

        // use more sophisticated drawing methods
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));

        // save our width and height to be handly available
        int w = getWidth();
        int h = getHeight();

        // sample handler base line x position
        int basex = w / 2;

        // magnetometer boxes' y positions and widths
        int box1y = (int) ((long) h * posDemagZ / maxposition);
        int box2y = (int) ((long) h * posBG / maxposition);
        int box1w = w * 3 / 5;
        int box2w = w * 4 / 5;

        // "sample" width, height and depth, rotation arrow length
        int samplew = w / 3;
        int sampleh = w / 4;
        int sampled = h / 12;
        int arrowlength = w / 6;

        // sample y position
        int sampley = (int) ((long) h * position / maxposition);

        // do the drawing...

        // handler base line
        g2.drawLine(basex, 0, basex, box1y);

        // magnetometer boxes
        g2.drawRect(basex - box1w / 2, box1y, box1w, box2y - box1y);
        g2.drawRect(basex - box2w / 2, box2y, box2w, h - box2y - 2);

        // "sample"
        drawFillOval(g2, Color.WHITE, basex - samplew / 2, sampley - sampled, samplew, sampleh);
        drawFillSideRect(g2, Color.WHITE, basex - samplew / 2, sampley - sampled + sampleh / 2, samplew, sampled);
        drawFillOval(g2, Color.WHITE, basex - samplew / 2, sampley, samplew, sampleh);

        // sample rotation arrow
        drawArrow(g2, basex, sampley + sampleh / 2, arrowlength, rotation);

        // restore original Graphics
        g2.dispose();
    }

    /**
     * Draws a filled oval with line.
     */
    private void drawFillOval(Graphics2D g2, Color fill, int x, int y, int width, int height) {
        Color saved = g2.getColor();
        g2.setColor(fill);
        g2.fillOval(x, y, width, height);
        g2.setColor(saved);
        g2.drawOval(x, y, width, height);
    }

    /**
     * Draws a filled rectangle with lines on left and right side.
     */
    private void drawFillSideRect(Graphics2D g2, Color fill, int x, int y, int width, int height) {
        Color saved = g2.getColor();
        g2.setColor(fill);
        g2.fillRect(x, y, width, height);
        g2.setColor(saved);
        g2.drawLine(x, y, x, y + height);
        g2.drawLine(x + width, y, x + width, y + height);
    }

    /**
     * Draws the rotation arrow.
     *
     * @param g2 marsu.
     * @param x x-center.
     * @param y y-center.
     * @param length arrow length; arrow pointing lines' length will be length/4.
     * @param rotation rotation angle as 0..maxrotation (meaning 0..360 degrees).
     */
    private void drawArrow(Graphics2D g2, int x, int y, int length, int rotation) {
        g2 = (Graphics2D) g2.create();
        g2.translate(x, y);
        g2.rotate(Math.PI * 2 * rotation / maxrotation);
        g2.drawLine(0, -length / 2, 0, length / 2);
        g2.drawLine(0, -length / 2, -length / 4, -length / 2 + length / 4);
        g2.drawLine(0, -length / 2, length / 4, -length / 2 + length / 4);
        g2.dispose();
    }
}
