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
import ikayaki.ProjectEvent;
import ikayaki.MeasurementEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Shows details of measurement selected in MeasurementSequencePanel.
 *
 * @author Mikko Jormalainen
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

    private DefaultTableModel measurementModel;
    private DefaultTableModel errorModel;

    /**
     * Tells if currently measured row in MeasurementSequenceTable is selected.
     */
    private boolean rowSelected;

    /**
     * Creates default MeasurementDetailsPanel.
     */
    public MeasurementDetailsPanel() {
        setLayout(new GridLayout(0, 1, 5, 5));
        measurementModel = new DefaultTableModel(7, 4);
        measurementDetails = new JTable(measurementModel);
        measurementModel.setValueAt("X", 0, 1);
        measurementModel.setValueAt("Y", 0, 2);
        measurementModel.setValueAt("Z", 0, 3);
        measurementModel.setValueAt("BG", 1, 0);
        measurementModel.setValueAt("0", 2, 0);
        measurementModel.setValueAt("90", 3, 0);
        measurementModel.setValueAt("180", 4, 0);
        measurementModel.setValueAt("270", 5, 0);
        measurementModel.setValueAt("BG", 6, 0);
        add(measurementDetails);
        errorModel = new DefaultTableModel(2, 4);
        errorDetails = new JTable(errorModel);
        errorModel.setValueAt("S/D", 0, 1);
        errorModel.setValueAt("S/H", 0, 2);
        errorModel.setValueAt("S/N", 0, 3);
        errorModel.setValueAt("Error", 1, 0);
        add(errorDetails);
        rowSelected = true;
    }

    /**
     * Calls super.setProject(project), clears tables and shows new projects measurement details.
     */
    public void setProject(Project project) {
        super.setProject(project);
    }

    public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.DATA_CHANGED) {
            // TODO
        }
        else if (event.getType() == ProjectEvent.Type.STATE_CHANGED) {
            // TODO
        }
    }

    public void measurementUpdated(MeasurementEvent event) {
        if (event.getType() == MeasurementEvent.Type.VALUE_MEASURED && rowSelected) {
            // TODO
        }
        else if (event.getType() == MeasurementEvent.Type.STEP_START && rowSelected) {
            // TODO
        }
    }

    public void toggleSelected() {
        if (rowSelected) {
            rowSelected = false;
        }
        else {
            rowSelected = true;
        }
    }

    public boolean getSelected() {
        return rowSelected;
    }
}