/*
 * DeviceSettingsPanel.java
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
import ikayaki.Settings;

import javax.comm.CommPortIdentifier;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Creates its components and updates changes to Settings and saves them in Configuration file.
 * These settings are critical for SQUID to work.
 *
 * @author Aki Korpua
 */
public class DeviceSettingsPanel extends JPanel {

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
    private JFormattedTextField xAxisCalibration;

    /**
     * Calibration constants with polarization (factory set?)
     */
    private JFormattedTextField yAxisCalibration;

    /**
     * Calibration constants with polarization (factory set?)
     */
    private JFormattedTextField zAxisCalibration;

    /**
     * how fast demagnetization goes
     */
    private JComboBox demagRamp;

    /**
     * How long SQUID waits on ramping?
     */
    private JComboBox demagDelay;

    /**
     * Handler acceleration
     */
    private JFormattedTextField acceleration;

    /**
     * Handler deceleration
     */
    private JFormattedTextField deceleration;

    /**
     * Handler Max speed
     */
    private JFormattedTextField velocity;

    /**
     * speed in measurement, should be small
     */
    private JFormattedTextField measurementVelocity;

    /**
     * AF demag position for transverse
     */
    private JFormattedTextField transverseYAFPosition;

    /**
     * axial AF demag position in steps, must be divisible by 10. Relative to Home.
     */
    private JFormattedTextField axialAFPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home. (same as Home?)
     */
    private JFormattedTextField sampleLoadPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private JFormattedTextField backgroundPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private JFormattedTextField measurementPosition;

    /**
     * steps to perform full rotation, must be clockwise, determined by sign
     */
    private JFormattedTextField rotation;

    /**
     * rotation velocity
     */
    private JFormattedTextField rotationVelocity;

    /**
     * rotation acceleration
     */
    private JFormattedTextField rotationAcc;

    /**
     * rotation deceleration
     */
    private JFormattedTextField rotationDec;

    /**
     * Maximum field to allow for equipment
     */
    private JFormattedTextField maximumField;

    /**
     * Contains the layout.
     */
    private JPanel contentPane;
    private JLabel warningLabel;

    private JButton saveButton;
    private JButton cancelButton;
    private Action saveAction;
    private Action cancelAction;

    private JDialog creator;

    /**
     * Creates all components and puts them in right places. Labels are created only here (no global fields). Creates
     * ActionListeners for buttons.
     */
    public DeviceSettingsPanel(JDialog creator) {

        this.creator = creator;

        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        /* Warning label */
        warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD, 14.0F));
        warningLabel.setForeground(new Color(0x993333));

        /* load values from Setting */
        this.acceleration.setValue(Settings.getHandlerAcceleration());
        this.deceleration.setValue(Settings.getHandlerDeceleration());
        this.axialAFPosition.setValue(Settings.getHandlerAxialAFPosition());
        this.transverseYAFPosition.setValue(Settings.getHandlerTransverseYAFPosition());
        this.measurementPosition.setValue(Settings.getHandlerMeasurementPosition());
        this.velocity.setValue(Settings.getHandlerVelocity());
        this.measurementVelocity.setValue(Settings.getHandlerMeasurementVelocity());
        this.xAxisCalibration.setValue(Settings.getMagnetometerXAxisCalibration());
        this.yAxisCalibration.setValue(Settings.getMagnetometerYAxisCalibration());
        this.zAxisCalibration.setValue(Settings.getMagnetometerZAxisCalibration());
        this.demagRamp.addItem(3);
        this.demagRamp.addItem(5);
        this.demagRamp.addItem(7);
        this.demagRamp.addItem(9);
        int rampValue = Settings.getDegausserRamp();
        if (rampValue == 3) {
            this.demagRamp.setSelectedIndex(0);
        } else if (rampValue == 5) {
            this.demagRamp.setSelectedIndex(1);
        }
        if (rampValue == 7) {
            this.demagRamp.setSelectedIndex(2);
        } else {
            this.demagRamp.setSelectedIndex(3);
        }
        for (int i = 1; i < 10; i++) {
            this.demagDelay.addItem(i);
        }
        this.demagRamp.setSelectedIndex(Settings.getDegausserDelay() - 1);
        this.sampleLoadPosition.setValue(Settings.getHandlerSampleLoadPosition());
        this.backgroundPosition.setValue(Settings.getHandlerBackgroundPosition());
        this.rotation.setValue(Settings.getHandlerRotation());
        this.rotationVelocity.setValue(Settings.getHandlerRotationVelocity());
        this.rotationAcc.setValue(Settings.getHandlerAcceleration());
        this.rotationDec.setValue(Settings.getHandlerDeceleration());
        this.maximumField.setValue(Settings.getDegausserMaximumField());

        /* Format Number-only Text Fields */
        MyFormatterFactory factory = new MyFormatterFactory();
        acceleration.setFormatterFactory(factory);
        deceleration.setFormatterFactory(factory);
        velocity.setFormatterFactory(factory);
        measurementVelocity.setFormatterFactory(factory);
        transverseYAFPosition.setFormatterFactory(factory);
        axialAFPosition.setFormatterFactory(factory);
        sampleLoadPosition.setFormatterFactory(factory);
        backgroundPosition.setFormatterFactory(factory);
        measurementPosition.setFormatterFactory(factory);
        rotation.setFormatterFactory(factory);
        rotationVelocity.setFormatterFactory(factory);
        rotationAcc.setFormatterFactory(factory);
        rotationDec.setFormatterFactory(factory);
        xAxisCalibration.setFormatterFactory(factory);
        yAxisCalibration.setFormatterFactory(factory);
        zAxisCalibration.setFormatterFactory(factory);
        maximumField.setFormatterFactory(factory);

        /* Find all ports system has */
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();

        ArrayList<String> portList = new ArrayList<String>();

        if (!ports.hasMoreElements()) {
            System.err.println("No comm ports found!");
        } else {
            while (ports.hasMoreElements()) {
                /*
                 *  Get the specific port
                 */
                CommPortIdentifier portId = (CommPortIdentifier) ports.nextElement();

                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    portList.add(portId.getName());
                }
            }
        }
        Collections.sort(portList);

        /* Add all port to lists */
        for (int i = 0; i < portList.size(); i++) {
            this.magnetometerPort.addItem(portList.get(i));
            this.handlerPort.addItem(portList.get(i));
            this.demagnetizerPort.addItem(portList.get(i));
        }

        /* Select currently used port */
        this.magnetometerPort.setSelectedItem(Settings.getMagnetometerPort());
        this.handlerPort.setSelectedItem(Settings.getHandlerPort());
        this.demagnetizerPort.setSelectedItem(Settings.getDegausserPort());

        /* Buttons */
        saveButton.setAction(this.getSaveAction());
        getSaveAction().setEnabled(false);
        cancelButton.setAction(this.getCancelAction());

        /* Update listener for FormattedTextFields */
        DocumentListener saveListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (correctValues()) {
                    getSaveAction().setEnabled(true);
                } else {
                    getSaveAction().setEnabled(false);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (correctValues()) {
                    getSaveAction().setEnabled(true);
                } else {
                    getSaveAction().setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };

        /* Update listener for Comboboxes */
        ActionListener propertiesActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (correctValues()) {
                    getSaveAction().setEnabled(true);
                } else {
                    getSaveAction().setEnabled(false);
                }
            }
        };

        /* Lets set all listeners for changes */
        magnetometerPort.addActionListener(propertiesActionListener);
        demagnetizerPort.addActionListener(propertiesActionListener);
        handlerPort.addActionListener(propertiesActionListener);

        xAxisCalibration.getDocument().addDocumentListener(saveListener);
        yAxisCalibration.getDocument().addDocumentListener(saveListener);
        zAxisCalibration.getDocument().addDocumentListener(saveListener);

        demagRamp.addActionListener(propertiesActionListener);
        demagDelay.addActionListener(propertiesActionListener);

        acceleration.getDocument().addDocumentListener(saveListener);
        deceleration.getDocument().addDocumentListener(saveListener);
        velocity.getDocument().addDocumentListener(saveListener);
        measurementVelocity.getDocument().addDocumentListener(saveListener);

        transverseYAFPosition.getDocument().addDocumentListener(saveListener);
        axialAFPosition.getDocument().addDocumentListener(saveListener);
        sampleLoadPosition.getDocument().addDocumentListener(saveListener);
        backgroundPosition.getDocument().addDocumentListener(saveListener);
        measurementPosition.getDocument().addDocumentListener(saveListener);

        rotation.getDocument().addDocumentListener(saveListener);
        rotationVelocity.addActionListener(propertiesActionListener);
        rotationAcc.addActionListener(propertiesActionListener);
        rotationDec.addActionListener(propertiesActionListener);

        maximumField.addActionListener(propertiesActionListener);

        saveButton.setEnabled(correctValues());
    }

    /**
     * Saves all settings to Settings-singleton and calls closeWindow().
     */
    public void saveSettings() {
        try {
            Settings.setDegausserPort((String) this.demagnetizerPort.getSelectedItem());
            Settings.setDegausserDelay(((Number) this.demagDelay.getSelectedItem()).intValue());
            Settings.setDegausserRamp(((Number) this.demagRamp.getSelectedItem()).intValue());
            Settings.setDegausserMaximumField(((Number) this.maximumField.getValue()).doubleValue());
            Settings.setHandlerPort((String) this.handlerPort.getSelectedItem());
            Settings.setHandlerAcceleration(((Number) this.acceleration.getValue()).intValue());
            Settings.setHandlerAxialAFPosition(((Number) this.axialAFPosition.getValue()).intValue());
            Settings.setHandlerBackgroundPosition(((Number) this.backgroundPosition.getValue()).intValue());
            Settings.setHandlerDeceleration(((Number) this.deceleration.getValue()).intValue());
            Settings.setHandlerMeasurementPosition(((Number) this.measurementPosition.getValue()).intValue());
            Settings.setHandlerMeasurementVelocity(((Number) this.measurementVelocity.getValue()).intValue());
            Settings.setHandlerRotation(((Number) this.rotation.getValue()).intValue());
            Settings.setHandlerSampleLoadPosition(((Number) this.sampleLoadPosition.getValue()).intValue());
            Settings.setHandlerTransverseYAFPosition(((Number) this.transverseYAFPosition.getValue()).intValue());
            Settings.setHandlerVelocity(((Number) this.velocity.getValue()).intValue());
            Settings.setHandlerRotationVelocity(((Number) this.rotationVelocity.getValue()).intValue());
            Settings.setHandlerRotationDeceleration(((Number) this.rotationDec.getValue()).intValue());
            Settings.setHandlerRotationAcceleration(((Number) this.rotationAcc.getValue()).intValue());

            Settings.setMagnetometerPort((String) this.magnetometerPort.getSelectedItem());
            Settings.setMagnetometerXAxisCalibration(((Number) this.xAxisCalibration.getValue()).doubleValue());
            Settings.setMagnetometerYAxisCalibration(((Number) this.yAxisCalibration.getValue()).doubleValue());
            Settings.setMagnetometerZAxisCalibration(((Number) this.zAxisCalibration.getValue()).doubleValue());
            creator.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Should check if COM ports are selected correctly
     *
     * @return boolean
     */
    private boolean correctValues() {
        try {
            //TODO: check COM ports
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Action getSaveAction() {
        if (saveAction == null) {
            saveAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    saveSettings();
                }
            };
            saveAction.putValue(Action.NAME, "Save");
        }
        return saveAction;
    }

    public Action getCancelAction() {
        if (cancelAction == null) {
            cancelAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    creator.setVisible(false);
                }
            };

            cancelAction.putValue(Action.NAME, "Cancel");
            cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
            // TODO: doesn't work
            cancelAction.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        }
        return cancelAction;
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
        contentPane.setLayout(new GridLayoutManager(3, 3, new Insets(11, 11, 11, 11), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
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
        label2.setText("Calibration constant with polarity (Am²/flux quantum)");
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
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel3,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
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
        panel5.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
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
        label9.setText("Maximum Field");
        panel5.add(label9,
                new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        maximumField = new JFormattedTextField();
        maximumField.setHorizontalAlignment(4);
        panel5.add(maximumField,
                new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
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
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel6,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Handler"));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(17, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        rotation = new JFormattedTextField();
        rotation.setHorizontalAlignment(4);
        panel10.add(rotation,
                new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label10 = new JLabel();
        label10.setText("Rotation counts");
        panel10.add(label10,
                new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel10.add(spacer3,
                new GridConstraints(16, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        deceleration = new JFormattedTextField();
        deceleration.setHorizontalAlignment(4);
        panel10.add(deceleration,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label11 = new JLabel();
        label11.setText("Velocity");
        panel10.add(label11,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        velocity = new JFormattedTextField();
        velocity.setHorizontalAlignment(4);
        panel10.add(velocity,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label12 = new JLabel();
        label12.setText("Velocity Meas.");
        panel10.add(label12,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementVelocity = new JFormattedTextField();
        measurementVelocity.setHorizontalAlignment(4);
        panel10.add(measurementVelocity,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label13 = new JLabel();
        label13.setText("Transverse Y AF");
        panel10.add(label13,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        transverseYAFPosition = new JFormattedTextField();
        transverseYAFPosition.setHorizontalAlignment(4);
        panel10.add(transverseYAFPosition,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label14 = new JLabel();
        label14.setText("Translation positions");
        panel10.add(label14,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label15 = new JLabel();
        label15.setText("Axial AF");
        panel10.add(label15,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        axialAFPosition = new JFormattedTextField();
        axialAFPosition.setHorizontalAlignment(4);
        panel10.add(axialAFPosition,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label16 = new JLabel();
        label16.setText("Sample Load");
        panel10.add(label16,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sampleLoadPosition = new JFormattedTextField();
        sampleLoadPosition.setHorizontalAlignment(4);
        panel10.add(sampleLoadPosition,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label17 = new JLabel();
        label17.setText("Background");
        panel10.add(label17,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        backgroundPosition = new JFormattedTextField();
        backgroundPosition.setHorizontalAlignment(4);
        panel10.add(backgroundPosition,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label18 = new JLabel();
        label18.setText("Measurement");
        panel10.add(label18,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementPosition = new JFormattedTextField();
        measurementPosition.setHorizontalAlignment(4);
        panel10.add(measurementPosition,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        rotationAcc = new JFormattedTextField();
        rotationAcc.setHorizontalAlignment(4);
        panel10.add(rotationAcc,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        rotationVelocity = new JFormattedTextField();
        rotationVelocity.setHorizontalAlignment(4);
        rotationVelocity.setText("");
        panel10.add(rotationVelocity,
                new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        rotationDec = new JFormattedTextField();
        rotationDec.setHorizontalAlignment(4);
        panel10.add(rotationDec,
                new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label19 = new JLabel();
        label19.setText("Rotation acc.");
        panel10.add(label19,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label20 = new JLabel();
        label20.setText("Rotation velocity");
        panel10.add(label20,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label21 = new JLabel();
        label21.setText("Rotation dec.");
        panel10.add(label21,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label22 = new JLabel();
        label22.setText("COM port:");
        panel10.add(label22,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        handlerPort = new JComboBox();
        panel10.add(handlerPort,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label23 = new JLabel();
        label23.setText("Acceleration");
        panel10.add(label23,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        acceleration = new JFormattedTextField();
        acceleration.setHorizontalAlignment(4);
        panel10.add(acceleration,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(150, -1), null));
        final JLabel label24 = new JLabel();
        label24.setText("Deceleration");
        panel10.add(label24,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel11,
                new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel11.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel12.add(cancelButton,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer4 = new Spacer();
        panel12.add(spacer4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel12.add(saveButton,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer5 = new Spacer();
        panel12.add(spacer5,
                new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        warningLabel = new JLabel();
        warningLabel.setHorizontalAlignment(0);
        warningLabel.setHorizontalTextPosition(0);
        warningLabel.setText("WARNING! Incorrect configuration may damage the equipment.");
        contentPane.add(warningLabel,
                new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }

    /**
     * Custom formatter factory for the JFormattedTextFields in this class.
     */
    private class MyFormatterFactory
            extends JFormattedTextField.AbstractFormatterFactory {
        /**
         * Returns an <code>AbstractFormatter</code> that can handle formatting of the passed in
         * <code>JFormattedTextField</code>.
         *
         * @param tf JFormattedTextField requesting AbstractFormatter
         * @return AbstractFormatter to handle formatting duties, a null return value implies the JFormattedTextField
         *         should behave like a normal JTextField
         */
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
            NumberFormatter formatter;

            if (tf == xAxisCalibration || tf == yAxisCalibration ||
                    tf == zAxisCalibration) {
                // show all numbers
                DecimalFormat format = new DecimalFormat();
                format.setDecimalSeparatorAlwaysShown(true);
                format.setGroupingUsed(true);
                format.setMaximumFractionDigits(12);
                formatter = new NumberFormatter(format);
            } else if (tf == maximumField) {
                DecimalFormat format = new DecimalFormat();
                format.setDecimalSeparatorAlwaysShown(true);
                format.setGroupingUsed(true);
                format.setMinimumFractionDigits(1);
                format.setMaximumFractionDigits(1);
                formatter = new NumberFormatter(format);

            } else {
                // show Integer
                NumberFormat format = NumberFormat.getIntegerInstance();
                format.setGroupingUsed(true);
                formatter = new NumberFormatter(format);
            }

            // set value ranges
            if (tf == acceleration || tf == deceleration || tf == rotationAcc || tf == rotationDec) {
                formatter.setMinimum(new Integer(0));
                formatter.setMaximum(new Integer(127));
            } else if (tf == velocity || tf == measurementVelocity || tf == rotationVelocity) {
                formatter.setMinimum(new Integer(50));
                formatter.setMaximum(new Integer(20000));
            } else if (tf == measurementPosition || tf == sampleLoadPosition ||
                    tf == backgroundPosition
                    || tf == axialAFPosition || tf == transverseYAFPosition ||
                    tf == measurementPosition) {
                formatter.setMinimum(new Integer(0));
                formatter.setMaximum(new Integer(16777215));
            } else if (tf == rotation) {
                formatter.setMinimum(new Integer(Integer.MIN_VALUE));
                formatter.setMaximum(new Integer(Integer.MAX_VALUE));
            } else if (tf == maximumField) {
                formatter.setMinimum(new Double(1.1));
                formatter.setMaximum(new Double(300.0));
            }
            return formatter;
        }
    }
}
