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

import javax.swing.*;

/**
 * Magnetometer manual controls. MeasurementControlsPanel disables these whenever a normal measurement step is going.
 *
 * @author
 */
public class ManualControlsPanel extends JPanel {
/*
Event A: On moveXXX click - call project.doManualMove(int) with clicked position.
If false is returned, show small error message. Position values are found from Settings;
demagZ is Settings.instance().getAxialAFPosition() and demagY is Settings.instance().getTransverseYAFPosition().
*/
/*
Event B: On rotateXXX click - call project.doManualRotate(int) with clicked angle. If
false is returned, show small error message.
*/
/*
Event C: On measureAllButton click - call project.doManualMeasure(). If false is returned,
show small error message.
*/
/*
Event D: On resetAllButton click - call project.doManualReset()? If false is returned,
show small error message.
*/
/*
Event E: On DemagZButton click - call project.doManualDemagZ(double) with value
from demagAmplitudeField. If false is returned, show small error message.
*/
/*
Event F: On DemagYButton click - call project.doManualDemagY(double) with value
from demagAmplitudeField. If false is returned, show small error message.
*/

    /**
     * Groups together all sample holder moving RadioButtons (moveXXX).
     */
    private ButtonGroup moveButtonGroup;

    /**
     * Moves sample holder to home position.
     */
    private JRadioButton moveHome;

    /**
     * Moves sample holder to demagnetize-Z position.
     */
    private JRadioButton moveDemagZ;

    /**
     * Moves sample holder to demagnetize-Y position.
     */
    private JRadioButton moveDemagY;

    /**
     * Moves sample holder to background position.
     */
    private JRadioButton moveBG;

    /**
     * Moves sample holder to measurement position.
     */
    private JRadioButton moveMeasure;

    /**
     * Groups together all sample holder rotating RadioButtons (rotateXXX).
     */
    private ButtonGroup rotateButtonGroup;

    /**
     * Rotates sample holder to angle 0.
     */
    private JRadioButton rotate0;

    /**
     * Rotates sample holder to angle 90.
     */
    private JRadioButton rotate90;

    /**
     * Rotates sample holder to angle 180.
     */
    private JRadioButton rotate180;

    /**
     * Rotates sample holder to angle 270.
     */
    private JRadioButton rotate270;

    /**
     * Measures X, Y and Z (at current sample holder position) by calling project.doManualMeasure().
     */
    private JButton measureAllButton;

    /**
     * Resets X, Y and Z by calling project.doManualReset()? Does what?
     */
    private JButton resetAllButton;

    /**
     * Demagnetization amplitude in mT, used when demagZ,YButton is clicked.
     */
    private JTextField demagAmplitudeField;

    /**
     * Demagnetizes in Z (at current sample holder position) by calling project.doManualDemagZ(double).
     */
    private JButton demagZButton;

    /**
     * Demagnetizes in Y (at current sample holder position) by calling project.doManualDemagY(double).
     */
    private JButton demagYButton;

}