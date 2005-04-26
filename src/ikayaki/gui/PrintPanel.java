/*
 * PrintPanel.java
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
import static ikayaki.gui.SequenceColumn.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;
import ikayaki.ProjectPrinter;
import javax.swing.table.TableColumnModel;

/**
 * Creates layout from MeasurementSequence and Plots to be printed
 *
 * @author Aki Korpua
 */
public class PrintPanel
    extends JPanel {

    private JDialog creator;
    private Project project;

    private JPanel contentPane;
    private JPanel printedPanel;
    private JPanel controlPanel;
    private AbstractPlot plot1;
    private AbstractPlot plot2;
    private AbstractPlot plot3;

    private JTable sequenceTable;
    private MeasurementSequenceTableModel sequenceTableModel;

    private JLabel operator;
    private JLabel volume;
    private JLabel mass;
    private JLabel header;
    private JLabel latitude;
    private JLabel susceptibility;
    private JLabel longitude;
    private JLabel strike;
    private JLabel dip;

    private JButton print;
    private JButton cancel;

    /**
     * All plots in this panel
     */
    private Vector<AbstractPlot> plots = new Vector<AbstractPlot> ();

    public PrintPanel(JDialog creator, Project project) {
        if (project == null) {
            throw new NullPointerException();
        }
        this.creator = creator;
        this.project = project;

        sequenceTableModel = new MeasurementSequenceTableModel();
        updateColumns();

        $$$setupUI$$$();

        /* Project Information */
        operator.setText(project.getProperty(Project.OPERATOR_PROPERTY, "") +
                         "/" +
                         project.getProperty(Project.DATE_PROPERTY,
                                             DateFormat.getDateInstance().
                                             format(new Date())));

        latitude.setText(project.getProperty(Project.LATITUDE_PROPERTY, ""));
        longitude.setText(project.getProperty(Project.LONGITUDE_PROPERTY, ""));
        strike.setText("" + project.getStrike());
        dip.setText("" + project.getDip());
        mass.setText("" + project.getMass());
        volume.setText("" + project.getVolume());
        susceptibility.setText("" + project.getSusceptibility());

        /* sequence */
        sequenceTableModel.setProject(project);


        /* plots */
        plots.add(plot1);
        plots.add(plot2);

        for (Plot plot : plots) {
            plot.reset();
            if (project != null) {
                for (int i = 0; i < project.getSteps(); i++) {
                    plot.add(project.getStep(i));
                }
            }
        }

        /* layout */
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);


        /* listeners */
        print.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ProjectPrinter.printComponent(getPrintedDocument());
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
    }

    public JPanel getPrintedDocument() {
        return printedPanel;
    }

    private void closeDialog() {
        if (creator != null) {
            creator.setVisible(false);
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
        contentPane.setLayout(new GridLayoutManager(2, 1,
            new Insets(11, 11, 11, 11), -1, -1));
        printedPanel = new JPanel();
        printedPanel.setLayout(new GridLayoutManager(1, 1,
            new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(printedPanel,
                        new GridConstraints(1, 0, 1, 1,
                                            GridConstraints.ANCHOR_CENTER,
                                            GridConstraints.FILL_BOTH,
                                            GridConstraints.
                                            SIZEPOLICY_CAN_SHRINK |
                                            GridConstraints.SIZEPOLICY_CAN_GROW,
                                            GridConstraints.
                                            SIZEPOLICY_CAN_SHRINK |
                                            GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        printedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.
            createEtchedBorder(), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1,
                                               -1));
        printedPanel.add(panel1,
                         new GridConstraints(0, 0, 1, 1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_BOTH,
                                             GridConstraints.
                                             SIZEPOLICY_CAN_SHRINK |
                                             GridConstraints.
                                             SIZEPOLICY_CAN_GROW,
                                             GridConstraints.
                                             SIZEPOLICY_CAN_SHRINK |
                                             GridConstraints.
                                             SIZEPOLICY_CAN_GROW, null, null, null));
        sequenceTable = new JTable(sequenceTableModel);
        sequenceTable.getTableHeader().setReorderingAllowed(false);
        sequenceTable.getTableHeader().setResizingAllowed(false);
        sequenceTable.setDefaultRenderer(StyledWrapper.class,
                                         new StyledTableCellRenderer());
        sequenceTable.setDefaultEditor(StyledWrapper.class,
                                       new StyledCellEditor(new JTextField()));
        panel1.add(sequenceTable,
                   new GridConstraints(1, 0, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_WANT_GROW,
                                       GridConstraints.SIZEPOLICY_WANT_GROW, null,
                                       new Dimension(150, 50), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1,
                                               -1));
        panel1.add(panel2,
                   new GridConstraints(2, 0, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        plot1 = new IntensityPlot();
        panel2.add(plot1,
                   new GridConstraints(0, 0, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(150, 150), null, null));
        plot2 = new StereoPlot();
        panel2.add(plot2,
                   new GridConstraints(0, 1, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(150, 150), null, null));
        plot3 = new IntensityPlot();
        panel2.add(plot3,
                   new GridConstraints(1, 0, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(300, 150), null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), -1,
                                               -1));
        panel1.add(panel3,
                   new GridConstraints(0, 0, 1, 1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_BOTH,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                       GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Operator / Date:");
        panel3.add(label1,
                   new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Mass (grams):");
        panel3.add(label2,
                   new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Volume (cm�):");
        panel3.add(label3,
                   new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        operator = new JLabel();
        operator.setText("N/A");
        panel3.add(operator,
                   new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        volume = new JLabel();
        volume.setText("N/A");
        panel3.add(volume,
                   new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mass = new JLabel();
        mass.setText("N/A");
        panel3.add(mass,
                   new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        header = new JLabel();
        header.setText("New Project");
        panel3.add(header,
                   new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Longitude:");
        panel3.add(label4,
                   new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("Strike:");
        panel3.add(label5,
                   new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        latitude = new JLabel();
        latitude.setText("N/A");
        panel3.add(latitude,
                   new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        susceptibility = new JLabel();
        susceptibility.setText("N/A");
        panel3.add(susceptibility,
                   new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        longitude = new JLabel();
        longitude.setText("N/A");
        panel3.add(longitude,
                   new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        strike = new JLabel();
        strike.setText("N/A");
        panel3.add(strike,
                   new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label6 = new JLabel();
        label6.setText("Dip:");
        panel3.add(label6,
                   new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dip = new JLabel();
        dip.setText("N/A");
        panel3.add(dip,
                   new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label7 = new JLabel();
        label7.setText("Latitude:");
        panel3.add(label7,
                   new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label8 = new JLabel();
        label8.setText("Susceptibility:");
        panel3.add(label8,
                   new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_EAST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayoutManager(1, 3,
            new Insets(0, 0, 4, 0), -1, -1));
        contentPane.add(controlPanel,
                        new GridConstraints(0, 0, 1, 1,
                                            GridConstraints.ANCHOR_CENTER,
                                            GridConstraints.FILL_BOTH,
                                            GridConstraints.
                                            SIZEPOLICY_CAN_SHRINK |
                                            GridConstraints.SIZEPOLICY_CAN_GROW,
                                            1, null, null, null));
        print = new JButton();
        print.setText("Print");
        controlPanel.add(print,
                         new GridConstraints(0, 0, 1, 1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_HORIZONTAL,
                                             GridConstraints.
                                             SIZEPOLICY_CAN_SHRINK |
                                             GridConstraints.
                                             SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        cancel = new JButton();
        cancel.setText("Cancel");
        controlPanel.add(cancel,
                         new GridConstraints(0, 2, 1, 1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_HORIZONTAL,
                                             GridConstraints.
                                             SIZEPOLICY_CAN_SHRINK |
                                             GridConstraints.
                                             SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        controlPanel.add(spacer1,
                         new GridConstraints(0, 1, 1, 1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_HORIZONTAL,
                                             GridConstraints.
                                             SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }
}
