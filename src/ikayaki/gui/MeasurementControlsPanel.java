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

import ikayaki.MeasurementEvent;
import ikayaki.ProjectEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
     * Holds all measuring buttons.
     */
    private JPanel buttonPanel = new JPanel();

    /**
     * Measure/pause -button; "Measure" when no measuring is being done, "Pause" when there is ongoing measuring
     * sequence.
     */
    private JButton measureButton;

    private JButton stepButton;

    private JButton abortButton;

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

    /* Swing Actions */
    private Action autoStepAction;
    private Action singleStepAction;
    private Action pauseAction;
    private Action abortAction;

    public MeasurementControlsPanel() {
        measureButton = new JButton(getAutoStepAction());
        stepButton = new JButton(getSingleStepAction());
        abortButton = new JButton(getAbortAction());
        updateActions();

        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(measureButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(abortButton);

        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.NORTH);

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
    public void projectUpdated(ProjectEvent event) {
        updateActions();
    }

    /**
     * Event F: On MeasurementEvent - call magnetometerStatusPanel.updateStatus(int, int) with the right values from
     * MeasurementEvent.
     *
     * @param event MeasurementEvent received.
     */
    public void measurementUpdated(MeasurementEvent event) {
        // TODO
    }

    /**
     * Checks the current state of the active project and enables/disables the measurement controls accordingly.
     */
    private void updateActions() {
        if (getProject() != null) {
            getAutoStepAction().setEnabled(getProject().isAutoStepEnabled());
            getSingleStepAction().setEnabled(getProject().isSingleStepEnabled());
            getPauseAction().setEnabled(getProject().isPauseEnabled());
            getAbortAction().setEnabled(getProject().isAbortEnabled());
        } else {
            getAutoStepAction().setEnabled(false);
            getSingleStepAction().setEnabled(false);
            getPauseAction().setEnabled(false);
            getAbortAction().setEnabled(false);
        }

        if (getAutoStepAction().isEnabled()) {
            measureButton.setAction(getAutoStepAction());
        } else {
            measureButton.setAction(getPauseAction());
        }
    }

    /* Getters for Swing Actions */

    public Action getAutoStepAction() {
        if (autoStepAction == null) {
            autoStepAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doAutoStep()) {
                        JOptionPane.showMessageDialog(MeasurementControlsPanel.this,
                                "Unable to measure.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            autoStepAction.putValue(Action.NAME, "Measure");
            //autoStepAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
            autoStepAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
            autoStepAction.putValue(Action.SMALL_ICON, new ImageIcon(ClassLoader.getSystemResource("resources/play.png")));
        }
        return autoStepAction;
    }

    public Action getSingleStepAction() {
        if (singleStepAction == null) {
            singleStepAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doSingleStep()) {
                        JOptionPane.showMessageDialog(MeasurementControlsPanel.this,
                                "Unable to single step.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            singleStepAction.putValue(Action.NAME, "Single Step");
            //singleStepAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
            singleStepAction.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
            singleStepAction.putValue(Action.SMALL_ICON, new ImageIcon(ClassLoader.getSystemResource("resources/step.png")));
        }
        return singleStepAction;
    }

    public Action getPauseAction() {
        if (pauseAction == null) {
            pauseAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doPause()) {
                        JOptionPane.showMessageDialog(MeasurementControlsPanel.this,
                                "Unable to pause.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            pauseAction.putValue(Action.NAME, "Pause");
            //pauseAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
            pauseAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
            pauseAction.putValue(Action.SMALL_ICON, new ImageIcon(ClassLoader.getSystemResource("resources/pause.png")));
        }
        return pauseAction;
    }

    public Action getAbortAction() {
        if (abortAction == null) {
            abortAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doAbort()) {
                        JOptionPane.showMessageDialog(MeasurementControlsPanel.this,
                                "Unable to abort.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            abortAction.putValue(Action.NAME, "Stop Now!");
            //abortAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
            abortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
            abortAction.putValue(Action.SMALL_ICON, new ImageIcon(ClassLoader.getSystemResource("resources/stop.png")));
        }
        return abortAction;
    }
}
