/*
 * MeasurementSequencePanel.java
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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ikayaki.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ikayaki.gui.SequenceColumn.*;

/**
 * Shows the measurements of a project and provides controls for modifying the sequence.
 *
 * @author Esko Luontola
 */
public class MeasurementSequencePanel extends ProjectComponent {

    /* Measurement Sequence Table */
    private JTable sequenceTable;
    private MeasurementSequenceTableModel sequenceTableModel;

    /* Add Sequence Controls */
    private JFormattedTextField sequenceStartField;
    private JFormattedTextField sequenceStepField;
    private JFormattedTextField sequenceStopField;
    private ComponentFlasher sequenceStartFieldFlasher;
    private ComponentFlasher sequenceStepFieldFlasher;
    private ComponentFlasher sequenceStopFieldFlasher;
    private JButton addSequenceButton;
    private JComboBox loadSequenceBox;

    private JLabel stepValueTypeLabel;  // stepValue's type depends on the project's type
    private JLabel sequenceStartLabel;
    private JLabel sequenceStepLabel;
    private JLabel sequenceStopLabel;
    private JLabel loadSequenceLabel;

    private JPanel controlsPane;

    /* Details Panel */
    private MeasurementDetailsPanel detailsPanel;



    /**
     * Creates default MeasurementSequencePanel.
     */
    public MeasurementSequencePanel() {

        /* Sequence Table */
        sequenceTableModel = new MeasurementSequenceTableModel();
        sequenceTable = new JTable(sequenceTableModel);
        sequenceTable.getTableHeader().setReorderingAllowed(false);
        sequenceTable.getTableHeader().setResizingAllowed(false);
        sequenceTable.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());
        sequenceTable.setDefaultEditor(StyledWrapper.class, new StyledCellEditor(new JTextField()));

        /* Add Sequence Controls */
        MyFormatterFactory factory = new MyFormatterFactory();
        sequenceStartField.setFormatterFactory(factory);
        sequenceStepField.setFormatterFactory(factory);
        sequenceStopField.setFormatterFactory(factory);
        sequenceStartFieldFlasher = new ComponentFlasher(sequenceStartField);
        sequenceStepFieldFlasher = new ComponentFlasher(sequenceStepField);
        sequenceStopFieldFlasher = new ComponentFlasher(sequenceStopField);

        /* Build Layout */
        controlsPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        JScrollPane scrollPane = new JScrollPane(sequenceTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        add(controlsPane, "North");
        add(scrollPane);

        /* Event Listeners */

        // resize the table columns to be always the right size
        sequenceTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) {
                updateColumns();
            }

            public void columnRemoved(TableColumnModelEvent e) {
                updateColumns();
            }

            public void columnMoved(TableColumnModelEvent e) {
                updateColumns();
            }

            public void columnMarginChanged(ChangeEvent e) {
                // DO NOTHING
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
                // DO NOTHING
            }
        });

        // show the details of the selected step in the details panel
        sequenceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (getProject() == null) {
                    return;
                }

                // if there is exactly one step selected, show that one
                if (sequenceTable.getSelectedRowCount() == 1) {
                    int index = sequenceTable.getSelectedRow();
                    if (index < getProject().getSteps()) {
                        getDetailsPanel().setStep(getProject().getStep(index));
                        return;
                    }
                }

                // if it is unclear that which step to show, show the measuring step (might be null)
                getDetailsPanel().setStep(getProject().getCurrentStep());
            }
        });

        // adding a sequence with the add sequence controls
        addSequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSequence();
            }
        });
        KeyListener keyListener = new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    addSequence();
                }
            }
        };
        sequenceStartField.addKeyListener(keyListener);
        sequenceStepField.addKeyListener(keyListener);
        sequenceStopField.addKeyListener(keyListener);

        // reset the add sequence controls when new rows are added, removed or stepValues are changed
        sequenceTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                resetAddSequence();
            }
        });

        // drop down menu for adding preset sequences
        loadSequenceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // append saved sequences when they are selected from the list
                Object obj = loadSequenceBox.getSelectedItem();
                if (obj != null && obj instanceof MeasurementSequence) {
                    MeasurementSequence sequence = (MeasurementSequence) obj;
                    getProject().addSequence(sequence);

                    // select the just added steps
                    final int addedSteps = sequence.getSteps();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            scrollToRow(getProject().getSteps());
                            int start = getProject().getSteps() - addedSteps;
                            int end = getProject().getSteps() - 1;
                            sequenceTable.getSelectionModel().clearSelection();
                            sequenceTable.getSelectionModel().setSelectionInterval(start, end);
                        }
                    });
                }
            }
        });
        loadSequenceBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                // clear the previous selection when the menu is opened
                if (loadSequenceBox.getSelectedItem() != null) {
                    loadSequenceBox.setSelectedItem(null);
                }
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // DO NOTHING
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
                // DO NOTHING
            }
        });

        // on table cell right-click: show a popup menu for adding/removing steps and saving sequences
        sequenceTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (getProject() == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int[] rows = sequenceTable.getSelectedRows();
                    List<MeasurementStep> steps = new ArrayList<MeasurementStep>();
                    for (int i : rows) {
                        if (i < getProject().getSteps()) {
                            steps.add(getProject().getStep(i));
                        }
                    }
                    JPopupMenu popup = new SequencePopupMenu(steps.toArray(new MeasurementStep[steps.size()]));
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // on table header right-click: show a popup menu for choosing the table columns
        sequenceTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (getProject() == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu popup = new HeaderPopupMenu();
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // initialize with no project
        updateLoadSequenceBox();
        setProject(null);
    }

    /**
     * Returns the component that will show the details of the active measurement step.
     */
    public MeasurementDetailsPanel getDetailsPanel() {
        if (detailsPanel == null) {
            detailsPanel = new MeasurementDetailsPanel();
        }
        return detailsPanel;
    }

    /**
     * Rebuilds the contents of the loadSequenceBox combobox by getting the saved sequences from the settings.
     */
    private void updateLoadSequenceBox() {
        // save old selection
        Object selected = loadSequenceBox.getSelectedItem();
        loadSequenceBox.removeAllItems();

        // get new items
        MeasurementSequence[] sequences = Settings.instance().getSequences();
        Arrays.sort(sequences);

        // insert new items and restore old selection
        loadSequenceBox.addItem(null);  // the first item is empty
        for (MeasurementSequence sequence : sequences) {
            loadSequenceBox.addItem(sequence);
        }
        loadSequenceBox.setSelectedItem(selected);
    }

    /**
     * Resize the table's columns to fit the content.
     */
    private void updateColumns() {
        TableColumnModel columnModel = sequenceTable.getColumnModel();
        for (int col = 0; col < columnModel.getColumnCount(); col++) {
            if (columnModel.getColumn(col).getHeaderValue().equals(COUNT.getColumnName(null))) {

                // find out the column's preferred width using the actual cell contents
                int width = 20;
                Component comp;
                for (int row = 0; row < sequenceTable.getRowCount(); row++) {
                    comp = sequenceTable.getCellRenderer(row, col).getTableCellRendererComponent(sequenceTable,
                            sequenceTable.getValueAt(row, col), false, false, row, col);
                    width = Math.max(width, comp.getPreferredSize().width);
                }
                width += 5;
                columnModel.getColumn(col).setMinWidth(width);
                columnModel.getColumn(col).setMaxWidth(width);
                return;
            }
        }
    }

    /**
     * Returns the latest stepValue which is greater than 0. If none is found, returns 0.
     */
    private double getLastPositiveStepValue() {
        for (int i = getProject().getSteps() - 1; i >= 0; i--) {
            double stepValue = getProject().getStep(i).getStepValue();
            if (stepValue > 0.0) {
                return stepValue;
            }
        }
        return 0.0;
    }

    /**
     * Returns the stepValue of the last step. The returned value is 0 or greater. If there are no steps, returns 0.
     */
    private double getLastStepValue() {
        double stepValue = 0.0;
        if (getProject().getSteps() > 0) {
            stepValue = getProject().getStep(getProject().getSteps() - 1).getStepValue();
        }
        return Math.max(0.0, stepValue);
    }

    /**
     * Resets the values for the Start-Step-Stop fields.
     */
    private void resetAddSequence() {
        sequenceStartField.setValue(null);
        sequenceStepField.setValue(null);
        sequenceStopField.setValue(null);
        if (getProject() == null) {
            return;
        }

        // set the latest step value to the Start field
        double stepValue = getLastPositiveStepValue();
        sequenceStartField.setValue(new Double(stepValue));
    }

    /**
     * Adds sequence determined by textfields to end of table. If successful, resets the values for the Start-Step-Stop
     * fields and moves the focus to the Start field. If unsuccessful, indicates the invalid text fields by blinking.
     */
    private void addSequence() {
        if (getProject() == null || !getProject().isSequenceEditEnabled()) {
            return;
        }

        // verify the values of the fields, flash them red if errors are found
        double startVal;
        double stepVal;
        double stopVal;
        if (sequenceStartField.getValue() != null
                && sequenceStepField.getValue() == null
                && sequenceStopField.getValue() == null) {
            // only start entered, add only one step
            startVal = ((Number) sequenceStartField.getValue()).doubleValue();
            stepVal = 1.0;
            stopVal = startVal;

        } else if (sequenceStartField.getValue() != null
                && sequenceStepField.getValue() != null
                && sequenceStopField.getValue() != null) {
            // all values entered, check their sizes
            startVal = ((Number) sequenceStartField.getValue()).doubleValue();
            stepVal = ((Number) sequenceStepField.getValue()).doubleValue();
            stopVal = ((Number) sequenceStopField.getValue()).doubleValue();

            JTextField fixThisField = null;
            if (startVal > stopVal) {
                sequenceStopFieldFlasher.flash();
                fixThisField = sequenceStopField;
            }
            if (stepVal <= 0.09) {
                sequenceStepFieldFlasher.flash();
                fixThisField = sequenceStepField;
            }
            if (fixThisField != null) {
                fixThisField.grabFocus();
                final JTextField f = fixThisField;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // needs to use invokeLater because it won't be possible to select text from a field which has no focus
                        f.setSelectionStart(0);
                        f.setSelectionEnd(f.getText().length());
                    }
                });
                return;
            }

        } else {
            // some required values have not been entered, so flash them
            if (sequenceStopField.getValue() == null) {
                sequenceStopFieldFlasher.flash();
                sequenceStopField.grabFocus();
            }
            if (sequenceStepField.getValue() == null) {
                sequenceStepFieldFlasher.flash();
                sequenceStepField.grabFocus();
            }
            if (sequenceStartField.getValue() == null) {
                sequenceStartFieldFlasher.flash();
                sequenceStartField.grabFocus();
            }
            return;
        }

        // add the steps to the sequence
        if (stepVal <= 0.09) {
            return;
        }
        MeasurementStep step = new MeasurementStep();
        for (double d = startVal; d <= stopVal; d += stepVal) {
            if (Math.abs(d - getLastStepValue()) < 0.09) {
                continue;
            }
            step.setStepValue(d);
            getProject().addStep(step);
        }

        // finally reset the fields, move focus to the Start field and show the added steps
        resetAddSequence();
        sequenceStartField.grabFocus();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollToRow(sequenceTableModel.getRowCount() - 1);
                sequenceStartField.setSelectionStart(0);
                sequenceStartField.setSelectionEnd(sequenceStartField.getText().length());
            }
        });
    }

    /**
     * Sets whether or not this component is enabled. Affects all measurement sequence controls.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        sequenceStartField.setEnabled(enabled);
        sequenceStepField.setEnabled(enabled);
        sequenceStopField.setEnabled(enabled);
        addSequenceButton.setEnabled(enabled);
        loadSequenceBox.setEnabled(enabled);

        stepValueTypeLabel.setEnabled(enabled);
        sequenceStartLabel.setEnabled(enabled);
        sequenceStepLabel.setEnabled(enabled);
        sequenceStopLabel.setEnabled(enabled);
        loadSequenceLabel.setEnabled(enabled);
    }

    /**
     * Sets the project whose sequence is shown in the table. Sets project listeners, enables or disables the sequence
     * edit controls and updates the table data.
     */
    public void setProject(final Project project) {
        super.setProject(project);
        sequenceTableModel.setProject(project);
        loadSequenceBox.setSelectedItem(null);
        setEnabled(project != null);
        resetAddSequence();

        // scroll the table so that as many measuments as possible are visible, plus a couple of empty rows
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                /* HACK: Must use invokeLater or otherwise the scrolling
                 * does not work at the start of the program, when the sizes
                 * of the components are not known. Causes the GUI to blink some.
                 */
                scrollToRow(0);
                if (project != null) {
                    scrollToRow(Math.min(project.getCompletedSteps() + 5, sequenceTableModel.getRowCount() - 1));
                } else {
                    scrollToRow(sequenceTableModel.getRowCount() - 1);
                }
            }
        });
    }

    /**
     * Scrolls the table to show the specified row.
     */
    private void scrollToRow(int rowIndex) {
        sequenceTable.scrollRectToVisible(sequenceTable.getCellRect(rowIndex, rowIndex, true));
    }

    /**
     * Updates the sequence table on project data change. The TableModel does not need to listen to ProjectEvents.
     */
    public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.DATA_CHANGED) {

            // refresh the table header, in case the header names have changed.
            for (int i = 0; i < sequenceTable.getColumnCount(); i++) {
                sequenceTable.getColumnModel().getColumn(i).setHeaderValue(sequenceTableModel.getColumnName(i));
            }
            sequenceTable.getTableHeader().repaint();

            // save the selected rows and update the table data
            int[] rows = sequenceTable.getSelectedRows();
            sequenceTableModel.fireTableDataChanged();
            for (int i : rows) {
                sequenceTable.getSelectionModel().addSelectionInterval(i, i);
            }
        }
    }

    public void measurementUpdated(MeasurementEvent event) {
        if (event.getType() == MeasurementEvent.Type.STEP_START) {

            // scroll the row visible
            for (int i = getProject().getSteps() - 1; i >= 0; i--) {
                if (getProject().getStep(i) == event.getStep()) {
                    scrollToRow(i);
                    scrollToRow(Math.min(i + 2, sequenceTableModel.getRowCount() - 1));
                    break;
                }
            }
        } else if (event.getType() == MeasurementEvent.Type.VALUE_MEASURED) {

            // show the details of the latest measurement
            getDetailsPanel().setStep(event.getStep());
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer !!! IMPORTANT !!! DO NOT edit this method OR call it in your
     * code!
     */
    private void $$$setupUI$$$() {
        controlsPane = new JPanel();
        controlsPane.setLayout(new GridLayoutManager(2, 7, new Insets(0, 0, 0, 0), 5, 0));
        sequenceStartField = new JFormattedTextField();
        sequenceStartField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStartField,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(35, -1), null));
        sequenceStepField = new JFormattedTextField();
        sequenceStepField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStepField,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(35, -1), null));
        sequenceStopField = new JFormattedTextField();
        sequenceStopField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStopField,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(35, -1), null));
        sequenceStartLabel = new JLabel();
        sequenceStartLabel.setText("Start");
        controlsPane.add(sequenceStartLabel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sequenceStepLabel = new JLabel();
        sequenceStepLabel.setText("Step");
        controlsPane.add(sequenceStepLabel,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sequenceStopLabel = new JLabel();
        sequenceStopLabel.setText("Stop");
        controlsPane.add(sequenceStopLabel,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        addSequenceButton = new JButton();
        addSequenceButton.setText("Add Sequence");
        controlsPane.add(addSequenceButton,
                new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        stepValueTypeLabel = new JLabel();
        stepValueTypeLabel.setText("mT");
        controlsPane.add(stepValueTypeLabel,
                new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        controlsPane.add(spacer1,
                new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(30, -1), null));
        loadSequenceBox = new JComboBox();
        controlsPane.add(loadSequenceBox,
                new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        loadSequenceLabel = new JLabel();
        loadSequenceLabel.setText("Load Set");
        controlsPane.add(loadSequenceLabel,
                new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }

    /**
     * Sets the format for the JFormattedTextFields of this panel.
     *
     * @author Esko Luontola
     */
    private class MyFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
            DecimalFormat format = new DecimalFormat();
            format.setGroupingUsed(false);
            format.setMaximumFractionDigits(1);

            NumberFormatter formatter = new NumberFormatter(format);
            formatter.setMinimum(new Double(0.0));
            formatter.setMaximum(new Double(9999.0));
            return formatter;
        }
    }

    /**
     * Popup menu for removing and adding steps from the sequence, and saving steps as a preset sequence. This popup
     * will assume that there is an open project while this popup is visible.
     *
     * @author Esko Luontola
     */
    private class SequencePopupMenu extends JPopupMenu {

        /**
         * The currently selected steps from the sequence, or an empty array if no steps are selected.
         */
        private MeasurementStep[] steps;

        /**
         * Creates a new SequencePopupMenu.
         *
         * @param steps the currently selected steps from the sequence, or an empty array if no steps are selected.
         * @throws NullPointerException if steps is null.
         */
        public SequencePopupMenu(MeasurementStep[] steps) {
            if (steps == null) {
                throw new NullPointerException();
            }
            this.steps = steps;

            add(getInsertBeforeAction());
            add(getInsertAfterAction());
            add(getDeleteSelectedAction());
            add(new JSeparator());
            add(getSaveSelectedAsAction());
            add(getSaveAllAsAction());
        }

        // TODO: Put these same actions to the program's main menu. Each action might then need to find out the selected rows itself and monitor the ListSelectionModel.

        private Action getInsertBeforeAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int index = getFirstIndex();
                    for (int i = 0; i < steps.length; i++) {
                        getProject().addStep(index, new MeasurementStep());
                    }
                }
            };
            action.putValue(Action.NAME, "Insert Before");
            action.putValue(Action.SHORT_DESCRIPTION,
                    "Inserts the selected number of new steps " +
                    "in front of the selected steps.");
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);

            if (steps.length == 0 || getFirstIndex() < getProject().getCompletedSteps()) {
                action.setEnabled(false);
            }
            return action;
        }

        private Action getInsertAfterAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int index = getLastIndex() + 1;
                    for (int i = 0; i < steps.length; i++) {
                        getProject().addStep(index, new MeasurementStep());
                    }
                }
            };
            action.putValue(Action.NAME, "Insert After");
            action.putValue(Action.SHORT_DESCRIPTION,
                    "Inserts the selected number of new steps " +
                    "after the selected steps.");
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);

            if (steps.length == 0 || getLastIndex() < getProject().getCompletedSteps() - 1) {
                action.setEnabled(false);
            }
            return action;
        }

        private Action getDeleteSelectedAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    // can not use removeStep(int,int) here because the selection might not be contiguous
                    int index;
                    while ((index = getLastIndex()) >= 0) {
                        getProject().removeStep(index);
                    }
                }
            };
            action.putValue(Action.NAME, "Delete Selected");
            action.putValue(Action.SHORT_DESCRIPTION,
                    "Removes the selected steps from the sequence.");
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);

            if (steps.length == 0 || getFirstIndex() < getProject().getCompletedSteps()) {
                action.setEnabled(false);
            }
            return action;
        }

        private Action getSaveSelectedAsAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {

                    // make a copy of the selected steps
                    MeasurementSequence sequence = new MeasurementSequence();
                    for (MeasurementStep step : steps) {
                        MeasurementStep copy = new MeasurementStep();
                        copy.setStepValue(step.getStepValue());
                        sequence.addStep(copy);
                    }

                    // ask for a name for the sequence
                    String name = JOptionPane.showInputDialog(getParentFrame(),
                            "Enter a name for the sequence",
                            "Save Selected As...", JOptionPane.PLAIN_MESSAGE);
                    if (name == null) {
                        return;
                    }

                    // save the sequence
                    sequence.setName(name);
                    Settings.instance().addSequence(sequence);
                    updateLoadSequenceBox();
                }
            };
            action.putValue(Action.NAME, "Save Selected As...");
            action.putValue(Action.SHORT_DESCRIPTION,
                    "Saves the selected steps as a new preset sequence.");
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

            if (steps.length == 0) {
                action.setEnabled(false);
            }
            return action;
        }

        private Action getSaveAllAsAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {

                    // make a copy of all the steps
                    MeasurementSequence sequence = getProject().copySequence(0, getProject().getSteps() - 1);

                    // ask for a name for the sequence
                    String name = JOptionPane.showInputDialog(getParentFrame(),
                            "Enter a name for the sequence",
                            "Save All As...", JOptionPane.PLAIN_MESSAGE);
                    if (name == null) {
                        return;
                    }

                    // save the sequence
                    sequence.setName(name);
                    Settings.instance().addSequence(sequence);
                    updateLoadSequenceBox();
                }
            };
            action.putValue(Action.NAME, "Save All As...");
            action.putValue(Action.SHORT_DESCRIPTION,
                    "Saves all of the steps as a new preset sequence.");
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);

            if (getProject().getSteps() == 0) {
                action.setEnabled(false);
            }
            return action;
        }

        /**
         * Returns the index of the first step, or -1 if there are no steps.
         */
        private int getFirstIndex() {
            for (int i = 0; i < getProject().getSteps(); i++) {
                MeasurementStep current = getProject().getStep(i);
                for (MeasurementStep step : steps) {
                    if (current == step) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Returns the index of the last step, or -1 if there are no steps.
         */
        private int getLastIndex() {
            for (int i = getProject().getSteps() - 1; i >= 0; i--) {
                MeasurementStep current = getProject().getStep(i);
                for (MeasurementStep step : steps) {
                    if (current == step) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Popup menu for selecting which columns to show in the sequence table. This popup will assume that there is an
     * open project while this popup is visible.
     *
     * @author Esko Luontola
     */
    private class HeaderPopupMenu extends JPopupMenu {

        public HeaderPopupMenu() {
            JMenuItem header = new JMenuItem("Visible Columns");
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            header.setEnabled(false);
            add(header);

            SequenceColumn[] columns = sequenceTableModel.getPossibleColumns();
            for (final SequenceColumn column : columns) {

                // add all of the columns to the menu as checkboxes
                final JCheckBox checkBox = new JCheckBox(column.getColumnName(getProject()));
                checkBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sequenceTableModel.setColumnVisible(column, checkBox.isSelected());
                    }
                });
                checkBox.setSelected(sequenceTableModel.isColumnVisible(column));
                checkBox.setToolTipText(column.getToolTipText(getProject()));
                add(checkBox);
            }
        }
    }
}
