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
import ikayaki.MeasurementEvent;
import ikayaki.Project;
import ikayaki.ProjectEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Allows creating, editing and removing measurement sequences. Shows measurement data. Right-click brings popup menu
 * for hiding columns, and saving sequence. Left-click selects a row. Multiple rows can be selected by ctrl-clicking or
 * shift-clicking. Allows dragging rows to different order if multiple rows are selected multiple rows are dragged. Has
 * three textfields for inserting new sequences, first field for start value, second for step and third for stop value.
 * Clicking Add sequence-button appends sequence into table. Saved sequences can be loaded from dropdown menu.
 *
 * @author Mikko Jormalainen, Esko Luontola
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
    private JLabel stepValueType;
    private JButton addSequenceButton;
    private JComboBox loadSequenceBox;

    private JPanel controlsPane;

    /**
     * Creates default MeasurementSequencePanel.
     */
    public MeasurementSequencePanel() {

        sequenceTableModel = new MeasurementSequenceTableModel();
        sequenceTable = new JTable(sequenceTableModel);

        controlsPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        JScrollPane scrollPane = new JScrollPane(sequenceTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        add(controlsPane, "North");
        add(scrollPane);

        // TODO
    }

    /**
     * Adds sequence determined by textfields to end of table.
     */
    private void addSequence() {
        // TODO
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
     * Calls super.setProject(project), clears table and calculates shown data from project’s measurement data.
     */
    public void setProject(Project project) {
        super.setProject(project);
        sequenceTableModel.setProject(project);
        setEnabled(project != null);
    }

    public void projectUpdated(ProjectEvent event) {
        // TODO
    }

    public void measurementUpdated(MeasurementEvent event) {
        // TODO
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
        controlsPane.setLayout(new GridLayoutManager(2, 7, new Insets(0, 0, 0, 0), -1, -1));
        sequenceStartField = new JFormattedTextField();
        sequenceStartField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStartField,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(30, -1), null));
        sequenceStepField = new JFormattedTextField();
        sequenceStepField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStepField,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(30, -1), null));
        sequenceStopField = new JFormattedTextField();
        sequenceStopField.setHorizontalAlignment(11);
        controlsPane.add(sequenceStopField,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(30, -1), null));
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
}