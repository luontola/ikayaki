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

import javax.swing.*;

/**
 * Has "Measure"/"Pause", "Single step" and "Stop now!" buttons for controlling measurements; "+z/-z" radiobuttons for
 * changing sample orientation used in calculations, help picture for inserting sample, picture of current magnetometer
 * status, and, manual controls. Listens MeasurementEvents and ProjectEvents, and updates buttons and magnetometer
 * status accordingly.
 *
 * @author
 */
public class MeasurementControlsPanel extends ProjectComponent {
/*
Event A: On measureButton click - call project.doAutoStep() or project.doPause(), depending
on current button status. Show error message if false is returned.
*/
/*
Event B: On singlestepButton click - call project.doSingleStep(); show error message if
false is returned.
*/
/*
Event C: On stopButton click - call project.doAbort(); show critical error message if false
is returned.
*/
/*
Event D: On zPlus,MinusRadioButton click - call project.setOrientation(boolean) where
Plus is true and Minus is false.
*/
/*
Event E: On ProjectEvent - update buttons and manual controls according to project.isXXXEnabled().
*/
/*
Event F: On MeasurementEvent - call magnetometerStatusPanel.updateStatus(int, int)
with the right values from MeasurementEvent.
*/

    /**
     * Measure/pause -button; "Measure" when no measuring is being done, "Pause" when there is ongoing measuring
     * sequence.
     */
    private JButton measureButton;

    private JButton singlestepButton;

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

}