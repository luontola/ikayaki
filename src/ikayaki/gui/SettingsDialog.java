/*
* SettingsDialog.java
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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import ikayaki.Settings;
import javax.comm.CommPortIdentifier;
import java.util.Enumeration;

/**
 * Creates its components and updats changes to Settings and saves them in Configuration file
 *
 * @author Aki Korpua
 */
public class SettingsDialog extends JDialog {
/*
Event A: On Save Clicked - saves current configuration to Settings-singleton and closes
window
*/
/*
Event B: On Cancel Clicked - closes window (discarding changes)
*/

    /**
     * COM port for magnetometer
     */
    private JComboBox magnetometerPort;

    /**
     * COM port for demagnetizer, can be sharing same port with magnetometer
     */
    private JComboBox demagnetizerPort;

    /**
     * COM port for sample handler
     */
    private JComboBox handlerPort;

    /**
     * Calibration constants with polarization (factory set?)
     */
    private JTextField xAxisCalibration;

    /**
     * Calibration constants with polarization (factory set?)
     */
    private JTextField yAxisCalibration;

    /**
     * Calibration constants with polarization (factory set?)
     */
    private JTextField zAxisCalibration;

    /**
     * how fast demagnetization goes
     */
    private JComboBox demagRamp;

    /**
     * ?
     */
    private JComboBox demagDelay;

    /**
     * Handler acceleration
     */
    private JTextField acceleration;

    /**
     * Handler deceleration
     */
    private JTextField deceleration;

    /**
     * Handler Max speed
     */
    private JTextField velocity;

    /**
     * speed in measurement, should be small
     */
    private JTextField measurementVelocity;

    /**
     * AF demag position for transverse
     */
    private JTextField transverseYAFPosition;

    /**
     * axial AF demag position in steps, must be divisible by 10. Relative to Home.
     */
    private JTextField axialAFPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home. (same as Home?)
     */
    private JTextField sampleLoadPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private JTextField backgroundPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private JTextField measurementPosition;

    /**
     * steps to perform full rotation, must be clockwise, determined by sign
     */
    private JTextField rotation;

    /**
     * Refers to right limit switch on translation axis. And usually sample holder motion toward right limit is
     * posivitive direction (default).
     */
    private JComboBox handlerRightLimit;

    private JFormattedTextField maximumField;
    private JCheckBox zAxis;
    private JCheckBox yAxis;
    private JCheckBox xAxis;

    /**
     * Contains the layout.
     */
    private JPanel contentPane;

    private JButton saveButton;
    private JButton cancelButton;

    private SettingsDialog(Frame owner, String message) {
        super(owner, message, true);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    /**
     * Creates all components and puts them in right places. Labels are created only here (no global fields). Creates
     * ActionListeners for buttons.
     */
    protected void dialogInit() {
        super.dialogInit();

        $$$setupUI$$$();
        setResizable(false);
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
        pack();

//        contentPane.addKeyListener(new KeyAdapter() {
//            @Override public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                    closeWindow();
//                }
//            }
//        });

        this.acceleration.setText("" + Settings.instance().getHandlerAcceleration());
        this.deceleration.setText("" + Settings.instance().getHandlerDeceleration());
        this.axialAFPosition.setText("" + Settings.instance().getHandlerAxialAFPosition());
        this.transverseYAFPosition.setText("" + Settings.instance().getHandlerTransverseYAFPosition());
        this.measurementPosition.setText("" + Settings.instance().getHandlerMeasurementPosition());
        this.measurementVelocity.setText(""+ Settings.instance().getHandlerMeasurementVelocity());
        this.xAxisCalibration.setText("" + Settings.instance().getMagnetometerXAxisCalibration());
        this.yAxisCalibration.setText("" + Settings.instance().getMagnetometerYAxisCalibration());
        this.zAxisCalibration.setText("" + Settings.instance().getMagnetometerZAxisCalibration());
        this.demagRamp.addItem(3);
        this.demagRamp.addItem(5);
        this.demagRamp.addItem(7);
        this.demagRamp.addItem(9);
        for(int i = 1;i<10;i++)
          this.demagDelay.addItem(i);
        this.sampleLoadPosition.setText("" + Settings.instance().getHandlerSampleLoadPosition());
        this.backgroundPosition.setText("" + Settings.instance().getHandlerBackgroundPosition());
        this.rotation.setText("" + Settings.instance().getHandlerRotation());
        this.handlerRightLimit.addItem("plus");
        this.handlerRightLimit.addItem("minus");
        this.handlerRightLimit.setSelectedIndex(Settings.instance().getHandlerRightLimit());

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();

        if (ports == null) {
          System.err.println("No comm ports found!");

          return;
        }
        else {
          while (ports.hasMoreElements()) {
            /*
             *  Get the specific port
             */

            CommPortIdentifier portId = (CommPortIdentifier)
                ports.nextElement();

            this.magnetometerPort.addItem(portId.getName());
            this.handlerPort.addItem(portId.getName());
            this.demagnetizerPort.addItem(portId.getName());
          }
        }

        getRootPane().setDefaultButton(saveButton);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
    }

    public static void showSettingsDialog(Frame owner, String message) {
        SettingsDialog d = new SettingsDialog(owner, message);
        d.setVisible(true);
    }


    /**
     * Closes window, no changes saved.
     */
    public void closeWindow() {
        setVisible(false);
    }

    /**
     * Saves all settings to Settings-singleton and calls closeWindow().
     */
    public void saveSettings() {
        return; // TODO
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer !!! IMPORTANT !!! DO NOT edit this method OR call it in your
     * code!
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Magnetometer"));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        magnetometerPort = new JComboBox();
        panel2.add(magnetometerPort,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("COM port:");
        panel2.add(label1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Calibration constant with polarisaty");
        panel2.add(label2,
                new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("X");
        panel2.add(label3,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Y");
        panel2.add(label4,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("Z");
        panel2.add(label5,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        yAxisCalibration = new JFormattedTextField();
        yAxisCalibration.setHorizontalAlignment(4);
        panel2.add(yAxisCalibration,
                new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        xAxisCalibration = new JFormattedTextField();
        xAxisCalibration.setHorizontalAlignment(4);
        panel2.add(xAxisCalibration,
                new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        zAxisCalibration = new JFormattedTextField();
        zAxisCalibration.setHorizontalAlignment(4);
        panel2.add(zAxisCalibration,
                new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Demagnetizer"));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JLabel label6 = new JLabel();
        label6.setText("COM port:");
        panel5.add(label6,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label7 = new JLabel();
        label7.setText("Ramp");
        panel5.add(label7,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label8 = new JLabel();
        label8.setText("Delay");
        panel5.add(label8,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label9 = new JLabel();
        label9.setText("Axes");
        panel5.add(label9,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label10 = new JLabel();
        label10.setText("Maximum Field");
        panel5.add(label10,
                new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        xAxis = new JCheckBox();
        xAxis.setText("X");
        panel5.add(xAxis,
                new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        yAxis = new JCheckBox();
        yAxis.setText("Y");
        panel5.add(yAxis,
                new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        zAxis = new JCheckBox();
        zAxis.setText("Z");
        panel5.add(zAxis,
                new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        maximumField = new JFormattedTextField();
        maximumField.setHorizontalAlignment(4);
        panel5.add(maximumField,
                new GridConstraints(5, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        demagRamp = new JComboBox();
        panel5.add(demagRamp,
                new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        demagDelay = new JComboBox();
        panel5.add(demagDelay,
                new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        demagnetizerPort = new JComboBox();
        panel5.add(demagnetizerPort,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel6,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Handler"));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(14, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("COM port:");
        panel8.add(label11,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        handlerPort = new JComboBox();
        panel8.add(handlerPort,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label12 = new JLabel();
        label12.setText("Acceleration");
        panel8.add(label12,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        acceleration = new JFormattedTextField();
        acceleration.setHorizontalAlignment(4);
        panel8.add(acceleration,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label13 = new JLabel();
        label13.setText("Deceleration");
        panel8.add(label13,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        deceleration = new JFormattedTextField();
        deceleration.setHorizontalAlignment(4);
        panel8.add(deceleration,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label14 = new JLabel();
        label14.setText("Velocity");
        panel8.add(label14,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        velocity = new JFormattedTextField();
        velocity.setHorizontalAlignment(4);
        panel8.add(velocity,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label15 = new JLabel();
        label15.setText("Velocity Meas.");
        panel8.add(label15,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementVelocity = new JFormattedTextField();
        measurementVelocity.setHorizontalAlignment(4);
        panel8.add(measurementVelocity,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label16 = new JLabel();
        label16.setText("Transverse Y AF");
        panel8.add(label16,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        transverseYAFPosition = new JFormattedTextField();
        transverseYAFPosition.setHorizontalAlignment(4);
        panel8.add(transverseYAFPosition,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label17 = new JLabel();
        label17.setText("Translation positions");
        panel8.add(label17,
                new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label18 = new JLabel();
        label18.setText("Axial AF");
        panel8.add(label18,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        axialAFPosition = new JFormattedTextField();
        axialAFPosition.setHorizontalAlignment(4);
        panel8.add(axialAFPosition,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label19 = new JLabel();
        label19.setText("Sample Load");
        panel8.add(label19,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sampleLoadPosition = new JFormattedTextField();
        sampleLoadPosition.setHorizontalAlignment(4);
        panel8.add(sampleLoadPosition,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label20 = new JLabel();
        label20.setText("Background");
        panel8.add(label20,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        backgroundPosition = new JFormattedTextField();
        backgroundPosition.setHorizontalAlignment(4);
        panel8.add(backgroundPosition,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label21 = new JLabel();
        label21.setText("Measurement");
        panel8.add(label21,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementPosition = new JFormattedTextField();
        measurementPosition.setHorizontalAlignment(4);
        panel8.add(measurementPosition,
                new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label22 = new JLabel();
        label22.setText("Rotation");
        panel8.add(label22,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        rotation = new JFormattedTextField();
        rotation.setHorizontalAlignment(4);
        panel8.add(rotation,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label23 = new JLabel();
        label23.setText("Right Limit");
        panel8.add(label23,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        handlerRightLimit = new JComboBox();
        panel8.add(handlerRightLimit,
                new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel9,
                new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel9.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel10.add(cancelButton,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer4 = new Spacer();
        panel10.add(spacer4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel10.add(saveButton,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer5 = new Spacer();
        panel10.add(spacer5,
                new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }
}
