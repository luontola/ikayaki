/*
* MeasurementControlsPanel.java
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ikayaki.ProjectEvent;
import ikayaki.MeasurementEvent;

/**
 * Has "Measure"/"Pause", "Single step" and "Stop now!" buttons for controlling measurements; "+z/-z" radiobuttons for
 * changing sample orientation used in calculations, help picture for inserting sample, picture of current magnetometer
 * status, and, manual controls.
 * <p/>
 * Listens MeasurementEvents and ProjectEvents, and updates buttons and magnetometer status accordingly.
 *
 * @author Samuli Kaipiainen
 */
public class MeasurementControlsPanel extends ProjectComponent {
    /**
     * Hold all measuring buttons
     */
    private JPanel buttonPanel = new JPanel();

    /**
     * Measure/pause -button; "Measure" when no measuring is being done, "Pause" when there is ongoing measuring
     * sequence.
     */
    private JButton measureButton;

    /**
     * Current button status: false=="Measure", true=="Pause" (tells also whether measuring is in action).
     */
    private boolean measureButtonMeasuring = false;

    private JButton stepButton;

    private JButton stopButton;

    /**
     * Groups together +z and -z RadioButtons.
     */
    private ButtonGroup zButtonGroup;

    /**
     * Changes sample orientation to +Z.
     */
    private JRadioButton zPlusRadioButton;

    /**
     * Changes sample orientation to -Z.
     */
    private JRadioButton zMinusRadioButton;

    /**
     * Draws a help image and text for sample inserting: "Put sample in holder arrow up."
     */
    private JPanel sampleInsertPanel;

    private MagnetometerStatusPanel magnetometerStatusPanel;

    private ManualControlsPanel manualControlsPanel;

    public MeasurementControlsPanel()
    {
        measureButton = new JButton("Measure");
        stepButton = new JButton("Single step");
        stopButton = new JButton("Stop now!");
        measureButton.setEnabled(false);
        stepButton.setEnabled(false);
        stopButton.setEnabled(false);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(measureButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(stopButton);

        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.NORTH);

        /*
        Event A: On measureButton click - call project.doAutoStep() or project.doPause(), depending
        on current button status. Show error message if false is returned.
        */
        measureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                assert project != null;

                boolean ok;
                if (measureButtonMeasuring) ok = project.doPause();
                else ok = project.doAutoStep();

                if (!ok) measureButton.setText(measureButton.getText() + " [error]");
                // TODO or what?
            }
        });

        /**
         * Event B: On stepButton click - call project.doSingleStep(); show error message if false is returned.
         */
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                assert project != null;
                boolean ok = project.doSingleStep();
                if (!ok) stepButton.setText(stepButton.getText() + " [error]");
                // TODO or what?
            }
        });

        /**
         * Event C: On stopButton click - call project.doAbort(); show critical error message if false
         * is returned.
         */
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                assert project != null;
                boolean ok = project.doAbort();
                if (!ok) stopButton.setText(stopButton.getText() + " [error!]");
                // TODO or what?
            }
        });

        /*
        Event D: On zPlus,MinusRadioButton click - call project.setOrientation(boolean) where
        Plus is true and Minus is false.
        */

        return; // TODO
    }

    /**
     * Event E: On ProjectEvent - update buttons and manual controls according to project.isXXXEnabled().
     *
     * @param event ProjectEvent received.
     */
    public void projectUpdated(ProjectEvent event)
    {
        // TODO: set names also?
        measureButton.setEnabled(project.isAutoStepEnabled());
        stepButton.setEnabled(project.isSingleStepEnabled());
        stopButton.setEnabled(project.isAbortEnabled());
    }

    /**
     * Event F: On MeasurementEvent - call magnetometerStatusPanel.updateStatus(int, int)
     * with the right values from MeasurementEvent.
     *
     * @param event MeasurementEvent received.
     */
    public void measurementUpdated(MeasurementEvent event)
    {
        // TODO
    }
}
