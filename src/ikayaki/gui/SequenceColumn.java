/*
 * SequenceColumn.java
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

import ikayaki.MeasurementStep;
import ikayaki.MeasurementValue;
import ikayaki.Project;
import ikayaki.Settings;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Represents a column in the measurement sequence table. Calculates the values of that column.
 *
 * @author Esko Luontola
 */
public enum SequenceColumn {

    /**
     * Showing ordinal number of the measurement step, starting from number 1.
     */
    COUNT("#") {
        @Override public StyledWrapper getValue(int rowIndex, Project project) {
            StyledWrapper wrapper = headerWrapper;
            if (project != null && project.getSteps() > rowIndex) {
                wrapper.value = new Integer(rowIndex + 1);
            } else {
                //wrapper.value = ">"; // greater-than sign
                wrapper.value = "\u00BB"; // right-pointing double angle quotation mark
                //wrapper.value = "\u2192"; // rightwards arrow
                //wrapper.value = "\u25BA"; // black right-pointing pointer
            }
            return wrapper;
        }
    },

    /**
     * Showing and editing the stepValue of the measurement step.
     */
    STEP("Step Value"){
        {
            // one decimal is the maximum presision
            getNumberFormat().setMaximumFractionDigits(1);
        }

        @Override public StyledWrapper getValue(int rowIndex, Project project) {
            if (rowIndex >= project.getSteps()) {
                return wrap(null, rowIndex, project);
            }
            double value = project.getStep(rowIndex).getStepValue();
            if (value < 0.0) {
                return wrap(null, rowIndex, project);
            }
            return wrap(getNumberFormat().format(value), rowIndex, project);
        }

        @Override public void setValue(Object data, int rowIndex, Project project) {
            if (project == null) {
                return;
            }
            if (data != null && !(data instanceof Number)) {
                if (data.toString().equals("")) {
                    data = null;
                } else {
                    try {
                        data = getNumberFormat().parse(data.toString());
                    } catch (ParseException e) {
                        throw new NumberFormatException("For input string: \"" + data.toString() + "\"");
                    }
                }
            }
            double value = data != null ? ((Number) data).doubleValue() : -1.0;

            if (rowIndex >= project.getSteps()) {
                // add new row
                if (value < 0.0) {
                    return;
                }
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
            if (project.getStep(rowIndex).getState() == MeasurementStep.State.READY) {
                return true;    // uncompleted steps
            }
            return false;       // completed steps
        }

        @Override public String getColumnName(Project project) {
            if (project == null) {
                return "Tesla";
            }
            String name;
            switch (project.getType()) {
            case AF:
            case CALIBRATION:
                name = "AF (mT)";
                break;
            case THELLIER:
            case THERMAL:
                name = "T (\u00b0C)";
                break;
            default:
                assert false;
                name = "";
                break;
            }
            return name;
        }
    },

    /**
     * Showing and editing the mass of the measurement step.
     */
    MASS("Mass"){ // unit is grams
        {
            // at least 5 decimals
            getNumberFormat().setMaximumFractionDigits(6);
        }

        @Override public StyledWrapper getValue(int rowIndex, Project project) {
            if (rowIndex >= project.getSteps()) {
                return wrap(null, rowIndex, project);
            }
            double value = project.getStep(rowIndex).getMass();
            if (value < 0.0) {
                value = project.getMass();
            }
            if (value < 0.0) {
                return wrap(null, rowIndex, project);
            }
            return wrap(getNumberFormat().format(value), rowIndex, project);
        }

        @Override public void setValue(Object data, int rowIndex, Project project) {
            if (project == null) {
                return;
            }
            if (data != null && !(data instanceof Number)) {
                if (data.toString().equals("")) {
                    data = null;
                } else {
                    try {
                        data = getNumberFormat().parse(data.toString());
                    } catch (ParseException e) {
                        throw new NumberFormatException("For input string: \"" + data.toString() + "\"");
                    }
                }
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
    },

    /**
     * Showing and editing the volume of the measurement step.
     */
    VOLUME("Volume"){ // unit is cm^3
        {
            // at least 2 decimals
            getNumberFormat().setMaximumFractionDigits(6);
        }

        @Override public StyledWrapper getValue(int rowIndex, Project project) {
            if (rowIndex >= project.getSteps()) {
                return wrap(null, rowIndex, project);
            }
            double value = project.getStep(rowIndex).getVolume();
            if (value < 0.0) {
                value = project.getVolume();
            }
            if (value < 0.0) {
                return wrap(null, rowIndex, project);
            }
            return wrap(getNumberFormat().format(value), rowIndex, project);
        }

        @Override public void setValue(Object data, int rowIndex, Project project) {
            if (project == null) {
                return;
            }
            if (data != null && !(data instanceof Number)) {
                if (data.toString().equals("")) {
                    data = null;
                } else {
                    try {
                        data = getNumberFormat().parse(data.toString());
                    } catch (ParseException e) {
                        throw new NumberFormatException("For input string: \"" + data.toString() + "\"");
                    }
                }
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
    },

    /*
     * Showing the values calculated by MeasurementValue.
     */
    X(MeasurementValue.X) {
        {
            setNumberFormat(new DecimalFormat("0.000E0"));
        }
    },
    Y(MeasurementValue.Y) {
        {
            setNumberFormat(new DecimalFormat("0.000E0"));
        }
    },
    Z(MeasurementValue.Z) {
        {
            setNumberFormat(new DecimalFormat("0.000E0"));
        }
    },
    DECLINATION(MeasurementValue.DECLINATION) {
        {
            // 2 decimals is good
            getNumberFormat().setMinimumFractionDigits(2);
            getNumberFormat().setMaximumFractionDigits(2);
        }
    },
    INCLINATION(MeasurementValue.INCLINATION) {
        {
            // 2 decimals is good
            getNumberFormat().setMinimumFractionDigits(2);
            getNumberFormat().setMaximumFractionDigits(2);
        }
    },
    MOMENT(MeasurementValue.MOMENT) {
        {
            setNumberFormat(new DecimalFormat("0.000E0"));
        }
    },
    REMANENCE(MeasurementValue.REMANENCE) { // TODO: "magnetization" would be a better name
        {
            setNumberFormat(new DecimalFormat("0.000E0"));
        }

        @Override public String getColumnName(Project project) {
            // TODO: change the unit according to whether it is volume or mass normalization
            return super.getColumnName(project);
        }
    },
    RELATIVE_REMANENCE(MeasurementValue.RELATIVE_REMANENCE) {
        {
            getNumberFormat().setMinimumFractionDigits(3);
            getNumberFormat().setMaximumFractionDigits(3);
        }
    },
    THETA63(MeasurementValue.THETA63) {
        {
            getNumberFormat().setMinimumFractionDigits(3);
            getNumberFormat().setMaximumFractionDigits(3);
        }
    };

    /* Styles for the cell renderer */

    private static final StyledWrapper defaultWrapper = Settings.getDefaultWrapperInstance();
    private static final StyledWrapper measuringWrapper = Settings.getMeasuringWrapperInstance();
    private static final StyledWrapper doneRecentlyWrapper = Settings.getDoneRecentlyWrapperInstance();
    private static final StyledWrapper headerWrapper = new StyledWrapper();
    private static final Border editableCellBorder =
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xEEEEFF));
    private static final Border editableCellFocusBorder =
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0x9999CC));

    static {
        // align all fields to right
        defaultWrapper.horizontalAlignment = SwingConstants.TRAILING;
        measuringWrapper.horizontalAlignment = SwingConstants.TRAILING;
        doneRecentlyWrapper.horizontalAlignment = SwingConstants.TRAILING;
        headerWrapper.horizontalAlignment = SwingConstants.TRAILING;

        // styles for the COUNT column
        Color headerBackground = new Color(0xE1E1E1);
        Border headerBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
//        Border headerBorder = BorderFactory.createCompoundBorder(
//                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x808080)),
//                BorderFactory.createEmptyBorder(1, 2, 1, 2));
        headerWrapper.opaque = true;
        headerWrapper.background = headerBackground;
        headerWrapper.selectedBackground = headerBackground;
        headerWrapper.focusBackground = headerBackground;
        headerWrapper.selectedFocusBackground = headerBackground;
        headerWrapper.border = headerBorder;
        headerWrapper.selectedBorder = headerBorder;
        headerWrapper.focusBorder = headerBorder;
        headerWrapper.selectedFocusBorder = headerBorder;
    }

    /* Begin class SequenceColumn */

    private String columnName;

    private MeasurementValue value;

    private NumberFormat numberFormat;

    private SequenceColumn(String columnName) {
        this.columnName = columnName;
        this.value = null;
        this.numberFormat = NumberFormat.getNumberInstance();
        this.numberFormat.setGroupingUsed(false);
    }

    private SequenceColumn(MeasurementValue value) {
        if (value.getUnit().equals("")) {
            this.columnName = value.getCaption();
        } else {
            this.columnName = value.getCaption() + " (" + value.getUnit() + ")";
        }
        this.value = value;
        this.numberFormat = NumberFormat.getNumberInstance();
        this.numberFormat.setGroupingUsed(false);
    }

    /**
     * Wraps the specified object to a styled renderer's wrapper according to the state of the measurement step.
     *
     * @param value    the object to be wrapped.
     * @param rowIndex the index of the row. Can be greater than the number of measurement steps.
     * @param project  the project whose value to get. Can be null.
     * @return the wrapped object.
     */
    public StyledWrapper wrap(Object value, int rowIndex, Project project) {

        // choose the style according to the state of the step
        StyledWrapper wrapper;
        if (project == null || rowIndex >= project.getSteps()) {
            wrapper = defaultWrapper;
        } else {
            switch (project.getStep(rowIndex).getState()) {
            case READY:
            case DONE:
                wrapper = defaultWrapper;
                break;
            case MEASURING:
                wrapper = measuringWrapper;
                break;
            case DONE_RECENTLY:
                wrapper = doneRecentlyWrapper;
                break;
            default:
                assert false;
                wrapper = null;
                break;
            }
        }

        // visible border for editable cells
        if (isCellEditable(rowIndex, project)) {
            wrapper.border = editableCellBorder;
            wrapper.selectedBorder = editableCellBorder;
            wrapper.focusBorder = editableCellFocusBorder;
            wrapper.selectedFocusBorder = editableCellFocusBorder;
        } else {
            wrapper.border = null;
            wrapper.selectedBorder = null;
            wrapper.focusBorder = null;
            wrapper.selectedFocusBorder = null;
        }

        // wrap the cell's value and return it
        wrapper.value = value;
        return wrapper;
    }

    /**
     * Returns the value for this column's specified row. The default implementation is to use the algoritm of a
     * MeasurementValue object. If no MeasurementValue has been provided, will return an empty string. Subclasses can
     * override the default behaviour.
     *
     * @param rowIndex the index of the row. Can be greater than the number of measurement steps.
     * @param project  the project whose value to get. Can be null.
     * @return the wrapped value that should be shown in that cell.
     */
    public StyledWrapper getValue(int rowIndex, Project project) {
        if (value == null || rowIndex >= project.getSteps()) {
            return wrap(null, rowIndex, project);
        }
        Object obj = project.getValue(rowIndex, value);
        if (obj == null || !(obj instanceof Number)) {
            return wrap(obj, rowIndex, project);
        }

        // format the return value with NumberFormatter
        double doubleValue = ((Number) obj).doubleValue();
        String formatted = getNumberFormat().format(obj);

        // check for Infinity and NaN
        if (formatted.charAt(0) == 65533) { // for some reason "doubleValue == Double.NaN" doesn't work
            formatted = "NaN";
        } else if (doubleValue == Double.POSITIVE_INFINITY) {
            formatted = "\u221e";
        } else if (doubleValue == Double.NEGATIVE_INFINITY) {
            formatted = "-\u221e";
        }
        return wrap(formatted, rowIndex, project);
    }

    /**
     * Sets the value for this column's specified row. The default implementation does nothing. Subclasses can override
     * the default behaviour.
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
     * Returns the name of this column. The name will be shown in the header of the table. The default implementation
     * returns always the columnName property. Subclasses can override the default behaviour.
     *
     * @param project the open project or null if no project is active.
     */
    public String getColumnName(Project project) {
        return columnName;
    }

    /**
     * Returns the class of this column regardless of the row. The default implementation is to return
     * StyledTableCellRenderer.Wrapper.class. Subclasses can override the default behaviour.
     */
    public Class<?> getColumnClass() {
        return StyledWrapper.class;
    }

    /**
     * Returns the number format used for rendering the numbers in this column.
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * Sets the number format used for rendering the numbers in this column.
     *
     * @throws NullPointerException if numberFormat is null.
     */
    public void setNumberFormat(NumberFormat numberFormat) {
        if (numberFormat == null) {
            throw new NullPointerException();
        }
        this.numberFormat = numberFormat;
    }
}
