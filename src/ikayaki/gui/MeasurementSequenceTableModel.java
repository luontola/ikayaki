/*
* MeasurementSequenceTableModel.java
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

import javax.swing.table.AbstractTableModel;

/**
 * Handles data in table.
 *
 * @author
 */
public class MeasurementSequenceTableModel extends AbstractTableModel {

    /**
     * Tells if volume is shown in table.
     */
    private boolean volume;

    /**
     * Creates SequenceTableModel
     */
    public MeasurementSequenceTableModel() {
        return; // TODO
    }

    /**
     * Shows named column.
     *
     * @param name name of the column to be shown. possible values VOLUME=0
     */
    public void showColumn(int name) {
        return; // TODO
    }

    /**
     * Hides named column.
     *
     * @param name name of the column to be hidden. possible values VOLUME=0
     */
    public void hideColumn(int name) {
        return; // TODO
    }

    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        return 0;
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine how many columns
     * it should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        return 0;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}