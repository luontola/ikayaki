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

import static ikayaki.gui.MeasurementSequenceTableModel.SequenceColumn.*;

/**
 * Handles data in table.
 *
 * @author Esko Luontola
 */
public class MeasurementSequenceTableModel extends AbstractTableModel implements ProjectListener, MeasurementListener {

    private Project project = null;
    private int lastStepCount;

    private List<SequenceColumn> visibleColumns = new ArrayList<SequenceColumn>();
    private List<SequenceColumn> possibleColumns = new ArrayList<SequenceColumn>();

    private static final String VISIBLE_COLUMNS_PROPERTY = "visibleColumns";

    /**
     * Creates SequenceTableModel
     */
    public MeasurementSequenceTableModel() {
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
            lastStepCount = project.getSteps();
        } else {
            lastStepCount = 0;
        }
        this.project = project;


        // reset columns to defaults
        possibleColumns.clear();
        possibleColumns.add(COUNT);
        possibleColumns.add(STEP);
        possibleColumns.add(MASS);
        possibleColumns.add(VOLUME);
        possibleColumns.add(DECLINATION);
        possibleColumns.add(INCLINATION);
        possibleColumns.add(REMANENCE);
        possibleColumns.add(RELATIVE_REMANENCE);
        possibleColumns.add(MOMENT);
        possibleColumns.add(X);
        possibleColumns.add(Y);
        possibleColumns.add(Z);
        possibleColumns.add(THETA63);

        // show default columns
        visibleColumns.clear();
        showColumn(COUNT, false);
        showColumn(STEP, false);
        showColumn(DECLINATION, false);
        showColumn(INCLINATION, false);
        showColumn(RELATIVE_REMANENCE, false);
        showColumn(X, false);
        showColumn(Y, false);
        showColumn(Z, false);
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

    public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.DATA_CHANGED) {
            if (project.getSteps() != lastStepCount) {
                fireTableStructureChanged(); // TODO: use fireTableRowsInserted() instead?
            } else {
                fireTableDataChanged();
            }
        }
    }

    public void measurementUpdated(MeasurementEvent event) {
        if (event.getType() == MeasurementEvent.Type.VALUE_MEASURED
                || event.getType() == MeasurementEvent.Type.STEP_START
                || event.getType() == MeasurementEvent.Type.STEP_END
                || event.getType() == MeasurementEvent.Type.STEP_ABORTED) {
            MeasurementStep step = event.getStep();
            for (int i = 0; i < project.getSteps(); i++) {
                // TODO: test some time later that this works. the project does not yet fire MeasurementEvents...
                if (project.getStep(i) == step) {
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
        }
    }

    /**
     * Shows the specified column. Makes sure that the columns are always in the same order. Saves the visible columns
     * to the project's properties.
     *
     * @param column the column to be shown.
     */
    public void showColumn(SequenceColumn column) {
        showColumn(column, true);
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
     * Hides the specified column. Saves the visible columns to the project's properties.
     *
     * @param column the column to be hidden.
     */
    public void hideColumn(SequenceColumn column) {
        hideColumn(column, true);
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
        if (project != null) {
            return project.getSteps() + 1;
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
        return visibleColumns.size();
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
        return visibleColumns.get(column).getColumnName();
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    @Override public Class<?> getColumnClass(int columnIndex) {
        return visibleColumns.get(columnIndex).getColumnClass();
    }

    /**
     * Represents a column in the measurement sequence table. Calculates the values of that column.
     */
    public enum SequenceColumn {

        COUNT("#") {
            @Override public Object getValue(int rowIndex, Project project) {
                return Integer.toString(rowIndex + 1);
            }
        },
        STEP("Tesla"){
            @Override public Object getValue(int rowIndex, Project project) {
                if (rowIndex >= project.getSteps()) {
                    return null;
                }
                double value = project.getStep(rowIndex).getStepValue();
                if (value < 0.0) {
                    return null;
                }
                return value;
            }

            @Override public void setValue(Object data, int rowIndex, Project project) {
                if (project == null) {
                    return;
                }
                if (!(data instanceof Number)) {
                    return;
                }
                double value = ((Number) data).doubleValue();

                if (rowIndex >= project.getSteps()) {
                    // add new row
                    MeasurementStep step = new MeasurementStep(project);
                    step.setStepValue(value);
                    project.addStep(step);
                } else {
                    // edit an existing row (the editing of completed steps is prevented by isCellEditable)
                    project.getStep(rowIndex).setStepValue(value);
                }
            }

            @Override public boolean isCellEditable(int rowIndex, Project project) {
                if (rowIndex >= project.getSteps()) {
                    return true;    // the last row
                }
                if (rowIndex < project.getCompletedSteps()) {
                    return false;   // completed steps
                }
                return true;        // uncompleted steps
            }

            @Override public Class<?> getColumnClass() {
                return Double.class;
            }

            // TODO or not?
        },
        MASS("Mass"){
            @Override public Object getValue(int rowIndex, Project project) {
                if (rowIndex >= project.getSteps()) {
                    return null;
                }
                double value = project.getStep(rowIndex).getMass();
                if (value < 0.0) {
                    value = project.getMass();
                }
                if (value < 0.0) {
                    return null;
                }
                return value;
            }

            @Override public void setValue(Object data, int rowIndex, Project project) {
                if (project == null) {
                    return;
                }
                if (data != null && !(data instanceof Number)) {
                    return;
                }
                double value = data != null ? ((Number) data).doubleValue() : -1.0;

                if (rowIndex < project.getSteps()) {
                    project.getStep(rowIndex).setMass(value);
                }
            }

            @Override public boolean isCellEditable(int rowIndex, Project project) {
                if (rowIndex < project.getSteps()) {
                    return true;
                }
                return false;
            }

            @Override public Class<?> getColumnClass() {
                return Double.class;
            }
        },
        VOLUME("Volume"){
            @Override public Object getValue(int rowIndex, Project project) {
                if (rowIndex >= project.getSteps()) {
                    return null;
                }
                double value = project.getStep(rowIndex).getVolume();
                if (value < 0.0) {
                    value = project.getVolume();
                }
                if (value < 0.0) {
                    return null;
                }
                return value;
            }

            @Override public void setValue(Object data, int rowIndex, Project project) {
                if (project == null) {
                    return;
                }
                if (data != null && !(data instanceof Number)) {
                    return;
                }
                double value = data != null ? ((Number) data).doubleValue() : -1.0;

                if (rowIndex < project.getSteps()) {
                    project.getStep(rowIndex).setVolume(value);
                }
            }

            @Override public boolean isCellEditable(int rowIndex, Project project) {
                if (rowIndex < project.getSteps()) {
                    return true;
                }
                return false;
            }

            @Override public Class<?> getColumnClass() {
                return Double.class;
            }
        },
        X(MeasurementValue.X),
        Y(MeasurementValue.Y),
        Z(MeasurementValue.Z),
        DECLINATION(MeasurementValue.DECLINATION),
        INCLINATION(MeasurementValue.INCLINATION),
        MOMENT(MeasurementValue.MOMENT),
        REMANENCE(MeasurementValue.REMANENCE),
        RELATIVE_REMANENCE(MeasurementValue.RELATIVE_REMANENCE),
        THETA63(MeasurementValue.THETA63);

        private String columnName;

        private MeasurementValue value;

        private SequenceColumn(String columnName) {
            this.columnName = columnName;
            this.value = null;
        }

        private SequenceColumn(MeasurementValue value) {
            if (value.getUnit().equals("")) {
                this.columnName = value.getCaption();
            } else {
                this.columnName = value.getCaption() + " (" + value.getUnit() + ")";
            }
            this.value = value;
        }

        /**
         * Returns the value for this column's specified row. The default implementation is to use the algoritm of a
         * MeasurementValue object. If no MeasurementValue has been provided, will return an empty string. Subclasses
         * can override the default behaviour.
         *
         * @param rowIndex the index of the row. Can be greater than the number of measurement steps.
         * @param project  the project whose value to get. Can be null.
         * @return the value that should be shown in that cell.
         */
        public Object getValue(int rowIndex, Project project) {
            if (value != null) {
                if (rowIndex >= project.getSteps()) {
                    return null;
                }
                return project.getValue(rowIndex, value);
            } else {
                return null;
            }
        }

        /**
         * Sets the value for this column's specified row. The default implementation does nothing. Subclasses can
         * override the default behaviour.
         *
         * @param data     new value for the cell.
         * @param rowIndex the index of the row. Can be greater than the number of measurement steps.
         * @param project  the project whose value to set. Can be null.
         */
        public void setValue(Object data, int rowIndex, Project project) {
            // DO NOTHING
        }

        /**
         * Tells whether the specified row in this column is editable. The default implementation returns always false.
         * Subclasses can override the default behaviour.
         *
         * @param rowIndex the index of the row. Can be greater than the number of measurement steps.
         * @param project  the project whose value to get. Can be null.
         * @return should the cell be editable or not.
         */
        public boolean isCellEditable(int rowIndex, Project project) {
            return false;
        }

        /**
         * Returns the name of this column. The name will be shown in the header of the table.
         */
        public final String getColumnName() {
            return columnName;
        }

        /**
         * Returns the class of this column regardless of the row. The default implementation is to return Object.class.
         * Subclasses can override the default behaviour.
         */
        public Class<?> getColumnClass() {
            return Object.class;
        }
    }
}