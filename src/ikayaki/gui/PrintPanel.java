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
import ikayaki.Project;
import ikayaki.ProjectPrinter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

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
    private JPanel plot1Panel;
    private JPanel plot2Panel;
    private JPanel plot3Panel;
    private JPanel plot4Panel;
    private AbstractPlot plot1;
    private AbstractPlot plot2;
    private AbstractPlot plot3;
    private AbstractPlot plot4;

    private JTable sequenceTable;
    private TableModel sequenceTableModel;

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
    private Vector<AbstractPlot> plots = new Vector<AbstractPlot>();

    public PrintPanel(JDialog creator, Project project) {
        if (project == null) {
            throw new NullPointerException();
        }
        this.creator = creator;
        this.project = project;

        $$$setupUI$$$();

        /* Project Information */
        header.setText(project.getName() + " (" + project.getType() + " Project)");
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        operator.setText(project.getProperty(Project.OPERATOR_PROPERTY, "") + " / " +
                project.getProperty(Project.DATE_PROPERTY, DateFormat.getDateInstance().format(new Date())));
        latitude.setText(project.getProperty(Project.LATITUDE_PROPERTY, ""));
        longitude.setText(project.getProperty(Project.LONGITUDE_PROPERTY, ""));
        strike.setText("" + project.getStrike());
        dip.setText("" + project.getDip());
        mass.setText("" + project.getMass());
        volume.setText("" + project.getVolume());
        susceptibility.setText("" + project.getSusceptibility());

        /* Sequence Table */
        sequenceTableModel = new PrintSequenceTableModel(project);
        sequenceTable.setModel(sequenceTableModel);
        sequenceTable.getTableHeader().setReorderingAllowed(false);
        sequenceTable.getTableHeader().setResizingAllowed(false);
        sequenceTable.setBorder(BorderFactory.createMatteBorder(sequenceTable.getIntercellSpacing().height,
                sequenceTable.getIntercellSpacing().width, 0, 0, sequenceTable.getGridColor()));
        updateColumns();

        /* Plots */
        plot1 = new IntensityPlot();
        plot2 = new StereoPlot();
        plots.add(plot1);
        plots.add(plot2);
        plot1Panel.setLayout(new BorderLayout());
        plot2Panel.setLayout(new BorderLayout());
        plot3Panel.setLayout(new BorderLayout());
        plot4Panel.setLayout(new BorderLayout());
        plot1Panel.add(plot1, "Center");
        plot2Panel.add(plot2, "Center");
        //plot3Panel.add(plot3, "Center");
        //plot4Panel.add(plot4, "Center");
        plot1Panel.setPreferredSize(new Dimension(200, 200));
        plot2Panel.setPreferredSize(new Dimension(200, 200));

        for (Plot plot : plots) {
            plot.reset();
            if (project != null) {
                for (int i = 0; i < project.getSteps(); i++) {
                    plot.add(project.getStep(i));
                }
            }
        }

        /* Layout */
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        /* White background */
        setOpaque(printedPanel, false);
        printedPanel.getParent().setBackground(Color.WHITE);

        /* Listeners */
        print.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
                ProjectPrinter.printComponent(getPrintedDocument());
            }
        });
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
    }

    /**
     * Recursively sets the opaque value of the specified JComponent and its subcomponents.
     */
    private static void setOpaque(JComponent container, boolean opaque) {
        Queue<Component> components = new LinkedList<Component>();
        components.add(container);
        Component component = null;
        while ((component = components.poll()) != null) {
            if (component instanceof JComponent) {
                ((JComponent) component).setOpaque(opaque);
            }
            if (component instanceof Container) {
                for (Component c : ((Container) component).getComponents()) {
                    components.add(c);
                }
            }
        }
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
            // find out the column's preferred width using the actual cell contents
            int width = 20;
            Component comp;
            for (int row = 0; row < sequenceTable.getRowCount(); row++) {
                comp = sequenceTable.getCellRenderer(row, col).getTableCellRendererComponent(sequenceTable,
                        sequenceTable.getValueAt(row, col), false, false, row, col);
                width = Math.max(width, comp.getPreferredSize().width);
            }
            columnModel.getColumn(col).setPreferredWidth(width);
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(11, 11, 11, 11), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        printedPanel = new JPanel();
        printedPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), 0, 16));
        panel1.add(printedPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        sequenceTable = new JTable();
        sequenceTable.setEnabled(false);
        printedPanel.add(sequenceTable,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null,
                        new Dimension(150, 50), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), 0, 0));
        printedPanel.add(panel2,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        plot1Panel = new JPanel();
        panel2.add(plot1Panel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        plot2Panel = new JPanel();
        panel2.add(plot2Panel,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        plot3Panel = new JPanel();
        panel2.add(plot3Panel,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        plot4Panel = new JPanel();
        panel2.add(plot4Panel,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), 4, 4));
        printedPanel.add(panel3,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Operator / Date:");
        panel3.add(label1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Mass (grams):");
        panel3.add(label2,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Volume (cm³):");
        panel3.add(label3,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        operator = new JLabel();
        operator.setText("N/A");
        panel3.add(operator,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        volume = new JLabel();
        volume.setText("N/A");
        panel3.add(volume,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mass = new JLabel();
        mass.setText("N/A");
        panel3.add(mass,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        header = new JLabel();
        header.setText("New Project");
        panel3.add(header,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Strike:");
        panel3.add(label4,
                new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        susceptibility = new JLabel();
        susceptibility.setText("N/A");
        panel3.add(susceptibility,
                new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        strike = new JLabel();
        strike.setText("N/A");
        panel3.add(strike,
                new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("Dip:");
        panel3.add(label5,
                new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dip = new JLabel();
        dip.setText("N/A");
        panel3.add(dip,
                new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label6 = new JLabel();
        label6.setText("Susceptibility:");
        panel3.add(label6,
                new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label7 = new JLabel();
        label7.setText("Latitude:");
        panel3.add(label7,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        latitude = new JLabel();
        latitude.setText("N/A");
        panel3.add(latitude,
                new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label8 = new JLabel();
        label8.setText("Longitude:");
        panel3.add(label8,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        longitude = new JLabel();
        longitude.setText("N/A");
        panel3.add(longitude,
                new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 4, 0), -1, -1));
        contentPane.add(controlPanel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        print = new JButton();
        print.setText("Print");
        controlPanel.add(print,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        cancel = new JButton();
        cancel.setText("Cancel");
        controlPanel.add(cancel,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        controlPanel.add(spacer1,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
    }

    /**
     * Shows the the data of a project in printable version. Uses the contents of the MeasurementSequenceTableModel.
     */
    private class PrintSequenceTableModel extends AbstractTableModel {

        private MeasurementSequenceTableModel model;

        public PrintSequenceTableModel(Project project) {
            model = new MeasurementSequenceTableModel();
            model.setProject(project);
        }

        public int getRowCount() {
            if (model.getProject().isSequenceEditEnabled()) {
                return model.getRowCount();         // do not show the last row (for adding steps)
            } else {
                return model.getRowCount() + 1;     // the first row is for the header
            }
        }

        public int getColumnCount() {
            return model.getColumnCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == 0) {
                return model.getColumnName(columnIndex);
            } else {
                Object obj = model.getValueAt(rowIndex - 1, columnIndex);
                if (obj instanceof StyledWrapper) {
                    StyledWrapper wrapper = (StyledWrapper) obj;
                    obj = wrapper.value;
                }
                return obj;
            }
        }
    }
}
