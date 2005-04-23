/*
 * ProgramSettingsPanel.java
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
import ikayaki.Ikayaki;
import ikayaki.Settings;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

/**
 * Controls for editing the program settings.
 *
 * @author Esko Luontola
 */
public class ProgramSettingsPanel extends JPanel {

    private JDialog creator;

    private JFormattedTextField measurementRotationsField;
    private JComboBox holderCalibrationCombo;
    private JTable sequencesTable;
    private JButton sequencesDeleteButton;
    private JPanel defaultColumnsPane;
    private JButton closeButton;
    private JPanel contentPane;

    public ProgramSettingsPanel(JDialog dialog) {
        this.creator = dialog;

        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        /* Measurement Rotations */

        NumberFormatter format = new NumberFormatter();
        format.setMinimum(new Integer(0));
        format.setMaximum(new Integer(999));
        measurementRotationsField.setFormatterFactory(new DefaultFormatterFactory(format));
        measurementRotationsField.setValue(new Integer(Settings.getMeasurementRotations()));

        // autosaving
        measurementRotationsField.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                int value = ((Number) measurementRotationsField.getValue()).intValue();
                Settings.setMeasurementRotations(value);
            }
        });

        // automatically select all text to make the entering of a new value easy
        measurementRotationsField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        measurementRotationsField.setSelectionStart(0);
                        measurementRotationsField.setSelectionEnd(measurementRotationsField.getText().length());
                    }
                });
            }

            public void focusLost(FocusEvent e) {
                // DO NOTHING
            }
        });

        /* Sample Holder Calibration */

        final File[] calibrationFiles = Settings.getCalibrationProjectFiles();
        File holderCalibrationFile = Settings.getHolderCalibrationFile();
        holderCalibrationCombo.addItem("");     // option for selecting no file

        // add all calibration projects to the list
        for (int i = 0; i < calibrationFiles.length; i++) {
            File file = calibrationFiles[i];
            String name = file.getName().substring(0, file.getName().lastIndexOf(Ikayaki.FILE_TYPE));
            holderCalibrationCombo.addItem(name);

            if (file.equals(holderCalibrationFile)) {
                holderCalibrationCombo.setSelectedIndex(holderCalibrationCombo.getItemCount() - 1);
            }
        }

        // autosaving
        holderCalibrationCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = holderCalibrationCombo.getSelectedIndex();
                index--;
                if (index >= 0 && index < calibrationFiles.length) {
                    Settings.setHolderCalibrationFile(calibrationFiles[index]);
                } else {
                    Settings.setHolderCalibrationFile(null);
                }
            }
        });

        /* Saved Sequences */

        

        /* Default Columns */

        SequenceColumn[] allColumns = SequenceColumn.getAllColumns();
        List<SequenceColumn> defaultColumns = Settings.getDefaultColumns();

        defaultColumnsPane.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.ipadx = 10;
        gc.gridy = 0;

        // add all columns to the list as checkboxes
        for (final SequenceColumn column : allColumns) {
            final JCheckBox checkBox = new JCheckBox(column.getColumnName(null));
            JLabel description = new JLabel(column.getToolTipText(null));

            // autosaving
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Settings.setDefaultColumn(column, checkBox.isSelected());
                }
            });
            checkBox.setSelected(defaultColumns.contains(column));

            gc.gridx = 0;
            defaultColumnsPane.add(checkBox, gc);
            gc.gridx = 1;
            defaultColumnsPane.add(description, gc);
            gc.gridy++;
        }

        /* Close */

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                creator.setVisible(false);
            }
        });

        // small hack to set the default button
        this.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if (getRootPane().getDefaultButton() != closeButton) {
                    getRootPane().setDefaultButton(closeButton);
                }
            }
        });

        // avoid the need to press enter twise in the measurementRotationsField to close the window
        measurementRotationsField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeButton.doClick();
            }
        });
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 2, new Insets(11, 11, 11, 11), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Saved Sequences"));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        panel1.add(scrollPane1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        sequencesTable = new JTable();
        sequencesTable.setPreferredScrollableViewportSize(new Dimension(200, 200));
        scrollPane1.setViewportView(sequencesTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        sequencesDeleteButton = new JButton();
        sequencesDeleteButton.setText("Delete Sequence");
        panel2.add(sequencesDeleteButton,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel3,
                new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Default Columns"));
        defaultColumnsPane = new JPanel();
        panel3.add(defaultColumnsPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 4, 4, 4), -1, -1));
        contentPane.add(panel4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Measurements"));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        holderCalibrationCombo = new JComboBox();
        panel5.add(holderCalibrationCombo,
                new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Sample Holder Calibration");
        panel5.add(label1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Measurement Rotations");
        panel5.add(label2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementRotationsField = new JFormattedTextField();
        panel5.add(measurementRotationsField,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1),
                        null));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel6,
                new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        closeButton = new JButton();
        closeButton.setText("Close");
        panel6.add(closeButton,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }
}
