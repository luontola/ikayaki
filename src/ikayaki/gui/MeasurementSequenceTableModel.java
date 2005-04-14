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

import ikayaki.*;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static ikayaki.gui.SequenceColumn.*;

/**
 * Handles the showing and editing of a project's measurement sequence. The columns that are being shown can be selected
 * on a per project basis, and the selections are remembered even after the project has been closed.
 *
 * @author Esko Luontola
 */
public class MeasurementSequenceTableModel extends AbstractTableModel implements ProjectListener, MeasurementListener {

    private static final String VISIBLE_COLUMNS_PROPERTY = "visibleColumns";

    private Project project = null;

    private List<SequenceColumn> visibleColumns = new ArrayList<SequenceColumn>();
    private List<SequenceColumn> possibleColumns = new ArrayList<SequenceColumn>();

    /**
     * Creates a new MeasurementSequenceTableModel with no active project.
     */
    public MeasurementSequenceTableModel() {
        // initialize with no project
        setProject(null);
    }

    /**
     * Returns the active project, or null if no project is active.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project for this model. Unregisters MeasurementListener and ProjectListener from the old project, and
     * registers them to the new project. Decides which colums to show in the table.
     *
     * @param project new active project, or null to make no project active.
     */
    public void setProject(Project project) {
        if (this.project != null) {
            this.project.removeProjectListener(this);
            this.project.removeMeasurementListener(this);
        }
        if (project != null) {
            project.addProjectListener(this);
            project.addMeasurementListener(this);
        }
        this.project = project;

        // set all possible columns, in order of appearance
        possibleColumns.clear();
        possibleColumns.add(COUNT);
        possibleColumns.add(STEP);
        possibleColumns.add(VOLUME);
        possibleColumns.add(MASS);
        possibleColumns.add(SUSCEPTIBILITY);
        possibleColumns.add(DECLINATION);
        possibleColumns.add(INCLINATION);
        possibleColumns.add(MAGNETIZATION);
        possibleColumns.add(RELATIVE_MAGNETIZATION);
        possibleColumns.add(MOMENT);
        possibleColumns.add(GEOGRAPHIC_X);
        possibleColumns.add(GEOGRAPHIC_Y);
        possibleColumns.add(GEOGRAPHIC_Z);
        possibleColumns.add(SAMPLE_X);
        possibleColumns.add(SAMPLE_Y);
        possibleColumns.add(SAMPLE_Z);
        possibleColumns.add(THETA63);

        // show default columns
        visibleColumns.clear();
        showColumn(COUNT, false);
        showColumn(STEP, false);
        showColumn(DECLINATION, false);
        showColumn(INCLINATION, false);
        showColumn(RELATIVE_MAGNETIZATION, false);
        showColumn(SAMPLE_X, false);
        showColumn(SAMPLE_Y, false);
        showColumn(SAMPLE_Z, false);
        showColumn(THETA63, false);

        if (project != null) {

            // show customized columns
            String property = project.getProperty(VISIBLE_COLUMNS_PROPERTY);
            if (property != null) {
                String[] cols = property.split(",");

                // set each of the explicitly specified columns visible or hidden
                for (String s : cols) {
                    if (!s.endsWith("+") && !s.endsWith("-")) {
                        System.err.println("Invalid " + VISIBLE_COLUMNS_PROPERTY + " property: " + s);
                        continue;
                    }

                    // split the property string to NAME and +/-
                    String column = s.substring(0, s.length() - 1);
                    boolean visible = s.substring(s.length() - 1).equals("+");
                    try {
                        if (visible) {
                            showColumn(SequenceColumn.valueOf(column), false);
                        } else {
                            hideColumn(SequenceColumn.valueOf(column), false);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Refreshes the table to reflect the changes in the project's data.
     *
     * @deprecated The selected rows need to be saved before updating the table data, and that can only be done with
     *             access to the JTable. That's why it is on MeasurementSequencePanel's responsibility is to react to
     *             ProjectEvents.
     */
    public void projectUpdated(ProjectEvent event) {
//        if (event.getType() == ProjectEvent.Type.DATA_CHANGED) {
//            fireTableDataChanged();
//        }
    }

    /**
     * Refreshes the table to reflect the changes in the measurement steps.
     */
    public void measurementUpdated(MeasurementEvent event) {
        if (event.getType() == MeasurementEvent.Type.VALUE_MEASURED
                || event.getType() == MeasurementEvent.Type.STEP_START
                || event.getType() == MeasurementEvent.Type.STEP_END
                || event.getType() == MeasurementEvent.Type.STEP_ABORTED) {
            MeasurementStep step = event.getStep();
            for (int i = 0; i < project.getSteps(); i++) {
                if (project.getStep(i) == step) {
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
        }
    }

    /**
     * Returns an array of columns that the current project can show. They are in the order of appearance.
     */
    public SequenceColumn[] getPossibleColumns() {
        return possibleColumns.toArray(new SequenceColumn[possibleColumns.size()]);
    }

    /**
     * Shows the specified column. Makes sure that the columns are always in the same order.
     *
     * @param column the column to be shown.
     * @param save   should this column change be saved to the project or not.
     */
    private void showColumn(SequenceColumn column, boolean save) {
        if (visibleColumns.indexOf(column) < 0) {
            int i, j;
            for (i = 0, j = 0; i < visibleColumns.size() && j < possibleColumns.size();) {
                if (visibleColumns.get(i) == possibleColumns.get(j)) {
                    // all in good sync
                    i++;
                    j++;
                } else if (column == possibleColumns.get(j)) {
                    // this is the place where to add column
                    break;
                } else {
                    // this possibleColumn item is not visible, but it is not the one we want to show either
                    j++;
                }
            }
            visibleColumns.add(i, column);
            fireTableStructureChanged();
            if (save) {
                saveColumn(column, true);
            }
        }
    }

    /**
     * Hides the specified column.
     *
     * @param column the column to be hidden.
     * @param save   should this column change be saved to the project or not.
     */
    private void hideColumn(SequenceColumn column, boolean save) {
        if (visibleColumns.remove(column)) {
            fireTableStructureChanged();
            if (save) {
                saveColumn(column, false);
            }
        }
    }

    /**
     * Tells if specified column is currently visible.
     *
     * @param column the column to be queried.
     * @return true if the column is visible, otherwise false.
     * @throws NullPointerException if column is null.
     */
    public boolean isColumnVisible(SequenceColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
        return visibleColumns.contains(column);
    }

    /**
     * Sets visibility of the specified column. Makes sure that the columns are always in the same order. Saves the
     * visible columns to the project's properties.
     *
     * @param column  the column to be changed.
     * @param visible true if the column should be visible, otherwise false.
     * @throws NullPointerException if column is null.
     */
    public void setColumnVisible(SequenceColumn column, boolean visible) {
        if (column == null) {
            throw new NullPointerException();
        }
        if (isColumnVisible(column) == visible) {
            return;
        }
        if (visible) {
            showColumn(column, true);
        } else {
            hideColumn(column, true);
        }
    }

    /**
     * Saves to the project's properties, whether the specified column should be shown or not. Will do nothing if the
     * current project is null.
     *
     * @param column  the column whose property is changed.
     * @param visible true to show the column, false to hide it.
     */
    private void saveColumn(SequenceColumn column, boolean visible) {
        if (project != null) {

            // get the project's old properties
            String[] cols = project.getProperty(VISIBLE_COLUMNS_PROPERTY, "").split(",");
            StringBuffer result = new StringBuffer();

            // keep the unaffected columns safe
            for (String s : cols) {
                if (!(s.startsWith(column.name()) && s.length() == (column.name().length() + 1))) {
                    if (result.length() != 0) {
                        result.append(',');
                    }
                    result.append(s);
                }
            }

            // explicitly hide/show this column
            if (result.length() != 0) {
                result.append(',');
            }
            result.append(column.name());
            result.append(visible ? '+' : '-');

            project.setProperty(VISIBLE_COLUMNS_PROPERTY, result.toString());
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
        if (project != null && visibleColumns.size() > 0) {
            if (project.isSequenceEditEnabled()) {
                return project.getSteps() + 1;
            } else {
                return project.getSteps();
            }
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine how many columns
     * it should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
        return Math.max(1, visibleColumns.size());
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return visibleColumns.get(columnIndex).getValue(rowIndex, project);
    }

    /**
     * Sets the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param data new value of the cell
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex the column whose value is to be queried
     */
    public void setValueAt(Object data, int rowIndex, int columnIndex) {
        visibleColumns.get(columnIndex).setValue(data, rowIndex, project);
    }

    /**
     * Returns false.  This is the default implementation for all cells.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return false
     */
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return visibleColumns.get(columnIndex).isCellEditable(rowIndex, project);
    }

    /**
     * Returns a name for the column. If <code>column</code> cannot be found, returns an empty string.
     *
     * @param column the column being queried.
     * @return a string containing the default name of column.
     */
    @Override public String getColumnName(int column) {
        if (visibleColumns.size() <= column) {
            return "< right-click to select columns >";
        }
        return visibleColumns.get(column).getColumnName(project);
    }

    /**
     * Returns the tooltip text for the specified column. Will be shown in the table header. 
     */
    public String getColumnToolTip(int column) {
        if (column < visibleColumns.size()) {
            return visibleColumns.get(column).getToolTipText(project);
        }
        return null;
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    @Override public Class<?> getColumnClass(int columnIndex) {
        if (visibleColumns.size() <= columnIndex) {
            return Object.class;
        }
        return visibleColumns.get(columnIndex).getColumnClass();
    }
}