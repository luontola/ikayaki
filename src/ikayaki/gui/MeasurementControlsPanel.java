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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private final JButton measureButton;
    private final JButton pauseButton;
    private final JButton stepButton;
    private final JButton abortButton;

    // error-flashers for buttons
    private final ComponentFlasher measureButtonFlasher;
    private final ComponentFlasher pauseButtonFlasher;
    private final ComponentFlasher stepButtonFlasher;
    private final ComponentFlasher abortButtonFlasher;

    /**
     * Groups together +z and -z RadioButtons.
     */
    private final ButtonGroup zButtonGroup;
    private final JRadioButton zPlusRadioButton;
    private final JRadioButton zMinusRadioButton;

    /**
     * Draws a help image and text for sample inserting: "Put sample in holder arrow up."
     */
    private final JPanel sampleInsertPanel;
    private final JLabel sampleInsertTextLabel;
    private final Icon sampleInsertZPlusIcon;
    private final Icon sampleInsertZMinusIcon;
    private final JLabel sampleInsertIconLabel;

    /**
     * Magnetometer manual controls.
     */
    private final MagnetometerStatusPanel.ManualControlsPanel manualControlsPanel;

    /**
     * Magnetometer status panel; also holds move-radiobuttons from ManualControlsPanel.
     */
    private final MagnetometerStatusPanel magnetometerStatusPanel;

    /* Swing Actions */
    private Action autoStepAction;
    private Action singleStepAction;
    private Action calibrateAction;
    private Action pauseAction;
    private Action abortAction;

    public MeasurementControlsPanel() {

        measureButton = new JButton(getAutoStepAction());
        pauseButton = new JButton(getPauseAction());
        stepButton = new JButton(getSingleStepAction());
        abortButton = new JButton(getAbortAction());

        measureButtonFlasher = new ComponentFlasher(measureButton);
        pauseButtonFlasher = new ComponentFlasher(pauseButton);
        stepButtonFlasher = new ComponentFlasher(stepButton);
        abortButtonFlasher = new ComponentFlasher(abortButton);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 2, 2)); // prevents button resize, looks a bit ugly
        buttonPanel.add(measureButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(abortButton);

        zButtonGroup = new ButtonGroup();
        zPlusRadioButton = new JRadioButton("+Z");
        zMinusRadioButton = new JRadioButton("-Z");
        zButtonGroup.add(zPlusRadioButton);
        zButtonGroup.add(zMinusRadioButton);

        JPanel zButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        zButtonPanel.add(zPlusRadioButton);
        zButtonPanel.add(zMinusRadioButton);

        sampleInsertTextLabel = new JLabel("Put sample in holder arrow up.");
        sampleInsertZPlusIcon = new ImageIcon(ClassLoader.getSystemResource("resources/zplus.png"));
        sampleInsertZMinusIcon = new ImageIcon(ClassLoader.getSystemResource("resources/zminus.png"));
        sampleInsertIconLabel = new JLabel();

        sampleInsertPanel = new JPanel(new BorderLayout(4, 4));
        sampleInsertPanel.add(sampleInsertTextLabel, BorderLayout.NORTH);
        sampleInsertPanel.add(zButtonPanel, BorderLayout.CENTER);
        sampleInsertPanel.add(sampleInsertIconLabel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        topPanel.add(sampleInsertPanel, BorderLayout.SOUTH);

        magnetometerStatusPanel = new MagnetometerStatusPanel();
        manualControlsPanel = magnetometerStatusPanel.manualControlsPanel;

        JPanel contentPane = new JPanel(new BorderLayout(0, 8));
        contentPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(magnetometerStatusPanel, BorderLayout.CENTER);
        contentPane.add(manualControlsPanel, BorderLayout.SOUTH);
        this.setLayout(new BorderLayout());
        this.add(contentPane);

        /**
         * Event D: On zPlus/MinusRadioButton click - call project.setOrientation(boolean) where
         * Plus is true and Minus is false.
         */
        zPlusRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getProject().setOrientation(Project.Orientation.PLUS_Z);
                sampleInsertIconLabel.setIcon(sampleInsertZPlusIcon);
            }
        });
        zMinusRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getProject().setOrientation(Project.Orientation.MINUS_Z);
                sampleInsertIconLabel.setIcon(sampleInsertZMinusIcon);
            }
        });

        // initialize with no project
        setProject(null);
    }

    /**
     * Call super.setProject(project), update buttons and manual controls according to project.isXXXEnabled().
     *
     * @param project project opened, or null to open no project.
     */
    @Override public void setProject(Project project) {
        super.setProject(project);
        updateActions();
        if (project != null) {
            setOrientation(project.getOrientation());
            if (project.getSquid() != null) {
                magnetometerStatusPanel.setSquid(project.getSquid());
            }
        }
        manualControlsPanel.setProject(project);
    }

    /**
     * Event E: On ProjectEvent - update buttons and manual controls according to project.isXXXEnabled().
     *
     * @param event ProjectEvent received.
     */
    @Override public void projectUpdated(ProjectEvent event) {
        updateActions();
        manualControlsPanel.setEnabled();
    }

    /**
     * Event F: On MeasurementEvent - call magnetometerStatusPanel.measurementUpdated(MeasurementEvent).
     *
     * @param event MeasurementEvent received.
     */
    @Override public void measurementUpdated(MeasurementEvent event) {
        magnetometerStatusPanel.measurementUpdated(event);
    }

    /**
     * Sets zPlus/Minus radiobutton enabled, and the corresponding image as sample inserting help image.
     */
    private void setOrientation(Project.Orientation orientation) {
        if (orientation == Project.Orientation.PLUS_Z) {
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

            sampleInsertTextLabel.setEnabled(true);
            sampleInsertIconLabel.setEnabled(true);
            zPlusRadioButton.setEnabled(true);
            zMinusRadioButton.setEnabled(true);
        } else {
            getAutoStepAction().setEnabled(false);
            getSingleStepAction().setEnabled(false);
            getCalibrateAction().setEnabled(false);
            getPauseAction().setEnabled(false);
            getAbortAction().setEnabled(false);

            sampleInsertTextLabel.setEnabled(false);
            sampleInsertIconLabel.setEnabled(false);
            zPlusRadioButton.setEnabled(false);
            zMinusRadioButton.setEnabled(false);
        }
    }

    /* Getters for Swing Actions */

    /**
     * Event A: On measureButton click - call project.doAutoStep() or project.doPause(), depending on current button
     * status. Show error message if false is returned.
     */
    public Action getAutoStepAction() {
        if (autoStepAction == null) {
            autoStepAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doAutoStep()) {
                        if (e.getSource() == measureButton) {
                            measureButtonFlasher.flash();
                        } else {
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "Unable to measure.", "Squid Error", JOptionPane.ERROR_MESSAGE);
                        }
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
                        if (e.getSource() == stepButton) {
                            stepButtonFlasher.flash();
                        } else {
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "Unable to single step.", "Squid Error", JOptionPane.ERROR_MESSAGE);
                        }
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
                        if (e.getSource() instanceof JButton) {
                            new ComponentFlasher((JComponent) e.getSource()).flash();
                        } else {
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "Unable to calibrate.", "Squid Error", JOptionPane.ERROR_MESSAGE);
                        }
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
                        if (e.getSource() == pauseButton) {
                            pauseButtonFlasher.flash();
                        } else if (e.getSource() == measureButton) {
                            measureButtonFlasher.flash();
                        } else {
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "Unable to pause.", "Squid Error", JOptionPane.ERROR_MESSAGE);
                        }
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
     * Event C: On stopButton click - call project.doAbort(); show critical error message if false is returned.
     */
    public Action getAbortAction() {
        if (abortAction == null) {
            abortAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (!getProject().doAbort()) {
                        if (e.getSource() == abortButton) {
                            abortButtonFlasher.flash();
                        } else {
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "Unable to abort!", "Squid Error", JOptionPane.ERROR_MESSAGE);
                        }
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
