/*
* MagnetometerStatusPanel.java
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
 * Picture of current magnetometer status, with sample holder position and rotation. Status is updated according to
 * MeasurementEvents received by MeasurementControlsPanel.
 *
 * @author
 */
public class MagnetometerStatusPanel extends JPanel {

    /**
     * Sets magnetometer status to current position.
     */
    public MagnetometerStatusPanel() {
        return; // TODO
    }

    /**
     * Updates magnetometer status picture; called by MeasurementControlsPanel when it receives MeasurementEvent.
     *
     * @param position sample holder position, from 1 to 16777215.
     * @param rotation sample holder rotation, from 0 (angle 0) to 2000 (angle 360).
     */
    public void updateStatus(int position, int rotation) {
        return; // TODO
    }
}