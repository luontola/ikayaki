/*
* SettingsPanel.java
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

import javax.swing.*;

/**
 * Creates its components and updats changes to Settings and saves them in Configuration file
 *
 * @author
 */
public class SettingsPanel extends JFrame {
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

    private JButton saveButton;
    private JButton cancelButton;

    /**
     * Creates all components and puts them in right places. Labels are created only here (no global fields). Creates
     * ActionListeners for buttons.
     */
    public SettingsPanel() {
        return; // TODO
    }

    /**
     * Closes window, no changes saved.
     */
    public void closeWindow() {
        return; // TODO
    }

    /**
     * Saves all settings to Settings-singleton and calls closeWindow().
     */
    public void saveSettings() {
        return; // TODO
    }
}