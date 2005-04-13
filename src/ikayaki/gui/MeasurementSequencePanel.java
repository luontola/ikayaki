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
 * Allows creating, editing and removing measurement sequences. Shows measurement data. Right-click brings popup menu
 * for hiding columns, and saving sequence. Left-click selects a row. Multiple rows can be selected by ctrl-clicking or
 * shift-clicking. Allows dragging rows to different order if multiple rows are selected multiple rows are dragged. Has
 * three textfields for inserting new sequences, first field for start value, second for step and third for stop value.
 * Clicking Add sequence-button appends sequence into table. Saved sequences can be loaded from dropdown menu.
 *
 * @author Esko Luontola
 */
public class MeasurementSequencePanel extends ProjectComponent {
/*
Event A: On SequenceTable mouse right-click - Create a MeasurementSequencePopupMenu.
*/
/*
Event B: On addSequence mouseclick - Add measurement sequence to project class and
tell MeasurementSequenceTableModel to update itself.
*/
/*
Event C: On sequenceSelector mouseclick - Bring dropdown menu for selecting premade
sequence.
*/
/*
Event D: On selecting sequence from dropdown menu - Add measurement sequence to
table and tell MeasurementSequenceTableModel to update itself.
*/
/*
Event E: On Project event - Update contest of table to correspond projects state.
*/
/*
Event F: On Measurement event - If measurement step is finished, get measurement
data from project class and if row being measured was selected select next row unless
measurement sequence ended.
*/
/*
Event G: On Drag event - Change measurement sequences row order in project class
and tell MeasurementSequenceTableModel to update itself to correspond new row order.
Order of rows with measurement data cannot be changed.
*/

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
    private JLabel stepValueType;
    private JButton addSequenceButton;
    private JComboBox loadSequenceBox;

    private JPanel controlsPane;

    /**
     * Creates default MeasurementSequencePanel.
     */
    public MeasurementSequencePanel() {

        /* Set up table */
        sequenceTableModel = new MeasurementSequenceTableModel();
        sequenceTable = new JTable(sequenceTableModel);
        sequenceTable.getTableHeader().setReorderingAllowed(false);
        sequenceTable.getTableHeader().setResizingAllowed(false);
        sequenceTable.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());
        sequenceTable.setDefaultEditor(StyledWrapper.class, new StyledCellEditor(new JTextField()));

        /* Build layout */
        controlsPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        JScrollPane scrollPane = new JScrollPane(sequenceTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        add(controlsPane, "North");
        add(scrollPane);

        // resize the columns to be always the right size
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

        /* Add Sequence Controls */
        MyFormatterFactory factory = new MyFormatterFactory();
        sequenceStartField.setFormatterFactory(factory);
        sequenceStepField.setFormatterFactory(factory);
        sequenceStopField.setFormatterFactory(factory);
        sequenceStartFieldFlasher = new ComponentFlasher(sequenceStartField);
        sequenceStepFieldFlasher = new ComponentFlasher(sequenceStepField);
        sequenceStopFieldFlasher = new ComponentFlasher(sequenceStopField);

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



        // TODO: popup menu for saving sequences

        // TODO: popup menu for choosing columns

        // TODO: ability to delete unmeasured steps

        // initialize with no project
        updateLoadSequenceBox();
        setProject(null);
    }

    private void updateLoadSequenceBox() {

        // rebuild the contents of the menu
        Object selected = loadSequenceBox.getSelectedItem();
        loadSequenceBox.removeAllItems();
        MeasurementSequence[] sequences = Settings.instance().getSequences();
//        sequences = new MeasurementSequence[]{
//            new MeasurementSequence("AA"),
//            new MeasurementSequence("CC"),
//            new MeasurementSequence("BB"),
//            new MeasurementSequence("DD")
//        };
        Arrays.sort(sequences);
        loadSequenceBox.addItem(null);  // the first item is empty
        for (MeasurementSequence sequence : sequences) {
            loadSequenceBox.addItem(sequence);
        }
        loadSequenceBox.setSelectedItem(selected);
    }

    /**
     * @deprecated
     */
    public String valueAt(int row, int column) {
        return (String) sequenceTable.getValueAt(row, column);
    }

    /**
     * @deprecated
     */
    public void updateComboBox() {
    }

    /**
     * @deprecated
     */
    public void removeRow(int index) {
        try {
            getProject().removeStep(index);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * @deprecated
     */
    public int[] selectedRows() {
        return sequenceTable.getSelectedRows();
    }

    /**
     * @deprecated
     */
    public int rowCount() {
        return sequenceTable.getRowCount();
    }

    /**
     * @deprecated
     */
    public void showVolume() {
        // No ei sit tätä
    }

    /**
     * @deprecated
     */
    public void hideVolume() {
        // Eikä tätä
    }

    /**
     * @deprecated
     */
    public boolean isVolumeShown() {
        // Oikeassa puolet ajasta
        if (Math.random() < 0.5) {
            return true;
        } else {
            return false;
        }
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
    }

    /**
     * Sets the project whose sequence is shown in the table. Sets project listeners, enables or disables the sequence
     * edit controls and updates the table data.
     */
    public void setProject(final Project project) {
        super.setProject(project);
        sequenceTableModel.setProject(project);
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
     * Refreshes the table header on project data change, in case the header names have changed.
     */
    public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.DATA_CHANGED) {
            for (int i = 0; i < sequenceTable.getColumnCount(); i++) {
                sequenceTable.getColumnModel().getColumn(i).setHeaderValue(sequenceTableModel.getColumnName(i));
            }
            sequenceTable.getTableHeader().repaint();
        }
    }

    public void measurementUpdated(MeasurementEvent event) {
        // on measurement step start, scroll the row visible
        if (event.getType() == MeasurementEvent.Type.STEP_START) {
            for (int i = getProject().getSteps() - 1; i >= 0; i--) {
                if (getProject().getStep(i) == event.getStep()) {
                    scrollToRow(i);
                    scrollToRow(Math.min(i + 2, sequenceTableModel.getRowCount() - 1));
                    return;
                }
            }
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
        final JLabel label1 = new JLabel();
        label1.setText("Start");
        controlsPane.add(label1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Step");
        controlsPane.add(label2,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Stop");
        controlsPane.add(label3,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        addSequenceButton = new JButton();
        addSequenceButton.setText("Add Sequence");
        controlsPane.add(addSequenceButton,
                new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        stepValueType = new JLabel();
        stepValueType.setText("mT");
        controlsPane.add(stepValueType,
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
        final JLabel label4 = new JLabel();
        label4.setText("Load Set");
        controlsPane.add(label4,
                new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }

    /**
     * Sets the format for the JFormattedTextFields of this panel.
     */
    private class MyFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
            //NumberFormat format = NumberFormat.getNumberInstance();
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

        private Action getInsertBeforeAction() {
            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
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
     */
    private class HeaderPopupMenu extends JPopupMenu {
        public HeaderPopupMenu() {
            add("HeaderPopupMenu");

            // TODO
        }
    }
}
