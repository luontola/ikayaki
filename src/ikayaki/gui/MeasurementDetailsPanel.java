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

import ikayaki.*;

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
        String[] detailNames = {"", "X", "y", "Z"};
        measurementModel = new DefaultTableModel(detailNames, 6);
        measurementDetails = new JTable(measurementModel);
        measurementDetails.setRowSelectionAllowed(false);
        measurementDetails.setColumnSelectionAllowed(false);
        measurementDetails.setValueAt("BG", 0, 0);
        measurementDetails.setValueAt("0", 1, 0);
        measurementDetails.setValueAt("90", 2, 0);
        measurementDetails.setValueAt("180", 3, 0);
        measurementDetails.setValueAt("270", 4, 0);
        measurementDetails.setValueAt("BG", 5, 0);
        add(BorderLayout.NORTH, measurementDetails);
        String[] errorNames = {"", "S/D", "S/H", "S/N"};
        errorModel = new DefaultTableModel(errorNames, 1);
        errorDetails = new JTable(errorModel);
        errorDetails.setRowSelectionAllowed(false);
        errorDetails.setColumnSelectionAllowed(false);
        errorDetails.setValueAt("Error", 0, 0);
        add(BorderLayout.SOUTH, errorDetails);
        rowSelected = true;
    }

    /**
     * Calls super.setProject(project), clears tables and shows new projects measurement details.
     */
    public void setProject(Project project) {
        super.setProject(project);
        rowSelected = true;
        clearTables();
        if (project != null) {
            if (project.getCurrentStep() != null) {
                setStep(project.getCurrentStep());
            }
        }
        measurementDetails.repaint();
        errorDetails.repaint();
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
            MeasurementResult result = null;
            for (int i=0; i<event.getStep().getResults(); ++i) {
                result = event.getStep().getResult(i);
                measurementDetails.setValueAt(result.getType(), i, 0);
                measurementDetails.setValueAt(result.getX(), i, 1);
                measurementDetails.setValueAt(result.getY(), i, 2);
                measurementDetails.setValueAt(result.getZ(), i, 3);
            }
            measurementDetails.repaint();
            // TODO S/D S/H S/N calculation (what formulas?)
            // TODO more than 2 BG ja 4 rotation measurements in step
        }
        else if (event.getType() == MeasurementEvent.Type.STEP_START && rowSelected) {
            clearTables();
            measurementDetails.repaint();
            errorDetails.repaint();
        }
    }

    public void setStep(MeasurementStep step) {
        if (getProject().getCurrentStep() == step && !rowSelected) {
            rowSelected = true;
        }
        else if (getProject().getCurrentStep() != step && rowSelected) {
            rowSelected = false;
        }
        MeasurementResult result = null;
        for (int i=0; i<step.getResults(); ++i) {
            result = step.getResult(i);
            measurementDetails.setValueAt(result.getType(), i, 0);
            measurementDetails.setValueAt(result.getX(), i, 1);
            measurementDetails.setValueAt(result.getY(), i, 2);
            measurementDetails.setValueAt(result.getZ(), i, 3);
        }
        measurementDetails.repaint();
        // TODO S/D S/H S/N calculation (what formulas?)
    }

    private void clearTables() {
        measurementDetails.setValueAt("BG", 0, 0);
        measurementDetails.setValueAt("0", 1, 0);
        measurementDetails.setValueAt("90", 2, 0);
        measurementDetails.setValueAt("180", 3, 0);
        measurementDetails.setValueAt("270", 4, 0);
        measurementDetails.setValueAt("BG", 5, 0);
        for (int i=1; i<4; ++i) {
            for (int j=0; j<6; ++j) {
                measurementDetails.setValueAt("", j, i);
            }
        }
        errorDetails.setValueAt("", 0, 1);
        errorDetails.setValueAt("", 0, 2);
        errorDetails.setValueAt("", 0, 3);
    }
}