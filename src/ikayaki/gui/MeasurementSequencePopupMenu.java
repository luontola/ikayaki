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
import java.awt.event.*;
import java.io.*;

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

    private JButton ok;
    private JButton cancel;
    private JButton saveFull;
    private JButton saveSelected;
    private JButton removeSelected;

    private MeasurementSequencePanel c;
    private boolean showVolume;

    /**
     * Creates SequencePopupMenu.
     */
    public MeasurementSequencePopupMenu(MeasurementSequencePanel creator) {
        // sama juttu kun tossa detailsissa en jaksa alkaa ulkoasua säätään kun ei nää lopputulosta
        c = creator;
        if (c.isShowing()) {
            showVolume = true;
            volume = new JCheckBox("Volume", true);
        }
        else {
            showVolume = false;
            volume = new JCheckBox("Volume", false);
        }
        volume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (volume.isSelected()) {
                    showVolume = true;
                }
                else {
                    showVolume = false;
                }
            }
        });
        add(volume);

        nameLabel = new JLabel("Name of sequence");
        add(nameLabel);
        nameTextField = new JTextField(20);
        add(nameTextField);

        cancel = new JButton("cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        add(cancel);

        ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showVolume && !c.isVolumeShown()) {
                    c.showVolume();
                }
                else if (!showVolume && c.isVolumeShown()) {
                    c.hideVolume();
                }
                close();
            }
        });
        add(ok);

        saveFull = new JButton("Save entire sequence");
        saveFull.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFullSequence();
                close();
            }
        });
        add(saveFull);

        saveSelected = new JButton("Save selected sequence");
        saveSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSelectedSequence();
                close();
            }
        });
        add(saveSelected);

        removeSelected = new JButton("remove selected sequence");
        removeSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeRows();
                close();
            }
        });
        add(removeSelected);
    }

    private MeasurementSequencePopupMenu() {}

    /**
     * Saves whole sequence into dropdown menu.
     */
    private void saveFullSequence() {
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(c.sequences, true), true);
            if (nameTextField.getText() != null && nameTextField.getText() != "") {
                writer.println(">>");
                writer.println(nameTextField.getText());
                for (int i=0; i<c.rowCount(); ++i) {
                    writer.println(c.valueAt(i, 1));
                }
                writer.println("<<");
            }
            writer.close();
            c.updateComboBox();
        }
        catch (Exception e) {}
    }

    /**
     * Saves selected sequence into dropdown menu.
     */
    private void saveSelectedSequence() {
        try {
            int[] rows = c.selectedRows();
            PrintWriter writer = new PrintWriter(new FileOutputStream(c.sequences, true), true);
            if (nameTextField.getText() != null && nameTextField.getText() != ""
                && rows != null) {
                writer.println(">>");
                writer.println(nameTextField.getText());
                for (int i=0; i<rows.length; ++i) {
                    writer.println(c.valueAt(rows[i], 1));
                }
                writer.println("<<");
            }
            writer.close();
            c.updateComboBox();
        }
        catch (Exception e) {}
    }

    /**
     * Removes selected rows. Rows with measurement data cannot be removed.
     */
    private void removeRows() {
        int[] rows = c.selectedRows();
        for (int i=0; i<rows.length; ++i) {
            c.removeRow(rows[i]);
        }
    }

    private void close() {
        setVisible(false);
    }
}