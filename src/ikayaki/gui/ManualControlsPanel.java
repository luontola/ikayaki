/*
* ManualControlsPanel.java
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

import ikayaki.Project;
import ikayaki.squid.Squid;
import ikayaki.squid.Handler;
import ikayaki.gui.ComponentFlasher;

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Magnetometer manual controls. MeasurementControlsPanel disables these whenever a normal measurement step is going.
 *
 * @author Samuli Kaipiainen
 */
public class ManualControlsPanel extends JPanel {
    /**
     * Currently open project.
     */
    private Project project;

    /**
     * Sample hanlder to command.
     */
    private Handler handler;

    /**
     * Groups together all sample holder moving RadioButtons (moveXXX).
     */
    private final ButtonGroup moveButtonGroup = new ButtonGroup();

    /**
     * Moves sample holder to left limit position.
     */
    final JRadioButton moveLeft = new JRadioButton("Left limit");

    /**
     * Moves sample holder to home position.
     */
    final JRadioButton moveHome = new JRadioButton("Home");

    /**
     * Moves sample holder to demagnetize-Z position.
     */
    final JRadioButton moveDemagZ = new JRadioButton("Demag Z");

    /**
     * Moves sample holder to demagnetize-Y position.
     */
    final JRadioButton moveDemagY = new JRadioButton("Demag Y");

    /**
     * Moves sample holder to background position.
     */
    final JRadioButton moveBG = new JRadioButton("BG");

    /**
     * Moves sample holder to measurement position.
     */
    final JRadioButton moveMeasure = new JRadioButton("Measure");

    /**
     * Moves sample holder to right limit position.
     */
    final JRadioButton moveRight = new JRadioButton("Right limit");

    /**
     * Groups together all sample holder rotating RadioButtons (rotateXXX).
     */
    private final ButtonGroup rotateButtonGroup = new ButtonGroup();

    /**
     * Rotates sample holder to angle 0.
     */
    private final JRadioButton rotate0 = new JRadioButton("0°");

    /**
     * Rotates sample holder to angle 90.
     */
    private final JRadioButton rotate90 = new JRadioButton("90°");

    /**
     * Rotates sample holder to angle 180.
     */
    private final JRadioButton rotate180 = new JRadioButton("180°");

    /**
     * Rotates sample holder to angle 270.
     */
    private final JRadioButton rotate270 = new JRadioButton("270°");

    /**
     * Measures X, Y and Z (at current sample holder position) by calling project.doManualMeasure().
     */
    private final JButton measureAllButton = new JButton("Measure All");
    private final ComponentFlasher measureAllButtonFlasher = new ComponentFlasher(measureAllButton);

    /**
     * Resets X, Y and Z by calling project.doManualReset()? Does what?
     */
    private final JButton resetAllButton = new JButton("Reset All?");
    private final ComponentFlasher resetAllButtonFlasher = new ComponentFlasher(resetAllButton);

    /**
     * Demagnetization amplitude in mT, used when demagZ/YButton is clicked.
     */
    private final JTextField demagAmplitudeField = new JTextField();
    private final JLabel demagAmplitudeLabel = new JLabel("mT");
    private final ComponentFlasher demagAmplitudeFieldFlasher = new ComponentFlasher(demagAmplitudeField);

    /**
     * Demagnetizes in Z (at current sample holder position) by calling project.doManualDemagZ(double).
     */
    private final JButton demagZButton = new JButton("Demag in Z");
    private final ComponentFlasher demagZButtonFlasher = new ComponentFlasher(demagZButton);

    /**
     * Demagnetizes in Y (at current sample holder position) by calling project.doManualDemagY(double).
     */
    private final JButton demagYButton = new JButton("Demag in Y");
    private final ComponentFlasher demagYButtonFlasher = new ComponentFlasher(demagYButton);

    // labels for command groups
    final JLabel moveLabel = new JLabel("Move");
    private final JLabel rotateLabel = new JLabel("Rotate");
    private final JLabel measureLabel = new JLabel("Measure");
    private final JLabel demagLabel = new JLabel("Demag");

    // don't say anything about this...
    private final Component[] components = new Component[] {
        moveLeft, moveHome, moveDemagZ, moveDemagY, moveBG, moveMeasure, moveRight,
        rotate0, rotate90, rotate180, rotate270,
        measureAllButton, resetAllButton, demagAmplitudeField, demagAmplitudeLabel, demagZButton, demagYButton,
        moveLabel, rotateLabel, measureLabel, demagLabel
    };

    /**
     * Creates our stupid ManualControlsPanel.
     */
    public ManualControlsPanel() {
        moveButtonGroup.add(moveHome);
        moveButtonGroup.add(moveDemagZ);
        moveButtonGroup.add(moveDemagY);
        moveButtonGroup.add(moveBG);
        moveButtonGroup.add(moveMeasure);
        moveButtonGroup.add(moveRight);

        rotateButtonGroup.add(rotate0);
        rotateButtonGroup.add(rotate90);
        rotateButtonGroup.add(rotate180);
        rotateButtonGroup.add(rotate270);

        moveLabel.setFont(moveLabel.getFont().deriveFont(Font.BOLD));
        rotateLabel.setFont(rotateLabel.getFont().deriveFont(Font.BOLD));
        measureLabel.setFont(measureLabel.getFont().deriveFont(Font.BOLD));
        demagLabel.setFont(demagLabel.getFont().deriveFont(Font.BOLD));

        moveHome.setMargin(new Insets(0, 0, 0, 0));
        moveDemagZ.setMargin(new Insets(0, 0, 0, 0));
        moveDemagY.setMargin(new Insets(0, 0, 0, 0));
        moveBG.setMargin(new Insets(0, 0, 0, 0));
        moveMeasure.setMargin(new Insets(0, 0, 0, 0));

        measureAllButton.setMargin(new Insets(1, 1, 1, 1));
        resetAllButton.setMargin(new Insets(1, 1, 1, 1));
        demagZButton.setMargin(new Insets(1, 1, 1, 1));
        demagYButton.setMargin(new Insets(1, 1, 1, 1));

        JPanel rotatePanel = new JPanel(new BorderLayout());
        JPanel rotateButtonPanel = new JPanel(new BorderLayout());
        rotate0.setHorizontalAlignment(JRadioButton.CENTER);
        rotate180.setHorizontalAlignment(JRadioButton.CENTER);
        rotateButtonPanel.add(rotate0, BorderLayout.NORTH);
        rotateButtonPanel.add(rotate90, BorderLayout.EAST);
        rotateButtonPanel.add(rotate180, BorderLayout.SOUTH);
        rotateButtonPanel.add(rotate270, BorderLayout.WEST);
        rotatePanel.add(rotateLabel, BorderLayout.NORTH);
        rotatePanel.add(rotateButtonPanel, BorderLayout.CENTER);

        JPanel measurePanel = new JPanel(new BorderLayout());
        JPanel measureButtonPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        measureButtonPanel.add(measureAllButton);
        measureButtonPanel.add(resetAllButton);
        measurePanel.add(measureLabel, BorderLayout.NORTH);
        measurePanel.add(measureButtonPanel, BorderLayout.CENTER);

        JPanel demagPanel = new JPanel(new BorderLayout());
        JPanel demagButtonPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        JPanel demagAmplitudePanel = new JPanel(new BorderLayout(4, 0));
        demagAmplitudePanel.add(demagAmplitudeField, BorderLayout.CENTER);
        demagAmplitudePanel.add(demagAmplitudeLabel, BorderLayout.EAST);
        demagButtonPanel.add(demagAmplitudePanel);
        demagButtonPanel.add(demagZButton);
        demagButtonPanel.add(demagYButton);
        demagPanel.add(demagLabel, BorderLayout.NORTH);
        demagPanel.add(demagButtonPanel, BorderLayout.CENTER);

        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        add(rotatePanel);
        add(measurePanel);
        add(demagPanel);

        //setPreferredSize(new Dimension(100, 400));
        //setMaximumSize(new Dimension(100, 400));

        // sample handler to command with move/rotate commands
        getHandler();

        /*
         * Event A: On moveXXX click - call project.doManualMove(int) with clicked position.
         * If false is returned, show small error message. Position values are found from Settings;
         * demagZ is Settings.instance().getAxialAFPosition() and demagY is Settings.instance().getTransverseYAFPosition().
         */

        moveHome.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.moveToHome();
            }
        });

        moveDemagZ.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.moveToDegausserZ();
            }
        });

        moveDemagY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.moveToDegausserY();
            }
        });

        moveBG.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.moveToBackground();
            }
        });

        moveMeasure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.moveToMeasurement();
            }
        });

        /*
         * Event B: On rotateXXX click - call project.doManualRotate(int) with clicked angle. If
         * false is returned, show small error message.
         */

        rotate0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.rotateTo(0);
            }
        });

        rotate90.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.rotateTo(90);
            }
        });

        rotate180.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.rotateTo(180);
            }
        });

        rotate270.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.rotateTo(270);
            }
        });

        /*
         * Event C: On measureAllButton click - call project.doManualMeasure(). If false is returned,
         * show small error message.
         */
        measureAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!project.doManualMeasure()) measureAllButtonFlasher.flash();
            }
        });

        /*
         * Event D: On resetAllButton click - call project.doManualReset()? If false is returned,
         * show small error message.
         */
        resetAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: what to do?
                //if (!project.doManualReset()) resetAllButtonFlasher.flash();
            }
        });

        /*
         * Event E: On DemagZButton click - call project.doManualDemagZ(double) with value
         * from demagAmplitudeField. If false is returned, show small error message.
         */
        demagZButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double amplitude = getDemagAmplitude();
                if (amplitude < 0) demagAmplitudeFieldError();
                else if (!project.doManualDemagZ(amplitude)) demagZButtonFlasher.flash();
            }
        });

        /*
         * Event F: On DemagYButton click - call project.doManualDemagY(double) with value
         * from demagAmplitudeField. If false is returned, show small error message.
         */
        demagYButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double amplitude = getDemagAmplitude();
                if (amplitude < 0) demagAmplitudeFieldError();
                else if (!project.doManualDemagY(amplitude)) demagYButtonFlasher.flash();
            }
        });
    }

    /**
     * Gets sample handler from Squid.instance().getHandler(), saves it to this.handler.
     */
    private void getHandler() {
        try {
            this.handler = Squid.instance().getHandler();
        } catch (IOException ex) { }
    }

    /**
     * Reads demag amplitude from demagAmplitudeField.
     *
     * @return double demagAmplitudeField's double-value, or, -1 if not valid.
     */
    private double getDemagAmplitude() {
        double amplitude;
        try {
            amplitude = Double.valueOf(demagAmplitudeField.getText());
        } catch (NumberFormatException ex) {
            amplitude = -1;
        }

        return amplitude;
    }

    /**
     * Notifies of an error in demagAmplitudeField double-value: requests focus and flashes it.
     */
    private void demagAmplitudeFieldError() {
        //demagAmplitudeField.selectAll();
        demagAmplitudeField.requestFocusInWindow();
        demagAmplitudeFieldFlasher.flash();
    }

    /**
     * Enables/disables all our components. If enabled, also sets selected radioboxes to current handler status.
     *
     * @param enabled true==enabled, false==disabled.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.handler == null) enabled = false;
        for (Component component : components) component.setEnabled(enabled);

        // TODO: if enabled==true set selected radiobexes according to current handler status...
    }

    /**
     * Set active project, enable ourself if it's non-null.
     *
     * @param project active project, or null for none.
     */
    public void setProject(Project project) {
        this.project = project;

        if (this.project == null) setEnabled(false);
        else setEnabled(project.isManualControlEnabled());
    }
}
