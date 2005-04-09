/*
* MeasurementSequencePopupMenu.java
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

import javax.swing.*;

/**
 * Allows selection if volume is shown in table and saving sequence. Pops up when measurement sequence table is
 * right-clicked. Allows saving selected sequence or whole sequence.
 *
 * @author Mikko Jormalainen
 */
public class MeasurementSequencePopupMenu extends JPopupMenu {

    /**
     * If checked volume is shown in measurement sequence table.
     */
    private JCheckBox volume;

    private JLabel nameLabel;

    /**
     * Name of the sequence to be saved.
     */
    private JTextField nameTextField;

    private JButton cancel;
    private JButton saveFull;
    private JButton saveSelected;

    /**
     * Creates SequencePopupMenu.
     */
    public MeasurementSequencePopupMenu() {
        volume = new JCheckBox("Volume", false);
        add(volume);
        nameLabel = new JLabel("Name of sequence");
        add(nameLabel);
        nameTextField = new JTextField(20);
        add(nameTextField);
        cancel = new JButton("cancel");
        add(cancel);
        saveFull = new JButton("Save entire sequence");
        add(saveFull);
        saveSelected = new JButton("Save selected sequence");
        add(saveSelected);
    }

    /**
     * Saves whole sequence into dropdown menu.
     */
    private void saveFullSequence() {
        return; // TODO
    }

    /**
     * Saves selected sequence into dropdown menu.
     */
    private void saveSelectedSequence() {
        return; // TODO
    }

    /**
     * Removes selected rows. Rows with measurement data cannot be removed.
     */
    private void removeRows() {
        return; // TODO
    }
}