/*
* MeasurementDetailsPanel.java
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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Shows details of measurement selected in MeasurementSequencePanel.
 *
 * @author
 */
public class MeasurementDetailsPanel extends ProjectComponent {
/*
Event A: On project event - Update tables to correspond projects new state.
*/
/*
Event B: On change of selected row in MeasurementSequencePanel - Change tables to
correspond selected row.
*/
/*
Event C: On measurement event - If row corresponding to ongoing measurement is selected
in MeasurementSequencePanel update tables with new measurement data.
*/

    /**
     * X, Y and Z components of BG1, 0, 90, 180, 270, BG2
     */
    private JTable measurementDetails;

    /**
     * S/D, S/H and S/N of error
     */
    private JTable errorDetails;

    private DefaultTableModel tableModel;

    /**
     * Creates default MeasurementDetailsPanel.
     */
    public MeasurementDetailsPanel() {
        return; // TODO
    }

    /**
     * Calls super.setProject(project), clears tables and shows new projects measurement details.
     */
    public void setProject(Project project) {
        return; // TODO
    }
}