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

import ikayaki.Project;
import ikayaki.ProjectEvent;
import ikayaki.MeasurementEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Allows creating, editing and removing measurement sequences. Shows measurement data. Right-click brings popup menu
 * for hiding columns, and saving sequence. Left-click selects a row. Multiple rows can be selected by ctrl-clicking or
 * shift-clicking. Allows dragging rows to different order if multiple rows are selected multiple rows are dragged. Has
 * three textfields for inserting new sequences, first field for start value, second for step and third for stop value.
 * Clicking Add sequence-button appends sequence into table. Saved sequences can be loaded from dropdown menu.
 *
 * @author Mikko Jormalainen
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
    private JLabel startLabel;
    private JLabel stepLabel;
    private JLabel stopLabel;
    private JLabel mtLabel;
    private JLabel loadsetLabel;
    private JButton addSequence;
    private JComboBox sequenceSelector;
    private JTextField sequenceStart;
    private JTextField sequenceStep;
    private JTextField sequenceStop;
    private JTable sequenceTable;
    private MeasurementSequenceTableModel tableModel;

    /**
     * Creates default MeasurementSequencePanel.
     */
    public MeasurementSequencePanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.WEST;

        startLabel = new JLabel("Start");
        gridbag.setConstraints(startLabel, c);
        add(startLabel);

        stepLabel = new JLabel("Step");
        gridbag.setConstraints(stepLabel, c);
        add(stepLabel);

        stopLabel = new JLabel("Stop");
        gridbag.setConstraints(stopLabel, c);
        add(stopLabel);

        loadsetLabel = new JLabel("Load Set");
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(loadsetLabel, c);
        add(loadsetLabel);

        sequenceStart = new JTextField(5);
        c.weightx = 4.0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        c.fill =  GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(sequenceStart, c);
        add(sequenceStart);

        sequenceStep = new JTextField(5);
        gridbag.setConstraints(sequenceStep, c);
        add(sequenceStep);

        sequenceStop = new JTextField(5);
        gridbag.setConstraints(sequenceStop, c);
        add(sequenceStop);

        mtLabel = new JLabel("mT");
        c.weightx = 1.0;
        c.fill =  GridBagConstraints.NONE;
        gridbag.setConstraints(mtLabel, c);
        add(mtLabel);

        addSequence = new JButton("Add Sequence");
        c.weightx = 0.5;
        gridbag.setConstraints(addSequence, c);
        add(addSequence);

        sequenceSelector = new JComboBox();
        c.weightx = 2.0;
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sequenceSelector, c);
        add(sequenceSelector);

        tableModel = new MeasurementSequenceTableModel();
        c.weightx = 100.0;
        c.weighty = 100.0;
        c.anchor = GridBagConstraints.NORTH;
        c.fill =  GridBagConstraints.HORIZONTAL;
        sequenceTable = new JTable(tableModel);
        gridbag.setConstraints(sequenceTable, c);
        add(sequenceTable);
    }

    /**
     * Adds sequence determined by textfields to end of table.
     */
    private void addSequence() {
        // TODO
    }

    /**
     * Calls super.setProject(project), clears table and calculates shown data from project’s measurement data.
     */
    public void setProject(Project project) {
        super.setProject(project);
    }

    public void projectUpdated(ProjectEvent event) {
        // DOES NOTHING
    }

    public void measurementUpdated(MeasurementEvent event) {
        // DOES NOTHING
    }
}