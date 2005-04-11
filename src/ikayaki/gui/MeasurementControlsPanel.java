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
import ikayaki.Project;
import ikayaki.ProjectEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
     * Measure/pause -button; "Measure" when no measuring is being done, "Pause" when there is ongoing measuring
     * sequence.
     */
    private final JButton measureButton;
    private final JButton stepButton;
    private final JButton abortButton;

    /**
     * Groups together +z and -z RadioButtons.
     */
    private final ButtonGroup zButtonGroup;

    /**
     * Changes sample orientation to +Z.
     */
    private final JRadioButton zPlusRadioButton;

    /**
     * Changes sample orientation to -Z.
     */
    private final JRadioButton zMinusRadioButton;

    /**
     * Draws a help image and text for sample inserting: "Put sample in holder arrow up."
     */
    private final JPanel sampleInsertPanel;
    private final Icon sampleInsertZPlusIcon;
    private final Icon sampleInsertZMinusIcon;
    private final JLabel sampleInsertIconLabel;

    private final MagnetometerStatusPanel magnetometerStatusPanel;

    private final ManualControlsPanel manualControlsPanel;

    /* Swing Actions */
    private Action autoStepAction;
    private Action singleStepAction;
    private Action calibrateAction;
    private Action pauseAction;
    private Action abortAction;

    public MeasurementControlsPanel() {
        measureButton = new JButton(getAutoStepAction());
        stepButton = new JButton(getSingleStepAction());
        abortButton = new JButton(getAbortAction());
        updateActions();

        //JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 2, 2)); // prevents button resize, looks a bit ugly
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 2, 2)); // prevents button resize, looks a bit ugly
        //JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        buttonPanel.add(measureButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(abortButton);

        zButtonGroup = new ButtonGroup();
        zPlusRadioButton = new JRadioButton("+z", true);
        zMinusRadioButton = new JRadioButton("-z");
        zButtonGroup.add(zPlusRadioButton);
        zButtonGroup.add(zMinusRadioButton);

        JPanel zButtonPanel = new JPanel(new GridLayout(2, 1));
        zButtonPanel.add(zPlusRadioButton);
        zButtonPanel.add(zMinusRadioButton);

        JLabel sampleUpLabel = new JLabel("Put sample in holder arrow up");
        sampleInsertZPlusIcon = new ImageIcon(ClassLoader.getSystemResource("resources/zplus.png"));
        sampleInsertZMinusIcon = new ImageIcon(ClassLoader.getSystemResource("resources/zminus.png"));
        sampleInsertIconLabel = new JLabel(sampleInsertZPlusIcon);

        sampleInsertPanel = new JPanel(new BorderLayout(8, 4));
        sampleInsertPanel.add(sampleUpLabel, BorderLayout.NORTH);
        sampleInsertPanel.add(sampleInsertIconLabel, BorderLayout.WEST);
        sampleInsertPanel.add(zButtonPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        topPanel.add(sampleInsertPanel, BorderLayout.SOUTH);

        magnetometerStatusPanel = new MagnetometerStatusPanel();
        manualControlsPanel = new ManualControlsPanel();

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(manualControlsPanel, BorderLayout.WEST);
        add(magnetometerStatusPanel, BorderLayout.EAST);

        /**
         * Event D: On zPlus/MinusRadioButton click - call project.setOrientation(boolean) where
         * Plus is true and Minus is false.
         */
        zPlusRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getProject().setOrientation(true);
                sampleInsertIconLabel.setIcon(sampleInsertZPlusIcon);
            }
        });
        zMinusRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getProject().setOrientation(false);
                sampleInsertIconLabel.setIcon(sampleInsertZMinusIcon);
            }
        });

        return; // TODO
    }

    @Override public void setProject(Project project) {
        super.setProject(project);
        updateActions();
        if (project != null) setOrientation(project.getOrientation());
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
     * Sets zPlus/Minus radiobutton enabled, and the corresponding image as sample inserting help image.
     *
     * @param orientation true for +z, false for -z.
     */
    private void setOrientation(boolean orientation) {
        if (orientation) {
            zPlusRadioButton.setSelected(true);
            sampleInsertIconLabel.setIcon(sampleInsertZPlusIcon);
        } else {
            zMinusRadioButton.setSelected(true);
            sampleInsertIconLabel.setIcon(sampleInsertZMinusIcon);
        }
    }

    /**
     * Checks the current state of the active project and enables/disables the measurement controls accordingly.
     */
    private void updateActions() {
        if (getProject() != null) {
            getAutoStepAction().setEnabled(getProject().isAutoStepEnabled());
            getSingleStepAction().setEnabled(getProject().isSingleStepEnabled());
            getCalibrateAction().setEnabled(getProject().isSingleStepEnabled()
                    && getProject().getType() == Project.Type.CALIBRATION);
            getPauseAction().setEnabled(getProject().isPauseEnabled());
            getAbortAction().setEnabled(getProject().isAbortEnabled());
        } else {
            getAutoStepAction().setEnabled(false);
            getSingleStepAction().setEnabled(false);
            getCalibrateAction().setEnabled(false);
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

    /**
     * Event A: On measureButton click - call project.doAutoStep() or project.doPause(), depending
     * on current button status. Show error message if false is returned.
     */
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
            autoStepAction.putValue(Action.SMALL_ICON,
                    new ImageIcon(ClassLoader.getSystemResource("resources/play.png")));
        }
        return autoStepAction;
    }

    /**
     * Event B: On stepButton click - call project.doSingleStep(); show error message if false is returned.
     */
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
            singleStepAction.putValue(Action.SMALL_ICON,
                    new ImageIcon(ClassLoader.getSystemResource("resources/step.png")));
        }
        return singleStepAction;
    }

    public Action getCalibrateAction() {
        if (calibrateAction == null) {
            calibrateAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doSingleStep()) {
                        JOptionPane.showMessageDialog(MeasurementControlsPanel.this,
                                "Unable to calibrate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            calibrateAction.putValue(Action.NAME, "Calibrate");
            //calibrateAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
            //calibrateAction.putValue(Action.ACCELERATOR_KEY,
            //        KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
            calibrateAction.putValue(Action.SMALL_ICON,
                    new ImageIcon(ClassLoader.getSystemResource("resources/step.png")));
        }
        return calibrateAction;
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
            pauseAction.putValue(Action.SMALL_ICON,
                    new ImageIcon(ClassLoader.getSystemResource("resources/pause.png")));
        }
        return pauseAction;
    }

    /**
     * Event C: On stopButton click - call project.doAbort(); show critical error message if false
     * is returned.
     */
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
