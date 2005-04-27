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
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Shows the details of the active measurement step.
 *
 * @author Esko Luontola
 */
public class MeasurementDetailsPanel extends ProjectComponent {

    private JTable detailsTable;
    private DetailsTableModel detailsTableModel;

    private JTable errorsTable;
    private ErrorsTableModel errorsTableModel;

    /**
     * The measurement step whose details are being shown or null to show a blank table.
     */
    private MeasurementStep step;

    public MeasurementDetailsPanel() {

        // build the tables
        detailsTableModel = new DetailsTableModel();
        detailsTable = new JTable(detailsTableModel);
        detailsTable.setFocusable(false);
        detailsTable.setEnabled(false);
        detailsTable.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());

        errorsTableModel = new ErrorsTableModel();
        errorsTable = new JTable(errorsTableModel);
        errorsTable.setFocusable(false);
        errorsTable.setEnabled(false);
        errorsTable.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());

        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        detailsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        detailsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        detailsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        detailsTable.getTableHeader().setReorderingAllowed(false);
        detailsTable.getTableHeader().setResizingAllowed(false);

        errorsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        errorsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        errorsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        errorsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        errorsTable.getTableHeader().setReorderingAllowed(false);
        errorsTable.getTableHeader().setResizingAllowed(false);

        // emulate the looks of a JScrollPane
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.add(detailsTable.getTableHeader(), BorderLayout.NORTH);
        detailsTablePanel.add(detailsTable, BorderLayout.CENTER);
        detailsTablePanel.setBorder(new JScrollPane().getBorder());

        JPanel errorsTablePanel = new JPanel(new BorderLayout());
        errorsTablePanel.add(errorsTable.getTableHeader(), BorderLayout.NORTH);
        errorsTablePanel.add(errorsTable, BorderLayout.CENTER);
        errorsTablePanel.setBorder(new JScrollPane().getBorder());

        // lay out the components
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.add(detailsTablePanel, "Center");
        tablePanel.add(errorsTablePanel, "South");

        JPanel contentPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contentPane.add(tablePanel);

        JScrollPane scrollPane = new JScrollPane(contentPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
        this.setMinimumSize(new Dimension(0, 225));
        this.setPreferredSize(new Dimension(400, 225));
    }

    @Override public void setProject(Project project) {
        super.setProject(project);
        setStep(null);
    }

    public MeasurementStep getStep() {
        return step;
    }

    public void setStep(MeasurementStep step) {
        this.step = step;
        this.detailsTableModel.setStep(step);
        this.errorsTableModel.setStep(step);
    }

    @Override public void measurementUpdated(MeasurementEvent event) {
        if (event.getStep() == step) {
            detailsTableModel.fireTableDataChanged();
            errorsTableModel.fireTableDataChanged();
        }
    }

    /**
     * Table model for the details table.
     *
     * @author Esko Luontola
     */
    private class DetailsTableModel extends AbstractTableModel {

        private MeasurementStep step;

        private final String[] COLUMNS = new String[]{" ", "X", "Y", "Z"};
        private final int HEADER_COLUMN = 0;
        private final int X_COLUMN = 1;
        private final int Y_COLUMN = 2;
        private final int Z_COLUMN = 3;

        private NumberFormat numberFormat = new DecimalFormat("0.000000E0");

        private StyledWrapper defaultWrapper = new StyledWrapper();
        private StyledWrapper headerWrapper = new StyledWrapper();

        public DetailsTableModel() {
            defaultWrapper.horizontalAlignment = SwingConstants.TRAILING;
            headerWrapper.horizontalAlignment = SwingConstants.TRAILING;
            headerWrapper.font = new JLabel("").getFont().deriveFont(Font.BOLD);
        }

        public MeasurementStep getStep() {
            return step;
        }

        public void setStep(MeasurementStep step) {
            this.step = step;
            fireTableDataChanged();
        }

        public int getRowCount() {
            
            // calculate the expected number of results based on measurement rotations and project type
            int nonSample = 3;
            if (step != null && step.getProject().isHolderCalibration()) {
                nonSample--;
            }
            int expected = Math.max(1, 4 * Settings.getMeasurementRotations()) + nonSample;

            if (step == null) {
                return expected;

            } else if (step.getState() == MeasurementStep.State.DONE
                    || step.getState() == MeasurementStep.State.DONE_RECENTLY) {
                return step.getResults();

            } else {
                // try to estimate the number of steps
                int count = step.getResults();

                // if the last step is not BG, there are more steps coming
                if (count >= nonSample && step.getResult(count - 1).getType() != MeasurementResult.Type.NOISE) {
                    count++;
                }
                return Math.max(expected, count);
            }
        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return StyledWrapper.class;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value;
            if (step != null && rowIndex < step.getResults()) {

                // get the values from the step
                switch (columnIndex) {
                case HEADER_COLUMN:
                    switch (step.getResult(rowIndex).getType()) {
                    case HOLDER:
                        value = "Holder";
                        break;
                    case NOISE:
                        value = "BG";
                        break;
                    case SAMPLE:
                        value = Integer.toString(step.getResult(rowIndex).getRotation());
                        break;
                    default:
                        assert false;
                        value = null;
                        break;
                    }
                    break;
                case X_COLUMN:
                    value = numberFormat.format(step.getResult(rowIndex).getRawX());
                    break;
                case Y_COLUMN:
                    value = numberFormat.format(step.getResult(rowIndex).getRawY());
                    break;
                case Z_COLUMN:
                    value = numberFormat.format(step.getResult(rowIndex).getRawZ());
                    break;
                default:
                    assert false;
                    value = null;
                    break;
                }

            } else {

                // try to guess the values
                if (columnIndex == HEADER_COLUMN) {
                    int i = (step != null && step.getProject().isHolderCalibration())
                            ? rowIndex + 1
                            : rowIndex;

                    if (i == 0) {
                        value = "Holder";
                    } else if (i == 1 || rowIndex == getRowCount() - 1) {
                        value = "BG";
                    } else {
                        switch ((i - 2) % 4) {
                        case 0:
                            value = "0";
                            break;
                        case 1:
                            value = "90";
                            break;
                        case 2:
                            value = "180";
                            break;
                        case 3:
                            value = "270";
                            break;
                        default:
                            assert false;
                            value = null;
                            break;
                        }
                    }
                } else {
                    value = null;
                }
            }
            return wrap(value, rowIndex, columnIndex);
        }

        public StyledWrapper wrap(Object value, int rowIndex, int columnIndex) {
            StyledWrapper wrapper;

            // choose the style according to the column
            if (columnIndex == HEADER_COLUMN) {
                wrapper = headerWrapper;
            } else {
                wrapper = defaultWrapper;
            }

            // wrap the cell's value and return it
            wrapper.value = value;
            return wrapper;
        }
    }

    /**
     * Table model for the error table.
     *
     * @author Esko Luontola
     */
    private class ErrorsTableModel extends AbstractTableModel {

        private MeasurementStep step;

        private final String[] COLUMNS = new String[]{" ", "Signal/Noise", "Signal/Drift", "Signal/Holder"};
        private final int HEADER_COLUMN = 0;
        private final int SIGNAL_NOISE_COLUMN = 1;
        private final int SIGNAL_DRIFT_COLUMN = 2;
        private final int SIGNAL_HOLDER_COLUMN = 3;

        private StyledWrapper defaultWrapper = new StyledWrapper();
        private StyledWrapper headerWrapper = new StyledWrapper();
        private DecimalFormat numberFormat = new DecimalFormat();

        public ErrorsTableModel() {
            defaultWrapper.horizontalAlignment = SwingConstants.TRAILING;
            headerWrapper.horizontalAlignment = SwingConstants.TRAILING;
            headerWrapper.font = new JLabel("").getFont().deriveFont(Font.BOLD);
            numberFormat.setMinimumFractionDigits(3);
            numberFormat.setMaximumFractionDigits(3);
        }

        public MeasurementStep getStep() {
            return step;
        }

        public void setStep(MeasurementStep step) {
            this.step = step;
            fireTableDataChanged();
        }

        public int getRowCount() {
            return 1;
        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return StyledWrapper.class;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value;
            switch (columnIndex) {
            case HEADER_COLUMN:
                value = "Error";
                break;
            case SIGNAL_NOISE_COLUMN:
                value = MeasurementValue.SIGNAL_TO_NOISE.getValue(step);
                break;
            case SIGNAL_DRIFT_COLUMN:
                value = MeasurementValue.SIGNAL_TO_DRIFT.getValue(step);
                break;
            case SIGNAL_HOLDER_COLUMN:
                value = MeasurementValue.SIGNAL_TO_HOLDER.getValue(step);
                break;
            default:
                value = null;
                break;
            }

            if (value != null && value instanceof Number) {
                value = numberFormat.format(((Number) value).doubleValue());
            }
            return wrap(value, rowIndex, columnIndex);
        }

        public StyledWrapper wrap(Object value, int rowIndex, int columnIndex) {
            StyledWrapper wrapper;

            // choose the style according to the column
            if (columnIndex == HEADER_COLUMN) {
                wrapper = headerWrapper;
            } else {
                wrapper = defaultWrapper;
            }

            // wrap the cell's value and return it
            wrapper.value = value;
            return wrapper;
        }
    }
}
