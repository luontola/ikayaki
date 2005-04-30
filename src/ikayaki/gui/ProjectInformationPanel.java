/*
 * ProjectInformationPanel.java
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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Allows inserting and editing project information.
 *
 * @author Esko Luontola
 */
public class ProjectInformationPanel extends ProjectComponent {

    /* Radio Button Groups */
    private ButtonGroup measurementType;
    private JRadioButton measurementTypeAuto;
    private JRadioButton measurementTypeManual;

    private ButtonGroup sampleType;
    private JRadioButton sampleTypeHand;
    private JRadioButton sampleTypeCore;

    private ButtonGroup normalization;
    private JRadioButton normalizationVolume;
    private JRadioButton normalizationMass;

    /* Plain Text Fields */
    private JTextField operatorField;
    private JTextField dateField;
    private JTextField rockTypeField;
    private JTextField areaField;
    private JTextField siteField;
    private JTextArea commentArea;

    /* Number-only Text Fields */
    private JFormattedTextField latitudeField;
    private JFormattedTextField longitudeField;
    private JFormattedTextField strikeField;
    private JFormattedTextField dipField;
    private JFormattedTextField massField;
    private JFormattedTextField volumeField;
    private JFormattedTextField susceptibilityField;

    private JPanel contentPane;

    private boolean propertiesModified = false;
    private boolean parametersModified = false;

    /**
     * Creates default ProjectInformationPanel with no current project. Starts an autosaving thread.
     */
    public ProjectInformationPanel() {
        setLayout(new BorderLayout());
        add(contentPane, "Center");
        contentPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 4));

        /* Radio Button Groups */
        measurementType = new ButtonGroup();
        measurementType.add(measurementTypeAuto);
        measurementType.add(measurementTypeManual);

        sampleType = new ButtonGroup();
        sampleType.add(sampleTypeHand);
        sampleType.add(sampleTypeCore);

        normalization = new ButtonGroup();
        normalization.add(normalizationVolume);
        normalization.add(normalizationMass);

        /* Formatted Text Fields */
        MyFormatterFactory factory = new MyFormatterFactory();
        latitudeField.setFormatterFactory(factory);
        longitudeField.setFormatterFactory(factory);
        strikeField.setFormatterFactory(factory);
        dipField.setFormatterFactory(factory);
        massField.setFormatterFactory(factory);
        volumeField.setFormatterFactory(factory);
        susceptibilityField.setFormatterFactory(factory);

        /* Listeners for properties */
        DocumentListener propertiesDocumentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                initSaveProperties();
            }

            public void removeUpdate(DocumentEvent e) {
                initSaveProperties();
            }

            public void changedUpdate(DocumentEvent e) {
                initSaveProperties();
            }
        };
        ActionListener propertiesActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initSaveProperties();
            }
        };
        PropertyChangeListener propertiesPropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                initSaveProperties();
            }
        };

        measurementTypeAuto.addActionListener(propertiesActionListener);
        measurementTypeManual.addActionListener(propertiesActionListener);

        operatorField.getDocument().addDocumentListener(propertiesDocumentListener);
        dateField.getDocument().addDocumentListener(propertiesDocumentListener);
        rockTypeField.getDocument().addDocumentListener(propertiesDocumentListener);
        areaField.getDocument().addDocumentListener(propertiesDocumentListener);
        siteField.getDocument().addDocumentListener(propertiesDocumentListener);
        commentArea.getDocument().addDocumentListener(propertiesDocumentListener);

        latitudeField.addPropertyChangeListener("value", propertiesPropertyChangeListener);
        longitudeField.addPropertyChangeListener("value", propertiesPropertyChangeListener);

        /* Listeners for parameters */
        ActionListener parametersActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initSaveParameters();
            }
        };
        PropertyChangeListener parametersPropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                initSaveParameters();
            }
        };

        sampleTypeHand.addActionListener(parametersActionListener);
        sampleTypeCore.addActionListener(parametersActionListener);
        normalizationVolume.addActionListener(parametersActionListener);
        normalizationMass.addActionListener(parametersActionListener);

        strikeField.addPropertyChangeListener("value", parametersPropertyChangeListener);
        dipField.addPropertyChangeListener("value", parametersPropertyChangeListener);
        massField.addPropertyChangeListener("value", parametersPropertyChangeListener);
        volumeField.addPropertyChangeListener("value", parametersPropertyChangeListener);
        susceptibilityField.addPropertyChangeListener("value", parametersPropertyChangeListener);

        /* Autosaving at regular intervals */
        Timer autosave = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveProperties();
                saveParameters();
            }
        });
        autosave.start();
        
        // initialize with no project
        setProject(null);
    }

    /**
     * Sets whether or not this component is enabled. Affects all project information form fields.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Queue<Component> components = new LinkedList<Component>();
        for (Component c : getComponents()) {
            components.add(c);
        }
        Component component = null;
        while ((component = components.poll()) != null) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                for (Component c : ((Container) component).getComponents()) {
                    components.add(c);
                }
            }
        }
    }

    /**
     * Calls super.setProject(project) and updates textfield with new projects data.
     */
    public void setProject(Project project) {

        // save the old project's values
        saveProperties();
        saveParameters();

        super.setProject(project);
        setEnabled(project != null);

        if (project != null) {
            // get values from the new project

            /* Radio Button Groups */
            measurementTypeAuto.setSelected(project.getProperty(Project.MEASUREMENT_TYPE_PROPERTY,
                    Project.MEASUREMENT_TYPE_AUTO_VALUE).equals(Project.MEASUREMENT_TYPE_AUTO_VALUE));
            measurementTypeManual.setSelected(project.getProperty(Project.MEASUREMENT_TYPE_PROPERTY,
                    Project.MEASUREMENT_TYPE_AUTO_VALUE).equals(Project.MEASUREMENT_TYPE_MANUAL_VALUE));
            sampleTypeHand.setSelected(project.getSampleType() == Project.SampleType.HAND);
            sampleTypeCore.setSelected(project.getSampleType() == Project.SampleType.CORE);
            normalizationVolume.setSelected(project.getNormalization() == Project.Normalization.VOLUME);
            normalizationMass.setSelected(project.getNormalization() == Project.Normalization.MASS);

            /* Plain Text Fields */
            operatorField.setText(project.getProperty(Project.OPERATOR_PROPERTY, ""));
            dateField.setText(
                    project.getProperty(Project.DATE_PROPERTY, DateFormat.getDateInstance().format(new Date())));
            rockTypeField.setText(project.getProperty(Project.ROCK_TYPE_PROPERTY, ""));
            areaField.setText(project.getProperty(Project.AREA_PROPERTY, ""));
            siteField.setText(project.getProperty(Project.SITE_PROPERTY, ""));
            commentArea.setText(project.getProperty(Project.COMMENT_PROPERTY, ""));
            commentArea.setCaretPosition(0);    // scroll the viewport to the top

            /* Number Fields */
            latitudeField.setText(project.getProperty(Project.LATITUDE_PROPERTY, ""));
            longitudeField.setText(project.getProperty(Project.LONGITUDE_PROPERTY, ""));
            strikeField.setValue(new Double(project.getStrike()));
            dipField.setValue(new Double(project.getDip()));
            massField.setValue(new Double(project.getMass()));
            volumeField.setValue(new Double(project.getVolume()));
            susceptibilityField.setValue(new Double(project.getSusceptibility()));
        } else {
            // no project active, clear the form fields

            /* Radio Button Groups */
            measurementTypeAuto.setSelected(true);
            measurementTypeManual.setSelected(false);
            sampleTypeHand.setSelected(true);
            sampleTypeCore.setSelected(false);
            normalizationVolume.setSelected(true);
            normalizationMass.setSelected(false);

            /* Plain Text Fields */
            operatorField.setText("");
            dateField.setText("");
            rockTypeField.setText("");
            areaField.setText("");
            siteField.setText("");
            commentArea.setText("");

            /* Number Fields */
            latitudeField.setValue(null);
            longitudeField.setValue(null);
            strikeField.setValue(null);
            dipField.setValue(null);
            massField.setValue(null);
            volumeField.setValue(null);
            susceptibilityField.setValue(null);
        }

        // prevent the saving of unchanged values
        propertiesModified = false;
        parametersModified = false;
    }

    /**
     * Schedules the running of saveProperties().
     */
    private void initSaveProperties() {
        propertiesModified = true;
    }

    /**
     * Schedules the running of saveParameters().
     */
    private void initSaveParameters() {
        parametersModified = true;
    }

    /**
     * Saves to the project file those properties, that do not affect the measurement calculations. Will do nothing if
     * propertiesModified is false.
     *
     * @throws NullPointerException if the current project is null.
     */
    private void saveProperties() {
        if (!propertiesModified) {
            return;
        }

        /* Radio Button Groups */
        if (measurementTypeAuto.isSelected()) {
            getProject().setProperty(Project.MEASUREMENT_TYPE_PROPERTY, Project.MEASUREMENT_TYPE_AUTO_VALUE);
        }
        if (measurementTypeManual.isSelected()) {
            getProject().setProperty(Project.MEASUREMENT_TYPE_PROPERTY, Project.MEASUREMENT_TYPE_MANUAL_VALUE);
        }

        /* Plain Text Fields */
        getProject().setProperty(Project.OPERATOR_PROPERTY, operatorField.getText());
        getProject().setProperty(Project.DATE_PROPERTY, dateField.getText());
        getProject().setProperty(Project.ROCK_TYPE_PROPERTY, rockTypeField.getText());
        getProject().setProperty(Project.AREA_PROPERTY, areaField.getText());
        getProject().setProperty(Project.SITE_PROPERTY, siteField.getText());
        getProject().setProperty(Project.COMMENT_PROPERTY, commentArea.getText());

        /* Number Fields */
        getProject().setProperty(Project.LATITUDE_PROPERTY, latitudeField.getText());
        getProject().setProperty(Project.LONGITUDE_PROPERTY, longitudeField.getText());

        propertiesModified = false;
    }

    /**
     * Saves to the project file those parameters, that affect the measurement calculations. Will do nothing if
     * parametersModified is false.
     *
     * @throws NullPointerException if the current project is null.
     */
    private void saveParameters() {
        if (!parametersModified) {
            return;
        }

        /* Radio Button Groups */
        if (sampleTypeHand.isSelected()) {
            getProject().setSampleType(Project.SampleType.HAND);
        }
        if (sampleTypeCore.isSelected()) {
            getProject().setSampleType(Project.SampleType.CORE);
        }
        if (normalizationVolume.isSelected()) {
            getProject().setNormalization(Project.Normalization.VOLUME);
        }
        if (normalizationMass.isSelected()) {
            getProject().setNormalization(Project.Normalization.MASS);
        }

        /* Number-only Text Fields */
        Number value;
        value = (Number) strikeField.getValue();
        getProject().setStrike(value.doubleValue());
        value = (Number) dipField.getValue();
        getProject().setDip(value.doubleValue());
        value = (Number) massField.getValue();
        getProject().setMass(value.doubleValue());
        value = (Number) volumeField.getValue();
        getProject().setVolume(value.doubleValue());
        value = (Number) susceptibilityField.getValue();
        getProject().setSusceptibility(value.doubleValue());

        parametersModified = false;
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
        contentPane.setLayout(new GridLayoutManager(15, 5, new Insets(0, 0, 0, 0), 4, 4));
        siteField = new JTextField();
        contentPane.add(siteField,
                new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        areaField = new JTextField();
        contentPane.add(areaField,
                new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 4, 4));
        contentPane.add(panel1,
                new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        measurementTypeAuto = new JRadioButton();
        measurementTypeAuto.setText("Auto");
        panel1.add(measurementTypeAuto,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementTypeManual = new JRadioButton();
        measurementTypeManual.setText("Manual");
        panel1.add(measurementTypeManual,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final Spacer spacer2 = new Spacer();
        contentPane.add(spacer2,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 4, 4));
        contentPane.add(panel2,
                new GridConstraints(12, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        normalizationMass = new JRadioButton();
        normalizationMass.setText("Mass");
        panel2.add(normalizationMass,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        normalizationVolume = new JRadioButton();
        normalizationVolume.setText("Volume");
        panel2.add(normalizationVolume,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        latitudeField = new JFormattedTextField();
        contentPane.add(latitudeField,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        longitudeField = new JFormattedTextField();
        contentPane.add(longitudeField,
                new GridConstraints(8, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        strikeField = new JFormattedTextField();
        contentPane.add(strikeField,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        final JLabel label1 = new JLabel();
        label1.setText("Dip");
        contentPane.add(label1,
                new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dipField = new JFormattedTextField();
        contentPane.add(dipField,
                new GridConstraints(9, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        volumeField = new JFormattedTextField();
        contentPane.add(volumeField,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        massField = new JFormattedTextField();
        contentPane.add(massField,
                new GridConstraints(10, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        susceptibilityField = new JFormattedTextField();
        contentPane.add(susceptibilityField,
                new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(40, -1), null));
        final Spacer spacer4 = new Spacer();
        contentPane.add(spacer4,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null));
        final JLabel label2 = new JLabel();
        label2.setText("Volume (cm³)");
        contentPane.add(label2,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Susceptibility");
        contentPane.add(label3,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Normalize by");
        contentPane.add(label4,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("Comments");
        contentPane.add(label5,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label6 = new JLabel();
        label6.setText("Longitude");
        contentPane.add(label6,
                new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label7 = new JLabel();
        label7.setText("Operator / Date");
        contentPane.add(label7,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label8 = new JLabel();
        label8.setText("Measurement type");
        contentPane.add(label8,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label9 = new JLabel();
        label9.setText("Site");
        contentPane.add(label9,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label10 = new JLabel();
        label10.setText("Location");
        contentPane.add(label10,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("Latitude");
        contentPane.add(label11,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label12 = new JLabel();
        label12.setText("Strike");
        contentPane.add(label12,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label13 = new JLabel();
        label13.setText("Mass (grams)");
        contentPane.add(label13,
                new GridConstraints(10, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 4, 0));
        contentPane.add(panel3,
                new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        operatorField = new JTextField();
        panel3.add(operatorField,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        dateField = new JTextField();
        panel3.add(dateField,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1),
                        null));
        final JLabel label14 = new JLabel();
        label14.setText("/");
        panel3.add(label14,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setEnabled(true);
        scrollPane1.setHorizontalScrollBarPolicy(31);
        contentPane.add(scrollPane1,
                new GridConstraints(14, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        new Dimension(-1, 50), null, null));
        commentArea = new JTextArea();
        commentArea.setLineWrap(true);
        commentArea.setRows(3);
        scrollPane1.setViewportView(commentArea);
        rockTypeField = new JTextField();
        contentPane.add(rockTypeField,
                new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        final JLabel label15 = new JLabel();
        label15.setText("Rock type");
        contentPane.add(label15,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label16 = new JLabel();
        label16.setText("Sample type");
        contentPane.add(label16,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 4, 4));
        contentPane.add(panel4,
                new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        final Spacer spacer5 = new Spacer();
        panel4.add(spacer5,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        sampleTypeCore = new JRadioButton();
        sampleTypeCore.setText("Core");
        panel4.add(sampleTypeCore,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sampleTypeHand = new JRadioButton();
        sampleTypeHand.setText("Hand");
        panel4.add(sampleTypeHand,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer6 = new Spacer();
        contentPane.add(spacer6,
                new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(4, -1), null));
        final Spacer spacer7 = new Spacer();
        contentPane.add(spacer7,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null));
    }

    /**
     * Custom formatter factory for the JFormattedTextFields in this class.
     *
     * @author Esko Luontola
     */
    private class MyFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
        /**
         * Returns an <code>AbstractFormatter</code> that can handle formatting of the passed in
         * <code>JFormattedTextField</code>.
         *
         * @param tf JFormattedTextField requesting AbstractFormatter
         * @return AbstractFormatter to handle formatting duties, a null return value implies the JFormattedTextField
         *         should behave like a normal JTextField
         */
        public JFormattedTextField.AbstractFormatter getFormatter(final JFormattedTextField tf) {
            NumberFormatter formatter;
            DecimalFormat format;

            if (tf == latitudeField || tf == longitudeField) {
                // allow null values
                format = new NullableDecimalFormat();
            } else if (tf == massField || tf == volumeField || tf == susceptibilityField) {
                // show only positive numbers
                format = new PositiveDecimalFormat();
            } else {
                // show all numbers
                format = new DecimalFormat();
            }
            format.setGroupingUsed(true);
            format.setMaximumFractionDigits(6);
            formatter = new NumberFormatter(format);

            // set value ranges
            if (tf == strikeField) {
                formatter.setMinimum(new Double(0));
                formatter.setMaximum(new Double(360));
            } else if (tf == dipField) {
                formatter.setMinimum(new Double(-90));
                formatter.setMaximum(new Double(90));
            }

            // commit changes when pressing enter
            tf.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        tf.commitEdit();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            return formatter;
        }
    }
}