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
 * @author Mikko Jormalainen
 */
public class MeasurementSequenceTableModel extends AbstractTableModel {

    /**
     * Tells if volume is shown in table.
     */
    private boolean volume;

    /**
     * Number of Columns shown by table.
     */
    private int numberOfColumns;

    /**
     * Number of Rows shown by table.
     */
    private int numberOfRows;

    private final int VOL = 0;
    private final int MAXROWS = 255;
    private final int MAXCOLUMNS = 11;
    
    /**
     * Table data.
     */
    private String[][] tableData;
    
    /**
     * Creates SequenceTableModel
     */
    public MeasurementSequenceTableModel() {
        tableData = new String[MAXROWS][MAXCOLUMNS];
        volume = false;
        numberOfColumns = 10;
        numberOfRows = 2;
        tableData[0][0] = "#";
        tableData[0][1] = "Tesla";
        tableData[0][2] = "D(o)";
        tableData[0][3] = "I(\")";
        tableData[0][4] = "J(T)";
        tableData[0][5] = "M(T)";
        tableData[0][6] = "X(T)";
        tableData[0][7] = "Y(T)";
        tableData[0][8] = "Z(T)";
        tableData[0][9] = "O63";
        tableData[0][10] = "V";
        tableData[1][0] = "0";
    }

    /**
     * Shows named column.
     *
     * @param name name of the column to be shown. possible values VOLUME=0
     */
    public void showColumn(int name) {
        if (volume == false && VOL == name) {
            volume = true;
            numberOfColumns++;
        }
    }

    /**
     * Hides named column.
     *
     * @param name name of the column to be hidden. possible values VOLUME=0
     */
    public void hideColumn(int name) {
        if (volume == true && VOL == name) {
            volume = false;
            numberOfColumns--;
        }
    }

    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        return numberOfRows;
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine how many columns
     * it should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        return numberOfColumns;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableData[rowIndex][columnIndex];
    }

    /**
     * Sets the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param   data        new value of the cell
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     */
    public void setValueAt(Object data, int rowIndex, int columnIndex) {
		tableData[rowIndex][columnIndex] = data.toString();
	}
}